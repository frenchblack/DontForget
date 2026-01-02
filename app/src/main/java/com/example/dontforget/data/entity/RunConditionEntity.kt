package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "run_condition",
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConditionDefinitionEntity::class,
            parentColumns = ["condition_def_id"],
            childColumns = ["condition_def_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["condition_def_id"])
    ]
)
data class RunConditionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "condition_id")
    val condition_id: Long = 0L,

    @ColumnInfo(name = "session_id")
    val session_id: Long,

    @ColumnInfo(name = "condition_def_id")
    val condition_def_id: Long,

    // ✅ 통계용 선택 코드 (VERY_BAD / BAD / NORMAL / GOOD / VERY_GOOD)
    @ColumnInfo(name = "value_code")
    val value_code: String = "",

    // ✅ 추가 설명 / 자유 메모 (기존 value 역할)
    @ColumnInfo(name = "value")
    val value: String = "",

    @ColumnInfo(name = "phase")
    val phase: ConditionPhase,

    @ColumnInfo(name = "created_at")
    val created_at: Long = System.currentTimeMillis()
)
