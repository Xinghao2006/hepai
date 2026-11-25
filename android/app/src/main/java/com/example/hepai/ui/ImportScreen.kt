package com.example.hepai.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hepai.data.AppUiState
import com.example.hepai.data.ScheduleLogic
import com.example.hepai.data.Timetable

@Composable
fun ImportScreen(
    state: AppUiState,
    onPickImage: (Uri) -> Unit,
    onRefresh: () -> Unit,
    onManualAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val picker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri != null) onPickImage(uri)
        }

    val meId = state.me?.personId
    val invite = state.space?.inviteCode.orEmpty()
    val members = state.space?.members.orEmpty()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "TWO TIMETABLES",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("课表", style = MaterialTheme.typography.displayLarge)
                    Text(".", style = MaterialTheme.typography.displayLarge, color = Hepai.Rose)
                }
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = "刷新", tint = Hepai.Muted)
            }
        }

        Spacer(Modifier.height(6.dp))
        Surface(
            color = Hepai.Green,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Text(
                            "共同空间",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                            color = Hepai.White.copy(alpha = 0.55f),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                invite.ifBlank { "等待创建" },
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 26.sp,
                                        letterSpacing = 2.sp,
                                        color = Hepai.White,
                                    ),
                            )
                            Spacer(Modifier.width(7.dp))
                            IconButton(
                                onClick = {
                                    copyText(context, invite)
                                },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "复制邀请码",
                                    tint = Hepai.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                    Pill(
                        if (members.size >= 2) "两人已连接" else "等待对方",
                        color = Hepai.White,
                        background = Hepai.White.copy(alpha = 0.13f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    members.forEach { member ->
                        Row(
                            modifier =
                                Modifier
                                    .background(Hepai.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(Modifier.size(5.dp).background(Hepai.Leaf, CircleShape))
                            Text(
                                member.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                color = Hepai.White.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    "SOURCES",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Text("两个人的课表", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                "本地识别",
                style = MaterialTheme.typography.labelSmall,
                color = Hepai.Muted,
            )
        }
        Spacer(Modifier.height(7.dp))
        SourceCard(
            name = "成员 A",
            initial = "A",
            color = Hepai.Green,
            timetable = state.timetables["member_a"],
            isMe = meId == "member_a",
            busy = state.ocrBusy && meId == "member_a",
            onPick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        )
        Spacer(Modifier.height(7.dp))
        SourceCard(
            name = "成员 B",
            initial = "B",
            color = Hepai.Rose,
            timetable = state.timetables["member_b"],
            isMe = meId == "member_b",
            busy = state.ocrBusy && meId == "member_b",
            onPick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        )

        Spacer(Modifier.height(12.dp))
        Surface(
            color = Hepai.Card,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    modifier = Modifier.size(24.dp).background(Hepai.LeafWash, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = Hepai.Green,
                        modifier = Modifier.size(13.dp),
                    )
                }
                Column {
                    Text(
                        "截图只在手机本地识别",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    )
                    Text(
                        "图片不会上传；确认后的课程 JSON 才会同步给对方。",
                        style = MaterialTheme.typography.labelSmall,
                        color = Hepai.Muted,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "上传后可以逐节修改，确认前不会影响对方的课表。",
            style = MaterialTheme.typography.labelSmall,
            color = Hepai.MutedLight,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        Spacer(Modifier.height(6.dp))
        SoftButton("手动添加课程", onManualAdd, Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SourceCard(
    name: String,
    initial: String,
    color: Color,
    timetable: Timetable?,
    isMe: Boolean,
    busy: Boolean,
    onPick: () -> Unit,
) {
    val count = timetable?.courses?.size ?: 0
    Surface(
        color = Hepai.Card,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarBadge(initial, color)
                Spacer(Modifier.width(7.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        if (isMe) Pill("我", color = Hepai.Green, background = Hepai.LeafWash)
                    }
                    Text(
                        if (count > 0) {
                            "$count 节课 · ${ScheduleLogic.formatUpdatedAt(
                                timetable?.updatedAt ?: 0,
                            )}"
                        } else {
                            "还没有上传课表"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Hepai.Muted,
                    )
                }
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 1.5.dp,
                        color = color,
                    )
                } else if (isMe) {
                    Surface(
                        modifier = Modifier.clickable(onClick = onPick),
                        color = Hepai.LeafWash,
                        shape = RoundedCornerShape(9.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                Icons.Outlined.UploadFile,
                                contentDescription = null,
                                tint = Hepai.Green,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                if (count > 0) "重新上传" else "上传",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                color = Hepai.Green,
                            )
                        }
                    }
                }
            }
            if (count > 0) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    timetable?.courses.orEmpty().take(3).forEach { course ->
                        Text(
                            course.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.5.sp),
                            color = Hepai.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .background(Hepai.CardDeep, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                    if (count > 3) {
                        Text(
                            "+${count - 3}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.5.sp),
                            color = Hepai.Muted,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp),
                        )
                    }
                }
            } else if (!isMe) {
                Spacer(Modifier.height(7.dp))
                Text(
                    "等对方上传后会自动出现。",
                    style = MaterialTheme.typography.labelSmall,
                    color = Hepai.MutedLight,
                )
            }
        }
    }
}

private fun copyText(
    context: Context,
    text: String,
) {
    if (text.isBlank()) return
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("邀请码", text))
}
