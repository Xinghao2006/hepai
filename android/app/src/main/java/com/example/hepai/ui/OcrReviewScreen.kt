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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hepai.data.AppUiState
import com.example.hepai.data.Course
import com.example.hepai.data.CourseEditDraft
import com.example.hepai.data.ScheduleLogic

@Composable
fun OcrReviewScreen(
    state: AppUiState,
    onClose: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onConfirm: () -> Unit,
    onEditDraftChange: (Course) -> Unit,
    onEditCancel: () -> Unit,
    onEditSave: () -> Unit,
) {
    val courses = state.ocrCourses.orEmpty()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Hepai.Paper),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "LOCAL REVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Text("识别结果", style = MaterialTheme.typography.headlineLarge)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "关闭", tint = Hepai.Ink)
            }
        }

        Surface(
            modifier = Modifier.padding(horizontal = 18.dp),
            color = Hepai.LeafWash,
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.ocrNotice ?: "图片只在本地识别，没有上传。确认后只同步课程 JSON。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Hepai.Green,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                )
                Pill("${courses.size} 节", color = Hepai.Green, background = Hepai.White.copy(alpha = 0.65f))
            }
        }

        if (courses.isEmpty()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("没有识别出课程", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(5.dp))
                Text(
                    "可能是版式特殊或图片不够清晰。可以手动添加。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Hepai.Muted,
                )
                Spacer(Modifier.height(14.dp))
                SoftButton("手动添加一节课", onAdd)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item { Spacer(Modifier.height(10.dp)) }
                itemsIndexed(courses) { index, course ->
                    CourseRow(
                        course = course,
                        onEdit = { onEdit(index) },
                        onDelete = { onDelete(index) },
                    )
                }
                item {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onAdd)
                                .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            tint = Hepai.Green,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text("再加一节课", style = MaterialTheme.typography.bodySmall, color = Hepai.Green)
                    }
                }
                item { Spacer(Modifier.height(78.dp)) }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Hepai.Paper)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            PrimaryButton(
                text =
                    when {
                        state.busy -> "同步中…"
                        courses.isEmpty() -> "还没有课程"
                        else -> "同步这 ${courses.size} 节课"
                    },
                enabled = courses.isNotEmpty() && !state.busy,
                onClick = onConfirm,
            )
        }
    }

    if (state.editCourse != null) {
        CourseEditDialog(
            draft = state.editCourse,
            onChange = onEditDraftChange,
            onDismiss = onEditCancel,
            onSave = onEditSave,
        )
    }
}

@Composable
private fun CourseRow(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        color = Hepai.Card,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.width(48.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    ScheduleLogic.dayFullNames.getOrNull(course.day) ?: "周一",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    "${course.start}–${course.end}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.2.sp),
                    color = Hepai.Muted,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    course.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                )
                Text(
                    listOf(course.location, course.periodLabel).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Hepai.Muted,
                    maxLines = 1,
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "编辑",
                    tint = Hepai.Muted,
                    modifier = Modifier.size(14.dp),
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "删除",
                    tint = Hepai.MutedLight,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun CourseEditDialog(
    draft: CourseEditDraft,
    onChange: (Course) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val course = draft.course
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Hepai.Paper,
            border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    if (draft.index == null) "新增课程" else "修改课程",
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    value = course.name,
                    onValueChange = { onChange(course.copy(name = it.take(30))) },
                    label = { Text("课程名") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = editFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    ScheduleLogic.dayNames.forEachIndexed { index, name ->
                        val active = course.day == index
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .background(if (active) Hepai.LeafWash else Hepai.Card, RoundedCornerShape(7.dp))
                                    .clickable { onChange(course.copy(day = index)) }
                                    .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) Hepai.Green else Hepai.Muted,
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = course.start,
                        onValueChange = { onChange(course.copy(start = it.take(5))) },
                        label = { Text("开始") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = editFieldColors(),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = course.end,
                        onValueChange = { onChange(course.copy(end = it.take(5))) },
                        label = { Text("结束") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = editFieldColors(),
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = course.location,
                    onValueChange = { onChange(course.copy(location = it.take(40))) },
                    label = { Text("地点（可选）") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = editFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    SoftButton("取消", onDismiss, Modifier.weight(1f))
                    PrimaryButton("保存课程", onSave, Modifier.weight(1.4f))
                }
            }
        }
    }
}

@Composable
private fun editFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Hepai.GreenMid,
        unfocusedBorderColor = Hepai.Line,
        focusedContainerColor = Hepai.Card,
        unfocusedContainerColor = Hepai.Card,
        cursorColor = Hepai.Green,
    )
