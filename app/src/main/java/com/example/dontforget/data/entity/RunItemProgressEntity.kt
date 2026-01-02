package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "run_item_progress",
    primaryKeys = ["session_id", "item_id"]
)
data class RunItemProgressEntity(
    @ColumnInfo(name = "session_id")
    val session_id: Long,

    @ColumnInfo(name = "item_id")
    val item_id: Long,

    // 0 / 1
    @ColumnInfo(name = "is_completed")
    val is_completed: Int = 0,

    @ColumnInfo(name = "updated_at")
    val updated_at: Long = System.currentTimeMillis()
)
