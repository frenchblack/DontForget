package com.example.dontforget.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dontforget.data.entity.*

@Dao
interface DataPortDao {

    // ==============
    // EXPORT (SELECT)
    // ==============
    @Query("SELECT * FROM check_item ORDER BY item_id ASC")
    suspend fun get_all_check_item(): List<CheckItemEntity>

    @Query("SELECT * FROM condition_definition ORDER BY condition_def_id ASC")
    suspend fun get_all_condition_definition(): List<ConditionDefinitionEntity>

    @Query("SELECT * FROM result_definition ORDER BY result_def_id ASC")
    suspend fun get_all_result_definition(): List<ResultDefinitionEntity>

    @Query("SELECT * FROM run_session ORDER BY session_id ASC")
    suspend fun get_all_run_session(): List<RunSessionEntity>

    @Query("SELECT * FROM run_item ORDER BY run_item_id ASC")
    suspend fun get_all_run_item(): List<RunItemEntity>

    @Query("SELECT * FROM run_item_progress ORDER BY session_id ASC, item_id ASC")
    suspend fun get_all_run_item_progress(): List<RunItemProgressEntity>

    @Query("SELECT * FROM run_condition ORDER BY condition_id ASC")
    suspend fun get_all_run_condition(): List<RunConditionEntity>

    @Query("SELECT * FROM run_summary ORDER BY summary_id ASC")
    suspend fun get_all_run_summary(): List<RunSummaryEntity>

    // ==============
    // IMPORT (DELETE)
    // ==============
    // FK 때문에 "자식 → 부모" 순서로 삭제
    @Query("DELETE FROM run_summary")
    suspend fun delete_all_run_summary()

    @Query("DELETE FROM run_condition")
    suspend fun delete_all_run_condition()

    @Query("DELETE FROM run_item_progress")
    suspend fun delete_all_run_item_progress()

    @Query("DELETE FROM run_item")
    suspend fun delete_all_run_item()

    @Query("DELETE FROM run_session")
    suspend fun delete_all_run_session()

    @Query("DELETE FROM result_definition")
    suspend fun delete_all_result_definition()

    @Query("DELETE FROM condition_definition")
    suspend fun delete_all_condition_definition()

    @Query("DELETE FROM check_item")
    suspend fun delete_all_check_item()

    // ==============
    // IMPORT (INSERT)
    // ==============
    // 덮어쓰기니까 ABORT로 두면 “중복/깨짐” 바로 잡힘
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_check_item(items: List<CheckItemEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_condition_definition(items: List<ConditionDefinitionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_result_definition(items: List<ResultDefinitionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_run_session(items: List<RunSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_run_item(items: List<RunItemEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_run_item_progress(items: List<RunItemProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_run_condition(items: List<RunConditionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert_all_run_summary(items: List<RunSummaryEntity>)
}
