package com.example.dontforget.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dontforget.data.repo.HistorySessionBundle
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.ui.vm.HistoryViewModel

private enum class HistoryViewType {
    DATE_DETAIL,
    STATS_CONDITION,
    STATS_ITEM,
    STATS_TIME,
    STATS_SUMMARY,
    ANALYSIS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vm: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    var view_type by remember { mutableStateOf(HistoryViewType.STATS_TIME) }
    var sheet_open by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { sheet_open = true },
                containerColor = Color(0xFF202020)
            ) { Text("선택", color = Color.White) }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when (view_type) {
                HistoryViewType.DATE_DETAIL ->
                    DateDetailScreen(vm = vm)

                HistoryViewType.STATS_CONDITION ->
                    SimpleCard("컨디션 통계(테스트)", "컨디션항목 선택 → 날짜별 그래프 + 메모 표")

                HistoryViewType.STATS_ITEM ->
                    ItemStatsDemo()

                HistoryViewType.STATS_TIME ->
                    SimpleCard("연습시간 통계(테스트)", "날짜별 총 연습시간 그래프")

                HistoryViewType.STATS_SUMMARY ->
                    SimpleCard("요약 통계(테스트)", "요약항목 선택 → 날짜별 그래프 + 메모")

                HistoryViewType.ANALYSIS ->
                    SimpleCard("분석(테스트)", "최근 추이 기반 코멘트 출력")
            }
        }
    }

    if (sheet_open) {
        AlertDialog(
            onDismissRequest = { sheet_open = false },
            confirmButton = {},
            title = { Text("무엇을 볼까?", color = Color.Black) },
            text = {
                Column {
                    SheetItem("날짜 상세보기") { view_type = HistoryViewType.DATE_DETAIL; sheet_open = false }
                    SheetItem("통계 - 컨디션") { view_type = HistoryViewType.STATS_CONDITION; sheet_open = false }
                    SheetItem("통계 - 아이템") { view_type = HistoryViewType.STATS_ITEM; sheet_open = false }
                    SheetItem("통계 - 연습시간") { view_type = HistoryViewType.STATS_TIME; sheet_open = false }
                    SheetItem("통계 - 요약") { view_type = HistoryViewType.STATS_SUMMARY; sheet_open = false }
                    SheetItem("분석") { view_type = HistoryViewType.ANALYSIS; sheet_open = false }
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun DateDetailScreen(vm: HistoryViewModel) {
    val date_rows by vm.date_rows.collectAsState()
    val selected_date by vm.selected_date.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val bundle by vm.selected_bundle.collectAsState()

    // ✅ 이름 매핑(컨디션/요약)
    val cond_name_map by vm.condition_name_map.collectAsState()
    val res_name_map by vm.result_name_map.collectAsState()

    LaunchedEffect(Unit) {
        vm.load_recent_dates(days = 60)
    }

    // ✅ 세션 상세(bundle) 로드 결과 로그
    LaunchedEffect(bundle) {
        bundle?.let {
            Log.d("DF_HISTORY", "bundle loaded: session_id=${it.session.session_id} items=${it.items.size} start=${it.start_conditions.size} mid=${it.mid_conditions.size} end=${it.end_conditions.size} summaries=${it.summaries.size}")
        }
    }

    if (selected_date == null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("날짜 선택", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Text("완료된 세션이 있는 날짜만 표시", color = Color.Black)
                Spacer(Modifier.height(12.dp))

                LazyColumn {
                    items(date_rows) { r ->
                        TextButton(
                            onClick = {
                                Log.d("DF_HISTORY", "select_date=${r.date}")
                                vm.select_date(r.date)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(r.date, color = Color.Black)
                                Text(
                                    "세션 ${r.session_count}회 · 총 ${ms_to_min(r.total_ms)}분",
                                    color = Color.Black
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
        return
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("날짜 상세", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                TextButton(onClick = {
                    Log.d("DF_HISTORY", "clear_date")
                    vm.clear_date()
                }) { Text("뒤로", color = Color.Black) }
            }

            Text("날짜: $selected_date", color = Color.Black)
            Spacer(Modifier.height(12.dp))

            Text("세션 목록", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(sessions) { s ->
                    SessionRow(
                        session = s,
                        on_click = {
                            Log.d("DF_HISTORY", "select_session_id=${s.session_id}")
                            vm.select_session(s.session_id)
                        }
                    )
                    Divider()
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    if (bundle == null) {
                        Text("세션을 눌러 상세보기", color = Color.Black)
                    } else {
                        SessionDetail(
                            bundle = bundle!!,
                            cond_name_map = cond_name_map,
                            res_name_map = res_name_map,
                            on_close = {
                                Log.d("DF_HISTORY", "clear_session_detail")
                                vm.clear_session_detail()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: RunSessionEntity,
    on_click: () -> Unit
) {
    val dur = ((session.end_time ?: 0L) - session.start_time).coerceAtLeast(0L)

    TextButton(onClick = on_click, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text("세션ID: ${session.session_id}", color = Color.Black)
            Text("소요: ${ms_to_min(dur)}분", color = Color.Black)
        }
    }
}

private fun run_result_text(s: Int, f: Int, c: Int): String {
    return when {
        s > 0 -> "성공"
        f > 0 -> "실패"
        c > 0 -> "취소"
        else -> "미선택"
    }
}

private enum class RunResultType { SUCCESS, FAIL, CANCEL, NONE }

private fun run_result_type(s: Int, f: Int, c: Int): RunResultType {
    return when {
        s > 0 -> RunResultType.SUCCESS
        f > 0 -> RunResultType.FAIL
        c > 0 -> RunResultType.CANCEL
        else -> RunResultType.NONE
    }
}

private fun run_result_order(t: RunResultType): Int {
    return when (t) {
        RunResultType.SUCCESS -> 0
        RunResultType.FAIL -> 1
        RunResultType.CANCEL -> 2
        RunResultType.NONE -> 3
    }
}

/**
 * 0인 카운트는 아예 출력에서 제외
 * 예) success=1, fail=0, cancel=0 -> "성공"
 * 예) success=0, fail=1, cancel=0 -> "실패"
 * 예) 모두 0 -> "미선택"
 */
private fun run_result_text_compact(s: Int, f: Int, c: Int): String {
    val parts = buildList {
        if (s > 0) add("성공")
        if (f > 0) add("실패")
        if (c > 0) add("취소")
    }
    return if (parts.isEmpty()) "미선택" else parts.joinToString("/")
}


@Composable
private fun SessionDetail(
    bundle: HistorySessionBundle,
    cond_name_map: Map<Long, String>,
    res_name_map: Map<Long, String>,
    on_close: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("세션 상세", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                TextButton(onClick = on_close) { Text("닫기", color = Color.Black) }
            }

            val s = bundle.session
            val dur = ((s.end_time ?: 0L) - s.start_time).coerceAtLeast(0L)

            Text("세션ID: ${s.session_id}", color = Color.Black)
            Text("시간: ${ms_to_min(dur)}분", color = Color.Black)

            Spacer(Modifier.height(10.dp))

            Section(
                title = "컨디션(START)",
                lines = bundle.start_conditions.map {
                    val name = cond_name_map[it.condition_def_id] ?: "알수없음(${it.condition_def_id})"
                    val level = code_to_kor(it.value_code)
                    val memo = it.value
                    if (memo.isBlank()) "$name : $level" else "$name : $level / $memo"
                }
            )

            Section(
                title = "컨디션(MID)",
                lines = bundle.mid_conditions.map {
                    val name = cond_name_map[it.condition_def_id] ?: "알수없음(${it.condition_def_id})"
                    val level = code_to_kor(it.value_code)
                    val memo = it.value
                    if (memo.isBlank()) "$name : $level" else "$name : $level / $memo"
                }
            )

            Section(
                title = "컨디션(END)",
                lines = bundle.end_conditions.map {
                    val name = cond_name_map[it.condition_def_id] ?: "알수없음(${it.condition_def_id})"
                    val level = code_to_kor(it.value_code)
                    val memo = it.value
                    if (memo.isBlank()) "$name : $level" else "$name : $level / $memo"
                }
            )

            Section(
                title = "아이템 결과",
                lines = if (bundle.items.isEmpty()) {
                    listOf("없음 (run_item 저장 안됐거나 0개)")
                } else {

                    // ✅ 결과 기준 정렬: 성공 → 실패 → 취소 → 미선택
                    val sorted = bundle.items.sortedWith(
                        compareBy<com.example.dontforget.data.entity.RunItemEntity> { ri ->
                            run_result_order(
                                run_result_type(
                                    ri.success_count,
                                    ri.fail_count,
                                    ri.cancel_count
                                )
                            )
                        }.thenBy { ri ->
                            ri.title
                        }
                    )

                    sorted.map { ri ->
                        val r = run_result_text_compact(
                            ri.success_count,
                            ri.fail_count,
                            ri.cancel_count
                        )
                        "${ri.title} | 결과: $r"
                    }
                }
            )

            Section(
                title = "오늘요약",
                lines = bundle.summaries.map {
                    val name = res_name_map[it.result_def_id] ?: "알수없음(${it.result_def_id})"
                    val level = code_to_kor(it.value_code)
                    val memo = it.value
                    if (memo.isBlank()) "$name : $level" else "$name : $level / $memo"
                }
            )
        }
    }
}

@Composable
private fun Section(title: String, lines: List<String>) {
    Spacer(Modifier.height(10.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Black)
    Spacer(Modifier.height(4.dp))
    if (lines.isEmpty()) {
        Text("없음", color = Color.Black)
    } else {
        lines.forEach { Text("• $it", color = Color.Black) }
    }
}

private fun code_to_kor(code: String?): String {
    return when (code) {
        "VERY_BAD" -> "매우나쁨"
        "BAD" -> "나쁨"
        "NORMAL" -> "보통"
        "GOOD" -> "좋음"
        "VERY_GOOD" -> "매우좋음"
        null, "" -> ""
        else -> code
    }
}

private fun ms_to_min(ms: Long): Long = (ms / 1000L / 60L)

// ===========================
// 아래는 유지(테스트용)
// ===========================

@Composable
private fun SheetItem(text: String, on_click: () -> Unit) {
    TextButton(onClick = on_click, modifier = Modifier.fillMaxWidth()) {
        Text(text = text, color = Color.Black)
    }
}

@Composable
private fun SimpleCard(title: String, desc: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            Text(desc, color = Color.Black)
        }
    }
}

/* ===========================
   ✅ 아이템 통계 표(더미)
   =========================== */

private enum class ItemSortKey { DATE, SUCCESS, FAIL, CANCEL }
private enum class SortDir { ASC, DESC }

private data class ItemStatRow(
    val date: String,
    val success: Int,
    val fail: Int,
    val cancel: Int
)

@Composable
private fun ItemStatsDemo() {
    val rows = remember {
        listOf(
            ItemStatRow("2026-01-03", success = 5, fail = 2, cancel = 0),
            ItemStatRow("2026-01-02", success = 3, fail = 4, cancel = 1),
            ItemStatRow("2026-01-01", success = 6, fail = 1, cancel = 0),
            ItemStatRow("2025-12-31", success = 2, fail = 5, cancel = 1),
            ItemStatRow("2025-12-30", success = 4, fail = 3, cancel = 0)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("아이템 통계(테스트)", style = MaterialTheme.typography.titleLarge, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            Text("헤더 누르면 오름/내림 정렬됨", color = Color.Black)
            Spacer(Modifier.height(12.dp))
            ItemStatsTable(rows = rows)
        }
    }
}

@Composable
private fun ItemStatsTable(rows: List<ItemStatRow>) {
    var sort_key by remember { mutableStateOf(ItemSortKey.DATE) }
    var sort_dir by remember { mutableStateOf(SortDir.DESC) }

    val sorted = remember(rows, sort_key, sort_dir) {
        val base = when (sort_key) {
            ItemSortKey.DATE -> rows.sortedBy { it.date }
            ItemSortKey.SUCCESS -> rows.sortedBy { it.success }
            ItemSortKey.FAIL -> rows.sortedBy { it.fail }
            ItemSortKey.CANCEL -> rows.sortedBy { it.cancel }
        }
        if (sort_dir == SortDir.DESC) base.reversed() else base
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            HeaderCell("날짜", sort_key, sort_dir, ItemSortKey.DATE) {
                toggle_sort(ItemSortKey.DATE, sort_key, sort_dir) { k, d -> sort_key = k; sort_dir = d }
            }
            HeaderCell("성공", sort_key, sort_dir, ItemSortKey.SUCCESS) {
                toggle_sort(ItemSortKey.SUCCESS, sort_key, sort_dir) { k, d -> sort_key = k; sort_dir = d }
            }
            HeaderCell("실패", sort_key, sort_dir, ItemSortKey.FAIL) {
                toggle_sort(ItemSortKey.FAIL, sort_key, sort_dir) { k, d -> sort_key = k; sort_dir = d }
            }
            HeaderCell("취소", sort_key, sort_dir, ItemSortKey.CANCEL) {
                toggle_sort(ItemSortKey.CANCEL, sort_key, sort_dir) { k, d -> sort_key = k; sort_dir = d }
            }
        }

        Divider()

        sorted.forEach { r ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                BodyCell(r.date)
                BodyCell(r.success.toString())
                BodyCell(r.fail.toString())
                BodyCell(r.cancel.toString())
            }
            Divider()
        }
    }
}

private fun toggle_sort(
    clicked: ItemSortKey,
    current_key: ItemSortKey,
    current_dir: SortDir,
    set: (ItemSortKey, SortDir) -> Unit
) {
    if (clicked == current_key) {
        set(clicked, if (current_dir == SortDir.ASC) SortDir.DESC else SortDir.ASC)
    } else {
        set(clicked, SortDir.ASC)
    }
}

@Composable
private fun RowScope.HeaderCell(
    text: String,
    active_key: ItemSortKey,
    dir: SortDir,
    my_key: ItemSortKey,
    on_click: () -> Unit
) {
    val arrow = if (active_key == my_key) {
        if (dir == SortDir.ASC) " ▲" else " ▼"
    } else ""

    TextButton(
        onClick = on_click,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(text = text + arrow, color = Color.Black)
    }
}

@Composable
private fun RowScope.BodyCell(text: String) {
    Text(text = text, modifier = Modifier.weight(1f), color = Color.Black)
}
