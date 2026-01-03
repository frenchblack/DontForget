package com.example.dontforget.data.repo

import com.example.dontforget.data.dao.CheckItemDao
import com.example.dontforget.data.dao.RunConditionDao
import com.example.dontforget.data.dao.RunDao
import com.example.dontforget.data.dao.RunItemProgressDao
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.RunConditionEntity
import com.example.dontforget.data.entity.RunItemEntity
import com.example.dontforget.data.entity.RunItemStatus
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.data.entity.RunStatus

enum class RunResultPick { SUCCESS, FAIL, CANCEL }

class RunRepo(
    private val dao: RunDao,
    private val condition_dao: RunConditionDao,
    private val check_item_dao: CheckItemDao,
    private val progress_dao: RunItemProgressDao
) {
    suspend fun get_in_progress(): RunSessionEntity? {
        return dao.get_latest_by_status(RunStatus.IN_PROGRESS)
    }

    // ✅ 세션 시작 + run_item 미리 생성
    suspend fun start_new_session(now: Long = System.currentTimeMillis()): Long {
        val session_id = dao.insert_session(
            RunSessionEntity(
                start_time = now,
                end_time = null,
                status = RunStatus.IN_PROGRESS,
                created_at = now
            )
        )

        val active_items = check_item_dao.get_active_list()

        val run_items = active_items.map { item ->
            RunItemEntity(
                session_id = session_id,
                item_id = item.item_id,
                title = item.title,
                note = item.note,
                success_count = 0,
                fail_count = 0,
                cancel_count = 0,
                status = RunItemStatus.PROCESS,
                created_at = now
            )
        }

        // RunDao에 이미 insert_run_items(List<RunItemEntity>) 있는 상태니까 그대로 사용
        if (run_items.isNotEmpty()) {
            dao.insert_run_items(run_items)
        }

        return session_id
    }

    suspend fun finish_session(session_id: Long) {
        dao.finish_session(
            session_id = session_id,
            end_time = System.currentTimeMillis(),
            status = RunStatus.COMPLETED
        )
    }

    // ✅ 런 중 결과 선택(0/1로만 저장)
    suspend fun pick_result(session_id: Long, item_id: Long, pick: RunResultPick) {
        val (s, f, c) = when (pick) {
            RunResultPick.SUCCESS -> Triple(1, 0, 0)
            RunResultPick.FAIL -> Triple(0, 1, 0)
            RunResultPick.CANCEL -> Triple(0, 0, 1)
        }

        dao.update_run_item_result(
            session_id = session_id,
            item_id = item_id,
            success = s,
            fail = f,
            cancel = c,
            status = RunItemStatus.COMPLETE
        )
    }

    // ✅ 선택 해제(다시 미선택 상태)
    suspend fun clear_result(session_id: Long, item_id: Long) {
        dao.clear_run_item_result(
            session_id = session_id,
            item_id = item_id,
            status = RunItemStatus.PROCESS
        )
    }

    // =========================
    // 컨디션 로직(니 기존 그대로)
    // =========================

    suspend fun save_conditions_start(session_id: Long, items: List<RunConditionEntity>) {
        condition_dao.delete_by_session_phase(session_id = session_id, phase = ConditionPhase.START)
        if (items.isNotEmpty()) condition_dao.insert_all(items)
    }

    suspend fun get_conditions_start(session_id: Long): List<RunConditionEntity> {
        return condition_dao.get_by_session_phase(session_id = session_id, phase = ConditionPhase.START)
    }

    suspend fun save_conditions(session_id: Long, phase: ConditionPhase, items: List<RunConditionEntity>) {
        condition_dao.delete_by_session_phase(session_id = session_id, phase = phase)
        if (items.isNotEmpty()) condition_dao.insert_all(items)
    }

    suspend fun get_conditions(session_id: Long, phase: ConditionPhase): List<RunConditionEntity> {
        return condition_dao.get_by_session_phase(session_id = session_id, phase = phase)
    }

    suspend fun apply_success_from_progress(session_id: Long) {
        val completed_ids = progress_dao.get_completed_ids(session_id) // 아래 3번 DAO 참고

        if (completed_ids.isEmpty()) {
            // 전부 미선택 상태로
            dao.clear_all(session_id)
            return
        }

        // 완료된 애들은 성공=1
        dao.mark_success_for_ids(session_id, completed_ids)

        // 나머지는 0/0/0 유지
        dao.clear_for_others(session_id, completed_ids)
    }

    suspend fun mark_cancel(session_id: Long, item_id: Long) {
        dao.mark_cancel(session_id, item_id)
    }

    suspend fun mark_fail(session_id: Long, item_id: Long) {
        dao.mark_fail(session_id, item_id)
    }

    suspend fun clear_one(session_id: Long, item_id: Long) {
        dao.clear_one(session_id, item_id)
    }
}
