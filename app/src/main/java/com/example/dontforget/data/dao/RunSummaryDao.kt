package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.RunSummaryEntity

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
}
