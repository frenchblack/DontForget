package com.example.dontforget.ui.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dontforget.data.repo.HistorySessionBundle
import com.example.dontforget.data.entity.RunSessionEntity
import com.example.dontforget.ui.vm.HistoryViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.abs

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.BoxWithConstraints
import kotlin.math.max
import kotlin.math.min



private enum class HistoryViewType {
    DATE_DETAIL,
    STATS_CONDITION,
    STATS_ITEM,
    STATS_TIME,
    STATS_SUMMARY,
    ANALYSIS
}

private val STAT_SECTION_MIN_HEIGHT = 180.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vm: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    var view_type by remember { mutableStateOf<HistoryViewType?>(null) }  // ✅ 초기 null
    var sheet_open by remember { mutableStateOf(false) }

    // ✅ 시트(선택창) 안에서 임시로 고르는 타입
    var sheet_pick by remember { mutableStateOf(view_type) }

    // 선택창 내부 상태
    var pick_tab by remember { mutableStateOf(PickTab.DATE) }

// 날짜탭 내부 흐름: 날짜목록 → 세션목록
    var date_step by remember { mutableStateOf(DatePickStep.DATE_LIST) }

// 통계탭 내부 선택
    var stats_pick by remember { mutableStateOf(StatsPickType.TIME) }

    val HistoryBg = Color(0xFFEAF2FF) // 연한 파스텔 블루



    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pick_tab = PickTab.DATE
                    date_step = DatePickStep.DATE_LIST
                    sheet_open = true
                },
                containerColor = Color(0xf0f0f0)
            ) { Text("선택", color = Color.White) }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .background(Color(0xFFEAF2FF)) // ✅ 여기! 제일 큰 배경
                .padding(inner)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when (view_type) {
                null -> SimpleCard("히스토리", "아래 '선택'을 눌러 화면을 고르세요")

                HistoryViewType.DATE_DETAIL -> DateDetailScreen(vm = vm)

                HistoryViewType.STATS_CONDITION -> StatsConditionScreen(vm = vm)
                HistoryViewType.STATS_ITEM -> StatsItemScreen(vm = vm)
                HistoryViewType.STATS_TIME -> StatsTimeScreen(vm = vm)
                HistoryViewType.STATS_SUMMARY -> StatsSummaryScreen(vm = vm)

                HistoryViewType.ANALYSIS -> SimpleCard("분석(테스트)", "최근 추이 기반 코멘트 출력")
            }
        }
    }

    if (sheet_open) {
        AlertDialog(
            onDismissRequest = { sheet_open = false },
            confirmButton = {
                // 날짜탭은 "세션 클릭 시 자동 적용"이라 confirm 없어도 됨.
                // 통계/분석은 "적용" 버튼으로 메인에 띄우기.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { sheet_open = false }) { Text("닫기") }

                    if (pick_tab != PickTab.DATE) {
                        Button(onClick = {
                            view_type = when (pick_tab) {
                                PickTab.STATS -> when (stats_pick) {
                                    StatsPickType.CONDITION -> HistoryViewType.STATS_CONDITION
                                    StatsPickType.ITEM -> HistoryViewType.STATS_ITEM
                                    StatsPickType.TIME -> HistoryViewType.STATS_TIME
                                    StatsPickType.SUMMARY -> HistoryViewType.STATS_SUMMARY
                                }
                                PickTab.ANALYSIS -> HistoryViewType.ANALYSIS
                                PickTab.DATE -> HistoryViewType.DATE_DETAIL // 사실 DATE는 세션 클릭으로 처리
                            }
                            sheet_open = false
                        }) { Text("적용") }
                    }
                }
            },
            title = { Text("선택", color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // ✅ 상단 탭 3개
                    TabRow(selectedTabIndex = pick_tab.ordinal) {
                        Tab(
                            selected = pick_tab == PickTab.DATE,
                            onClick = { pick_tab = PickTab.DATE; date_step = DatePickStep.DATE_LIST },
                            text = { Text("날짜 상세보기") }
                        )
                        Tab(
                            selected = pick_tab == PickTab.STATS,
                            onClick = { pick_tab = PickTab.STATS },
                            text = { Text("통계") }
                        )
                        Tab(
                            selected = pick_tab == PickTab.ANALYSIS,
                            onClick = { pick_tab = PickTab.ANALYSIS },
                            text = { Text("분석") }
                        )
                    }

                    when (pick_tab) {
                        PickTab.DATE -> DatePickPanel(
                            vm = vm,
                            step = date_step,
                            on_step_change = { date_step = it },
                            on_apply_close = {
                                // ✅ "선택창 아래로 내리고" = 닫기
                                view_type = HistoryViewType.DATE_DETAIL
                                sheet_open = false
                            }
                        )

                        PickTab.STATS -> StatsPickPanel(
                            vm = vm,
                            stats_pick = stats_pick,
                            on_pick = { stats_pick = it }
                        )

                        PickTab.ANALYSIS -> SimpleCard("분석", "최근 추이 코멘트(룰 기반) 예정")
                    }
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
        Column(Modifier.padding(16.dp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // ✅ 전체 스크롤
        ) {
            var sessions_open by remember { mutableStateOf(false) } // ✅ 기본 접힘

            TitleHeader(
                title = "날짜 상세",
                subtitle = "날짜: $selected_date"
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("세션 목록", style = MaterialTheme.typography.titleMedium, color = Color.Black)

                TextButton(onClick = { sessions_open = !sessions_open }) {
                    Text(if (sessions_open) "접기" else "펼치기", color = Color.Black)
                }
            }

            AnimatedVisibility(visible = sessions_open) {
                Column {
                    Spacer(Modifier.height(8.dp))

                    sessions.forEach { s ->
                        SessionRow(
                            session = s,
                            on_click = {
                                Log.d("DF_HISTORY", "select_session_id=${s.session_id}")
                                vm.select_session(s.session_id)
                            }
                        )
                        Divider()
                    }
                }
            }


            Spacer(Modifier.height(12.dp))

            if (bundle == null) {
                Text("세션을 선택하면 상세가 표시됩니다", color = Color(0xFF666666))
            } else {
                SessionDetail(
                    bundle = bundle!!,
                    cond_name_map = cond_name_map,
                    res_name_map = res_name_map
                )
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
    res_name_map: Map<Long, String>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // ✅ 타이틀 느낌 강화
            Text(
                text = "세션 상세",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
            Divider()

            val s = bundle.session
            val dur = ((s.end_time ?: 0L) - s.start_time).coerceAtLeast(0L)

            Text("세션ID: ${s.session_id}", color = Color.Black)
            Text("시간: ${ms_to_min(dur)}분", color = Color.Black)

            // ✅ 컨디션 한 섹션에서 START/MID/END 구분
            SectionBlock(title = "컨디션") {
                val start_entries = bundle.start_conditions.map {
                    LevelEntry(
                        name = cond_name_map[it.condition_def_id] ?: "알수없음(${it.condition_def_id})",
                        code = it.value_code,
                        memo = it.value
                    )
                }
                val mid_entries = bundle.mid_conditions.map {
                    LevelEntry(
                        name = cond_name_map[it.condition_def_id] ?: "알수없음(${it.condition_def_id})",
                        code = it.value_code,
                        memo = it.value
                    )
                }
                val end_entries = bundle.end_conditions.map {
                    LevelEntry(
                        name = cond_name_map[it.condition_def_id] ?: "알수없음(${it.condition_def_id})",
                        code = it.value_code,
                        memo = it.value
                    )
                }

                LevelEntryGroup("START", start_entries)
                Spacer(Modifier.height(20.dp))
                LevelEntryGroup("MID", mid_entries)
                Spacer(Modifier.height(20.dp))
                LevelEntryGroup("END", end_entries)
            }

            // ✅ 아이템 결과 -> 연습결과 / 성공 실패 취소 미선택 그룹 + 카운트
            SectionBlock(title = "연습결과") {
                if (bundle.items.isEmpty()) {
                    Text("없음 (run_item 저장 안됐거나 0개)", color = Color(0xFF666666))
                    return@SectionBlock
                }

                val grouped = bundle.items.groupBy { ri ->
                    run_result_type(ri.success_count, ri.fail_count, ri.cancel_count)
                }

                ResultGroup("성공", grouped[RunResultType.SUCCESS].orEmpty())
                if (grouped[RunResultType.SUCCESS].orEmpty().isNotEmpty()) Spacer(Modifier.height(12.dp))

                ResultGroup("실패", grouped[RunResultType.FAIL].orEmpty())
                if (grouped[RunResultType.FAIL].orEmpty().isNotEmpty()) Spacer(Modifier.height(12.dp))

                ResultGroup("취소", grouped[RunResultType.CANCEL].orEmpty())
                if (grouped[RunResultType.CANCEL].orEmpty().isNotEmpty()) Spacer(Modifier.height(12.dp))

                ResultGroup("미선택", grouped[RunResultType.NONE].orEmpty())
            }

            // ✅ 오늘요약도 컨디션처럼 섹션블록
            SectionBlock(title = "오늘요약") {
                val entries = bundle.summaries.map {
                    LevelEntry(
                        name = res_name_map[it.result_def_id] ?: "알수없음(${it.result_def_id})",
                        code = it.value_code,
                        memo = it.value
                    )
                }

                if (entries.isEmpty()) {
                    Text("없음", color = Color(0xFF666666))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        entries.forEach { e ->
                            SummaryEntryRow(e)
                        }
                    }
                }
            }
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

private fun code_to_level(code: String?): Int {
    return when (code) {
        "VERY_BAD" -> 1
        "BAD" -> 2
        "NORMAL" -> 3
        "GOOD" -> 4
        "VERY_GOOD" -> 5
        else -> 0
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

// ===========================
// ✅ 통계 공통 UX 틀 + STATS_TIME 실동작 버전
// ===========================

private enum class StatRange(val label: String, val days: Int?) {
    D7("7일", 7),
    D30("30일", 30),
    D90("90일", 90),
    ALL("전체", null)
}

@Composable
private fun StatBaseLayout(
    title: String,
    header: @Composable () -> Unit,
    chart: @Composable () -> Unit,
    detail: @Composable () -> Unit
) {
    val CHART_H = 180.dp
    val DETAIL_H = 360.dp   // ✅ 표 더 길게 (원하는 만큼 320~500)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())  // ✅ 이게 핵심
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.Black)

            header()

            Divider(thickness = 1.5.dp)

            Text("그래프", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
                modifier = Modifier.fillMaxWidth().height(CHART_H)
            ) {
                Box(Modifier.fillMaxSize().padding(12.dp)) { chart() }
            }

            Divider(thickness = 1.5.dp)

            Text("상세", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
                modifier = Modifier.fillMaxWidth().height(DETAIL_H)
            ) {
                Box(Modifier.fillMaxSize().padding(12.dp)) { detail() }
            }
        }
    }
}




@Composable
private fun RangeSelector(
    current: StatRange,
    on_select: (StatRange) -> Unit
) {
    // M3 segmented 버튼. (Material3 최신이면 동작)
    SingleChoiceSegmentedButtonRow {
        StatRange.entries.forEachIndexed { idx, r ->
            SegmentedButton(
                selected = (current == r),
                onClick = { on_select(r) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = idx,
                    count = StatRange.entries.size
                )
            ) {
                Text(r.label)
            }
        }
    }
}

@Composable
private fun ChartPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Gray)
    }
}

// ------------------------------------------------------
// ✅ STATS_TIME: 네가 이미 만들어둔 date_rows를 그대로 써서 "진짜 동작"하게 만듦
// ------------------------------------------------------
@Composable
private fun StatsTimeScreen(vm: HistoryViewModel) {
    var range by remember { mutableStateOf(StatRange.D30) }

    val date_rows by vm.date_rows.collectAsState()

    // ✅ range 변경 시, 그 기간만큼 날짜 집계 다시 로딩
    LaunchedEffect(range) {
        val days = range.days ?: 3650 // ALL이면 넉넉히 크게
        vm.load_recent_dates(days = days)
    }

    // ✅ 상단 총합(기간별)
    val total_ms = remember(date_rows) { date_rows.sumOf { it.total_ms } }
    val total_sessions = remember(date_rows) { date_rows.sumOf { it.session_count } }
    val points = remember(date_rows) {
        date_rows
            .sortedBy { it.date } // 오래된 -> 최신 (원하면 reversed로 바꿔)
            .map { DateMsPoint(date = it.date, total_ms = it.total_ms) }
    }

    StatBaseLayout(
        title = "연습시간 통계",
        header = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeSelector(current = range, on_select = { range = it })

                // 간단 요약 줄(UX 고정 포인트)
                Text(
                    "세션 ${total_sessions}회 · 총 ${ms_to_min(total_ms)}분",
                    color = Color.Black
                )
            }
        },
        chart = {
            // points: List<DateMsPoint>
            InteractiveLineChartV2(
                count = points.size,
                labelAt = { i -> points[i].date },
                valueAt = { i -> points[i].total_ms.toFloat() },
                valueTextAt = { i ->
                    val min = ms_to_min(points[i].total_ms)
                    "연습시간: ${min}분"
                },
                modifier = Modifier.fillMaxSize()
            )
        },
        detail = {
            if (date_rows.isEmpty()) {
                Text("표시할 데이터가 없음", color = Color.Black)
                return@StatBaseLayout
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                date_rows.forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(r.date, color = Color.Black)
                        Text("세션 ${r.session_count} · ${ms_to_min(r.total_ms)}분", color = Color.Black)
                    }
                    Divider()
                }
            }
        }
    )
}

