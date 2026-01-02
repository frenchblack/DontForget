package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "result_definition")
data class ResultDefinitionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "result_def_id")
    val result_def_id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "input_type")
    val input_type: InputType = InputType.SCORE,

    @ColumnInfo(name = "is_active")
    val is_active: Int = 1,

    @ColumnInfo(name = "sort_order")
    val sort_order: Int = 0
)
