package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.ConditionDefinitionDao

class ConditionDefinitionRepo(
    private val dao: ConditionDefinitionDao
) {
    fun observe_active() = dao.observe_active()
}
