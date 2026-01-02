package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dontforget.data.entity.CheckItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckItemDao {

    @Query("""
        SELECT * 
          FROM check_item 
         WHERE status = 'ACTIVE'
         ORDER BY item_id DESC
    """)
    fun observe_active(): Flow<List<CheckItemEntity>>

    @Query("""
        SELECT * 
          FROM check_item 
         WHERE status = 'MASTERED'
         ORDER BY item_id DESC
    """)
    fun observe_mastered(): Flow<List<CheckItemEntity>>

    @Insert
    suspend fun insert(entity: CheckItemEntity): Long

    @Query("UPDATE check_item SET status = :status WHERE item_id = :item_id")
    suspend fun update_status(item_id: Long, status: String)

    @Query("""
    UPDATE check_item
       SET title = :title
         , note = :note
         , confidence = :confidence
     WHERE item_id = :item_id
""")
    suspend fun update_item(
        item_id: Long,
        title: String,
        note: String,
        confidence: Int
    )

    @Query("DELETE FROM check_item WHERE item_id = :item_id")
    suspend fun delete_item(item_id: Long)

    @Query("""
    UPDATE check_item
       SET mistake_count = mistake_count + 1
     WHERE item_id = :item_id
""")
    suspend fun inc_mistake(item_id: Long)

    @Query("""
    UPDATE check_item
       SET revert_count = revert_count + 1
         , status = 'ACTIVE'
     WHERE item_id = :item_id
""")
    suspend fun revert_to_active(item_id: Long)
}