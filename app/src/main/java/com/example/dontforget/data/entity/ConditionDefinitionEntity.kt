package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condition_definition")
data class ConditionDefinitionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "condition_def_id")
    val condition_def_id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "input_type")
    val input_type: InputType = InputType.SCORE,

    @ColumnInfo(name = "is_active")
    val is_active: Int = 1, // 1: true, 0: false (sqlite friendly)

    @ColumnInfo(name = "sort_order")
    val sort_order: Int = 0
)
