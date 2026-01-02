package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.RunConditionEntity

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
}
