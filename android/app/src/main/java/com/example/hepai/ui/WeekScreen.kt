package com.example.hepai.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hepai.data.AppUiState
import com.example.hepai.data.ScheduleLogic
import com.example.hepai.data.Window

private data class RailDay(
    val index: Int,
    val label: String,
    val date: String,
    val today: Boolean,
    val memberA: List<IntRange>,
    val memberB: List<IntRange>,
)

@Composable
fun WeekScreen(
    state: AppUiState,
    onNext: (Window) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dates = remember { ScheduleLogic.buildWeekDates() }
    val todayIndex = remember { ScheduleLogic.currentDayIndex() }
    val memberA = state.timetables["member_a"]?.courses.orEmpty()
    val memberB = state.timetables["member_b"]?.courses.orEmpty()
    val windows = remember(state.timetables) { ScheduleLogic.commonWindows(state.timetables) }
    val totalFree = windows.sumOf { it.minutes }
    val next =
        remember(windows) {
            ScheduleLogic.nextWindow(windows, todayIndex, ScheduleLogic.currentMinuteOfDay())
        }
    val overlapDays =
        remember(state.timetables) {
            (0..6).count { day ->
                ScheduleLogic.overlapForDay(memberA, memberB, day).sumOf { it.last - it.first + 1 } >= 20
            }
        }
    val memberACount = memberA.size
    val memberBCount = memberB.size
    val days =
        remember(dates, memberA, memberB, todayIndex) {
            dates.mapIndexed { index, date ->
                RailDay(
                    index = index,
                    label = ScheduleLogic.dayNames[index],
                    date = ScheduleLogic.dateLabel(date),
                    today = index == todayIndex,
                    memberA = ScheduleLogic.personIntervals(memberA, index),
                    memberB = ScheduleLogic.personIntervals(memberB, index),
                )
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoLockup()
            Row(
                modifier =
                    Modifier
                        .background(Hepai.Card, RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.width(5.dp).height(5.dp).background(Hepai.Leaf, CircleShape))
                Text(
                    if (state.busy) "同步中" else "已同步",
                    style = MaterialTheme.typography.labelSmall,
                    color = Hepai.Muted,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${ScheduleLogic.seasonLabel()}  /  ${ScheduleLogic.monthRangeLabel(dates)}",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.7.sp),
                    color = Hepai.Muted,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("共同周历", style = MaterialTheme.typography.displayLarge)
                    Text(".", style = MaterialTheme.typography.displayLarge, color = Hepai.Rose)
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(top = 24.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        ScheduleLogic.formatDuration(totalFree).replace(" ", ""),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                    )
                }
                Text(
                    "共同空档 · $overlapDays 天同时有课",
                    style = MaterialTheme.typography.labelSmall,
                    color = Hepai.Muted,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonSummary("成员 A", "$memberACount 段课", Hepai.Green, Hepai.GreenMid)
            Spacer(Modifier.weight(1f))
            NextSlotButton(next, onNext)
            Spacer(Modifier.weight(1f))
            PersonSummary("成员 B", "$memberBCount 段课", Hepai.RoseDeep, Hepai.Rose, alignEnd = true)
        }

        Spacer(Modifier.height(16.dp))
        SoftCard {
            Column(modifier = Modifier.padding(12.dp)) {
                SectionHead(
                    kicker = "WEEK AT A GLANCE",
                    title = "一周总览",
                    trailing = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            LegendDot("成员 A", Hepai.GreenMid)
                            LegendDot("成员 B", Hepai.Rose)
                        }
                    },
                )
                Spacer(Modifier.height(6.dp))
                WeekChart(days)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PersonSummary(
    name: String,
    count: String,
    avatarColor: Color,
    textColor: Color,
    alignEnd: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (alignEnd) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = textColor,
                )
                Text(count, style = MaterialTheme.typography.labelSmall, color = Hepai.Muted)
            }
            AvatarBadge("B", avatarColor)
        } else {
            AvatarBadge("A", avatarColor)
            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = textColor,
                )
                Text(count, style = MaterialTheme.typography.labelSmall, color = Hepai.Muted)
            }
        }
    }
}

