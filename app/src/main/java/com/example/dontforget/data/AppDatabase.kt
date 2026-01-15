package com.example.dontforget.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dontforget.data.dao.CheckItemDao
import com.example.dontforget.data.dao.RunDao
import com.example.dontforget.data.dao.ConditionDefinitionDao
import com.example.dontforget.data.dao.DataPortDao
import com.example.dontforget.data.dao.ResultDefinitionDao
import com.example.dontforget.data.dao.RunConditionDao
import com.example.dontforget.data.dao.RunItemProgressDao
import com.example.dontforget.data.dao.RunSummaryDao
import com.example.dontforget.data.db.Converters
import com.example.dontforget.data.entity.CheckItemEntity
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.entity.RunItemEntity
import com.example.dontforget.data.entity.RunItemProgressEntity
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunSummaryEntity

@Database(
    entities = [
        CheckItemEntity::class,
        RunItemProgressEntity::class,
        RunSessionEntity::class,
        RunItemEntity::class,
        RunConditionEntity::class,
        RunSummaryEntity::class,
        ConditionDefinitionEntity::class,
        ResultDefinitionEntity::class
    ],
    version = AppSchema.DB_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun check_item_dao(): CheckItemDao
    abstract fun run_dao(): RunDao
    abstract fun condision_dao(): ConditionDefinitionDao
    abstract fun run_condition_dao(): RunConditionDao
    abstract fun run_item_progress_dao(): RunItemProgressDao

    // ✅ 오늘정리(자기평가)
    abstract fun result_definition_dao(): ResultDefinitionDao
    abstract fun run_summary_dao(): RunSummaryDao
    abstract fun analysisDao(): com.example.dontforget.data.dao.AnalysisDao
    abstract fun data_port_dao(): DataPortDao

    // abstract fun run_dao(): RunDao
}
