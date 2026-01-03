package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.RunItemEntity
import com.example.dontforget.data.entity.RunItemStatus
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunStatus

// ✅ 날짜별 요약 row (Room 쿼리 결과 매핑용)
data class RunDateAggRow(
    val date: String,
    val session_count: Int,
    val total_ms: Long
)

@Dao
interface RunDao {

    @Query("""
        SELECT *
        FROM run_session
        WHERE status = :status
        ORDER BY start_time DESC
        LIMIT 1
    """)
    suspend fun get_latest_by_status(status: RunStatus = RunStatus.IN_PROGRESS): RunSessionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_session(entity: RunSessionEntity): Long

    @Query("""
        UPDATE run_session
        SET end_time = :end_time
          , status = :status
        WHERE session_id = :session_id
    """)
    suspend fun finish_session(
        session_id: Long,
        end_time: Long,
        status: RunStatus = RunStatus.COMPLETED
    )

    @Query("SELECT COUNT(*) FROM run_session")
    suspend fun count_sessions(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_run_items(items: List<RunItemEntity>)

    @Query("SELECT COUNT(*) FROM run_item")
    suspend fun count_run_items(): Int

    // =========================
    // ✅ 히스토리(날짜 상세)용
    // =========================

    @Query("""
        SELECT *
        FROM run_session
        WHERE session_id = :session_id
        LIMIT 1
    """)
    suspend fun get_session_by_id(session_id: Long): RunSessionEntity?

    // 최근 기간 날짜별: 세션수/총시간 (COMPLETED만)
    @Query("""
        SELECT
            date(start_time / 1000, 'unixepoch', 'localtime') AS date
          , COUNT(*) AS session_count
          , SUM(CASE 
                WHEN end_time IS NOT NULL THEN (end_time - start_time)
                ELSE 0
              END) AS total_ms
        FROM run_session
        WHERE start_time >= :from_ms
          AND start_time < :to_ms
          AND status = 'COMPLETED'
        GROUP BY date
        ORDER BY date DESC
    """)
    suspend fun get_date_aggs(from_ms: Long, to_ms: Long): List<RunDateAggRow>

    // 특정 날짜(구간) 세션 목록
    @Query("""
        SELECT *
        FROM run_session
        WHERE start_time >= :day_start_ms
          AND start_time < :day_end_ms
          AND status = 'COMPLETED'
        ORDER BY start_time DESC
    """)
    suspend fun get_sessions_by_range(day_start_ms: Long, day_end_ms: Long): List<RunSessionEntity>

    // 세션의 run_item 목록
    @Query("""
        SELECT *
        FROM run_item
        WHERE session_id = :session_id
        ORDER BY run_item_id ASC
    """)
    suspend fun get_run_items_by_session(session_id: Long): List<RunItemEntity>

    @Query("SELECT COUNT(*) FROM run_item WHERE session_id = :session_id")
    suspend fun count_run_items_by_session(session_id: Long): Int

    @Query("""
    SELECT *
    FROM run_item
    ORDER BY run_item_id DESC
    LIMIT 20
""")
    suspend fun get_recent_run_items(): List<RunItemEntity>

    @Query("""
    UPDATE run_item
       SET success_count = :success
         , fail_count = :fail
         , cancel_count = :cancel
         , status = :status
     WHERE session_id = :session_id
       AND item_id = :item_id
""")
    suspend fun update_run_item_result(
        session_id: Long,
        item_id: Long,
        success: Int,
        fail: Int,
        cancel: Int,
        status: RunItemStatus
    )

    @Query("""
    UPDATE run_item
       SET success_count = 0
         , fail_count = 0
         , cancel_count = 0
         , status = :status
     WHERE session_id = :session_id
       AND item_id = :item_id
""")
    suspend fun clear_run_item_result(
        session_id: Long,
        item_id: Long,
        status: RunItemStatus = RunItemStatus.PROCESS
    )

    @Query("""
    UPDATE run_item
       SET success_count = 1
         , fail_count = 0
         , cancel_count = 0
         , status = 'COMPLETE'
     WHERE session_id = :session_id
       AND item_id IN (:item_ids)
""")
    suspend fun mark_success_for_ids(session_id: Long, item_ids: List<Long>)

    @Query("""
    UPDATE run_item
       SET success_count = 0
         , fail_count = 0
         , cancel_count = 0
         , status = 'PROCESS'
     WHERE session_id = :session_id
       AND item_id NOT IN (:item_ids)
""")
    suspend fun clear_for_others(session_id: Long, item_ids: List<Long>)

    @Query("""
    UPDATE run_item
       SET success_count = 0
         , fail_count = 0
         , cancel_count = 0
         , status = 'PROCESS'
     WHERE session_id = :session_id
""")
    suspend fun clear_all(session_id: Long)

    @Query("""
    UPDATE run_item
       SET success_count = 0
         , fail_count = 0
         , cancel_count = 1
         , status = 'COMPLETE'
     WHERE session_id = :session_id
       AND item_id = :item_id
""")
    suspend fun mark_cancel(session_id: Long, item_id: Long)

    @Query("""
    UPDATE run_item
       SET success_count = 0
         , fail_count = 1
         , cancel_count = 0
         , status = 'COMPLETE'
     WHERE session_id = :session_id
       AND item_id = :item_id
""")
    suspend fun mark_fail(session_id: Long, item_id: Long)

    @Query("""
    UPDATE run_item
       SET success_count = 0
         , fail_count = 0
         , cancel_count = 0
         , status = 'PROCESS'
     WHERE session_id = :session_id
       AND item_id = :item_id
""")
    suspend fun clear_one(session_id: Long, item_id: Long)
}
