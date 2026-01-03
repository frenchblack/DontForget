package com.example.dontforget

import kotlin.math.max
import kotlin.random.Random
import com.example.dontforget.data.AppDatabase
import com.example.dontforget.data.entity.*

suspend fun seed_run_dummy_data_if_empty(
    db: AppDatabase,
    session_count: Int = 180
) {
    val run_dao = db.run_dao()
    val check_item_dao = db.check_item_dao()
    val condition_def_dao = db.condision_dao()
    val run_condition_dao = db.run_condition_dao()
    val result_def_dao = db.result_definition_dao()
    val run_summary_dao = db.run_summary_dao()
    val progress_dao = db.run_item_progress_dao()

    val sess_cnt = run_dao.count_sessions()
    val item_cnt = run_dao.count_run_items()

// ✅ run_item이 이미 있으면 더미 필요 없음
    if (item_cnt > 0) return

//    if (run_dao.count_sessions() > 0) return

    val all_items = check_item_dao.get_all_items()
    val condition_defs = condition_def_dao.get_all_active_ordered()
    val result_defs = result_def_dao.get_all_active_ordered()
    if (all_items.isEmpty() || condition_defs.isEmpty() || result_defs.isEmpty()) return

    val active_items = all_items.filter { it.status == "ACTIVE" }
    val frequent_mistake_item_ids = active_items.take(3).map { it.item_id }

    val rng = Random(777)

    val memo_good = listOf(
        "공명 잘 붙음, 고음 안정",
        "호흡 지지 잘 됨, 턱힘 빠짐",
        "연결 부드러움, 소리 가벼움",
        "오늘은 전체적으로 편안"
    )
    val memo_bad = listOf(
        "목 잠김 느낌, 소리 눌림",
        "턱/혀 긴장 올라옴",
        "워밍업 부족해서 고음 불안",
        "코막힘 + 긴장으로 공명 안 잡힘"
    )
    val memo_mid = listOf(
        "루틴은 수행, 무난",
        "컨디션 보통, 체크 위주",
        "연습량은 했는데 집중이 흔들림"
    )

    fun pick_days_ago(): Int {
        val roll = rng.nextInt(100)
        return when {
            roll < 45 -> rng.nextInt(0, 30)
            roll < 80 -> rng.nextInt(30, 180)
            else -> rng.nextInt(180, 365)
        }
    }

    fun millis_days_ago(days_ago: Int): Long {
        val day = 24L * 60L * 60L * 1000L
        return System.currentTimeMillis() - days_ago * day
    }

    fun random_condition_score(): Int {
        val roll = rng.nextInt(100)
        return when {
            roll < 15 -> 1
            roll < 35 -> 2
            roll < 65 -> 3
            roll < 85 -> 4
            else -> 5
        }
    }

    fun condition_level_code(score_1_5: Int): String {
        return when (score_1_5) {
            1 -> ConditionLevel.VERY_BAD.code
            2 -> ConditionLevel.BAD.code
            3 -> ConditionLevel.NORMAL.code
            4 -> ConditionLevel.GOOD.code
            else -> ConditionLevel.VERY_GOOD.code
        }
    }

    repeat(session_count) {
        val days_ago = pick_days_ago()
        val base = millis_days_ago(days_ago)

        val start = base + rng.nextLong(8 * 60 * 60 * 1000L, 23 * 60 * 60 * 1000L)
        val dur_min = rng.nextInt(8, 46)
        val end = start + dur_min * 60_000L

        val condition_score = random_condition_score()
        val session_status =
            if (rng.nextInt(100) < 92) RunStatus.COMPLETED else RunStatus.ABANDONED

        val session_id = run_dao.insert_session(
            RunSessionEntity(
                start_time = start,
                end_time = if (session_status == RunStatus.COMPLETED) end else null,
                status = session_status
            )
        )

        val item_count = rng.nextInt(6, 13)
        val picked = all_items.shuffled(Random(session_id)).take(item_count)

        val run_items = picked.map { item ->
            val base_fail = when (condition_score) {
                5 -> rng.nextInt(0, 2)
                4 -> rng.nextInt(0, 3)
                3 -> rng.nextInt(0, 4)
                2 -> rng.nextInt(1, 5)
                else -> rng.nextInt(2, 7)
            }

            val extra =
                if (item.item_id in frequent_mistake_item_ids) rng.nextInt(0, 3) else 0

            val fail = base_fail + extra
            val success = max(0, rng.nextInt(1, 6) - (fail / 2))
            val cancel = if (rng.nextInt(100) < 8) 1 else 0

            val st =
                if (session_status == RunStatus.COMPLETED) RunItemStatus.COMPLETE else RunItemStatus.PROCESS

            RunItemEntity(
                session_id = session_id,
                item_id = item.item_id,
                title = item.title,
                note = item.note,
                success_count = success,
                fail_count = fail,
                cancel_count = cancel,
                status = st
            )
        }

        // ✅ 여기 핵심: RunDao에서 벌크 인서트
        run_dao.insert_run_items(run_items)

        run_items.forEach { ri ->
            val is_completed =
                if (ri.status == RunItemStatus.COMPLETE && rng.nextInt(100) < 70) 1 else 0

            progress_dao.upsert(
                RunItemProgressEntity(
                    session_id = session_id,
                    item_id = ri.item_id,
                    is_completed = is_completed
                )
            )
        }

        val run_conditions = condition_defs.map { def ->
            val phase = when (rng.nextInt(100)) {
                in 0..59 -> ConditionPhase.START
                in 60..84 -> ConditionPhase.MID
                else -> ConditionPhase.END
            }

            val value_code =
                if (def.input_type == InputType.LEVEL_5) {
                    val score = (condition_score + rng.nextInt(-1, 2)).coerceIn(1, 5)
                    condition_level_code(score)
                } else ""

            val value_text =
                if (def.input_type == InputType.TEXT) {
                    when (condition_score) {
                        4, 5 -> memo_good[rng.nextInt(memo_good.size)]
                        1, 2 -> memo_bad[rng.nextInt(memo_bad.size)]
                        else -> memo_mid[rng.nextInt(memo_mid.size)]
                    }
                } else ""

            RunConditionEntity(
                session_id = session_id,
                condition_def_id = def.condition_def_id,
                value_code = value_code,
                value = value_text,
                phase = phase
            )
        }

        run_condition_dao.insert_all(run_conditions)

        val summaries = result_defs.map { def ->
            val value_code =
                if (def.input_type == InputType.LEVEL_5) {
                    val score = (condition_score + rng.nextInt(-1, 2)).coerceIn(1, 5)
                    score.toString()
                } else ""

            val value_text =
                if (def.input_type == InputType.TEXT) {
                    when (condition_score) {
                        4, 5 -> "총평: ${memo_good[rng.nextInt(memo_good.size)]}"
                        1, 2 -> "총평: ${memo_bad[rng.nextInt(memo_bad.size)]}"
                        else -> "총평: ${memo_mid[rng.nextInt(memo_mid.size)]}"
                    }
                } else ""

            RunSummaryEntity(
                session_id = session_id,
                result_def_id = def.result_def_id,
                value_code = value_code,
                value = value_text
            )
        }

        run_summary_dao.insert_all(summaries)
    }
}
