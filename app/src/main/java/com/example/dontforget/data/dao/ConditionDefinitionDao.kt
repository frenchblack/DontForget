package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dontforget.data.entity.ConditionDefinitionEntity
@Dao
interface ConditionDefinitionDao {

    @Query("SELECT COUNT(*) FROM condition_definition")
    suspend fun count_all(): Int

    @Insert
    suspend fun insert_all(items: List<ConditionDefinitionEntity>)
}