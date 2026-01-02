package com.example.dontforget.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dontforget.data.dao.CheckItemDao
import com.example.dontforget.data.dao.RunDao
import com.example.dontforget.data.dao.ConditionDefinitionDao
import com.example.dontforget.data.db.Converters
import com.example.dontforget.data.entity.CheckItemEntity
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.entity.RunItemEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunSummaryEntity

@Database(
    entities = [
        CheckItemEntity::class,
        RunSessionEntity::class,
        RunItemEntity::class,
        RunConditionEntity::class,
        RunSummaryEntity::class,
        ConditionDefinitionEntity::class,
        ResultDefinitionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun check_item_dao(): CheckItemDao
    abstract fun run_dao(): RunDao
    abstract fun condision_dao(): ConditionDefinitionDao

    // abstract fun run_dao(): RunDao
}
