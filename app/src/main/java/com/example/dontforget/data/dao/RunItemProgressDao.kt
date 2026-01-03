package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.RunItemProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunItemProgressDao {

    @Query("""
        SELECT item_id
          FROM run_item_progress
         WHERE session_id = :session_id
           AND is_completed = 1
    """)
    fun observe_completed_ids(session_id: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RunItemProgressEntity)

    suspend fun mark_completed(session_id: Long, item_id: Long) {
        upsert(
            RunItemProgressEntity(
                session_id = session_id,
                item_id = item_id,
                is_completed = 1,
                updated_at = System.currentTimeMillis()
            )
        )
    }

    suspend fun unmark_completed(session_id: Long, item_id: Long) {
        upsert(
            RunItemProgressEntity(
                session_id = session_id,
                item_id = item_id,
                is_completed = 0,
                updated_at = System.currentTimeMillis()
            )
        )
    }

    @Query("DELETE FROM run_item_progress WHERE session_id = :session_id")
    suspend fun clear_session(session_id: Long)

    @Query("""
    SELECT item_id
      FROM run_item_progress
     WHERE session_id = :session_id
       AND is_completed = 1
""")
    suspend fun get_completed_ids(session_id: Long): List<Long>
}
