package com.example.hepai.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hepai.BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

enum class HepaiTab { WEEK, FREE, IMPORT }

data class PlanDraft(
    val day: Int,
    val startMin: Int,
    val endMin: Int,
    val title: String = "",
    val saving: Boolean = false,
)

data class CourseEditDraft(
    val index: Int?,
    val course: Course,
)

data class AppUiState(
    val booting: Boolean = true,
    val busy: Boolean = false,
    val space: Space? = null,
    val me: Me? = null,
    val timetables: Map<String, Timetable> = emptyMap(),
    val plans: List<Plan> = emptyList(),
    val version: Int = 0,
    val tab: HepaiTab = HepaiTab.WEEK,
    val focusDay: Int? = null,
    val daySheet: Int? = null,
    val planDraft: PlanDraft? = null,
    val ocrBusy: Boolean = false,
    val ocrNotice: String? = null,
    val ocrCourses: List<Course>? = null,
    val editCourse: CourseEditDraft? = null,
    val message: String? = null,
    val error: String? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val api = ApiClient()
    private val tokenStore = TokenStore(application)
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    private val boundCode = BindingConfig.SPACE_CODE.ifBlank { BuildConfig.BOUND_SPACE_CODE }
    private val boundPerson = BindingConfig.PERSON_ID.ifBlank { BuildConfig.BOUND_PERSON_ID }
    private val boundName =
        BindingConfig.NAME.ifBlank {
            when (boundPerson) {
                "member_a" -> "成员 A"
                "member_b" -> "成员 B"
                else -> BuildConfig.BOUND_NAME
            }
        }
    private var token: String? = tokenStore.token
    private var socket: WebSocket? = null
    private var socketJob: Job? = null
    private var pollingJob: Job? = null
    private var bootJob: Job? = null
    private val refreshing = AtomicBoolean(false)

    init {
        // Paint cached data before touching the network. The first Compose frame
        // must never wait for DNS/TLS.
        cachedState()?.let { cached ->
            if (boundCode.isBlank() || cached.space?.inviteCode == boundCode) {
                applyState(cached, persist = false)
                _state.update { it.copy(error = null) }
            }
        }
        launchBoot()
        pollingJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(15_000)
                    doRefresh(silent = true)
                }
            }
    }

    private fun cachedState(): StateResponse? =
        tokenStore.snapshot?.let { raw ->
            runCatching { json.decodeFromString(StateResponse.serializer(), raw) }.getOrNull()
        }

    private fun launchBoot() {
        if (bootJob?.isActive == true) return
        bootJob =
            viewModelScope.launch(Dispatchers.IO) {
                boot()
            }
    }

    private suspend fun boot() {
        val cached = cachedState()
        val cachedForBound =
            cached != null &&
                (boundCode.isBlank() || cached.space?.inviteCode == boundCode)
        if (cachedForBound && _state.value.space == null) {
            applyState(cached!!, persist = false)
            _state.update { it.copy(error = null) }
        }
        try {
            if (token.isNullOrBlank()) {
                val auth = api.bootstrap()
                token = auth.token
                tokenStore.token = auth.token
            }
            var response = api.state(token!!)
            if (boundCode.isNotBlank()) {
                if (response.space?.inviteCode != null && response.space?.inviteCode != boundCode) {
                    // An older install belonged to a different room. Start a clean
                    // device identity for this bound build.
                    tokenStore.token = null
                    tokenStore.snapshot = null
                    val auth = api.bootstrap()
                    token = auth.token
                    tokenStore.token = auth.token
                    response = api.state(auth.token)
                }
                if (response.space == null) {
                    response = autoJoinBoundSpace(token!!)
                }
            }
            applyState(response)
            startSocket()
        } catch (error: Exception) {
            _state.update {
                it.copy(
                    booting = false,
                    error = if (cached != null) "当前离线，显示的是上次同步的数据。" else readable(error),
                )
            }
        }
    }

    private fun applyState(
        response: StateResponse,
        persist: Boolean = true,
    ) {
        if (persist) {
            runCatching {
                tokenStore.snapshot = json.encodeToString(StateResponse.serializer(), response)
            }
        }
        _state.update {
            // Never let an empty server timetable wipe a non-empty local cache.
            // This protects the app from backend/test data loss and from a
            // transient empty state response.
            val merged = response.timetables.toMutableMap()
            for ((personId, local) in it.timetables) {
                val remote = merged[personId]
                if (local.courses.isNotEmpty() && (remote == null || remote.courses.isEmpty())) {
                    merged[personId] = local
                }
            }
            it.copy(
                booting = false,
                busy = false,
                space = response.space,
                me = response.me ?: it.me,
                timetables = merged,
                plans = response.plans,
                version = response.space?.version ?: 0,
                error = null,
            )
        }
        if (response.space != null) startSocket()
    }

    private fun readable(error: Throwable): String =
        when (error) {
            is ApiException -> error.message ?: "请求失败"
            is UnknownHostException -> "无法解析服务器地址，请检查网络后重试。"
            is ConnectException, is SocketTimeoutException -> "连接服务器超时，请切换 Wi-Fi/流量后重试。"
            is IOException -> "网络连接失败，请检查网络后重试。"
            else -> "操作失败，请稍后重试。"
        }

    fun retryConnection() {
        if (token.isNullOrBlank() || _state.value.space == null) {
            _state.update { it.copy(booting = _state.value.space == null, error = null) }
            launchBoot()
        } else {
            doRefresh(silent = false)
        }
    }

    private fun doRefresh(silent: Boolean) {
        val currentToken = token ?: return
        if (!refreshing.compareAndSet(false, true)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.state(currentToken)
                applyState(response)
            } catch (error: Exception) {
                if (error is ApiException && error.status == 401) {
                    token = null
                    tokenStore.token = null
                    socket?.close(1000, "auth")
                    _state.update {
                        it.copy(
                            booting = false,
                            busy = false,
                            space = null,
                            error = "登录状态失效了，请用邀请码重新加入空间。",
                        )
                    }
                } else if (!silent) {
                    _state.update { it.copy(error = readable(error)) }
                }
            } finally {
                refreshing.set(false)
            }
        }
    }

    fun refresh() = doRefresh(silent = false)

    private fun startSocket() {
        if (token.isNullOrBlank()) return
        if (socketJob?.isActive == true) return
        socketJob =
            viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    if (token.isNullOrBlank() || _state.value.space == null) break
                    val closed = CompletableDeferred<Unit>()
                    val currentToken = token ?: break
                    val ws =
                        api.openSocket(
                            currentToken,
                            object : WebSocketListener() {
                                override fun onMessage(
                                    webSocket: WebSocket,
                                    text: String,
                                ) {
                                    if (text.contains("state_changed")) {
                                        viewModelScope.launch { doRefresh(silent = true) }
                                    }
                                }

                                override fun onFailure(
                                    webSocket: WebSocket,
                                    t: Throwable,
                                    response: okhttp3.Response?,
                                ) {
                                    closed.complete(Unit)
                                }

                                override fun onClosed(
                                    webSocket: WebSocket,
                                    code: Int,
                                    reason: String,
                                ) {
                                    closed.complete(Unit)
                                }
                            },
                        )
                    socket = ws
                    closed.await()
                    socket = null
                    delay(3_000)
                }
            }
    }

    private suspend fun autoJoinBoundSpace(authToken: String): StateResponse {
        val name =
            boundName.ifBlank {
                if (boundPerson == "member_b") "成员 B" else "成员 A"
            }
        return try {
            // A reinstall should reclaim the same role instead of taking the
            // other person's seat.
            api.rejoinSpace(authToken, name, boundPerson, boundCode)
        } catch (error: ApiException) {
            if (error.status == 404) {
                api.joinSpace(authToken, name, boundPerson, boundCode)
            } else {
                throw error
            }
        }
    }

    private suspend fun ensureToken(): String {
        token?.let { if (it.isNotBlank()) return it }
        val auth = api.bootstrap()
        token = auth.token
        tokenStore.token = auth.token
        return auth.token
    }

    fun createSpace(
        name: String,
        personId: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(busy = true, error = null) }
            try {
                val authToken = ensureToken()
                val response = api.createSpace(authToken, name, personId)
                applyState(response)
                _state.update { it.copy(tab = HepaiTab.WEEK, message = "空间已创建，把邀请码发给她吧。") }
                startSocket()
            } catch (error: Exception) {
                _state.update { it.copy(busy = false, error = readable(error)) }
            }
        }
    }

    fun joinSpace(
        name: String,
        personId: String,
        code: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(busy = true, error = null) }
            try {
                val authToken = ensureToken()
                val response = api.joinSpace(authToken, name, personId, code)
                applyState(response)
                _state.update { it.copy(tab = HepaiTab.WEEK, message = "已经加入共同空间。") }
                startSocket()
            } catch (error: Exception) {
                _state.update { it.copy(busy = false, error = readable(error)) }
            }
        }
    }

    fun rejoinSpace(
        name: String,
        personId: String,
        code: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(busy = true, error = null) }
            try {
                val authToken = ensureToken()
                val response = api.rejoinSpace(authToken, name, personId, code)
                applyState(response)
                _state.update { it.copy(tab = HepaiTab.WEEK, message = "原身份已恢复。") }
                startSocket()
            } catch (error: Exception) {
                _state.update { it.copy(busy = false, error = readable(error)) }
            }
        }
    }

    fun selectTab(tab: HepaiTab) {
        _state.update { it.copy(tab = tab) }
    }

    fun focusWindow(window: Window) {
        _state.update { it.copy(tab = HepaiTab.FREE, focusDay = window.day) }
    }

    fun openDay(day: Int) {
        _state.update { it.copy(daySheet = day) }
    }

    fun closeDay() {
        _state.update { it.copy(daySheet = null) }
    }

    fun openPlan(
        day: Int,
        startMin: Int,
        endMin: Int,
    ) {
        _state.update {
            it.copy(planDraft = PlanDraft(day = day, startMin = startMin, endMin = endMin))
        }
    }

    fun setPlanTitle(title: String) {
        _state.update { it.copy(planDraft = it.planDraft?.copy(title = title)) }
    }

    fun closePlan() {
        _state.update { it.copy(planDraft = null) }
    }

    fun savePlan() {
        val draft = _state.value.planDraft ?: return
        val authToken = token ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(planDraft = draft.copy(saving = true)) }
            try {
                api.savePlan(
                    authToken,
                    PlanRequest(
                        day = draft.day,
                        startMin = draft.startMin,
                        endMin = draft.endMin,
                        title = draft.title.ifBlank { "共同安排" },
                    ),
                )
                _state.update {
                    it.copy(planDraft = null, message = "安排已同步。")
                }
                doRefresh(silent = true)
            } catch (error: Exception) {
                _state.update {
                    it.copy(planDraft = draft.copy(saving = false), error = readable(error))
                }
            }
        }
    }

    fun deletePlan(planId: String) {
        val authToken = token ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                api.deletePlan(authToken, planId)
                _state.update { it.copy(message = "安排已删除。") }
                doRefresh(silent = true)
            } catch (error: Exception) {
                _state.update { it.copy(error = readable(error)) }
            }
        }
    }

    fun startOcr(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(ocrBusy = true, ocrNotice = null, error = null) }
            try {
                val personId = _state.value.me?.personId ?: "member_a"
                val tone = if (personId == "member_b") "a" else "b"
                val authToken = token ?: error("设备尚未登录")
                val imageBytes = TimetableUpload.jpeg(getApplication(), uri, personId)
                val recognized = api.recognizeTimetable(authToken, imageBytes, personId)
                val courses =
                    recognized.mapNotNull { item ->
                        val name = item.name.trim()
                        if (name.length < 2) {
                            null
                        } else {
                            Course(
                                day = item.day,
                                start = item.start,
                                end = item.end,
                                name = name,
                                tone = tone,
                            )
                        }
                    }
                val refined = CourseLexicon.refine(courses, tone)
                _state.update {
                    it.copy(
                        ocrBusy = false,
                        ocrNotice =
                            if (refined.isEmpty()) {
                                "DeepSeek 没有读出课程，请换一张更清晰的原始课表或手动添加。"
                            } else {
                                "已按原始课表的星期栏和节次行识别整图；教室信息已忽略，语料库已纠错。请确认。"
                            },
                        ocrCourses = refined,
                        message =
                            if (refined.isEmpty()) {
                                "没有识别出课程，可以手动添加。"
                            } else {
                                "识别到 ${refined.size} 节课，请确认后同步。"
                            },
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(ocrBusy = false, error = "识别失败：${readable(error)}")
                }
            }
        }
    }

    fun startManualCourse() {
        val course =
            Course(
                day = 0,
                start = "08:00",
                end = "09:35",
                name = "新课程",
                tone = if (_state.value.me?.personId == "member_b") "a" else "b",
            )
        _state.update {
            it.copy(
                ocrCourses = emptyList(),
                editCourse = CourseEditDraft(index = 0, course = course),
            )
        }
    }

    fun closeOcr() {
        _state.update { it.copy(ocrCourses = null, ocrNotice = null, editCourse = null) }
    }

    fun addOcrCourse() {
        val current = _state.value.ocrCourses.orEmpty()
        _state.update {
            it.copy(
                ocrCourses =
                    current +
                        Course(
                            day = 0,
                            start = "08:00",
                            end = "09:35",
                            name = "新课程",
                            tone = if (it.me?.personId == "member_b") "a" else "b",
                        ),
                editCourse =
                    CourseEditDraft(
                        index = current.size,
                        course =
                            Course(
                                day = 0,
                                start = "08:00",
                                end = "09:35",
                                name = "新课程",
                                tone = if (it.me?.personId == "member_b") "a" else "b",
                            ),
                    ),
            )
        }
    }

    fun editOcrCourse(index: Int) {
        val course = _state.value.ocrCourses?.getOrNull(index) ?: return
        _state.update { it.copy(editCourse = CourseEditDraft(index = index, course = course)) }
    }

    fun updateEditCourse(course: Course) {
        val draft = _state.value.editCourse ?: return
        _state.update { it.copy(editCourse = draft.copy(course = course)) }
    }

    fun saveEditCourse() {
        val draft = _state.value.editCourse ?: return
        val course =
            draft.course.copy(
                name = draft.course.name.trim().ifBlank { "新课程" },
                start = draft.course.start.ifBlank { "08:00" },
                end = draft.course.end.ifBlank { "09:35" },
            )
        val current = _state.value.ocrCourses.orEmpty().toMutableList()
        val index = draft.index
        if (index != null && index in current.indices) {
            current[index] = course
        } else {
            current.add(course)
        }
        _state.update {
            it.copy(
                ocrCourses =
                    current.sortedWith(
                        compareBy<Course> {
                                c ->
                            c.day
                        }.thenBy { c -> c.start },
                    ),
                editCourse = null,
            )
        }
    }

    fun cancelEditCourse() {
        _state.update { it.copy(editCourse = null) }
    }

    fun deleteOcrCourse(index: Int) {
        val current = _state.value.ocrCourses.orEmpty().toMutableList()
        if (index in current.indices) current.removeAt(index)
        _state.update { it.copy(ocrCourses = current) }
    }

    fun confirmOcr() {
        val courses = _state.value.ocrCourses.orEmpty()
        val personId = _state.value.me?.personId
        val authToken = token
        if (personId.isNullOrBlank() || authToken.isNullOrBlank()) {
            _state.update { it.copy(error = "还没有加入共同空间。") }
            return
        }
        if (courses.isEmpty()) {
            _state.update { it.copy(error = "至少保留一节课程。") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(busy = true, error = null) }
            try {
                api.saveTimetable(authToken, personId, courses)
                _state.update {
                    it.copy(
                        busy = false,
                        ocrCourses = null,
                        tab = HepaiTab.WEEK,
                        message = "课表已同步到对方。",
                    )
                }
                doRefresh(silent = true)
            } catch (error: Exception) {
                _state.update { it.copy(busy = false, error = readable(error)) }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun consumeError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        socket?.close(1000, "viewmodel cleared")
        socketJob?.cancel()
        pollingJob?.cancel()
        api.close()
        super.onCleared()
    }
}
