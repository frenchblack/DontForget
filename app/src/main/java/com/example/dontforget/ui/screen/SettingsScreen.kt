package com.example.dontforget.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.ui.vm.ConditionDefManageViewModel
import com.example.dontforget.ui.vm.ResultDefManageViewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.background

private enum class SettingsMenu {
    MENU_LIST,
    CONDITION,
    RESULT
}

private fun input_type_label(t: InputType): String {
    return when (t) {
        InputType.LEVEL_5 -> "5단계(매우나쁨~매우좋음)"
        InputType.TEXT -> "텍스트(메모)"
        InputType.TOGGLE -> "토글(예/아니오)"
        InputType.SCORE -> "점수"
    }
}
@Composable
fun SettingsScreen(
    condition_vm: ConditionDefManageViewModel,
    result_vm: ResultDefManageViewModel,
    modifier: Modifier = Modifier
) {
    var menu by remember { mutableStateOf(SettingsMenu.MENU_LIST) }

    when (menu) {
        SettingsMenu.MENU_LIST -> SettingsMenuListScreen(
            on_open_condition = { menu = SettingsMenu.CONDITION },
            on_open_result = { menu = SettingsMenu.RESULT },
            modifier = modifier
        )

        SettingsMenu.CONDITION -> DefinitionManageScreen_Condition(
            vm = condition_vm,
            title = "컨디션관리",
            on_back = { menu = SettingsMenu.MENU_LIST },
            modifier = modifier
        )

        SettingsMenu.RESULT -> DefinitionManageScreen_Result(
            vm = result_vm,
            title = "요약관리",
            on_back = { menu = SettingsMenu.MENU_LIST },
            modifier = modifier
        )
    }
}

@Composable
private fun SettingsMenuListScreen(
    on_open_condition: () -> Unit,
    on_open_result: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 배경 회색 유지, 내부 카드 흰색
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("설정", color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Divider(color = Color(0xFFDDDDDD))
                Spacer(Modifier.height(12.dp))

                MenuRow(title = "컨디션관리", desc = "컨디션 항목 생성/삭제(비활성)", onClick = on_open_condition)
                Spacer(Modifier.height(10.dp))
                MenuRow(title = "요약관리", desc = "오늘요약 항목 생성/삭제(비활성)", onClick = on_open_result)
            }
        }
    }
}

