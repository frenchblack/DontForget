package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.CheckItemDao
import com.example.dontforget.data.entity.CheckItemEntity

class CheckItemRepo(private val dao: CheckItemDao) {

    fun observe_active() = dao.observe_active()
    fun observe_mastered() = dao.observe_mastered()

    suspend fun add_item(title: String, note: String, confidence: Int) {
        dao.insert(CheckItemEntity(
            title = title,
            note = note,
            confidence = confidence.coerceIn(0, 5)
        ))
    }
    suspend fun add_item(title: String) {
        dao.insert(CheckItemEntity(title = title))
    }

    suspend fun to_mastered(item_id: Long) {
        dao.update_status(item_id, "MASTERED")
    }

    suspend fun to_active(item_id: Long) {
        dao.update_status(item_id, "ACTIVE")
    }

    suspend fun update_item(item_id: Long, title: String, note: String, confidence: Int) {
        dao.update_item(item_id, title, note, confidence)
    }

    suspend fun delete_item(item_id: Long) {
        dao.delete_item(item_id)
    }

    suspend fun add_mistake(item_id: Long) {
        dao.inc_mistake(item_id)
    }

    suspend fun revert(item_id: Long) {
        dao.revert_to_active(item_id)
    }
}