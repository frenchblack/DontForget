package com.example.dontforget.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dontforget.data.entity.CheckItemEntity
import com.example.dontforget.ui.vm.ItemsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.example.dontforget.data.entity.ConditionLevel
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.ui.vm.RunViewModel

private enum class PracticeTab { PROCESS, COMPLETE }

@Composable
fun RunItemsScreen(
    items_vm: ItemsViewModel,
    run_vm: RunViewModel,
    sessionId: Long?,
    startedAt: Long?,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val active by items_vm.active.collectAsStateWithLifecycle()
    var condition_mid_dialog_open by remember { mutableStateOf(false) }
    // ✅ completed_ids Flow를 매 리컴포지션마다 새로 만들지 않도록 고정
    val completed_ids_state = remember(sessionId) {
        if (sessionId != null) items_vm.completed_ids(sessionId) else null
    }

    val completed_ids by if (completed_ids_state != null) {
        completed_ids_state.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val list_state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var now_ms by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(startedAt) {
        if (startedAt == null) return@LaunchedEffect
        while (true) {
            now_ms = System.currentTimeMillis()
            delay(1000)
        }
    }

    fun format_hms(ms: Long): String {
        val total_sec = (ms / 1000).coerceAtLeast(0)
        val hh = total_sec / 3600
        val mm = (total_sec % 3600) / 60
        val ss = total_sec % 60
        return "%02d : %02d : %02d".format(hh, mm, ss)
    }

    val elapsed_ms = if (startedAt != null) (now_ms - startedAt) else 0L
    val elapsed_text = format_hms(elapsed_ms)

    val completed_set = remember(completed_ids) { completed_ids.toSet() }

    val process_list = remember(active, completed_set) { active.filter { it.item_id !in completed_set } }
    val complete_list = remember(active, completed_set) { active.filter { it.item_id in completed_set } }

    var tab by remember { mutableStateOf(PracticeTab.PROCESS) }
    var expanded_item_id by remember { mutableStateOf<Long?>(null) }

    // 확인창 상태
    var confirm_open by remember { mutableStateOf(false) }
    var confirm_mode by remember { mutableStateOf("") } // "COMPLETE" / "REVERT" / "FINISH"
    var confirm_item_id by remember { mutableStateOf<Long?>(null) }

    // 설정 메뉴
    var menu_open by remember { mutableStateOf(false) }

    // ✅ 체크리스트 추가 다이얼로그 상태
    var add_dialog_open by remember { mutableStateOf(false) }
    var add_title by remember { mutableStateOf("") }
    var add_note by remember { mutableStateOf("") }
    var add_confidence by remember { mutableIntStateOf(3) }

    // ✅ 탭 이동하면 리스트 상단으로
    LaunchedEffect(tab) {
        list_state.animateScrollToItem(0)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.padding(16.dp)) {

            // 헤더 + 설정버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f) // ✅ 왼쪽이 폭을 다 먹지 못하게 고정
                ) {
                    Text("체크리스트 진행", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "연습시간 : $elapsed_text",
                            color = Color.Black
                        )

                        Text(
                            text = "세션: ${sessionId ?: "없음"}",
                            color = Color.Black
                        )
                    }
                }

                Spacer(Modifier.width(8.dp)) // ✅ 살짝 간격

                Box(
                    modifier = Modifier.wrapContentSize() // ✅ 아이콘 영역 보장
                ) {
                    IconButton(
                        onClick = { menu_open = true },
                        modifier = Modifier.size(48.dp) // ✅ 시각적/터치 영역 보장
                    ) {
                        // ✅ 아이콘이 안 보이면 Text가 아니라 Icon을 쓰는 게 정석
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "menu",
                            tint = Color.Black
                        )
                    }

                    DropdownMenu(
                        expanded = menu_open,
                        onDismissRequest = { menu_open = false },
                        containerColor = Color(0xFFF2F2F2) // ✅ 연한 회색
                    ) {
                        DropdownMenuItem(
                            text = { Text("체크리스트 추가", color = Color.Black) },
                            onClick = {
                                menu_open = false
                                add_title = ""
                                add_note = ""
                                add_confidence = 3
                                add_dialog_open = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("컨디션 추가", color = Color.Black) },
                            onClick = {
                                menu_open = false
                                if (sessionId != null) {
                                    condition_mid_dialog_open = true
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("연습 종료", color = Color.Black) },
                            onClick = {
                                menu_open = false
                                confirm_mode = "FINISH"
                                confirm_item_id = null
                                confirm_open = true
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 탭 (배경 검정 / 글자 흰색)
            TabRow(
                selectedTabIndex = if (tab == PracticeTab.PROCESS) 0 else 1,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = { tab_positions ->
                    TabRowDefaults.Indicator(
                        color = Color.White,
                        modifier = Modifier.tabIndicatorOffset(
                            tab_positions[if (tab == PracticeTab.PROCESS) 0 else 1]
                        )
                    )
                }
            ) {
                Tab(
                    selected = tab == PracticeTab.PROCESS,
                    onClick = { tab = PracticeTab.PROCESS; expanded_item_id = null },
                    text = { Text("적용중 (${process_list.size})", color = Color.White) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
                Tab(
                    selected = tab == PracticeTab.COMPLETE,
                    onClick = { tab = PracticeTab.COMPLETE; expanded_item_id = null },
                    text = { Text("적용완료 (${complete_list.size})", color = Color.White) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(12.dp))

            if (sessionId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("세션이 없습니다. 뒤로 가서 다시 시작해줘.", color = Color.Gray)
                }
                return@Card
            }

            val list = if (tab == PracticeTab.PROCESS) process_list else complete_list

            LazyColumn(
                state = list_state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list, key = { it.item_id }) { item ->
                    RunItemCard(
                        item = item,
                        expanded = expanded_item_id == item.item_id,
                        onToggle = {
                            expanded_item_id =
                                if (expanded_item_id == item.item_id) null else item.item_id
                        },
                        button_text = if (tab == PracticeTab.PROCESS) "완료" else "성공취소",
                        onButton = {
                            confirm_mode = if (tab == PracticeTab.PROCESS) "COMPLETE" else "REVERT"
                            confirm_item_id = item.item_id
                            confirm_open = true
                        }
                    )
                }

                if (list.isEmpty()) {
                    item { Text("항목이 없습니다.", color = Color.Gray) }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("뒤로") }
            }
        }
    }

    // ✅ 확인창 (완료/취소/종료)
    if (confirm_open) {
        val title = when (confirm_mode) {
            "COMPLETE" -> "완료 처리할까?"
            "REVERT" -> "성공 취소할까?"
            else -> "연습 종료할까?"
        }
        val msg = when (confirm_mode) {
            "COMPLETE" -> "완료하면 COMPLETE 탭으로 이동하고 연습성공 +1 됩니다."
            "REVERT" -> "성공취소하면 PROCESS 탭으로 돌아가고 성공취소 +1 됩니다."
            else -> "지금 연습을 종료할까요?"
        }

        AlertDialog(
            onDismissRequest = { confirm_open = false },
            containerColor = Color.White,
            title = { Text(title, color = Color.Black) },
            text = { Text(msg, color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        when (confirm_mode) {
                            "COMPLETE" -> {
                                val sid = sessionId ?: return@Button
                                val id = confirm_item_id ?: 0L
                                if (id != 0L) items_vm.practice_complete(session_id = sid, item_id = id)
                                tab = PracticeTab.COMPLETE
                                expanded_item_id = null
                            }

                            "REVERT" -> {
                                val sid = sessionId ?: return@Button
                                val id = confirm_item_id ?: 0L
                                if (id != 0L) items_vm.practice_revert(session_id = sid, item_id = id)
                                tab = PracticeTab.PROCESS
                                expanded_item_id = null
                            }

                            else -> {
                                onFinish()
                            }
                        }

                        confirm_open = false
                        confirm_item_id = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("확인") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirm_open = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) { Text("취소") }
            }
        )
    }

    // ✅ 체크리스트 추가 다이얼로그
    if (add_dialog_open) {
        AlertDialog(
            onDismissRequest = { add_dialog_open = false },
            containerColor = Color.White,
            title = { Text("체크리스트 추가", color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = add_title,
                        onValueChange = { add_title = it },
                        label = { Text("제목", color = Color.Black) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black
                        )
                    )

                    OutlinedTextField(
                        value = add_note,
                        onValueChange = { add_note = it },
                        label = { Text("내용(메모)", color = Color.Black) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("신뢰도: $add_confidence", color = Color.Black)
                        Slider(
                            value = add_confidence.toFloat(),
                            onValueChange = { add_confidence = it.toInt() },
                            valueRange = 0f..5f,
                            steps = 4
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sid = sessionId ?: return@Button
                        val t = add_title.trim()
                        if (t.isNotEmpty()) {
                            items_vm.practice_add_item(
                                session_id = sid,
                                title = t,
                                note = add_note,
                                confidence = add_confidence
                            )
                            tab = PracticeTab.PROCESS
                            expanded_item_id = null
                            add_dialog_open = false

                            // ✅ 탭이 이미 PROCESS여도 상단으로 올리기
                            scope.launch { list_state.animateScrollToItem(0) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("등록") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { add_dialog_open = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) { Text("취소") }
            }
        )
    }

    if (condition_mid_dialog_open) {
        val sid = sessionId
        if (sid != null) {
            RunConditionMidDialog_NewOnly(
                run_vm = run_vm,
                sessionId = sid,
                onDismiss = { condition_mid_dialog_open = false }
            )
        }
    }

}

@Composable
private fun RunItemCard(
    item: CheckItemEntity,
    expanded: Boolean,
    onToggle: () -> Unit,
    button_text: String,
    onButton: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onToggle
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 220))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("신뢰도 ${item.confidence}/5", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                    Text("실수 ${item.mistake_count}", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                    Text("성공 ${item.practice_success_count}", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                    Text("취소 ${item.practice_revert_count}", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                }
            }

            if (expanded) {
                if (item.note.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(item.note, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onButton,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) { Text(button_text) }
                }
            }
        }
    }
}

@Composable
private fun RunConditionMidDialog_NewOnly(
    run_vm: RunViewModel,
    sessionId: Long,
    onDismiss: () -> Unit
) {
    val defs = run_vm.condition_defs.collectAsStateWithLifecycle().value

    // ✅ 매번 신규 입력(로드 없음)
    val value_code_map = remember(sessionId) { mutableStateMapOf<Long, String>() }
    val value_text_map = remember(sessionId) { mutableStateMapOf<Long, String>() }

    // ✅ LEVEL_5 기본값: 보통(처음 열었을 때만)
    LaunchedEffect(defs, sessionId) {
        if (defs.isEmpty()) return@LaunchedEffect
        val default_level = ConditionLevel.entries.firstOrNull { it.label == "보통" } ?: return@LaunchedEffect

        defs.filter { it.input_type == InputType.LEVEL_5 }.forEach { def ->
            if (value_code_map[def.condition_def_id].isNullOrBlank()) {
                value_code_map[def.condition_def_id] = default_level.code
            }
        }
    }

    var cancel_confirm_open by remember { mutableStateOf(false) }
    var save_confirm_open by remember { mutableStateOf(false) }

    val black_textfield_colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color.Black,
        errorTextColor = Color.Black,

        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black,
        disabledLabelColor = Color.Black,
        errorLabelColor = Color.Black,

        cursorColor = Color.Black,

        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        errorContainerColor = Color.White
    )

    Dialog(onDismissRequest = { /* 밖 터치로 닫기 방지 */ }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp, max = 720.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 헤더
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "연습중 컨디션 추가",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("세션: $sessionId", color = Color.Black)
                }

                Divider(color = Color(0xFFDDDDDD))

                // 리스트(스크롤)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (defs.isEmpty()) {
                        item { Text("컨디션 항목이 없습니다.", color = Color.Black) }
                    } else {
                        items(defs, key = { it.condition_def_id }) { def ->
                            Column(modifier = Modifier.fillMaxWidth()) {

                                Text(
                                    def.name,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(10.dp))

                                when (def.input_type) {
                                    InputType.LEVEL_5 -> {
                                        var expanded by remember(def.condition_def_id) { mutableStateOf(false) }

                                        val current_code = value_code_map[def.condition_def_id] ?: ""
                                        val current_level = ConditionLevel.from_code(current_code)
                                        val display = current_level?.label ?: "선택"

                                        val memo = value_text_map[def.condition_def_id] ?: ""

                                        Column(Modifier.fillMaxWidth()) {

                                            Box(modifier = Modifier.fillMaxWidth(0.38f)) {
                                                ElevatedButton(
                                                    onClick = { expanded = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.elevatedButtonColors(
                                                        containerColor = Color.White,
                                                        contentColor = Color.Black
                                                    )
                                                ) { Text(display, color = Color.Black) }

                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false },
                                                    modifier = Modifier.background(Color(0xFFE0E0E0))
                                                ) {
                                                    ConditionLevel.entries.asReversed().forEach { level ->
                                                        DropdownMenuItem(
                                                            text = { Text(level.label, color = Color.Black) },
                                                            onClick = {
                                                                value_code_map[def.condition_def_id] = level.code
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = memo,
                                                onValueChange = { value_text_map[def.condition_def_id] = it },
                                                label = { Text("추가 설명") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = black_textfield_colors,
                                                singleLine = false,
                                                minLines = 3,
                                                maxLines = 6
                                            )
                                        }
                                    }

                                    InputType.TEXT -> {
                                        val v = value_text_map[def.condition_def_id] ?: ""
                                        OutlinedTextField(
                                            value = v,
                                            onValueChange = { value_text_map[def.condition_def_id] = it },
                                            label = { Text(def.name) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = black_textfield_colors
                                        )
                                    }

                                    InputType.SCORE -> {
                                        val v = value_text_map[def.condition_def_id] ?: ""
                                        OutlinedTextField(
                                            value = v,
                                            onValueChange = { value_text_map[def.condition_def_id] = it },
                                            label = { Text(def.name) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = black_textfield_colors
                                        )
                                    }

                                    InputType.TOGGLE -> {
                                        val v = value_text_map[def.condition_def_id] ?: ""
                                        OutlinedTextField(
                                            value = v,
                                            onValueChange = { value_text_map[def.condition_def_id] = it },
                                            label = { Text("토글(임시)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = black_textfield_colors
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = Color(0xFFDDDDDD))

                // 하단 고정 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { cancel_confirm_open = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) { Text("취소") }

                    Button(
                        onClick = { save_confirm_open = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) { Text("저장") }
                }
            }
        }
    }

    // 취소 확인
    if (cancel_confirm_open) {
        AlertDialog(
            onDismissRequest = { cancel_confirm_open = false },
            containerColor = Color.White,
            title = { Text("취소할까?", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("입력을 취소하시겠습니까?\n저장하지 않고 종료됩니다.", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        cancel_confirm_open = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("확인") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { cancel_confirm_open = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("취소") }
            }
        )
    }

    // 저장 확인
    if (save_confirm_open) {
        AlertDialog(
            onDismissRequest = { save_confirm_open = false },
            containerColor = Color.White,
            title = { Text("저장할까?", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("입력한 컨디션을 저장하시겠습니까?", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        save_confirm_open = false
                        run_vm.append_condition_mid(
                            session_id = sessionId,
                            value_code_map = value_code_map.toMap(),
                            value_text_map = value_text_map.toMap(),
                            on_done = { onDismiss() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("저장") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { save_confirm_open = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("취소") }
            }
        )
    }
}

