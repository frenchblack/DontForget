package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.dontforget.data.entity.ConditionPhase

// ✅ 아이템 집계 row
data class ItemAggRow(
    val item_id: Long,
    val title: String,
    val success_sum: Int,
    val fail_sum: Int,
    val cancel_sum: Int
)

// ✅ 기간 내 전체 세션/시간 요약
data class SessionAggRow(
    val session_count: Int,
    val total_ms: Long
)

// ✅ 날짜별 “연습한 날” 카운트용
data class ActiveDayRow(
    val date: String
)

@Dao
interface AnalysisDao {

    // 1) 기간 내 세션 수 / 총 연습시간
    @Query("""
        SELECT
            COUNT(*) AS session_count,
            SUM(CASE WHEN end_time IS NOT NULL THEN (end_time - start_time) ELSE 0 END) AS total_ms
        FROM run_session
        WHERE start_time >= :from_ms
          AND start_time < :to_ms
          AND status = 'COMPLETED'
    """)
    suspend fun get_session_agg(from_ms: Long, to_ms: Long): SessionAggRow?

    // 2) 기간 내 "연습한 날짜" 리스트 (active_days 계산)
    @Query("""
        SELECT DISTINCT date(start_time / 1000, 'unixepoch', 'localtime') AS date
        FROM run_session
        WHERE start_time >= :from_ms
          AND start_time < :to_ms
          AND status = 'COMPLETED'
        ORDER BY date ASC
    """)
    suspend fun get_active_days(from_ms: Long, to_ms: Long): List<ActiveDayRow>

    // 3) 아이템별 성공/실패/취소 합계
    @Query("""
        SELECT
            A.item_id AS item_id,
            MAX(A.title) AS title,
            SUM(A.success_count) AS success_sum,
            SUM(A.fail_count) AS fail_sum,
            SUM(A.cancel_count) AS cancel_sum
        FROM run_item A
        JOIN run_session S
          ON S.session_id = A.session_id
        WHERE S.start_time >= :from_ms
          AND S.start_time < :to_ms
          AND S.status = 'COMPLETED'
        GROUP BY A.item_id
    """)
    suspend fun get_item_aggs(from_ms: Long, to_ms: Long): List<ItemAggRow>

    // 4) 컨디션: START/MID/END 중 특정 phase에서 "나쁨/좋음" 분포(단순 힌트용)
    @Query("""
        SELECT
            A.value_code AS value_code,
            COUNT(*) AS cnt
        FROM run_condition A
        JOIN run_session S
          ON S.session_id = A.session_id
        WHERE S.start_time >= :from_ms
          AND S.start_time < :to_ms
          AND S.status = 'COMPLETED'
          AND A.phase = :phase
        GROUP BY A.value_code
        ORDER BY cnt DESC
    """)
    suspend fun get_condition_distribution(
        from_ms: Long,
        to_ms: Long,
        phase: ConditionPhase = ConditionPhase.START
    ): List<ValueCountRow>

    // 5) 요약(run_summary) value_code 분포(단순 힌트용)
    @Query("""
        SELECT
            A.value_code AS value_code,
            COUNT(*) AS cnt
        FROM run_summary A
        JOIN run_session S
          ON S.session_id = A.session_id
        WHERE S.start_time >= :from_ms
          AND S.start_time < :to_ms
          AND S.status = 'COMPLETED'
        GROUP BY A.value_code
        ORDER BY cnt DESC
    """)
    suspend fun get_summary_distribution(from_ms: Long, to_ms: Long): List<ValueCountRow>
}

// ✅ 공용 분포 row
data class ValueCountRow(
    val value_code: String,
    val cnt: Int
)
