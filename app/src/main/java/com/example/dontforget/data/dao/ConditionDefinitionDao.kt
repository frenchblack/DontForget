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

    @Query("""
    SELECT *
    FROM condition_definition
    WHERE is_active = 1
    ORDER BY sort_order ASC, condition_def_id ASC
""")
    suspend fun get_all_active(): List<ConditionDefinitionEntity>

    @Query("""
    SELECT *
      FROM condition_definition
     ORDER BY is_active DESC, sort_order ASC, condition_def_id ASC
""")
    fun observe_all_ordered(): Flow<List<ConditionDefinitionEntity>>

    @Insert
    suspend fun insert_one(entity: ConditionDefinitionEntity): Long

    @Query("""
    UPDATE condition_definition
       SET name = :name
         , is_active = :is_active
         , sort_order = :sort_order
     WHERE condition_def_id = :id
""")
    suspend fun update_basic(id: Long, name: String, is_active: Int, sort_order: Int)

    @Query("""
    UPDATE condition_definition
       SET is_active = 0
     WHERE condition_def_id = :id
""")
    suspend fun deactivate(id: Long)
}