@Composable
private fun MenuRow(
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color.Black, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(desc, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/* =========================
 *  공통 UI 조각
 * ========================= */

@Composable
private fun TopTitleBar(
    title: String,
    on_back: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "←",
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(end = 10.dp)
                .clickable { on_back() }
        )
        Text(title, color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(10.dp))
    Divider(color = Color(0xFFDDDDDD))
}

/* =========================
 *  컨디션관리(ConditionDefinitionEntity)
 * ========================= */

@Composable
private fun DefinitionManageScreen_Condition(
    vm: ConditionDefManageViewModel,
    title: String,
    on_back: () -> Unit,
    modifier: Modifier = Modifier
) {
    val list by vm.list.collectAsState()

    DefinitionManageScaffold(
        title = title,
        on_back = on_back,
        on_create = { name, inputType, isActive, sortOrder ->
            vm.create(name, inputType, isActive, sortOrder)
        },
        on_update = { id, name, isActive, sortOrder ->
            vm.update_basic(id, name, isActive, sortOrder)
        },
        on_deactivate = { id ->
            vm.deactivate(id)
        },
        rows = list.map {
            DefinitionRow(
                id = it.condition_def_id,
                name = it.name,
                inputType = it.input_type,
                isActive = it.is_active,
                sortOrder = it.sort_order
            )
        },
        modifier = modifier
    )
}

/* =========================
 *  요약관리(ResultDefinitionEntity)
 * ========================= */

@Composable
private fun DefinitionManageScreen_Result(
    vm: ResultDefManageViewModel,
    title: String,
    on_back: () -> Unit,
    modifier: Modifier = Modifier
) {
    val list by vm.list.collectAsState()

    DefinitionManageScaffold(
        title = title,
        on_back = on_back,
        on_create = { name, inputType, isActive, sortOrder ->
            vm.create(name, inputType, isActive, sortOrder)
        },
        on_update = { id, name, isActive, sortOrder ->
            vm.update_basic(id, name, isActive, sortOrder)
        },
        on_deactivate = { id ->
            vm.deactivate(id)
        },
        rows = list.map {
            DefinitionRow(
                id = it.result_def_id,
                name = it.name,
                inputType = it.input_type,
                isActive = it.is_active,
                sortOrder = it.sort_order
            )
        },
        modifier = modifier
    )
}

/* =========================
 *  공통 관리 화면(재사용)
 * ========================= */

private data class DefinitionRow(
    val id: Long,
    val name: String,
    val inputType: InputType,
    val isActive: Int,
    val sortOrder: Int
)

@Composable
private fun DefinitionManageScaffold(
    title: String,
    on_back: () -> Unit,
    on_create: (name: String, inputType: InputType, isActive: Int, sortOrder: Int) -> Unit,
    on_update: (id: Long, name: String, isActive: Int, sortOrder: Int) -> Unit,
    on_deactivate: (id: Long) -> Unit,
    rows: List<DefinitionRow>,
    modifier: Modifier = Modifier
) {
    // dialog state
    var create_open by remember { mutableStateOf(false) }
    var edit_target by remember { mutableStateOf<DefinitionRow?>(null) }
    var delete_target by remember { mutableStateOf<DefinitionRow?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TopTitleBar(title = title, on_back = on_back)
                Spacer(Modifier.height(12.dp))

                // ✅ 상단 크게 생성 버튼
                Button(
                    onClick = { create_open = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("생성", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rows, key = { it.id }) { row ->
                        DefinitionListItem(
                            row = row,
                            on_edit = { edit_target = row },
                            on_delete = { delete_target = row }
                        )
                    }
                }
            }
        }
    }

    if (create_open) {
        DefinitionEditDialog(
            title = "$title 생성",
            is_create = true,
            init_name = "",
            init_input_type = InputType.LEVEL_5,
            init_is_active = 1,
            init_sort_order = 99,
            on_dismiss = { create_open = false },
            on_confirm = { name, inputType, isActive, sortOrder ->
                on_create(name, inputType, isActive, sortOrder)
                create_open = false
            }
        )
    }

    edit_target?.let { target ->
        DefinitionEditDialog(
            title = "$title 수정",
            is_create = false, // 인풋타입 변경 불가
            init_name = target.name,
            init_input_type = target.inputType,
            init_is_active = target.isActive,
            init_sort_order = target.sortOrder,
            on_dismiss = { edit_target = null },
            on_confirm = { name, _, isActive, sortOrder ->
                on_update(target.id, name, isActive, sortOrder)
                edit_target = null
            }
        )
    }

    delete_target?.let { target ->
        AlertDialog(
            onDismissRequest = { delete_target = null },
            title = { Text("삭제 확인", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("“${target.name}” 항목을 삭제(비활성) 처리할까?", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    on_deactivate(target.id)
                    delete_target = null
                }) { Text("삭제", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { delete_target = null }) { Text("취소", color = Color.Black) }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun DefinitionListItem(
    row: DefinitionRow,
    on_edit: () -> Unit,
    on_delete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dim = row.isActive != 1
    val name_color = if (dim) Color(0xFF9E9E9E) else Color.Black
    val sub_color = if (dim) Color(0xFFB0B0B0) else Color.Black

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {

            // 상단 1줄 (이름 왼쪽 / 오른쪽: 정렬순서+사용여부)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    row.name,
                    color = name_color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "순서 : ${row.sortOrder}",
                        color = sub_color,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        if (row.isActive == 1) "✓ 사용" else "미사용",
                        color = sub_color,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (row.isActive == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(10.dp))

                    Text(
                        "타입 : ${input_type_label(row.inputType)}",
                        color = sub_color,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = on_edit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("수정", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = on_delete,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("삭제", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/* =========================
 *  생성/수정 팝업
 * ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefinitionEditDialog(
    title: String,
    is_create: Boolean,
    init_name: String,
    init_input_type: InputType,
    init_is_active: Int,
    init_sort_order: Int,
    on_dismiss: () -> Unit,
    on_confirm: (name: String, inputType: InputType, isActive: Int, sortOrder: Int) -> Unit
) {
    var name by remember { mutableStateOf(init_name) }
    var inputType by remember { mutableStateOf(init_input_type) }
    var isActive by remember { mutableStateOf(init_is_active) }
    var sortOrderText by remember { mutableStateOf(init_sort_order.toString()) }

    // input type drop
    var type_dropdown by remember { mutableStateOf(false) }
    var name_error_open by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = on_dismiss,
        title = { Text(title, color = Color.Black, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                Text("이름", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("이름 입력", color = Color(0xFF888888)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text("인풋타입", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))

                // 생성만 변경 가능 / 수정은 고정
                if (is_create) {
                    ExposedDropdownMenuBox(
                        expanded = type_dropdown,
                        onExpandedChange = { type_dropdown = !type_dropdown }
                    ) {
                        OutlinedTextField(
                            value = input_type_label(inputType), // 한글 라벨
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = type_dropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                disabledTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = type_dropdown,
                            onDismissRequest = { type_dropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            InputType.entries.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(input_type_label(t), color = Color.Black) },
                                    onClick = {
                                        inputType = t
                                        type_dropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = input_type_label(inputType), // 한글 라벨
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            cursorColor = Color.Black
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("사용여부", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isActive == 1,
                        onCheckedChange = { isActive = if (it) 1 else 0 }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text("정렬순서", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = sortOrderText,
                    onValueChange = { sortOrderText = it.filter { ch -> ch.isDigit() } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val final_name = name.trim()
                    val final_sort = sortOrderText.toIntOrNull() ?: 99

                    if (final_name.isBlank()) {
                        name_error_open = true
                        return@TextButton
                    }

                    on_confirm(final_name, inputType, isActive, final_sort)
                }
            ) {
                Text("확인", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = on_dismiss) { Text("취소", color = Color.Black) }
        },
        containerColor = Color.White
    )
    if (name_error_open) {
        AlertDialog(
            onDismissRequest = { name_error_open = false },
            title = { Text("입력 오류", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("이름은 필수야. 이름을 입력해줘.", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { name_error_open = false }) {
                    Text("확인", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }
}
