package com.example.hepai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.hepai.data.AppUiState
import com.example.hepai.data.Course
import com.example.hepai.data.HepaiTab
import com.example.hepai.data.Me
import com.example.hepai.data.Member
import com.example.hepai.data.Plan
import com.example.hepai.data.Space
import com.example.hepai.data.Timetable
import com.example.hepai.ui.BottomNav
import com.example.hepai.ui.DayDetailSheet
import com.example.hepai.ui.FreeScreen
import com.example.hepai.ui.HepaiTheme
import com.example.hepai.ui.WeekScreen

/** Debug-only demo screen used to make the public, sanitized preview. */
class DemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screen = intent.getStringExtra("screen") ?: "week"
        setContent {
            HepaiTheme {
                val state = remember { demoState(screen) }
                var openDay by remember { mutableStateOf(if (screen == "day") 2 else null) }
                val selected =
                    when (screen) {
                        "week" -> HepaiTab.WEEK
                        "day" -> HepaiTab.FREE
                        else -> HepaiTab.FREE
                    }
                Scaffold(
                    containerColor = com.example.hepai.ui.Hepai.Paper,
                    bottomBar = { BottomNav(selected = selected, onSelect = {}) },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        when (screen) {
                            "week" -> WeekScreen(state = state, onNext = {})
                            "free" ->
                                FreeScreen(
                                    state = state,
                                    onOpenDay = { openDay = it },
                                    onOpenPlan = { _, _, _ -> },
                                    onDeletePlan = {},
                                )
                            "day" -> {
                                FreeScreen(
                                    state = state,
                                    onOpenDay = { openDay = it },
                                    onOpenPlan = { _, _, _ -> },
                                    onDeletePlan = {},
                                )
                            }
                        }
                    }
                    openDay?.let { day ->
                        DayDetailSheet(
                            state = state,
                            day = day,
                            onDismiss = { openDay = null },
                            onOpenPlan = { _, _, _ -> },
                        )
                    }
                }
            }
        }
    }

    private fun demoState(screen: String): AppUiState {
        val memberA =
            listOf(
                Course(0, "10:05", "11:40", "高等数学", tone = "b"),
                Course(1, "08:00", "09:35", "程序设计", tone = "b"),
                Course(2, "10:05", "11:40", "计算机网络", tone = "b"),
                Course(3, "14:00", "15:40", "数据结构", tone = "b"),
            )
        val memberB =
            listOf(
                Course(0, "10:05", "11:40", "大学英语", tone = "a"),
                Course(1, "14:00", "15:40", "大学物理", tone = "a"),
                Course(2, "16:00", "17:40", "大学英语", tone = "a"),
                Course(3, "10:05", "11:40", "高等数学", tone = "a"),
            )
        return AppUiState(
            booting = false,
            space =
                Space(
                    id = "demo",
                    inviteCode = "DEMO01",
                    version = 1,
                    members =
                        listOf(
                            Member("member_a", "成员 A"),
                            Member("member_b", "成员 B"),
                        ),
                ),
            me = Me("member_a", "成员 A"),
            timetables =
                mapOf(
                    "member_a" to Timetable(memberA),
                    "member_b" to Timetable(memberB),
                ),
            plans =
                listOf(
                    Plan(
                        id = "demo-plan",
                        day = 2,
                        startMin = 12 * 60 + 10,
                        endMin = 13 * 60 + 30,
                        title = "一起吃饭",
                        createdBy = "member_a",
                    ),
                ),
            tab = if (screen == "week") HepaiTab.WEEK else HepaiTab.FREE,
        )
    }
}
