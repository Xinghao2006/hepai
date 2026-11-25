package com.example.hepai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hepai.data.BindingConfig
import com.example.hepai.data.HepaiTab
import com.example.hepai.data.MainViewModel

@Composable
fun HepaiApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error
        if (!text.isNullOrBlank() && state.space != null) {
            snackbar.showSnackbar(text)
            if (state.message != null) viewModel.consumeMessage()
            if (state.error != null) viewModel.consumeError()
        }
    }

    if (state.booting) {
        SplashScreen()
        return
    }

    if (state.space == null) {
        if (BindingConfig.SPACE_CODE.isNotBlank()) {
            BoundWaitingScreen(
                error = state.error,
                onRetry = viewModel::retryConnection,
            )
        } else {
            Box(Modifier.fillMaxSize()) {
                OnboardingScreen(
                    state = state,
                    onCreate = viewModel::createSpace,
                    onJoin = viewModel::joinSpace,
                    onRejoin = viewModel::rejoinSpace,
                    onRetry = viewModel::retryConnection,
                )
                HepaiSnackbarHost(
                    snackbar = snackbar,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = Hepai.Paper,
        snackbarHost = {
            HepaiSnackbarHost(
                snackbar = snackbar,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        },
        bottomBar = {
            BottomNav(
                selected = state.tab,
                onSelect = viewModel::selectTab,
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Hepai.Paper)
                    .padding(innerPadding),
        ) {
            when (state.tab) {
                HepaiTab.WEEK ->
                    WeekScreen(
                        state = state,
                        onNext = viewModel::focusWindow,
                    )
                HepaiTab.FREE ->
                    FreeScreen(
                        state = state,
                        onOpenDay = viewModel::openDay,
                        onOpenPlan = viewModel::openPlan,
                        onDeletePlan = viewModel::deletePlan,
                    )
                HepaiTab.IMPORT ->
                    ImportScreen(
                        state = state,
                        onPickImage = viewModel::startOcr,
                        onRefresh = viewModel::refresh,
                        onManualAdd = viewModel::startManualCourse,
                    )
            }
        }
    }

    state.daySheet?.let { day ->
        DayDetailSheet(
            state = state,
            day = day,
            onDismiss = viewModel::closeDay,
            onOpenPlan = viewModel::openPlan,
        )
    }

    state.planDraft?.let { draft ->
        PlanDialog(
            draft = draft,
            onTitleChange = viewModel::setPlanTitle,
            onDismiss = viewModel::closePlan,
            onSave = viewModel::savePlan,
        )
    }

    if (state.ocrCourses != null) {
        OcrReviewScreen(
            state = state,
            onClose = viewModel::closeOcr,
            onAdd = viewModel::addOcrCourse,
            onEdit = viewModel::editOcrCourse,
            onDelete = viewModel::deleteOcrCourse,
            onConfirm = viewModel::confirmOcr,
            onEditDraftChange = viewModel::updateEditCourse,
            onEditCancel = viewModel::cancelEditCourse,
            onEditSave = viewModel::saveEditCourse,
        )
    }
}

@Composable
private fun BoundWaitingScreen(
    error: String?,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Hepai.Paper), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp),
        ) {
            LogoMark()
            Spacer(Modifier.height(18.dp))
            Text("正在进入共同空间", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            if (error == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Hepai.Green,
                )
            } else {
                Text(
                    "暂时连接不上服务器。请检查网络后重试。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Hepai.Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                SoftButton("重新连接", onRetry)
            }
        }
    }
}

@Composable
private fun HepaiSnackbarHost(
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = snackbar, modifier = modifier) { data ->
        Snackbar(
            containerColor = Hepai.Ink,
            contentColor = Hepai.White,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                data.visuals.message,
                style = MaterialTheme.typography.bodySmall.copy(color = Hepai.White),
                color = Hepai.White,
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Hepai.Paper,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.background(Hepai.Card, MaterialTheme.shapes.large).padding(26.dp)) {
                LogoMark()
            }
        }
    }
}
