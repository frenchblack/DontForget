package com.example.dontforget.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dontforget.data.entity.ConditionLevel
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.ui.vm.RunViewModel

@Composable
fun ConditionStartScreen(
    vm: RunViewModel,
    sessionId: Long?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val defs = vm.condition_defs.collectAsStateWithLifecycle().value

    val loaded_code_map = vm.start_value_code_map.collectAsStateWithLifecycle().value
    val loaded_text_map = vm.start_value_text_map.collectAsStateWithLifecycle().value

    // ✅ 통계용 코드(드롭다운) 저장 맵: condition_def_id -> code
    val value_code_map = remember(sessionId) { mutableStateMapOf<Long, String>() }
    val value_text_map = remember(sessionId) { mutableStateMapOf<Long, String>() }

    // ✅ LEVEL_5 기본값: "보통" 자동 세팅 (처음 진입 시)
    LaunchedEffect(defs, sessionId) {
        if (defs.isEmpty()) return@LaunchedEffect

        // "보통"에 해당하는 enum을 하나 고정으로 찾기
        // (ConditionLevel에 NORMAL/OK 같은 이름이 뭔지 몰라서, label로 찾되 1회만)
        val default_level = ConditionLevel.entries.firstOrNull { it.label == "보통" } ?: return@LaunchedEffect

        defs
            .filter { it.input_type == InputType.LEVEL_5 }
            .forEach { def ->
                // 이미 저장/로드된 값이 있으면 건드리지 않음
                if (value_code_map[def.condition_def_id].isNullOrBlank()) {
                    value_code_map[def.condition_def_id] = default_level.code
                }
            }
    }

    // 세션 진입 시 DB 값 로드 (이어하기)
    LaunchedEffect(sessionId) {
        val sid = sessionId ?: return@LaunchedEffect
        vm.load_condition_start(sid)
    }

    // 로드된 값을 화면 입력용 map에 주입
    LaunchedEffect(loaded_code_map, loaded_text_map) {
        // 이미 사용자가 입력 중이면 덮어쓰지 않음
        value_code_map.clear()
        value_code_map.putAll(loaded_code_map)

        value_text_map.clear()
        value_text_map.putAll(loaded_text_map)
    }

    // ✅ TextField 글자/라벨/커서/배경을 "무조건" 검정/흰색으로 고정
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

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scroll)
        ) {
            Text(
                text = "시작 컨디션 입력",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text("세션: ${sessionId ?: "없음"}", color = Color.Black)

            Spacer(Modifier.height(16.dp))

            if (defs.isEmpty()) {
                Text("컨디션 항목이 없습니다.", color = Color.Black)
            } else {

                // ✅ 여기서부터 "항목 간 구분선 + 간격" 적용됨
                defs.forEachIndexed { index, def ->

                    // 첫 항목 제외: 위쪽 여백 + 구분선
                    if (index != 0) {
                        Spacer(Modifier.height(18.dp))
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp
                        )
                        Spacer(Modifier.height(14.dp))
                    } else {
                        // 첫 항목은 살짝만 띄우기
                        Spacer(Modifier.height(6.dp))
                    }

                    Text(
                        text = def.name,
                        color = Color.Black,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(Modifier.height(10.dp))

                    when (def.input_type) {

                        // ✅ 드롭다운(위) + 텍스트(아래 길게)
                        InputType.LEVEL_5 -> {
                            var expanded by remember(def.condition_def_id) { mutableStateOf(false) }

                            val current_code = value_code_map[def.condition_def_id] ?: ""
                            val current_level = ConditionLevel.from_code(current_code)
                            val display = current_level?.label ?: "선택"

                            val memo = value_text_map[def.condition_def_id] ?: ""

                            // 드롭다운 버튼은 "이전 0.38" 느낌 유지
                            val drop_w = 0.38f

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // ✅ 위: 드롭다운 버튼 (그림자/입체감)
                                Box(
                                    modifier = Modifier.fillMaxWidth(drop_w)
                                ) {
                                    ElevatedButton(
                                        onClick = { expanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = ButtonDefaults.elevatedButtonElevation(
                                            defaultElevation = 6.dp,
                                            pressedElevation = 2.dp,
                                            hoveredElevation = 8.dp,
                                            focusedElevation = 8.dp
                                        ),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Text(display, color = Color.Black)
                                    }

                                    // ✅ 드롭다운 메뉴 배경 회색 + 매우좋음이 위로 오게 역순
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color(0xFFE0E0E0))
                                    ) {
                                        ConditionLevel.entries
                                            .asReversed()
                                            .forEach { level ->
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

                                // ✅ 아래: 길게 입력(멀티라인)
                                OutlinedTextField(
                                    value = memo,
                                    onValueChange = { value_text_map[def.condition_def_id] = it },
                                    label = { Text("추가 설명") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                                    colors = black_textfield_colors,
                                    singleLine = false,
                                    minLines = 3,
                                    maxLines = 6
                                )
                            }
                        }

                        // ✅ TEXT는 텍스트만 (검정 고정)
                        InputType.TEXT -> {
                            val v = value_text_map[def.condition_def_id] ?: ""
                            OutlinedTextField(
                                value = v,
                                onValueChange = { value_text_map[def.condition_def_id] = it },
                                label = { Text(def.name) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                                colors = black_textfield_colors
                            )
                        }

                        // ✅ SCORE (임시: 텍스트필드) - 검정 고정
                        InputType.SCORE -> {
                            val v = value_text_map[def.condition_def_id] ?: ""
                            OutlinedTextField(
                                value = v,
                                onValueChange = { value_text_map[def.condition_def_id] = it },
                                label = { Text(def.name) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                                colors = black_textfield_colors
                            )
                        }

                        // ✅ TOGGLE (임시: 텍스트필드) - 검정 고정
                        InputType.TOGGLE -> {
                            val v = value_text_map[def.condition_def_id] ?: ""
                            OutlinedTextField(
                                value = v,
                                onValueChange = { value_text_map[def.condition_def_id] = it },
                                label = { Text("토글(임시)") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                                colors = black_textfield_colors
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                        vm.save_condition_start(
                            session_id = sid,
                            value_code_map = value_code_map.toMap(),
                            value_text_map = value_text_map.toMap(),
                            on_done = { onSave() }
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
