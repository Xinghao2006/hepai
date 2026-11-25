package com.example.hepai.data

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

object ScheduleLogic {
    const val DAY_START = 8 * 60
    const val DAY_END = 21 * 60 + 30
    const val SPAN = DAY_END - DAY_START

    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val dayFullNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun parseTime(value: String): Int? {
        val match = Regex("^(\\d{1,2})[:：](\\d{2})$").find(value.trim()) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    fun minutesToTime(minutes: Int): String {
        val safe = minutes.coerceIn(0, 24 * 60 - 1)
        return "%02d:%02d".format(safe / 60, safe % 60)
    }

    fun formatDuration(minutes: Int): String {
        if (minutes < 60) return "${minutes}m"
        val hours = minutes / 60
        val rest = minutes % 60
        return if (rest == 0) "${hours}h" else "${hours}h ${rest}m"
    }

    fun formatRange(
        startMin: Int,
        endMin: Int,
    ): String = "${minutesToTime(startMin)}–${minutesToTime(endMin)}"

    fun parseDuration(course: Course): Int? {
        val start = parseTime(course.start) ?: return null
        val end = parseTime(course.end) ?: return null
        if (end <= start) return null
        return end - start
    }

    fun clampWindow(
        start: Int,
        end: Int,
    ): IntRange? {
        val s = max(start, DAY_START)
        val e = min(end, DAY_END)
        return if (e > s) s until e else null
    }

    private fun merge(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return emptyList()
        val sorted = ranges.sortedBy { it.first }
        val result = mutableListOf<IntRange>()
        var current = sorted.first()
        for (range in sorted.drop(1)) {
            current =
                if (range.first <= current.last + 1) {
                    current.first..max(current.last, range.last)
                } else {
                    result.add(current)
                    range
                }
        }
        result.add(current)
        return result
    }

    private fun busyForDay(courses: List<Course>): List<IntRange> =
        merge(
            courses.filter { it.day in 0..6 }.mapNotNull { course ->
                val start = parseTime(course.start) ?: return@mapNotNull null
                val end = parseTime(course.end) ?: return@mapNotNull null
                clampWindow(start, end)
            },
        )

    fun freeForDay(
        courses: List<Course>,
        minimumMinutes: Int = 20,
    ): List<IntRange> {
        val busy = busyForDay(courses)
        if (busy.isEmpty()) return listOf(DAY_START until DAY_END)
        val result = mutableListOf<IntRange>()
        var cursor = DAY_START
        for (block in busy) {
            if (block.first > cursor) {
                val gap = cursor until block.first
                if (gap.last - gap.first + 1 >= minimumMinutes) result.add(gap)
            }
            cursor = max(cursor, block.last + 1)
        }
        if (cursor < DAY_END && DAY_END - cursor >= minimumMinutes) result.add(cursor until DAY_END)
        return result
    }

    fun commonFreeForDay(
        memberA: List<Course>,
        memberB: List<Course>,
        minimumMinutes: Int = 20,
    ): List<IntRange> {
        val busy = merge(busyForDay(memberA) + busyForDay(memberB))
        if (busy.isEmpty()) return listOf(DAY_START until DAY_END)
        val result = mutableListOf<IntRange>()
        var cursor = DAY_START
        for (block in busy) {
            if (block.first > cursor) {
                val gap = cursor until block.first
                if (gap.last - gap.first + 1 >= minimumMinutes) result.add(gap)
            }
            cursor = max(cursor, block.last + 1)
        }
        if (cursor < DAY_END && DAY_END - cursor >= minimumMinutes) result.add(cursor until DAY_END)
        return result
    }

    fun commonWindows(
        timetables: Map<String, Timetable>,
        minimumMinutes: Int = 20,
    ): List<Window> {
        val memberA = timetables["member_a"]?.courses.orEmpty()
        val memberB = timetables["member_b"]?.courses.orEmpty()
        return (0..6).flatMap { day ->
            commonFreeForDay(memberA.filter { it.day == day }, memberB.filter { it.day == day }, minimumMinutes)
                .map { Window(day, it.first, it.last + 1) }
        }.sortedWith(compareBy<Window> { it.day }.thenBy { it.startMin })
    }

    fun personIntervals(
        courses: List<Course>,
        day: Int,
    ): List<IntRange> = busyForDay(courses.filter { it.day == day })

    fun overlapForDay(
        memberA: List<Course>,
        memberB: List<Course>,
        day: Int,
    ): List<IntRange> {
        val a = busyForDay(memberA.filter { it.day == day })
        val b = busyForDay(memberB.filter { it.day == day })
        val result = mutableListOf<IntRange>()
        for (x in a) for (y in b) {
            val start = max(x.first, y.first)
            val end = min(x.last, y.last)
            if (end >= start) result.add(start..end)
        }
        return merge(result)
    }

    fun nextWindow(
        windows: List<Window>,
        today: Int,
        nowMinute: Int,
    ): Window? {
        if (windows.isEmpty()) return null
        return windows.firstOrNull { it.day > today || (it.day == today && it.endMin > nowMinute) }
            ?: windows.first()
    }

    fun buildWeekDates(today: LocalDate = LocalDate.now()): List<LocalDate> {
        val monday = today.with(DayOfWeek.MONDAY)
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    fun dateLabel(date: LocalDate): String = "%02d/%02d".format(date.monthValue, date.dayOfMonth)

    fun monthRangeLabel(dates: List<LocalDate>): String {
        if (dates.isEmpty()) return ""
        val first = dates.first()
        val last = dates.last()
        val month = first.month.name.take(3)
        val lastMonth = last.month.name.take(3)
        return if (first.month == last.month) {
            "$month ${first.dayOfMonth} — ${last.dayOfMonth}"
        } else {
            "$month ${first.dayOfMonth} — $lastMonth ${last.dayOfMonth}"
        }
    }

    fun seasonLabel(today: LocalDate = LocalDate.now()): String {
        val month = today.monthValue
        val season =
            when (month) {
                in 3..5 -> "SPRING"
                in 6..8 -> "SUMMER"
                in 9..11 -> "AUTUMN"
                else -> "WINTER"
            }
        return "${today.year} $season"
    }

    fun currentDayIndex(today: LocalDate = LocalDate.now()): Int = (today.dayOfWeek.value - 1).coerceIn(0, 6)

    fun currentMinuteOfDay(): Int {
        val now = java.time.LocalTime.now()
        return now.hour * 60 + now.minute
    }

    fun formatUpdatedAt(epochMillis: Long): String {
        if (epochMillis <= 0) return "还没更新"
        val date =
            java.time.Instant.ofEpochMilli(epochMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        return "${date.monthValue}/${date.dayOfMonth} 更新"
    }

    fun normalizedCourse(course: Course): Course =
        course.copy(
            start = minutesToTime(parseTime(course.start) ?: 0),
            end = minutesToTime(parseTime(course.end) ?: 0),
            name = course.name.trim().ifEmpty { "未命名课程" },
        )

    fun courseCount(timetable: Timetable?): Int = timetable?.courses?.size ?: 0
}
