package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.dontforget.data.entity.CheckItemEntity

@Entity(
    tableName = "run_item",
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CheckItemEntity::class,
            parentColumns = ["item_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["item_id"])
    ]
)
data class RunItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "run_item_id")
    val run_item_id: Long = 0L,

    @ColumnInfo(name = "session_id")
    val session_id: Long,

    @ColumnInfo(name = "item_id")
    val item_id: Long, // Items 원본 연결(필수)

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "success_count")
    val success_count: Int = 0,

    @ColumnInfo(name = "fail_count")
    val fail_count: Int = 0,

    @ColumnInfo(name = "cancel_count")
    val cancel_count: Int = 0,

    @ColumnInfo(name = "status")
    val status: RunItemStatus = RunItemStatus.PROCESS,

    @ColumnInfo(name = "created_at")
    val created_at: Long = System.currentTimeMillis()
)
