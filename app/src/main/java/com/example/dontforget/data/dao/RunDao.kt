package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.RunItemEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunStatus

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
}
