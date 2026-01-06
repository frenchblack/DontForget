package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.RunConditionEntity

// ✅ 컨디션 통계 row (날짜/상태/메모)
data class ConditionStatRow(
    val date: String,
    val value_code: String,
    val memo: String,
    val start_time: Long
)

@Dao
interface RunConditionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all(items: List<RunConditionEntity>)

    @Query("""
        DELETE FROM run_condition
         WHERE session_id = :session_id
           AND phase = :phase
    """)
    suspend fun delete_by_session_phase(
        session_id: Long,
        phase: ConditionPhase
    )

    @Query("""
        SELECT *
          FROM run_condition
         WHERE session_id = :session_id
           AND phase = :phase
         ORDER BY condition_id ASC
    """)
    suspend fun get_by_session_phase(
        session_id: Long,
        phase: ConditionPhase
    ): List<RunConditionEntity>

    // =========================
    // ✅ 통계용 (기간 + 항목 + phase)
    // =========================
    @Query("""
        SELECT
            date(S.start_time / 1000, 'unixepoch', 'localtime') AS date
          , C.value_code AS value_code
          , C.value AS memo
          , S.start_time AS start_time
        FROM run_condition C
        JOIN run_session S
          ON S.session_id = C.session_id
        WHERE S.start_time >= :from_ms
          AND S.start_time < :to_ms
          AND S.status = 'COMPLETED'
          AND C.condition_def_id = :condition_def_id
          AND C.phase = :phase
        ORDER BY S.start_time DESC, C.condition_id DESC
    """)
    suspend fun get_condition_stats(
        from_ms: Long,
        to_ms: Long,
        condition_def_id: Long,
        phase: ConditionPhase
    ): List<ConditionStatRow>
}
