package com.example.hepai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val personId: String,
    val name: String,
)

@Serializable
data class Space(
    val id: String = "",
    val inviteCode: String = "",
    val version: Int = 1,
    val createdAt: Long = 0,
    val members: List<Member> = emptyList(),
)

@Serializable
data class Course(
    val day: Int,
    val start: String,
    val end: String,
    val name: String,
    val location: String = "",
    val teacher: String = "",
    val kind: String = "",
    val tone: String = "a",
    val periodLabel: String = "",
)

@Serializable
data class Timetable(
    val courses: List<Course> = emptyList(),
    val imageKey: String? = null,
    val updatedAt: Long = 0,
)

@Serializable
data class Plan(
    val id: String,
    val day: Int,
    val startMin: Int,
    val endMin: Int,
    val title: String,
    val note: String = "",
    val createdBy: String = "",
    val updatedAt: Long = 0,
)

@Serializable
data class Me(
    val personId: String? = null,
    val name: String = "",
)

@Serializable
data class StateResponse(
    val space: Space? = null,
    val me: Me? = null,
    val timetables: Map<String, Timetable> = emptyMap(),
    val plans: List<Plan> = emptyList(),
)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class CreateRequest(
    val name: String,
    val personId: String,
)

@Serializable
data class JoinRequest(
    val name: String,
    val personId: String,
    val code: String,
)

@Serializable
data class RejoinRequest(
    val name: String,
    val personId: String,
    val code: String,
)

@Serializable
data class TimetableRequest(
    val personId: String,
    val courses: List<Course>,
)

@Serializable
data class PlanRequest(
    val id: String? = null,
    val day: Int,
    val startMin: Int,
    val endMin: Int,
    val title: String,
    val note: String = "",
)

@Serializable
data class OcrCourse(
    val day: Int,
    val start: String,
    val end: String,
    val name: String = "",
)

@Serializable
data class OcrCourseResponse(
    val courses: List<OcrCourse> = emptyList(),
)

@Serializable
data class OcrImageRequest(
    val image: String,
    val personId: String,
)

@Serializable
data class ErrorResponse(val error: String = "")

@Serializable
data class StatusResponse(
    val ok: Boolean = false,
    @SerialName("version") val version: Int = 0,
    @SerialName("count") val count: Int = 0,
)

data class Window(
    val day: Int,
    val startMin: Int,
    val endMin: Int,
) {
    val minutes: Int get() = endMin - startMin
}

data class PersonDaySegments(
    val member_a: List<ClosedRange<Int>> = emptyList(),
    val member_b: List<ClosedRange<Int>> = emptyList(),
)
