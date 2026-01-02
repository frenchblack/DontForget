package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "run_summary",
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ResultDefinitionEntity::class,
            parentColumns = ["result_def_id"],
            childColumns = ["result_def_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["result_def_id"])
    ]
)
data class RunSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "summary_id")
    val summary_id: Long = 0L,

    @ColumnInfo(name = "session_id")
    val session_id: Long,

    @ColumnInfo(name = "result_def_id")
    val result_def_id: Long,

    @ColumnInfo(name = "value")
    val value: String,

    @ColumnInfo(name = "created_at")
    val created_at: Long = System.currentTimeMillis()
)
