package com.example.hepai.data

/**
 * Small local course-name lexicon used after OCR.
 *
 * This is a generic example. Replace these entries with your own course names
 * or load them from a remote configuration. Matching is conservative: it only
 * rewrites an OCR name when it is an alias, a substring, or one/two edits away.
 */
object CourseLexicon {
    private data class Entry(val canonical: String, val aliases: List<String> = emptyList())

    private val memberA =
        listOf(
            Entry("高等数学", listOf("高数", "数学")),
            Entry("大学英语", listOf("英语", "大英")),
            Entry("程序设计", listOf("程序设计基础", "编程")),
            Entry("数据结构", listOf("数据结构与算法")),
            Entry("计算机网络", listOf("网络")),
            Entry("大学体育", listOf("体育")),
        )

    private val memberB =
        listOf(
            Entry("高等数学", listOf("高数", "数学")),
            Entry("大学英语", listOf("英语", "大英")),
            Entry("大学物理", listOf("物理")),
            Entry("无机化学", listOf("无机及分析化学", "化学")),
            Entry("有机化学", listOf("有机")),
            Entry("大学体育", listOf("体育")),
        )

    fun canonicalize(
        raw: String,
        tone: String,
    ): String {
        val source = raw.trim()
        if (source.length < 2) return source
        val normalized = normalize(source)
        if (normalized.isBlank()) return source
        val entries = if (tone == "a") memberA else memberB

        var best: Entry? = null
        var bestScore = Int.MAX_VALUE
        var bestLength = 0

        for (entry in entries) {
            val names = listOf(entry.canonical) + entry.aliases
            for (candidate in names) {
                val target = normalize(candidate)
                if (target.isBlank()) continue
                val score =
                    when {
                        normalized == target -> 0
                        normalized.contains(target) || target.contains(normalized) -> 1
                        else -> editDistance(normalized, target)
                    }
                val limit =
                    when {
                        target.length <= 3 -> 0
                        target.length <= 5 -> 1
                        else -> 2
                    }
                if (score <= limit && score <= bestScore) {
                    if (score < bestScore || target.length > bestLength) {
                        best = entry
                        bestScore = score
                        bestLength = target.length
                    }
                }
            }
        }
        return best?.canonical ?: source
    }

    fun refine(
        courses: List<Course>,
        tone: String,
    ): List<Course> {
        val mapped =
            courses.map { course ->
                course.copy(name = canonicalize(course.name, tone))
            }.sortedWith(compareBy<Course> { it.day }.thenBy { it.start })

        val merged = mutableListOf<Course>()
        for (course in mapped) {
            val existingIndex =
                merged.indexOfFirst { existing ->
                    existing.day == course.day &&
                        existing.name == course.name &&
                        withinSameBlock(existing, course)
                }
            if (existingIndex < 0) {
                merged += course
            } else {
                val existing = merged[existingIndex]
                val start = minOf(existing.start, course.start)
                val end = maxOf(existing.end, course.end)
                merged[existingIndex] =
                    existing.copy(
                        start = start,
                        end = end,
                        periodLabel = mergePeriod(existing.periodLabel, course.periodLabel),
                    )
            }
        }
        return merged
            .distinctBy { "${it.day}|${it.start}|${it.end}|${it.name}" }
            .sortedWith(compareBy<Course> { it.day }.thenBy { it.start })
    }

    private fun withinSameBlock(
        a: Course,
        b: Course,
    ): Boolean {
        val aStart = ScheduleLogic.parseTime(a.start) ?: return false
        val aEnd = ScheduleLogic.parseTime(a.end) ?: return false
        val bStart = ScheduleLogic.parseTime(b.start) ?: return false
        val bEnd = ScheduleLogic.parseTime(b.end) ?: return false
        val overlap = aStart <= bEnd && bStart <= aEnd
        val adjacent = aStart <= bEnd + 15 && bStart <= aEnd + 15
        return overlap || adjacent
    }

    private fun mergePeriod(
        a: String,
        b: String,
    ): String {
        if (a.isBlank()) return b
        if (b.isBlank()) return a
        return if (a == b) a else "$a / $b"
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("""\[[^\]]*]"""), "")
            .replace("（", "").replace("）", "")
            .replace("(", "").replace(")", "")
            .replace("【", "").replace("】", "")
            .replace(" ", "")
            .replace("\t", "")
            .replace("...", "")
            .replace("…", "")
            .replace(Regex("""[·,，.。:：;；\-—_/\\]"""), "")
            .replace("@", "")
            .trim()
    }

    private fun editDistance(
        a: String,
        b: String,
    ): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] =
                    minOf(
                        current[j - 1] + 1,
                        previous[j] + 1,
                        previous[j - 1] + cost,
                    )
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[b.length]
    }
}
