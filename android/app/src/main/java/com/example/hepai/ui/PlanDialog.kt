package com.example.hepai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hepai.data.PlanDraft
import com.example.hepai.data.ScheduleLogic

@Composable
fun PlanDialog(
    draft: PlanDraft,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Hepai.Paper,
            border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "PLAN A WINDOW",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Text("安排空档", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${ScheduleLogic.dayFullNames[draft.day]}  ${ScheduleLogic.formatRange(
                        draft.startMin,
                        draft.endMin,
                    )}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Hepai.Muted,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = { onTitleChange(it.take(30)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("比如：一起吃饭", style = MaterialTheme.typography.bodyMedium, color = Hepai.MutedLight)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(11.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Hepai.Ink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Hepai.GreenMid,
                            unfocusedBorderColor = Hepai.Line,
                            focusedContainerColor = Hepai.Card,
                            unfocusedContainerColor = Hepai.Card,
                            cursorColor = Hepai.Green,
                        ),
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SoftButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = if (draft.saving) "保存中…" else "保存安排",
                        onClick = onSave,
                        enabled = !draft.saving,
                        modifier = Modifier.weight(1.35f),
                    )
                }
            }
        }
    }
}
