package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dontforget.data.entity.ResultDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDefinitionDao {

    @Query("""
        SELECT *
          FROM result_definition
         WHERE is_active = 1
         ORDER BY sort_order ASC, result_def_id ASC
    """)
    fun observe_active(): Flow<List<ResultDefinitionEntity>>

    @Query("SELECT COUNT(*) FROM result_definition")
    suspend fun count_all(): Int

    @Insert
    suspend fun insert_all(items: List<ResultDefinitionEntity>)

    @Query("""
        SELECT *
          FROM result_definition
         WHERE is_active = 1
         ORDER BY sort_order ASC, result_def_id ASC
    """)
    suspend fun get_all_active_ordered(): List<ResultDefinitionEntity>
}
