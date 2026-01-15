package com.example.dontforget.data.repo

import androidx.room.withTransaction
import com.example.dontforget.data.AppDatabase
import com.example.dontforget.data.port.ExportBundle
import com.example.dontforget.data.port.ExportTables
import com.example.dontforget.data.port.PreviewCounts

class DataPortRepo(
    private val db: AppDatabase
) {
    private val dao = db.data_port_dao()

    suspend fun export_all(): ExportBundle {
        val check_items = dao.get_all_check_item()
        val condition_defs = dao.get_all_condition_definition()
        val result_defs = dao.get_all_result_definition()
        val run_sessions = dao.get_all_run_session()
        val run_items = dao.get_all_run_item()
        val run_item_progress = dao.get_all_run_item_progress()
        val run_conditions = dao.get_all_run_condition()
        val run_summaries = dao.get_all_run_summary()

        val counts = linkedMapOf(
            "check_item" to check_items.size,
            "condition_definition" to condition_defs.size,
            "result_definition" to result_defs.size,
            "run_session" to run_sessions.size,
            "run_item" to run_items.size,
            "run_item_progress" to run_item_progress.size,
            "run_condition" to run_conditions.size,
            "run_summary" to run_summaries.size
        )

        return ExportBundle(
            db_schema_version = db.openHelper.readableDatabase.version, // Room version과 같음(7)
            created_at = System.currentTimeMillis(),
            counts = counts,
            tables = ExportTables(
                check_item = check_items,
                condition_definition = condition_defs,
                result_definition = result_defs,
                run_session = run_sessions,
                run_item = run_items,
                run_item_progress = run_item_progress,
                run_condition = run_conditions,
                run_summary = run_summaries
            )
        )
    }

    fun preview_counts(bundle: ExportBundle): PreviewCounts {
        return PreviewCounts(bundle.counts)
    }

    /**
     * 유효성 검사:
     * - app_id / export_version / schema_version
     * - FK 무결성(런 관련 참조 존재 여부)
     */
    fun validate_bundle(bundle: ExportBundle, current_schema_version: Int): String? {
        if (bundle.app_id != "dontforget") return "유효하지 않은 형식입니다(app_id)."
        if (bundle.export_version != 1) return "유효하지 않은 형식입니다(export_version)."
        if (bundle.db_schema_version > current_schema_version) {
            return "유효하지 않은 형식입니다(스키마 버전이 너무 최신)."
        }

        val t = bundle.tables

        val item_ids = t.check_item.map { it.item_id }.toHashSet()
        val condition_def_ids = t.condition_definition.map { it.condition_def_id }.toHashSet()
        val result_def_ids = t.result_definition.map { it.result_def_id }.toHashSet()
        val session_ids = t.run_session.map { it.session_id }.toHashSet()

        // run_item FK 체크
        for (ri in t.run_item) {
            if (!session_ids.contains(ri.session_id)) return "유효하지 않은 형식입니다(run_item.session_id 참조 없음)."
            if (!item_ids.contains(ri.item_id)) return "유효하지 않은 형식입니다(run_item.item_id 참조 없음)."
        }

        // run_item_progress FK 체크
        for (p in t.run_item_progress) {
            if (!session_ids.contains(p.session_id)) return "유효하지 않은 형식입니다(run_item_progress.session_id 참조 없음)."
            if (!item_ids.contains(p.item_id)) return "유효하지 않은 형식입니다(run_item_progress.item_id 참조 없음)."
        }

        // run_condition FK 체크
        for (c in t.run_condition) {
            if (!session_ids.contains(c.session_id)) return "유효하지 않은 형식입니다(run_condition.session_id 참조 없음)."
            if (!condition_def_ids.contains(c.condition_def_id)) return "유효하지 않은 형식입니다(run_condition.condition_def_id 참조 없음)."
        }

        // run_summary FK 체크
        for (s in t.run_summary) {
            if (!session_ids.contains(s.session_id)) return "유효하지 않은 형식입니다(run_summary.session_id 참조 없음)."
            if (!result_def_ids.contains(s.result_def_id)) return "유효하지 않은 형식입니다(run_summary.result_def_id 참조 없음)."
        }

        return null // OK
    }

    /**
     * C안: import 시 기존 데이터 전부 삭제하고 덮어쓰기
     * - 트랜잭션
     * - 삭제: 자식 -> 부모
     * - 삽입: 부모 -> 자식
     */
    suspend fun import_overwrite(bundle: ExportBundle) {
        val t = bundle.tables

        db.withTransaction {
            // delete (child -> parent)
            dao.delete_all_run_summary()
            dao.delete_all_run_condition()
            dao.delete_all_run_item_progress()
            dao.delete_all_run_item()
            dao.delete_all_run_session()
            dao.delete_all_result_definition()
            dao.delete_all_condition_definition()
            dao.delete_all_check_item()

            // insert (parent -> child)
            dao.insert_all_check_item(t.check_item)
            dao.insert_all_condition_definition(t.condition_definition)
            dao.insert_all_result_definition(t.result_definition)

            dao.insert_all_run_session(t.run_session)
            dao.insert_all_run_item(t.run_item)
            dao.insert_all_run_item_progress(t.run_item_progress)
            dao.insert_all_run_condition(t.run_condition)
            dao.insert_all_run_summary(t.run_summary)
        }
    }
}
