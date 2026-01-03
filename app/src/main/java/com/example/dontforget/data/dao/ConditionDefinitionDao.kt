package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionDefinitionDao {

    @Query("""
        SELECT *
        FROM condition_definition A
        WHERE A.is_active = 1
        ORDER BY A.sort_order ASC
    """)
    fun observe_active(): Flow<List<ConditionDefinitionEntity>>

    @Query("SELECT COUNT(*) FROM condition_definition")
    suspend fun count_all(): Int

    @Insert
    suspend fun insert_all(items: List<ConditionDefinitionEntity>)

    @Query("""
        SELECT *
          FROM condition_definition A
         WHERE A.is_active = 1
         ORDER BY A.sort_order ASC, A.condition_def_id ASC
    """)
    suspend fun get_all_active_ordered(): List<ConditionDefinitionEntity>
}