// ------------------------------------------------------
// ✅ STATS_ITEM: 기존 ItemStatsDemo(더미)를 "같은 틀" 안에 넣어 UX 통일
// (나중에 실데이터 붙여도 레이아웃은 그대로)
// ------------------------------------------------------

//private enum class ItemChartMetric(val label: String) { SUCCESS("성공"), FAIL("실패"), CANCEL("취소"), TOTAL("합계") }
private enum class ItemChartMetric(val label: String) {
    SUCCESS("성공"),
    FAIL("실패"),
    CANCEL("취소"),
    TOTAL("합계")
}

@Composable
private fun StatsItemScreen(vm: HistoryViewModel) {
    var range by remember { mutableStateOf(StatRange.D30) }

    val options by vm.item_options.collectAsState()
    val rows by vm.item_stat_rows.collectAsState()

    var picked_item_id by remember { mutableStateOf<Long?>(null) }

    // ✅ metric은 header 밖으로 빼야 chart에서도 쓸 수 있음
    var metric by remember { mutableStateOf(ItemChartMetric.SUCCESS) }

    LaunchedEffect(Unit) {
        vm.load_item_options()
    }

    LaunchedEffect(range, picked_item_id) {
        val id = picked_item_id ?: return@LaunchedEffect
        val days = range.days ?: 3650
        vm.load_item_stats(item_id = id, days = days)
    }

    // ✅ 그래프용 points (metric에 따라 v가 바뀜)
    val points = remember(rows, metric) {
        rows
            .sortedBy { it.date }
            .map { r ->
                val v = when (metric) {
                    ItemChartMetric.SUCCESS -> r.success
                    ItemChartMetric.FAIL -> r.fail_count
                    ItemChartMetric.CANCEL -> r.cancel
                    ItemChartMetric.TOTAL -> (r.success + r.fail_count + r.cancel)
                }
                DateIntPoint(date = r.date, v = v)
            }
    }

    StatBaseLayout(
        title = "아이템 통계",
        header = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RangeSelector(current = range, on_select = { range = it })
                ItemDropdown(
                    options = options,
                    picked_id = picked_item_id,
                    on_pick = { picked_item_id = it }
                )

                // ✅ 지표 선택
                SingleChoiceSegmentedButtonRow {
                    ItemChartMetric.entries.forEachIndexed { idx, m ->
                        SegmentedButton(
                            selected = (metric == m),
                            onClick = { metric = m },
                            shape = SegmentedButtonDefaults.itemShape(idx, ItemChartMetric.entries.size)
                        ) { Text(m.label) }
                    }
                }
            }
        },
        chart = {
            if (picked_item_id == null) {
                ChartPlaceholder("아이템을 선택하세요")
            } else if (points.isEmpty()) {
                ChartPlaceholder("표시할 데이터가 없음")
            } else {
                InteractiveLineChartV2(
                    count = points.size,
                    labelAt = { i -> points[i].date },
                    valueAt = { i -> points[i].v.toFloat() },
                    valueTextAt = { i ->
                        "${metric.label}: ${points[i].v}"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        detail = {
            if (picked_item_id == null) {
                Text("아이템을 선택하세요", color = Color.Black)
                return@StatBaseLayout
            }
            if (rows.isEmpty()) {
                Text("표시할 데이터가 없음", color = Color.Black)
                return@StatBaseLayout
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("날짜", modifier = Modifier.weight(1.6f), color = Color.Black, maxLines = 1, softWrap = false)
                        Text("성공", modifier = Modifier.weight(1f), color = Color.Black, maxLines = 1, softWrap = false)
                        Text("실패", modifier = Modifier.weight(1f), color = Color.Black, maxLines = 1, softWrap = false)
                        Text("취소", modifier = Modifier.weight(1f), color = Color.Black, maxLines = 1, softWrap = false)
                    }
                    Divider()
                }

                items(rows) { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.date, modifier = Modifier.weight(1.6f), color = Color.Black, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                        Text(r.success.toString(), modifier = Modifier.weight(1f), color = Color.Black, maxLines = 1, softWrap = false)
                        Text(r.fail_count.toString(), modifier = Modifier.weight(1f), color = Color.Black, maxLines = 1, softWrap = false)
                        Text(r.cancel.toString(), modifier = Modifier.weight(1f), color = Color.Black, maxLines = 1, softWrap = false)
                    }
                    Divider()
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDropdown(
    options: List<com.example.dontforget.data.dao.ItemOptionRow>,
    picked_id: Long?,
    on_pick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val picked_name = remember(options, picked_id) {
        options.firstOrNull { it.item_id == picked_id }?.title ?: "아이템 선택"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = picked_name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("아이템") }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { o ->
                DropdownMenuItem(
                    text = { Text(o.title) },
                    onClick = {
                        on_pick(o.item_id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ------------------------------------------------------
// ✅ STATS_CONDITION: 틀만 고정 (나중에 항목 selector + 그래프 + 메모표 붙이면 됨)
// ------------------------------------------------------
@Composable
private fun StatsConditionScreen(vm: HistoryViewModel) {
    var range by remember { mutableStateOf(StatRange.D30) }

    val defs by vm.condition_defs.collectAsState()
    val rows by vm.condition_stat_rows.collectAsState()

    // ✅ 기본 phase START
    var phase by remember { mutableStateOf(com.example.dontforget.data.entity.ConditionPhase.START) }
    var picked_id by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        // 정의 리스트는 load_recent_dates에서 채워지지만, 혹시 통계만 먼저 들어올 수 있으니 한번 호출
        vm.load_recent_dates(days = range.days ?: 3650)
    }

    LaunchedEffect(range, picked_id, phase) {
        val days = range.days ?: 3650
        val id = picked_id
        if (id != null) vm.load_condition_stats(condition_def_id = id, days = days, phase = phase)
    }
    val points = remember(rows) {
        rows
            .sortedBy { it.date }
            .map { DateIntPoint(date = it.date, v = code_to_level(it.value_code)) }
    }
    StatBaseLayout(
        title = "컨디션 통계",
        header = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RangeSelector(current = range, on_select = { range = it })

                // phase 선택 (START/MID/END)
                SingleChoiceSegmentedButtonRow {
                    val all = listOf(
                        com.example.dontforget.data.entity.ConditionPhase.START,
                        com.example.dontforget.data.entity.ConditionPhase.MID,
                        com.example.dontforget.data.entity.ConditionPhase.END
                    )
                    all.forEachIndexed { idx, p ->
                        SegmentedButton(
                            selected = (phase == p),
                            onClick = { phase = p },
                            shape = SegmentedButtonDefaults.itemShape(idx, all.size)
                        ) {
                            Text(p.name)
                        }
                    }
                }

                // 항목 선택
                ConditionDefDropdown(
                    defs = defs,
                    picked_id = picked_id,
                    on_pick = { picked_id = it }
                )
            }
        },
        chart = {
            // rows: vm.condition_stat_rows (date, value_code, memo)
            val points = remember(rows) {
                rows
                    .sortedBy { it.date }
                    .mapNotNull { r ->
                        val score = code_to_score(r.value_code) ?: return@mapNotNull null
                        Triple(r.date, score, r.value_code)
                    }
            }

            if (picked_id == null) {
                ChartPlaceholder("컨디션 항목을 선택하세요")
            } else if (points.isEmpty()) {
                ChartPlaceholder("표시할 데이터가 없음")
            } else {
                InteractiveLineChartV2(
                    count = points.size,
                    labelAt = { i -> points[i].first },
                    valueAt = { i -> points[i].second.toFloat() },
                    valueTextAt = { i ->
                        val score = points[i].second
                        "컨디션: ${score_to_kor(score)} (${score})"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        detail = {
            if (picked_id == null) {
                Text("컨디션 항목을 선택하세요", color = Color.Black)
                return@StatBaseLayout
            }

            if (rows.isEmpty()) {
                Text("표시할 데이터가 없음", color = Color.Black)
                return@StatBaseLayout
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rows) { r ->
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.date, color = Color.Black)
                            LevelBadge(r.value_code)
                        }
                        if (r.memo.isNotBlank()) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F8))) {
                                Text(
                                    r.memo,
                                    modifier = Modifier.padding(10.dp),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionDefDropdown(
    defs: List<com.example.dontforget.data.entity.ConditionDefinitionEntity>,
    picked_id: Long?,
    on_pick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val picked_name = remember(defs, picked_id) {
        defs.firstOrNull { it.condition_def_id == picked_id }?.name ?: "컨디션 항목 선택"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = picked_name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("컨디션 항목") }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            defs.forEach { d ->
                DropdownMenuItem(
                    text = { Text(d.name) },
                    onClick = {
                        on_pick(d.condition_def_id)
                        expanded = false
                    }
                )
            }
        }
    }
}


// ------------------------------------------------------
// ✅ STATS_SUMMARY: 틀만 고정 (요약항목 selector + 그래프 + 메모표 붙이면 됨)
// ------------------------------------------------------
@Composable
private fun StatsSummaryScreen(vm: HistoryViewModel) {
    var range by remember { mutableStateOf(StatRange.D30) }

    val defs by vm.result_defs.collectAsState()
    val rows by vm.summary_stat_rows.collectAsState()

    var picked_id by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        vm.load_recent_dates(days = range.days ?: 3650)
    }

    LaunchedEffect(range, picked_id) {
        val days = range.days ?: 3650
        val id = picked_id
        if (id != null) vm.load_summary_stats(result_def_id = id, days = days)
    }

    StatBaseLayout(
        title = "요약 통계",
        header = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RangeSelector(current = range, on_select = { range = it })
                ResultDefDropdown(defs = defs, picked_id = picked_id, on_pick = { picked_id = it })
            }
        },
        chart = { val points = remember(rows) {
            rows
                .sortedBy { it.date }
                .mapNotNull { r ->
                    val score = code_to_score(r.value_code) ?: return@mapNotNull null
                    Triple(r.date, score, r.value_code)
                }
        }

            if (picked_id == null) {
                ChartPlaceholder("요약 항목을 선택하세요")
            } else if (points.isEmpty()) {
                ChartPlaceholder("표시할 데이터가 없음")
            } else {
                InteractiveLineChartV2(
                    count = points.size,
                    labelAt = { i -> points[i].first },
                    valueAt = { i -> points[i].second.toFloat() },
                    valueTextAt = { i ->
                        val score = points[i].second
                        "요약: ${score_to_kor(score)} (${score})"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } },
        detail = {
            if (picked_id == null) {
                Text("요약 항목을 선택하세요", color = Color.Black)
                return@StatBaseLayout
            }

            if (rows.isEmpty()) {
                Text("표시할 데이터가 없음", color = Color.Black)
                return@StatBaseLayout
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rows) { r ->
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.date, color = Color.Black)
                            LevelBadge(r.value_code)
                        }
                        if (r.memo.isNotBlank()) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F8))) {
                                Text(
                                    r.memo,
                                    modifier = Modifier.padding(10.dp),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultDefDropdown(
    defs: List<com.example.dontforget.data.entity.ResultDefinitionEntity>,
    picked_id: Long?,
    on_pick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val picked_name = remember(defs, picked_id) {
        defs.firstOrNull { it.result_def_id == picked_id }?.name ?: "요약 항목 선택"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = picked_name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("요약 항목") }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            defs.forEach { d ->
                DropdownMenuItem(
                    text = { Text(d.name) },
                    onClick = {
                        on_pick(d.result_def_id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DatePickPanel(
    vm: HistoryViewModel,
    step: DatePickStep,
    on_step_change: (DatePickStep) -> Unit,
    on_apply_close: () -> Unit
) {
    var range by remember { mutableStateOf(StatRange.D30) }

    val date_rows by vm.date_rows.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val selected_date by vm.selected_date.collectAsState()

    LaunchedEffect(range) {
        val days = range.days ?: 3650
        vm.load_recent_dates(days = days)
    }

    // 상단: 기간 선택
    RangeSelector(current = range, on_select = { range = it })

    Spacer(Modifier.height(8.dp))

    when (step) {
        DatePickStep.DATE_LIST -> {
            Text("날짜 목록", color = Color.Black)

            if (date_rows.isEmpty()) {
                Text("완료된 세션이 있는 날짜가 없음", color = Color.Black)
                return
            }

            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(date_rows) { r ->
                    TextButton(
                        onClick = {
                            vm.select_date(r.date)          // ✅ 세션 목록 로드
                            on_step_change(DatePickStep.SESSION_LIST)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(r.date, color = Color.Black)
                            Text("세션 ${r.session_count} · ${ms_to_min(r.total_ms)}분", color = Color.Black)
                        }
                    }
                    Divider()
                }
            }
        }

        DatePickStep.SESSION_LIST -> {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("세션 목록 (${selected_date ?: ""})", color = Color.Black)
                TextButton(onClick = {
                    vm.clear_session_detail()
                    on_step_change(DatePickStep.DATE_LIST)
                }) { Text("뒤로", color = Color.Black) }
            }

            if (sessions.isEmpty()) {
                Text("세션이 없음", color = Color.Black)
                return
            }

            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(sessions) { s ->
                    val dur = ((s.end_time ?: 0L) - s.start_time).coerceAtLeast(0L)

                    TextButton(
                        onClick = {
                            vm.select_session(s.session_id) // ✅ bundle 로드 시작
                            on_apply_close()                // ✅ 선택창 닫고 메인으로
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Text("세션ID: ${s.session_id}", color = Color.Black)
                            Text("소요: ${ms_to_min(dur)}분", color = Color.Black)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun StatsPickPanel(
    vm: HistoryViewModel,
    stats_pick: StatsPickType,
    on_pick: (StatsPickType) -> Unit
) {
    // 통계 4종 선택을 버튼/세그먼트로
    SingleChoiceSegmentedButtonRow {
        StatsPickType.entries.forEachIndexed { idx, t ->
            SegmentedButton(
                selected = (stats_pick == t),
                onClick = { on_pick(t) },
                shape = SegmentedButtonDefaults.itemShape(idx, StatsPickType.entries.size)
            ) {
                Text(
                    when (t) {
                        StatsPickType.CONDITION -> "컨디션"
                        StatsPickType.ITEM -> "아이템"
                        StatsPickType.TIME -> "연습량"
                        StatsPickType.SUMMARY -> "요약"
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    // 프리뷰
    Text("선택 후 '적용'을 누르면 메인 화면에 표시됩니다", color = Color.Black)
//    when (stats_pick) {
//        StatsPickType.CONDITION -> StatsConditionScreen(vm = vm)
//        StatsPickType.ITEM -> StatsItemScreen(vm = vm)
//        StatsPickType.TIME -> StatsTimeScreen(vm = vm)
//        StatsPickType.SUMMARY -> StatsSummaryScreen(vm = vm)
//    }
}

@Composable
private fun TitleHeader(
    title: String,
    subtitle: String? = null
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555)
            )
        }
        Spacer(Modifier.height(10.dp))
        Divider()
    }
}

@Composable
private fun SectionBlock(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
            }
            Spacer(Modifier.height(10.dp))
            Divider()
            Spacer(Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
private fun LabelGroupTitle(text: String) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF333333)
        )
        Spacer(Modifier.height(4.dp))
        Divider(thickness = 1.dp)
    }
}

@Composable
private fun ConditionGroup(
    group_title: String,
    lines: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LabelGroupTitle(group_title)
        if (lines.isEmpty()) {
            Text("없음", color = Color(0xFF666666))
        } else {
            lines.forEach { Text("• $it", color = Color.Black) }
        }
    }
}

private fun counts_text(s: Int, f: Int, c: Int): String {
    val parts = buildList {
        if (s > 0) add("성공 $s")
        if (f > 0) add("실패 $f")
        if (c > 0) add("취소 $c")
    }
    return if (parts.isEmpty()) "미선택" else parts.joinToString(" · ")
}

@Composable
private fun ResultGroup(
    title: String,
    items: List<com.example.dontforget.data.entity.RunItemEntity>
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LabelGroupTitle("$title (${items.size})")

        // ✅ 이제 아이템 줄에는 카운트 표시 안 함 (이름만)
        items
            .sortedBy { it.title }
            .forEach { ri ->
                Text("• ${ri.title}", color = Color.Black)
            }
    }
}

private data class LevelEntry(
    val name: String,
    val code: String?,
    val memo: String
)

private fun level_label(code: String?): String {
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

@Composable
private fun LevelBadge(code: String?) {
    val label = level_label(code)

    // ✅ 상태에 따라 칩 느낌만 다르게(색은 과하지 않게)
    val bg = when (code) {
        "VERY_BAD" -> Color(0xFFFFE7E7)
        "BAD" -> Color(0xFFFFF2E0)
        "NORMAL" -> Color(0xFFF0F0F0)
        "GOOD" -> Color(0xFFE9F6EA)
        "VERY_GOOD" -> Color(0xFFE3F2FF)
        else -> Color(0xFFF0F0F0)
    }

    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
@Composable
private fun LevelEntryRow(entry: LevelEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // ✅ 항목 위아래 여백
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(entry.name, color = Color.Black)
            if (!entry.code.isNullOrBlank()) {
                LevelBadge(entry.code)
            }
        }

        // ✅ 메모가 있으면 "한 단계 내려간 박스" 느낌
        if (entry.memo.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp) // 살짝 들여쓰기
            ) {
                Text(
                    text = entry.memo,
                    color = Color.Black,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
private fun LevelEntryGroup(
    group_title: String,
    entries: List<LevelEntry>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LabelGroupTitle(group_title)

        if (entries.isEmpty()) {
            Text("없음", color = Color(0xFF666666))
        } else {
            entries.forEachIndexed { idx, e ->
                LevelEntryRow(e)
                if (idx != entries.lastIndex) Divider()
            }
        }
    }
}

@Composable
private fun SummaryEntryRow(entry: LevelEntry) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.name, color = Color.Black)
                if (!entry.code.isNullOrBlank()) {
                    LevelBadge(entry.code)
                }
            }

            if (entry.memo.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = entry.memo,
                        color = Color.Black,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleLineChartByDate(
    rows: List<DateMsPoint>,
    modifier: Modifier = Modifier
) {
    if (rows.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("데이터 없음", color = Color.Gray)
        }
        return
    }

    val data = remember(rows) { rows } // 이미 정렬했으면 그대로
    val maxMs = remember(data) { data.maxOf { it.total_ms }.coerceAtLeast(1L) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val padL = 32f
        val padR = 12f
        val padT = 12f
        val padB = 24f

        val chartW = (w - padL - padR).coerceAtLeast(1f)
        val chartH = (h - padT - padB).coerceAtLeast(1f)

        fun xAt(i: Int): Float {
            if (data.size == 1) return padL
            return padL + (chartW * i / (data.size - 1).toFloat())
        }

        fun yAt(ms: Long): Float {
            val v = ms.toFloat() / maxMs.toFloat()
            return padT + chartH * (1f - v)
        }

        // 라인
        val path = androidx.compose.ui.graphics.Path()
        data.forEachIndexed { i, r ->
            val x = xAt(i)
            val y = yAt(r.total_ms)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = Color(0xFF2E6BE6),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )

        // 점
        data.forEachIndexed { i, r ->
            drawCircle(
                color = Color(0xFF2E6BE6),
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(xAt(i), yAt(r.total_ms))
            )
        }
    }
}

@Composable
private fun SimpleLineChartByDateInt(
    rows: List<DateIntPoint>,
    modifier: Modifier = Modifier
) {
    if (rows.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("데이터 없음", color = Color.Gray)
        }
        return
    }

    val data = remember(rows) { rows }
    val maxV = remember(data) { data.maxOf { it.v }.coerceAtLeast(1) }

    var picked_idx by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { tap ->
                    val padL = 32f
                    val padR = 12f

                    val w = size.width
                    val chartW = (w - padL - padR).coerceAtLeast(1f)

                    fun xAt(i: Int): Float {
                        if (data.size == 1) return padL
                        return padL + (chartW * i / (data.size - 1).toFloat())
                    }

                    // ✅ 탭한 X에 가장 가까운 포인트 index 찾기
                    var best = 0
                    var bestDist = Float.MAX_VALUE
                    for (i in data.indices) {
                        val d = abs(tap.x - xAt(i))
                        if (d < bestDist) {
                            bestDist = d
                            best = i
                        }
                    }
                    picked_idx = best
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val padL = 32f
            val padR = 12f
            val padT = 12f
            val padB = 24f

            val chartW = (w - padL - padR).coerceAtLeast(1f)
            val chartH = (h - padT - padB).coerceAtLeast(1f)

            fun xAt(i: Int): Float {
                if (data.size == 1) return padL
                return padL + (chartW * i / (data.size - 1).toFloat())
            }

            fun yAt(v: Int): Float {
                val p = v.toFloat() / maxV.toFloat()
                return padT + chartH * (1f - p)
            }

            // 라인
            val path = Path()
            data.forEachIndexed { i, r ->
                val x = xAt(i)
                val y = yAt(r.v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = Color(0xFF2E6BE6),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // 점
            data.forEachIndexed { i, r ->
                drawCircle(
                    color = Color(0xFF2E6BE6),
                    radius = 6f,
                    center = Offset(xAt(i), yAt(r.v))
                )
            }

            // ✅ 선택된 점 강조 + 세로 가이드
            val idx = picked_idx
            if (idx != null && idx in data.indices) {
                val x = xAt(idx)
                val y = yAt(data[idx].v)

                // 세로 가이드
                drawLine(
                    color = Color(0x552E6BE6),
                    start = Offset(x, padT),
                    end = Offset(x, padT + chartH),
                    strokeWidth = 3f
                )

                // 선택 점 크게
                drawCircle(
                    color = Color(0xFF2E6BE6),
                    radius = 10f,
                    center = Offset(x, y)
                )
            }
        }

        // ✅ 말풍선(오버레이) 텍스트
        val idx = picked_idx
        if (idx != null && idx in data.indices) {
            val p = data[idx]
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(p.date, color = Color.Black, style = MaterialTheme.typography.labelLarge)
                    Text("값: ${p.v}", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


private fun clampF(v: Float, lo: Float, hi: Float): Float = max(lo, min(hi, v))

@Composable
private fun InteractiveLineChartV2(
    count: Int,
    labelAt: (Int) -> String,      // 날짜 등
    valueAt: (Int) -> Float,       // y 값
    valueTextAt: (Int) -> String,  // 말풍선에 표시할 수치 문자열
    modifier: Modifier = Modifier
) {
    if (count <= 0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("데이터 없음", color = Color.Gray)
        }
        return
    }

    val maxV = remember(count) {
        (0 until count).maxOf { valueAt(it) }.coerceAtLeast(1f)
    }

    var pickedIdx by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val boxW = with(density) { maxWidth.toPx() }
        val boxH = with(density) { maxHeight.toPx() }

        val padL = 32f
        val padR = 12f
        val padT = 12f
        val padB = 24f

        val chartW = (boxW - padL - padR).coerceAtLeast(1f)
        val chartH = (boxH - padT - padB).coerceAtLeast(1f)

        fun xAt(i: Int): Float {
            if (count == 1) return padL
            return padL + (chartW * i / (count - 1).toFloat())
        }

        fun yAt(v: Float): Float {
            val p = v / maxV
            return padT + chartH * (1f - p)
        }

        fun nearestIndexByX(x: Float): Int {
            var best = 0
            var bestDist = Float.MAX_VALUE
            for (i in 0 until count) {
                val d = abs(x - xAt(i))
                if (d < bestDist) {
                    bestDist = d
                    best = i
                }
            }
            return best
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // ✅ 탭 토글
                .pointerInput(count) {
                    detectTapGestures { tap ->
                        val best = nearestIndexByX(tap.x)
                        pickedIdx = if (pickedIdx == best) null else best
                    }
                }
                // ✅ 드래그 이동
                .pointerInput(count) {
                    detectDragGestures(
                        onDragStart = { start ->
                            pickedIdx = nearestIndexByX(start.x)
                        },
                        onDrag = { change, _ ->
                            pickedIdx = nearestIndexByX(change.position.x)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 라인
                val path = androidx.compose.ui.graphics.Path()
                for (i in 0 until count) {
                    val x = xAt(i)
                    val y = yAt(valueAt(i))
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFF2E6BE6),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )

                // 점
                for (i in 0 until count) {
                    drawCircle(
                        color = Color(0xFF2E6BE6),
                        radius = 6f,
                        center = androidx.compose.ui.geometry.Offset(xAt(i), yAt(valueAt(i)))
                    )
                }

                // 선택 강조 + 세로 가이드 + 큰 점
                val idx = pickedIdx
                if (idx != null && idx in 0 until count) {
                    val x = xAt(idx)
                    val y = yAt(valueAt(idx))

                    drawLine(
                        color = Color(0x552E6BE6),
                        start = androidx.compose.ui.geometry.Offset(x, padT),
                        end = androidx.compose.ui.geometry.Offset(x, padT + chartH),
                        strokeWidth = 3f
                    )

                    drawCircle(
                        color = Color(0xFF2E6BE6),
                        radius = 10f,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }

            // ✅ 말풍선(점 근처 + clamp)
            val idx = pickedIdx
            if (idx != null && idx in 0 until count) {
                val x = xAt(idx)
                val y = yAt(valueAt(idx))

                val tooltipW = with(density) { 170.dp.toPx() }
                val tooltipH = with(density) { 60.dp.toPx() }
                val margin = with(density) { 8.dp.toPx() }

                // 기본 위치: 점 위쪽(위로 뜨게)
                var left = x - tooltipW / 2f
                var top = y - tooltipH - 10f

                // 위로 못 뜨면 아래로
                if (top < margin) top = y + 12f

                // clamp
                left = clampF(left, margin, boxW - tooltipW - margin)
                top = clampF(top, margin, boxH - tooltipH - margin)

                val leftDp = with(density) { left.toDp() }
                val topDp = with(density) { top.toDp() }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .offset(x = leftDp, y = topDp)
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text(labelAt(idx), color = Color.Black, style = MaterialTheme.typography.labelLarge)
                        Text(valueTextAt(idx), color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}




private data class DateMsPoint(
    val date: String,
    val total_ms: Long
)

private data class DateIntPoint(
    val date: String,
    val v: Int
)

private fun code_to_score(code: String?): Int? = when (code) {
    "VERY_BAD" -> 1
    "BAD" -> 2
    "NORMAL" -> 3
    "GOOD" -> 4
    "VERY_GOOD" -> 5
    null, "" -> null
    else -> null
}

private fun score_to_kor(score: Int): String = when (score) {
    1 -> "매우나쁨"
    2 -> "나쁨"
    3 -> "보통"
    4 -> "좋음"
    5 -> "매우좋음"
    else -> "-"
}

private enum class PickTab { DATE, STATS, ANALYSIS }
private enum class DatePickStep { DATE_LIST, SESSION_LIST }
private enum class StatsPickType { CONDITION, ITEM, TIME, SUMMARY }
