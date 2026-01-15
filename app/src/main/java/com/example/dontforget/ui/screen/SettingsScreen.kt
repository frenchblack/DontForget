package com.example.dontforget.ui.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.ui.vm.ConditionDefManageViewModel
import com.example.dontforget.ui.vm.ResultDefManageViewModel

import com.example.dontforget.data.port.DataPortJson
import com.example.dontforget.data.port.ExportBundle
import com.example.dontforget.data.repo.DataPortRepo
import com.example.dontforget.ui.util.NotifyUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    data_port_repo: DataPortRepo,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var menu by remember { mutableStateOf(SettingsMenu.MENU_LIST) }

    var loading by remember { mutableStateOf(false) }
    var dialog_text by remember { mutableStateOf<String?>(null) }

    // export confirm
    var export_confirm by remember { mutableStateOf(false) }

    // import flow state
    var picked_import_uri by remember { mutableStateOf<Uri?>(null) }
    var import_first_confirm by remember { mutableStateOf(false) }
    var import_second_confirm by remember { mutableStateOf(false) }

    // parsed bundle cached
    var import_bundle_text by remember { mutableStateOf<String?>(null) }
    var import_bundle_preview by remember { mutableStateOf<String?>(null) }
    var import_bundle_obj by remember { mutableStateOf<ExportBundle?>(null) }

    // SAF launchers
    val export_launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loading = true
        scope.launch {
            try {
                val bundle = data_port_repo.export_all()
                val json = DataPortJson.to_json(bundle)
                write_text_to_uri(ctx, uri, json)

                val file_name = query_display_name(ctx, uri)
                val msg = build_result_message(
                    title = "내보내기 완료",
                    file_name = file_name,
                    uri = uri,
                    counts = bundle.counts
                )

                dialog_text = msg
                NotifyUtil.notify_text(ctx, "내보내기 완료", msg)
            } catch (e: Exception) {
                dialog_text = "내보내기 실패: ${e.message ?: e.javaClass.simpleName}"
            } finally {
                loading = false
            }
        }
    }

    val import_launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        picked_import_uri = uri
        import_first_confirm = true
    }

    // ===== 화면 라우팅 =====
    when (menu) {
        SettingsMenu.MENU_LIST -> SettingsMenuListScreen(
            on_open_condition = { menu = SettingsMenu.CONDITION },
            on_open_result = { menu = SettingsMenu.RESULT },
            on_export_click = { export_confirm = true },
            on_import_click = {
                import_launcher.launch(arrayOf("application/json", "text/plain"))
            },
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

    // ===== Dialogs =====

    if (export_confirm) {
        AlertDialog(
            onDismissRequest = { export_confirm = false },
            title = { Text("데이터 내보내기", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("현재 데이터를 내보내시겠습니까?", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    export_confirm = false
                    val suggested = suggested_backup_filename()
                    export_launcher.launch(suggested)
                }) { Text("확인", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { export_confirm = false }) { Text("취소", color = Color.Black) }
            },
            containerColor = Color.White
        )
    }

    if (import_first_confirm && picked_import_uri != null) {
        AlertDialog(
            onDismissRequest = { import_first_confirm = false },
            title = { Text("데이터 불러오기", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("해당 데이터로 덮어쓰시겠습니까?\n현재 데이터는 삭제됩니다.", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    import_first_confirm = false
                    loading = true

                    scope.launch {
                        try {
                            val uri = picked_import_uri!!
                            val text = read_text_from_uri(ctx, uri)
                            import_bundle_text = text

                            val bundle = DataPortJson.from_json(text)
                            val current_schema = com.example.dontforget.data.AppSchema.EXPORT_VERSION // 너 AppDatabase version
                            val err = data_port_repo.validate_bundle(bundle, current_schema)

                            if (err != null) {
                                dialog_text = err
                                import_bundle_obj = null
                                import_bundle_preview = null
                            } else {
                                import_bundle_obj = bundle
                                val file_name = query_display_name(ctx, uri)
                                import_bundle_preview = build_result_message(
                                    title = "불러오기 미리보기",
                                    file_name = file_name,
                                    uri = uri,
                                    counts = bundle.counts
                                )
                                import_second_confirm = true
                            }
                        } catch (e: Exception) {
                            dialog_text = "유효하지 않은 형식입니다: ${e.message ?: e.javaClass.simpleName}"
                        } finally {
                            loading = false
                        }
                    }
                }) { Text("확인", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { import_first_confirm = false }) { Text("취소", color = Color.Black) }
            },
            containerColor = Color.White
        )
    }

    if (import_second_confirm && import_bundle_obj != null && picked_import_uri != null) {
        val preview = import_bundle_preview ?: ""
        AlertDialog(
            onDismissRequest = { import_second_confirm = false },
            title = { Text("덮어쓰기 확인", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text(preview, color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    import_second_confirm = false
                    loading = true

                    scope.launch {
                        try {
                            val uri = picked_import_uri!!
                            val bundle = import_bundle_obj!!
                            data_port_repo.import_overwrite(bundle)

                            val file_name = query_display_name(ctx, uri)
                            val msg = build_result_message(
                                title = "불러오기 완료",
                                file_name = file_name,
                                uri = uri,
                                counts = bundle.counts
                            )
                            dialog_text = msg
                            NotifyUtil.notify_text(ctx, "불러오기 완료", msg)
                        } catch (e: Exception) {
                            dialog_text = "불러오기 실패: ${e.message ?: e.javaClass.simpleName}"
                        } finally {
                            loading = false
                        }
                    }
                }) { Text("덮어쓰기", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { import_second_confirm = false }) { Text("취소", color = Color.Black) }
            },
            containerColor = Color.White
        )
    }

    if (loading) {
        AlertDialog(
            onDismissRequest = { /* block */ },
            confirmButton = {},
            title = { Text("처리 중...", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(12.dp))
                    Text("잠시만 기다려주세요.", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    if (dialog_text != null) {
        AlertDialog(
            onDismissRequest = { dialog_text = null },
            title = { Text("결과", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text(dialog_text ?: "", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { dialog_text = null }) { Text("확인", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun SettingsMenuListScreen(
    on_open_condition: () -> Unit,
    on_open_result: () -> Unit,
    on_export_click: () -> Unit,
    on_import_click: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        // ✅ 데이터 카드 (니가 원한 내보내기/불러오기)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("데이터", color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Divider(color = Color(0xFFDDDDDD))
                Spacer(Modifier.height(12.dp))

                SettingRow(title = "데이터 내보내기", onClick = on_export_click)
                Spacer(Modifier.height(8.dp))
                SettingRow(title = "데이터 불러오기", onClick = on_import_click)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ✅ 기존 설정 카드
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
private fun SettingRow(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(title, color = Color.Black)
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
 *  컨디션관리
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
 *  요약관리
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
            is_create = false,
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

                if (is_create) {
                    ExposedDropdownMenuBox(
                        expanded = type_dropdown,
                        onExpandedChange = { type_dropdown = !type_dropdown }
                    ) {
                        OutlinedTextField(
                            value = input_type_label(inputType),
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
                        value = input_type_label(inputType),
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

                Row(verticalAlignment = Alignment.CenterVertically) {
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

// ====== 파일명 추천 ======
private fun suggested_backup_filename(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = fmt.format(Date())
    return "${date}_backup.json"
}

// ====== SAF I/O ======
private fun write_text_to_uri(ctx: Context, uri: Uri, text: String) {
    ctx.contentResolver.openOutputStream(uri, "wt")?.use { os ->
        os.write(text.toByteArray(Charsets.UTF_8))
        os.flush()
    } ?: throw IllegalStateException("파일을 열 수 없습니다.")
}

private fun read_text_from_uri(ctx: Context, uri: Uri): String {
    ctx.contentResolver.openInputStream(uri)?.use { ins ->
        return ins.readBytes().toString(Charsets.UTF_8)
    }
    throw IllegalStateException("파일을 열 수 없습니다.")
}

private fun query_display_name(ctx: Context, uri: Uri): String? {
    val cr = ctx.contentResolver
    val cursor = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null) ?: return null
    cursor.use {
        if (it.moveToFirst()) {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) return it.getString(idx)
        }
    }
    return null
}

private fun build_result_message(
    title: String,
    file_name: String?,
    uri: Uri,
    counts: Map<String, Int>
): String {
    val sb = StringBuilder()
    sb.appendLine(title)
    sb.appendLine("")
    sb.appendLine("파일명: ${file_name ?: "(알 수 없음)"}")
    sb.appendLine("경로: $uri")
    sb.appendLine("")
    sb.appendLine("[테이블 저장 개수]")
    counts.forEach { (k, v) ->
        sb.appendLine("- $k: $v")
    }
    return sb.toString().trim()
}
