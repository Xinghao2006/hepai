package com.example.hepai.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hepai.data.AppUiState
import com.example.hepai.data.Course
import com.example.hepai.data.Plan
import com.example.hepai.data.ScheduleLogic
import com.example.hepai.data.Window
import java.time.LocalDate

@Composable
fun FreeScreen(
    state: AppUiState,
    onOpenDay: (Int) -> Unit,
    onOpenPlan: (Int, Int, Int) -> Unit,
    onDeletePlan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dates = remember { ScheduleLogic.buildWeekDates() }
    val windows = remember(state.timetables) { ScheduleLogic.commonWindows(state.timetables) }
    val quick = remember(windows) { windows.sortedByDescending { it.minutes }.take(3) }
    val total = windows.sumOf { it.minutes }

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
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "SHARED FREE TIME",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("空档", style = MaterialTheme.typography.displayLarge)
                    Text(".", style = MaterialTheme.typography.displayLarge, color = Hepai.Rose)
                }
            }
            val firstWindow = quick.firstOrNull()
            Surface(
                shape = CircleShape,
                color = Hepai.Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
            ) {
                IconButton(
                    onClick = {
                        firstWindow?.let { onOpenPlan(it.day, it.startMin, it.endMin) }
                    },
                    enabled = firstWindow != null,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "安排空档",
                        tint = if (firstWindow != null) Hepai.Muted else Hepai.MutedLight,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Surface(
            color = Hepai.Card,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                Text(
                    "NOV 23 — 29",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    color = Hepai.Muted,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        ScheduleLogic.formatDuration(total).replace(" ", ""),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 39.sp, lineHeight = 43.sp),
                        color = Hepai.Ink,
                    )
                }
                Text(
                    "共同空档 · 08:00 — 21:30",
                    style = MaterialTheme.typography.labelSmall,
                    color = Hepai.Muted,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    "BEST WINDOWS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Text("三个空档窗口", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            quick.forEach { window ->
                QuickWindowCard(
                    window = window,
                    date = dates.getOrNull(window.day),
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenPlan(window.day, window.startMin, window.endMin) },
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        SoftCard {
            Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp)) {
                SectionHead(
                    kicker = "DAY BY DAY",
                    title = "按天",
                    trailing = {
                        Text(
                            "08:00 — 21:30",
                            style = MaterialTheme.typography.labelSmall,
                            color = Hepai.Muted,
                        )
                    },
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 44.dp, end = 34.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf("08", "12", "16", "20", "21:30").forEach {
                        Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp), color = Hepai.Muted)
                    }
                }
                Spacer(Modifier.height(2.dp))
                dates.forEachIndexed { index, date ->
                    DayRow(
                        date = date,
                        dayIndex = index,
                        windows = windows.filter { it.day == index },
                        isToday = index == ScheduleLogic.currentDayIndex(),
                        isFocus = state.focusDay == index,
                        onClick = { onOpenDay(index) },
                    )
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
                    "PLANS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = Hepai.Muted,
                )
                Text("共同安排", style = MaterialTheme.typography.titleLarge)
            }
            Pill("${state.plans.size} 个", color = Hepai.Muted)
        }
        Spacer(Modifier.height(7.dp))
        if (state.plans.isEmpty()) {
            EmptyDashedCard(
                title = "还没有安排",
                subtitle = "点空白时间开始安排。",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                state.plans.take(4).forEach { plan ->
                    PlanRow(plan = plan, onDelete = { onDeletePlan(plan.id) })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QuickWindowCard(
    window: Window,
    date: LocalDate?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = Hepai.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
    ) {
        Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    ScheduleLogic.dayFullNames[window.day],
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    date?.let { ScheduleLogic.dateLabel(it) } ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                    color = Hepai.Muted,
                )
            }
            Text(
                ScheduleLogic.formatRange(window.startMin, window.endMin),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Medium),
                color = Hepai.InkSoft,
                maxLines = 1,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ScheduleLogic.formatDuration(window.minutes),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = Hepai.Muted,
                )
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .background(Hepai.LeafWash, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "安排",
                        tint = Hepai.Green,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRow(
    date: LocalDate,
    dayIndex: Int,
    windows: List<Window>,
    isToday: Boolean,
    isFocus: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    when {
                        isFocus -> Hepai.LeafWash.copy(alpha = 0.8f)
                        isToday -> Hepai.Butter.copy(alpha = 0.16f)
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(7.dp),
                )
                .clickable(onClick = onClick)
                .height(34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(44.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                ScheduleLogic.dayFullNames[dayIndex],
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Medium),
                color = if (isToday) Hepai.RoseDeep else Hepai.Ink,
            )
            Text(
                ScheduleLogic.dateLabel(date),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                color = Hepai.Muted,
            )
        }
        Canvas(
            modifier =
                Modifier
                    .weight(1f)
                    .height(16.dp)
                    .padding(end = 8.dp),
        ) {
            drawRoundRect(
                color = if (isToday) Hepai.White.copy(alpha = 0.65f) else Hepai.CardDeep,
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            )
            windows.forEach { window ->
                val start = (window.startMin - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN
                val end = (window.endMin - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN
                drawRoundRect(
                    color = if (isToday) Hepai.Leaf.copy(alpha = 0.9f) else Hepai.Leaf,
                    topLeft = Offset(size.width * start, 3.dp.toPx()),
                    size = Size((size.width * (end - start)).coerceAtLeast(2.dp.toPx()), size.height - 6.dp.toPx()),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                )
            }
        }
        Text(
            ScheduleLogic.formatDuration(windows.sumOf { it.minutes }),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
            color = Hepai.Muted,
            modifier = Modifier.width(34.dp),
        )
    }
}

@Composable
private fun PlanRow(
    plan: Plan,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = Hepai.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 2.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                Text(
                    "${ScheduleLogic.dayFullNames[plan.day]}  ${ScheduleLogic.formatRange(plan.startMin, plan.endMin)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Hepai.Muted,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "删除",
                    tint = Hepai.MutedLight,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(
    state: AppUiState,
    day: Int,
    onDismiss: () -> Unit,
    onOpenPlan: (Int, Int, Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val memberA = state.timetables["member_a"]?.courses.orEmpty().filter { it.day == day }
    val memberB = state.timetables["member_b"]?.courses.orEmpty().filter { it.day == day }
    val windows = ScheduleLogic.commonFreeForDay(memberA, memberB)
    val total = windows.sumOf { it.last - it.first + 1 }
    val date = ScheduleLogic.buildWeekDates().getOrNull(day)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Hepai.Paper,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .width(34.dp)
                        .height(4.dp)
                        .background(Hepai.Line, RoundedCornerShape(999.dp)),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        "${ScheduleLogic.dayFullNames[day].uppercase()} · ${date?.let { ScheduleLogic.dateLabel(it) } ?: ""}",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = Hepai.Muted,
                    )
                    Text(ScheduleLogic.dayFullNames[day], style = MaterialTheme.typography.headlineLarge)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "关闭", tint = Hepai.Ink)
                }
            }

            Surface(
                color = Hepai.LeafWash,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Leaf.copy(alpha = 0.3f)),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column {
                            Text(
                                ScheduleLogic.formatDuration(total),
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            )
                            Text("共同空档", style = MaterialTheme.typography.labelSmall, color = Hepai.Green)
                        }
                        Pill("${windows.size} 段空档", color = Hepai.Green, background = Hepai.White.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        listOf("08", "12", "16", "20", "21:30").forEach {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                                color = Hepai.Green,
                            )
                        }
                    }
                    Canvas(
                        modifier = Modifier.fillMaxWidth().height(14.dp).padding(top = 3.dp),
                    ) {
                        drawRoundRect(
                            color = Hepai.White.copy(alpha = 0.7f),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                        )
                        windows.forEach { range ->
                            val start = (range.first - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN
                            val end = (range.last + 1 - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN
                            drawRoundRect(
                                color = Hepai.Leaf,
                                topLeft = Offset(size.width * start, 3.dp.toPx()),
                                size =
                                    Size(
                                        (size.width * (end - start)).coerceAtLeast(2.dp.toPx()),
                                        size.height - 6.dp.toPx(),
                                    ),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PersonCourseColumn("成员 A", "A", Hepai.Green, Hepai.GreenMid, memberA, true, Modifier.weight(1f))
                PersonCourseColumn("成员 B", "B", Hepai.RoseDeep, Hepai.Rose, memberB, false, Modifier.weight(1f))
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("共同空档", style = MaterialTheme.typography.titleLarge)
                Text("08:00 — 21:30", style = MaterialTheme.typography.labelSmall, color = Hepai.Muted)
            }
            Spacer(Modifier.height(7.dp))
            windows.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { range ->
                        Surface(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clickable { onOpenPlan(day, range.first, range.last + 1) },
                            shape = RoundedCornerShape(11.dp),
                            color = Hepai.Card,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        ScheduleLogic.formatRange(range.first, range.last + 1),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    )
                                    Text(
                                        ScheduleLogic.formatDuration(range.last - range.first + 1),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Hepai.Muted,
                                    )
                                }
                                Box(
                                    modifier = Modifier.size(20.dp).background(Hepai.CardDeep, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Outlined.Add,
                                        contentDescription = "安排",
                                        tint = Hepai.InkSoft,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "安排一段空档",
                enabled = windows.isNotEmpty(),
                onClick = {
                    val best = windows.maxByOrNull { it.last - it.first } ?: return@PrimaryButton
                    onOpenPlan(day, best.first, best.last + 1)
                },
            )
        }
    }
}

@Composable
private fun PersonCourseColumn(
    name: String,
    initial: String,
    deepColor: Color,
    accent: Color,
    courses: List<Course>,
    isMemberA: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AvatarBadge(initial, if (isMemberA) Hepai.Green else accent)
            Text(
                name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = deepColor,
            )
            Text("${courses.size} 段", style = MaterialTheme.typography.labelSmall, color = Hepai.Muted)
        }
        Spacer(Modifier.height(6.dp))
        if (courses.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Hepai.Card,
                shape = RoundedCornerShape(11.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
            ) {
                Text(
                    "全天没课",
                    style = MaterialTheme.typography.bodySmall,
                    color = Hepai.Muted,
                    modifier = Modifier.padding(10.dp),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                courses.forEach { course ->
                    Surface(
                        color = if (isMemberA) Hepai.Green else Hepai.RoseWash,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "${course.start}–${course.end}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.5.sp),
                                    color = if (isMemberA) Hepai.White.copy(alpha = 0.72f) else Hepai.RoseDeep,
                                )
                                Text(
                                    course.periodLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                                    color = if (isMemberA) Hepai.White.copy(alpha = 0.55f) else Hepai.Muted,
                                )
                            }
                            Text(
                                course.name,
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isMemberA) Hepai.White else Hepai.Ink,
                                    ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (course.location.isNotBlank()) {
                                Text(
                                    course.location,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                                    color = if (isMemberA) Hepai.White.copy(alpha = 0.62f) else Hepai.Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
