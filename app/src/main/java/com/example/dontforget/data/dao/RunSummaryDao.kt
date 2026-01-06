package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.RunSummaryEntity

// ✅ 요약 통계 row
data class SummaryStatRow(
    val date: String,
    val value_code: String,
    val memo: String,
    val start_time: Long
)

@Dao
interface RunSummaryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all(items: List<RunSummaryEntity>)

    @Query("""
        DELETE FROM run_summary
         WHERE session_id = :session_id
    """)
    suspend fun delete_by_session(session_id: Long)

    @Query("""
        SELECT *
          FROM run_summary
         WHERE session_id = :session_id
         ORDER BY summary_id ASC
    """)
    suspend fun get_by_session(session_id: Long): List<RunSummaryEntity>

    // =========================
    // ✅ 통계용 (기간 + 요약항목)
    // =========================
    @Query("""
        SELECT
            date(S.start_time / 1000, 'unixepoch', 'localtime') AS date
          , R.value_code AS value_code
          , R.value AS memo
          , S.start_time AS start_time
        FROM run_summary R
        JOIN run_session S
          ON S.session_id = R.session_id
        WHERE S.start_time >= :from_ms
          AND S.start_time < :to_ms
          AND S.status = 'COMPLETED'
          AND R.result_def_id = :result_def_id
        ORDER BY S.start_time DESC, R.summary_id DESC
    """)
    suspend fun get_summary_stats(
        from_ms: Long,
        to_ms: Long,
        result_def_id: Long
    ): List<SummaryStatRow>
}
