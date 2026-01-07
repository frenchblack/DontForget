package com.example.dontforget.data.repo

import com.example.dontforget.data.analysis.AnalysisRange
import com.example.dontforget.data.analysis.AnalysisReport
import com.example.dontforget.data.analysis.FocusRow
import com.example.dontforget.data.dao.AnalysisDao
import com.example.dontforget.data.dao.ItemAggRow
import com.example.dontforget.data.entity.ConditionPhase

class AnalysisRepo(
    private val dao: AnalysisDao
) {

    suspend fun build_report(range: AnalysisRange): AnalysisReport {
        val days = range.days ?: 3650
        val (from_ms, to_ms) = range_ms(days)

        val session_agg = dao.get_session_agg(from_ms, to_ms)
        val active_days = dao.get_active_days(from_ms, to_ms).size

        val total_sessions = session_agg?.session_count ?: 0
        val total_ms = session_agg?.total_ms ?: 0L
        val total_minutes = total_ms / 1000L / 60L

        val avg_min_per_session = if (total_sessions > 0) (total_minutes / total_sessions) else 0L

        // 아이템 집계
        val item_aggs = dao.get_item_aggs(from_ms, to_ms)

        val focus_top3 = item_aggs
            .map { it.to_focus_row() }
            .sortedWith(compareByDescending<FocusRow> { it.rate }.thenByDescending { it.success_sum })
            .take(3)

        val risk_top3 = item_aggs
            .filter { it.fail_sum > 0 }
            .map { it.to_focus_row() }
            .sortedWith(compareByDescending<FocusRow> { it.fail_sum }.thenByDescending { it.cancel_sum })
            .take(3)

        // 컨디션/요약 분포 -> 아주 간단한 힌트(룰 기반)
        val cond_dist = dao.get_condition_distribution(from_ms, to_ms, ConditionPhase.START)
        val sum_dist = dao.get_summary_distribution(from_ms, to_ms)

        val condition_hint = build_dist_hint("컨디션(START)", cond_dist)
        val summary_hint = build_dist_hint("요약", sum_dist)

        val one_liner = when {
            total_sessions == 0 -> "최근 ${range.label} 동안 완료된 세션이 없습니다."
            focus_top3.isEmpty() -> "최근 ${range.label} 기준, 연습은 했지만 아이템 데이터가 부족합니다."
            else -> "최근 ${range.label}: ${total_sessions}회 · ${total_minutes}분 · 1등 아이템: ${focus_top3[0].title}"
        }

        // ✅ 추천 액션 (최대 8개)
        val actions = buildList {
            if (total_sessions == 0) {
                add("세션을 최소 1회 완료해서 데이터 기반 분석을 시작하세요.")
            } else {
                add("상위 아이템 1개를 '내일도 반복'로 고정(루틴화)하세요.")
                if (risk_top3.isNotEmpty()) add("실패 많은 아이템(Top1)을 '연습 분해(난이도 낮추기)'로 재설계하세요.")
                if (avg_min_per_session < 10) add("세션 평균 시간이 짧습니다. '10분 고정 세션'을 목표로 해보세요.")
                if (active_days <= (days / 4)) add("연습한 날이 적습니다. '격일 1세션'으로 빈도부터 올리세요.")
                add("실패/취소가 많은 날은 컨디션 메모를 남겨 원인 패턴을 잡아보세요.")
                add("다음 세션에서는 '실패 Top1 아이템'을 먼저 실행해서 컨디션 영향 여부를 확인하세요.")
                add("성공률 상위 아이템은 MASTERED 후보로 점검하세요(안정화 단계).")
            }
        }.take(8)

        return AnalysisReport(
            range = range,
            one_liner = one_liner,
            total_sessions = total_sessions,
            total_minutes = total_minutes,
            active_days = active_days,
            avg_minutes_per_session = avg_min_per_session,
            focus_top3 = focus_top3,
            risk_top3 = risk_top3,
            condition_hint = condition_hint,
            summary_hint = summary_hint,
            actions = actions
        )
    }

    private fun range_ms(days: Int): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val from = now - days.toLong() * 24L * 60L * 60L * 1000L
        val to = now + 1L
        return from to to
    }

    private fun ItemAggRow.to_focus_row(): FocusRow {
        val total = (success_sum + fail_sum + cancel_sum).coerceAtLeast(1)
        val rate = success_sum.toFloat() / total.toFloat()
        return FocusRow(
            title = title,
            rate = rate,
            success_sum = success_sum,
            fail_sum = fail_sum,
            cancel_sum = cancel_sum
        )
    }

    private fun build_dist_hint(title: String, dist: List<com.example.dontforget.data.dao.ValueCountRow>): String {
        if (dist.isEmpty()) return "$title: 데이터 없음"
        val top = dist.first()
        val label = when (top.value_code) {
            "VERY_BAD" -> "매우나쁨"
            "BAD" -> "나쁨"
            "NORMAL" -> "보통"
            "GOOD" -> "좋음"
            "VERY_GOOD" -> "매우좋음"
            "", " " -> "(빈값)"
            else -> top.value_code
        }
        return "$title: 최빈값 = $label (${top.cnt}회)"
    }
}