@Composable
private fun NextSlotButton(
    next: Window?,
    onNext: (Window) -> Unit,
) {
    if (next == null) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Hepai.Card,
        ) {
            Text(
                "暂无空档",
                style = MaterialTheme.typography.labelSmall,
                color = Hepai.Muted,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
        return
    }
    Surface(
        modifier = Modifier.clickable { onNext(next) },
        shape = RoundedCornerShape(10.dp),
        color = Hepai.LeafWash,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(Modifier.width(4.dp).height(4.dp).background(Hepai.Leaf, CircleShape))
                    Text("下一段", style = MaterialTheme.typography.labelSmall, color = Hepai.Muted)
                }
                Text(
                    "${ScheduleLogic.dayFullNames[next.day]} ${ScheduleLogic.formatRange(next.startMin, next.endMin)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Medium),
                    color = Hepai.InkSoft,
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = Hepai.InkSoft,
                modifier = Modifier.width(13.dp).height(13.dp),
            )
        }
    }
}

@Composable
private fun LegendDot(
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.width(7.dp).height(7.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp), color = Hepai.Muted)
    }
}

@Composable
private fun WeekChart(days: List<RailDay>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Hepai.Card,
    ) {
        androidx.compose.foundation.Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
        ) {
            val axisW = 38.dp.toPx()
            val topH = 34.dp.toPx()
            val bodyH = size.height - topH - 2.dp.toPx()
            val columnW = (size.width - axisW) / 7f
            val bodyTop = topH

            val labelPaint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.argb(255, 124, 128, 117)
                    textSize = 7.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                }
            val dayPaint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.argb(255, 23, 37, 31)
                    textSize = 10.sp.toPx()
                    typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.NORMAL)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            val datePaint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.argb(255, 166, 167, 155)
                    textSize = 6.2.sp.toPx()
                    typeface = android.graphics.Typeface.create("monospace", android.graphics.Typeface.NORMAL)
                    textAlign = android.graphics.Paint.Align.CENTER
                }

            val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx()), 0f)

            days.forEachIndexed { index, day ->
                val cx = axisW + columnW * index + columnW / 2f
                val capsuleTop = bodyTop
                val capsuleW = 27.dp.toPx()
                drawRoundRect(
                    color = if (day.today) Hepai.Butter.copy(alpha = 0.38f) else Hepai.CardDeep,
                    topLeft = Offset(cx - capsuleW / 2f, capsuleTop),
                    size = Size(capsuleW, bodyH),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                )
                if (day.today) {
                    drawRoundRect(
                        color = Hepai.Rose.copy(alpha = 0.16f),
                        topLeft = Offset(cx - capsuleW / 2f, capsuleTop),
                        size = Size(capsuleW, bodyH),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(day.label, cx, 13.dp.toPx(), dayPaint)
                drawContext.canvas.nativeCanvas.drawText(day.date, cx, 27.dp.toPx(), datePaint)
                if (day.today) {
                    drawCircle(Hepai.Rose, radius = 2.dp.toPx(), center = Offset(cx, 20.dp.toPx()))
                }
            }

            for (hour in 9..21) {
                val minute = hour * 60
                val y = bodyTop + (minute - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN * bodyH
                drawLine(
                    color = Hepai.Ink.copy(alpha = 0.10f),
                    start = Offset(axisW, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx() / 2f,
                    pathEffect = dash,
                )
            }

            val marks = (8..21).map { it * 60 } + (21 * 60 + 30)
            for (minute in marks) {
                val y = bodyTop + (minute - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN * bodyH
                val text = ScheduleLogic.minutesToTime(minute)
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    axisW - 6.dp.toPx(),
                    y + 2.2.dp.toPx(),
                    labelPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT },
                )
                drawLine(
                    color = Hepai.Ink.copy(alpha = 0.16f),
                    start = Offset(axisW - 4.dp.toPx(), y),
                    end = Offset(axisW - 1.dp.toPx(), y),
                    strokeWidth = 1.dp.toPx() / 2f,
                )
            }

            days.forEachIndexed { index, day ->
                val center = axisW + columnW * index + columnW / 2f
                drawRail(day.memberA, center - 5.5.dp.toPx(), Hepai.GreenMid, bodyTop, bodyH)
                drawRail(day.memberB, center + 5.5.dp.toPx(), Hepai.Rose, bodyTop, bodyH)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRail(
    intervals: List<IntRange>,
    centerX: Float,
    color: Color,
    bodyTop: Float,
    bodyH: Float,
) {
    val width = 6.dp.toPx()
    intervals.forEach { interval ->
        val start = (interval.first - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN
        val end = (interval.last + 1 - ScheduleLogic.DAY_START).toFloat() / ScheduleLogic.SPAN
        val top = bodyTop + start * bodyH
        val height = ((end - start) * bodyH).coerceAtLeast(3.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(centerX - width / 2f, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        )
    }
}
