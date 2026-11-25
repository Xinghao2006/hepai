package com.example.hepai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hepai.data.AppUiState

private enum class JoinMode { CREATE, JOIN, RECOVER }

@Composable
fun OnboardingScreen(
    state: AppUiState,
    onCreate: (String, String) -> Unit,
    onJoin: (String, String, String) -> Unit,
    onRejoin: (String, String, String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by rememberSaveable { mutableStateOf(JoinMode.CREATE) }
    var name by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var personId by rememberSaveable { mutableStateOf("member_a") }

    val isCreate = mode == JoinMode.CREATE

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(34.dp))
        LogoLockup()
        Spacer(Modifier.height(28.dp))
        Text(
            "把两张课表，\n放进同一周。",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 38.sp, lineHeight = 46.sp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "各自上传自己的课表，空档和安排会自动同步。",
            style = MaterialTheme.typography.bodyMedium,
            color = Hepai.Muted,
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Hepai.CardDeep, RoundedCornerShape(12.dp))
                    .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            ModeTab("创建", mode == JoinMode.CREATE, Modifier.weight(1f)) { mode = JoinMode.CREATE }
            ModeTab("加入", mode == JoinMode.JOIN, Modifier.weight(1f)) {
                mode = JoinMode.JOIN
                if (personId == "member_a") personId = "member_b"
            }
            ModeTab("恢复", mode == JoinMode.RECOVER, Modifier.weight(1f)) { mode = JoinMode.RECOVER }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            when (mode) {
                JoinMode.CREATE -> "先建一个只有你们两个人的空间。"
                JoinMode.JOIN -> "输入对方发来的 6 位邀请码。"
                JoinMode.RECOVER -> "重装 App 后，用邀请码找回原来的身份。"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Hepai.Muted,
        )
        Spacer(Modifier.height(12.dp))

        FieldLabel("你的名字")
        HepaiField(
            value = name,
            onValueChange = { name = it.take(12) },
            placeholder = if (personId == "member_a") "成员 A" else "成员 B",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))
        FieldLabel("你是")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoleChip("成员 A", "member_a", personId == "member_a") { personId = "member_a" }
            RoleChip("成员 B", "member_b", personId == "member_b") { personId = "member_b" }
        }

        if (!isCreate) {
            Spacer(Modifier.height(12.dp))
            FieldLabel("6 位邀请码")
            HepaiField(
                value = code,
                onValueChange = { code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(6) },
                placeholder = "例如 DGGQMV",
                modifier = Modifier.fillMaxWidth(),
                capitalization = KeyboardCapitalization.Characters,
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(10.dp))
            Surface(
                color = Hepai.RoseWash,
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(
                        state.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Hepai.RoseDeep,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "点这里重试连接",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Hepai.Green,
                        modifier = Modifier.clickable(onClick = onRetry),
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text =
                when (mode) {
                    JoinMode.CREATE -> if (state.busy) "创建中…" else "创建共同空间"
                    JoinMode.JOIN -> if (state.busy) "加入中…" else "加入共同空间"
                    JoinMode.RECOVER -> if (state.busy) "恢复中…" else "恢复原身份"
                },
            enabled = !state.busy && name.trim().isNotEmpty() && (isCreate || code.length == 6),
            onClick = {
                val finalName = name.trim().ifEmpty { if (personId == "member_a") "成员 A" else "成员 B" }
                when (mode) {
                    JoinMode.CREATE -> onCreate(finalName, personId)
                    JoinMode.JOIN -> onJoin(finalName, personId, code)
                    JoinMode.RECOVER -> onRejoin(finalName, personId, code)
                }
            },
        )

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "课表截图只在手机本地识别",
                style = MaterialTheme.typography.labelSmall,
                color = Hepai.MutedLight,
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun ModeTab(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .background(if (active) Hepai.White else Color.Transparent, RoundedCornerShape(9.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                ),
            color = if (active) Hepai.Ink else Hepai.Muted,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
        color = Hepai.Muted,
        modifier = Modifier.padding(bottom = 5.dp),
    )
}

@Composable
private fun HepaiField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Hepai.MutedLight) },
        singleLine = true,
        shape = RoundedCornerShape(11.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Hepai.Ink),
        keyboardOptions = KeyboardOptions(capitalization = capitalization),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Hepai.GreenMid,
                unfocusedBorderColor = Hepai.Line,
                focusedContainerColor = Hepai.Card,
                unfocusedContainerColor = Hepai.Card,
                cursorColor = Hepai.Green,
            ),
    )
}

@Composable
private fun RoleChip(
    label: String,
    id: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = if (id == "member_a") Hepai.Green else Hepai.RoseDeep
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (active) activeColor.copy(alpha = 0.10f) else Hepai.Card,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                if (active) activeColor.copy(alpha = 0.45f) else Hepai.Line,
            ),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (active) activeColor else Hepai.Muted,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
        )
    }
}
