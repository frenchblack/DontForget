package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_session")
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    val session_id: Long = 0L,

    @ColumnInfo(name = "start_time")
    val start_time: Long, // epoch millis

    @ColumnInfo(name = "end_time")
    val end_time: Long? = null, // epoch millis, nullable

    @ColumnInfo(name = "status")
    val status: RunStatus = RunStatus.IN_PROGRESS,

    @ColumnInfo(name = "created_at")
    val created_at: Long = System.currentTimeMillis()
)
