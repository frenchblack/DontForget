package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.ConditionDefinitionDao
import com.example.dontforget.data.dao.ResultDefinitionDao
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.data.entity.ResultDefinitionEntity
import kotlinx.coroutines.flow.Flow

class ConditionDefManageRepo(
    private val dao: ConditionDefinitionDao
) {
    fun observe_all_ordered(): Flow<List<ConditionDefinitionEntity>> = dao.observe_all_ordered()

    suspend fun create(
        name: String,
        input_type: InputType,
        is_active: Int,
        sort_order: Int
    ) {
        dao.insert_one(
            ConditionDefinitionEntity(
                name = name,
                input_type = input_type,
                is_active = is_active,
                sort_order = sort_order
            )
        )
    }

    suspend fun update_basic(
        id: Long,
        name: String,
        is_active: Int,
        sort_order: Int
    ) {
        dao.update_basic(id = id, name = name, is_active = is_active, sort_order = sort_order)
    }

    suspend fun deactivate(id: Long) {
        dao.deactivate(id)
    }
}

class ResultDefManageRepo(
    private val dao: ResultDefinitionDao
) {
    fun observe_all_ordered(): Flow<List<ResultDefinitionEntity>> = dao.observe_all_ordered()

    suspend fun create(
        name: String,
        input_type: InputType,
        is_active: Int,
        sort_order: Int
    ) {
        dao.insert_one(
            ResultDefinitionEntity(
                name = name,
                input_type = input_type,
                is_active = is_active,
                sort_order = sort_order
            )
        )
    }

    suspend fun update_basic(
        id: Long,
        name: String,
        is_active: Int,
        sort_order: Int
    ) {
        dao.update_basic(id = id, name = name, is_active = is_active, sort_order = sort_order)
    }

    suspend fun deactivate(id: Long) {
        dao.deactivate(id)
    }
}
