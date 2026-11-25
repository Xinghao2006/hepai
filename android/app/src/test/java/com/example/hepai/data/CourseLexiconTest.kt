package com.example.hepai.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CourseLexiconTest {
    @Test
    fun fixesTypoAndClassroomSuffix() {
        assertEquals("计算机网络", CourseLexicon.canonicalize("计算机网", "a"))
        assertEquals("高等数学", CourseLexicon.canonicalize("高等数学研201", "b"))
        assertEquals("大学英语", CourseLexicon.canonicalize("大学英语", "b"))
    }

    @Test
    fun fixesMemberBAtSuffix() {
        assertEquals("有机化学", CourseLexicon.canonicalize("有机化学I", "b"))
        assertEquals("无机化学", CourseLexicon.canonicalize("无机及分析化学", "b"))
    }

    @Test
    fun mergesAdjacentPiecesOfOneCourse() {
        val courses =
            listOf(
                Course(0, "10:05", "11:00", "高等数", tone = "b"),
                Course(0, "11:00", "11:40", "高等数学", tone = "b"),
            )
        val result = CourseLexicon.refine(courses, "b")
        assertEquals(1, result.size)
        assertEquals("10:05", result[0].start)
        assertEquals("11:40", result[0].end)
    }

    @Test
    fun keepsDifferentSessionsSeparate() {
        val courses =
            listOf(
                Course(0, "08:00", "09:35", "高等数学", tone = "b"),
                Course(0, "18:00", "19:35", "高等数学", tone = "b"),
            )
        val result = CourseLexicon.refine(courses, "b")
        assertEquals(2, result.size)
    }
}
