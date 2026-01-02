package com.example.dontforget.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_item")
data class CheckItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    val item_id: Long = 0L,

    //제목
    @ColumnInfo(name = "title")
    val title: String,

    //비고
    @ColumnInfo(name = "note")
    val note: String = "",

    //신뢰도
    @ColumnInfo(name = "confidence")
    val confidence: Int = 3, // 0~5

    //현재상태
    @ColumnInfo(name = "status")
    val status: String = "ACTIVE", // ACTIVE / MASTERED / ARCHIVED

    //생성시간
    @ColumnInfo(name = "created_at")
    val created_at: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "mistake_count")
    val mistake_count: Int = 0,

    @ColumnInfo(name = "revert_count")
    val revert_count: Int = 0

)