package com.example.hepai.data

import android.util.Base64
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiException(message: String, val status: Int = 0) : IOException(message)

class ApiClient(
    private val primaryBase: String = "https://api.example.com",
    private val fallbackBase: String = "https://hepai-api.example.workers.dev",
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val http =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private fun request(
        token: String?,
        builder: Request.Builder,
    ): Request.Builder {
        if (!token.isNullOrBlank()) builder.header("Authorization", "Bearer $token")
        return builder
    }

    private fun execute(request: Request): String {
        val response: Response = http.newCall(request).execute()
        response.use {
            val body = it.body?.string().orEmpty()
            if (!it.isSuccessful) {
                val message =
                    runCatching {
                        json.decodeFromString(ErrorResponse.serializer(), body).error
                    }.getOrNull().takeUnless { value -> value.isNullOrBlank() }
                        ?: "请求失败（${it.code}）"
                throw ApiException(message, it.code)
            }
            return body
        }
    }

    private fun bases(): List<String> = listOf(primaryBase, fallbackBase).distinct()

    private fun post(
        path: String,
        body: String,
        token: String? = null,
    ): String {
        var lastError: IOException? = null
        for (base in bases()) {
            try {
                val request =
                    request(
                        token,
                        Request.Builder()
                            .url("$base$path")
                            .post(body.toRequestBody(jsonMedia)),
                    ).build()
                return execute(request)
            } catch (error: ApiException) {
                throw error
            } catch (error: IOException) {
                lastError = error
            }
        }
        throw lastError ?: IOException("无法连接服务器")
    }

    private fun get(
        path: String,
        token: String,
    ): String {
        var lastError: IOException? = null
        for (base in bases()) {
            try {
                val request =
                    request(
                        token,
                        Request.Builder().url("$base$path").get(),
                    ).build()
                return execute(request)
            } catch (error: ApiException) {
                throw error
            } catch (error: IOException) {
                lastError = error
            }
        }
        throw lastError ?: IOException("无法连接服务器")
    }

    private fun delete(
        path: String,
        token: String,
    ): String {
        var lastError: IOException? = null
        for (base in bases()) {
            try {
                val request =
                    request(
                        token,
                        Request.Builder().url("$base$path").delete(),
                    ).build()
                return execute(request)
            } catch (error: ApiException) {
                throw error
            } catch (error: IOException) {
                lastError = error
            }
        }
        throw lastError ?: IOException("无法连接服务器")
    }

    fun bootstrap(): AuthResponse {
        val body = post("/v1/auth/device", "{}")
        return json.decodeFromString(AuthResponse.serializer(), body)
    }

    fun state(token: String): StateResponse {
        return json.decodeFromString(StateResponse.serializer(), get("/v1/state", token))
    }

    fun createSpace(
        token: String,
        name: String,
        personId: String,
    ): StateResponse {
        val body =
            json.encodeToString(
                CreateRequest.serializer(),
                CreateRequest(name = name, personId = personId),
            )
        return json.decodeFromString(StateResponse.serializer(), post("/v1/space/create", body, token))
    }

    fun joinSpace(
        token: String,
        name: String,
        personId: String,
        code: String,
    ): StateResponse {
        val body =
            json.encodeToString(
                JoinRequest.serializer(),
                JoinRequest(name = name, personId = personId, code = code),
            )
        return json.decodeFromString(StateResponse.serializer(), post("/v1/space/join", body, token))
    }

    fun rejoinSpace(
        token: String,
        name: String,
        personId: String,
        code: String,
    ): StateResponse {
        val body =
            json.encodeToString(
                RejoinRequest.serializer(),
                RejoinRequest(name = name, personId = personId, code = code),
            )
        return json.decodeFromString(StateResponse.serializer(), post("/v1/space/rejoin", body, token))
    }

    fun recognizeTimetable(
        token: String,
        imageBytes: ByteArray,
        personId: String,
    ): List<OcrCourse> {
        val body =
            json.encodeToString(
                OcrImageRequest.serializer(),
                OcrImageRequest(
                    image = Base64.encodeToString(imageBytes, Base64.NO_WRAP),
                    personId = personId,
                ),
            )
        val response = post("/v1/ocr", body, token)
        return json.decodeFromString(OcrCourseResponse.serializer(), response).courses
    }

    fun saveTimetable(
        token: String,
        personId: String,
        courses: List<Course>,
    ): StatusResponse {
        val body =
            json.encodeToString(
                TimetableRequest.serializer(),
                TimetableRequest(personId = personId, courses = courses),
            )
        return json.decodeFromString(StatusResponse.serializer(), post("/v1/timetable", body, token))
    }

    fun savePlan(
        token: String,
        draft: PlanRequest,
    ): StatusResponse {
        val body = json.encodeToString(PlanRequest.serializer(), draft)
        return json.decodeFromString(StatusResponse.serializer(), post("/v1/plan", body, token))
    }

    fun deletePlan(
        token: String,
        id: String,
    ): StatusResponse {
        return json.decodeFromString(
            StatusResponse.serializer(),
            delete("/v1/plan/$id", token),
        )
    }

    fun openSocket(
        token: String,
        listener: WebSocketListener,
    ): WebSocket {
        val url = primaryBase.replaceFirst("https://", "wss://") + "/v1/ws"
        val request =
            Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .build()
        return http.newWebSocket(request, listener)
    }

    fun close() {
        http.dispatcher.executorService.shutdown()
    }
}
