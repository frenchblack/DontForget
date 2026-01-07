package com.example.dontforget.data.analysis

// ✅ 분석 기간 (히스토리 선택창에서 사용)
enum class AnalysisRange(val label: String, val days: Int?) {
    D7("7일", 7),
    D30("30일", 30),
    D90("90일", 90),
    D365("1년", 365),
    ALL("전체", null)
}

// ✅ 리포트 Top3 항목(아이템/컨디션 등 공용)
data class FocusRow(
    val title: String,
    val rate: Float,     // 0~1
    val success_sum: Int,
    val fail_sum: Int,
    val cancel_sum: Int
)

// ✅ 최종 분석 결과(히스토리 메인에 그대로 렌더)
data class AnalysisReport(
    val range: AnalysisRange,
    val one_liner: String,

    // 1) 전체 요약
    val total_sessions: Int,
    val total_minutes: Long,

    // 2) 연속/패턴(간단)
    val active_days: Int,
    val avg_minutes_per_session: Long,

    // 3) 아이템 Top3
    val focus_top3: List<FocusRow>,

    // 4) 실패 Top3(위험)
    val risk_top3: List<FocusRow>,

    // 5) 컨디션/요약 영향(간단 문장)
    val condition_hint: String,
    val summary_hint: String,

    // 6) 추천 액션(최대 8개)
    val actions: List<String>
)

object AnalysisCalc {
    fun pct(rate: Float): String {
        val v = (rate * 1000f).toInt() / 10f
        return "${v}%"
    }
}
