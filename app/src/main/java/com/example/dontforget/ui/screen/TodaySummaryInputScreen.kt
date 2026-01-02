package com.example.dontforget.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dontforget.data.entity.ConditionLevel
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.ui.vm.TodaySummaryViewModel

@Composable
fun TodaySummaryInputScreen(
    vm: TodaySummaryViewModel,
    sessionId: Long?,
    onCancel: () -> Unit,
    onSaveDone: () -> Unit
) {
    val cdefs = vm.condition_defs.collectAsStateWithLifecycle().value
    val rdefs = vm.result_defs.collectAsStateWithLifecycle().value

    val loaded_end_code = vm.end_value_code_map.collectAsStateWithLifecycle().value
    val loaded_end_text = vm.end_value_text_map.collectAsStateWithLifecycle().value
    val loaded_r_code = vm.result_value_code_map.collectAsStateWithLifecycle().value
    val loaded_r_text = vm.result_value_text_map.collectAsStateWithLifecycle().value

    val end_code_map = remember(sessionId) { mutableStateMapOf<Long, String>() }
    val end_text_map = remember(sessionId) { mutableStateMapOf<Long, String>() }
    val r_code_map = remember(sessionId) { mutableStateMapOf<Long, String>() }
    val r_text_map = remember(sessionId) { mutableStateMapOf<Long, String>() }

    LaunchedEffect(sessionId) {
        val sid = sessionId ?: return@LaunchedEffect
        vm.load_end_condition(sid)
        vm.load_result_summary(sid)
    }

    // 로드값 주입
    LaunchedEffect(loaded_end_code, loaded_end_text) {
        end_code_map.clear(); end_code_map.putAll(loaded_end_code)
        end_text_map.clear(); end_text_map.putAll(loaded_end_text)
    }
    LaunchedEffect(loaded_r_code, loaded_r_text) {
        r_code_map.clear(); r_code_map.putAll(loaded_r_code)
        r_text_map.clear(); r_text_map.putAll(loaded_r_text)
    }

    // ✅ 드롭다운 기본값: "좋음"
    // - 이미 값 있으면 덮어쓰지 않음
    LaunchedEffect(cdefs, rdefs, sessionId) {
        if (sessionId == null) return@LaunchedEffect

        val default_level = ConditionLevel.entries.firstOrNull { it.label == "좋음" }
            ?: return@LaunchedEffect

        cdefs
            .filter { it.input_type == InputType.LEVEL_5 }
            .forEach { def ->
                if (end_code_map[def.condition_def_id].isNullOrBlank()) {
                    end_code_map[def.condition_def_id] = default_level.code
                }
            }

        rdefs
            .filter { it.input_type == InputType.LEVEL_5 }
            .forEach { def ->
                if (r_code_map[def.result_def_id].isNullOrBlank()) {
                    r_code_map[def.result_def_id] = default_level.code
                }
            }
    }

    // TextField 색 고정 (검정/흰색)
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

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 상단 제목 카드
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("오늘정리", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("세션: ${sessionId ?: "없음"}", color = Color.Black)
            }
        }

        if (sessionId == null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("세션이 없습니다.", color = Color.Black)
                }
            }
            return@Column
        }

        // =======================
        // 1) 종료 후 컨디션 체크 (카드 분리)
        // =======================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("종료 후 컨디션 체크", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                if (cdefs.isEmpty()) {
                    Text("컨디션 항목이 없습니다.", color = Color.Black)
                } else {
                    cdefs.forEachIndexed { idx, def ->
                        if (idx != 0) {
                            Spacer(Modifier.height(14.dp))
                            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                            Spacer(Modifier.height(12.dp))
                        }

                        Text(
                            def.name,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        when (def.input_type) {
                            InputType.LEVEL_5 -> {
                                var expanded by remember(def.condition_def_id) { mutableStateOf(false) }

                                val current_code = end_code_map[def.condition_def_id] ?: ""
                                val current_level = ConditionLevel.from_code(current_code)
                                val display = current_level?.label ?: "선택"
                                val memo = end_text_map[def.condition_def_id] ?: ""

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
                                                        end_code_map[def.condition_def_id] = level.code
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = memo,
                                        onValueChange = { end_text_map[def.condition_def_id] = it },
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
                                val v = end_text_map[def.condition_def_id] ?: ""
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { end_text_map[def.condition_def_id] = it },
                                    label = { Text(def.name) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = black_textfield_colors
                                )
                            }

                            else -> {
                                val v = end_text_map[def.condition_def_id] ?: ""
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { end_text_map[def.condition_def_id] = it },
                                    label = { Text(def.name) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = black_textfield_colors
                                )
                            }
                        }
                    }
                }
            }
        }

        // =======================
        // 2) 오늘 연습 결과 본인 체크 (카드 분리)
        // =======================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("오늘 연습 결과 본인 체크", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                if (rdefs.isEmpty()) {
                    Text("오늘정리 항목이 없습니다.", color = Color.Black)
                } else {
                    rdefs.forEachIndexed { idx, def ->
                        if (idx != 0) {
                            Spacer(Modifier.height(14.dp))
                            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                            Spacer(Modifier.height(12.dp))
                        }

                        Text(
                            def.name,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        when (def.input_type) {
                            InputType.LEVEL_5 -> {
                                var expanded by remember(def.result_def_id) { mutableStateOf(false) }

                                val current_code = r_code_map[def.result_def_id] ?: ""
                                val current_level = ConditionLevel.from_code(current_code)
                                val display = current_level?.label ?: "선택"
                                val memo = r_text_map[def.result_def_id] ?: ""

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
                                                        r_code_map[def.result_def_id] = level.code
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = memo,
                                        onValueChange = { r_text_map[def.result_def_id] = it },
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
                                val v = r_text_map[def.result_def_id] ?: ""
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { r_text_map[def.result_def_id] = it },
                                    label = { Text(def.name) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = black_textfield_colors,
                                    minLines = 3,
                                    maxLines = 6
                                )
                            }

                            else -> {
                                val v = r_text_map[def.result_def_id] ?: ""
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { r_text_map[def.result_def_id] = it },
                                    label = { Text(def.name) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = black_textfield_colors
                                )
                            }
                        }
                    }
                }
            }
        }

        // 하단 버튼 카드
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("취소") }

                Button(
                    onClick = {
                        val sid = sessionId ?: return@Button
                        vm.save_all(
                            session_id = sid,
                            end_code_map = end_code_map.toMap(),
                            end_text_map = end_text_map.toMap(),
                            result_code_map = r_code_map.toMap(),
                            result_text_map = r_text_map.toMap(),
                            on_done = onSaveDone
                        )
                    },
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
