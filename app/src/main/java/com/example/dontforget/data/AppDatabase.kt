package com.example.dontforget.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dontforget.data.dao.CheckItemDao
import com.example.dontforget.data.entity.CheckItemEntity

@Database(
    entities = [CheckItemEntity::class],
    version = 2, // ← 1 → 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun check_item_dao(): CheckItemDao
}