package com.example.dontforget.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dontforget.data.entity.CheckItemEntity
import com.example.dontforget.ui.vm.ItemsViewModel

@Composable
fun ItemsScreen(
    vm: ItemsViewModel,
    modifier: Modifier = Modifier
) {
    val active by vm.active.collectAsStateWithLifecycle()
    val mastered by vm.mastered.collectAsStateWithLifecycle()

    var expanded_item_id by remember { mutableStateOf<Long?>(null) }

    var addDialogOpen by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CheckItemEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CheckItemEntity?>(null) }

    Column(modifier.padding(16.dp)) {
        Text("Items (체크항목 관리)", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        BlackButton("항목 추가", Modifier.fillMaxWidth()) {
            addDialogOpen = true
        }

        Spacer(Modifier.height(16.dp))
        Text("ACTIVE", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(active, key = { it.item_id }) { item ->
                ItemCard(
                    item = item,
                    status = "ACTIVE",
                    expanded = expanded_item_id == item.item_id,
                    onToggle = {
                        expanded_item_id =
                            if (expanded_item_id == item.item_id) null else item.item_id
                    },
                    onComplete = { vm.to_mastered(item.item_id) },
                    onRevert = { },
                    onEdit = { editTarget = item },
                    onDelete = { deleteTarget = item },
                    onMistake = { vm.add_mistake(item.item_id) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("MASTERED", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mastered, key = { it.item_id }) { item ->
                ItemCard(
                    item = item,
                    status = "MASTERED",
                    expanded = expanded_item_id == item.item_id,
                    onToggle = {
                        expanded_item_id =
                            if (expanded_item_id == item.item_id) null else item.item_id
                    },
                    onComplete = { },
                    onRevert = { vm.revert(item.item_id) },
                    onEdit = { editTarget = item },
                    onDelete = { deleteTarget = item },
                    onMistake = { }
                )
            }
        }
    }

    if (addDialogOpen) {
        EditItemDialog(
            dialogTitle = "항목 추가",
            initialTitle = "",
            initialNote = "",
            initialConfidence = 3,
            onDismiss = { addDialogOpen = false },
            onSave = { t, n, c ->
                vm.add_item(t, n, c)
                addDialogOpen = false
            }
        )
    }

    editTarget?.let { target ->
        EditItemDialog(
            dialogTitle = "항목 수정",
            initialTitle = target.title,
            initialNote = target.note,
            initialConfidence = target.confidence,
            onDismiss = { editTarget = null },
            onSave = { t, n, c ->
                vm.update_item(target.item_id, t, n, c)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("삭제할까?") },
            text = { Text("‘${target.title}’ 항목을 삭제합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete_item(target.item_id)
                    deleteTarget = null
                }) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun ItemCard(
    item: CheckItemEntity,
    status: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onComplete: () -> Unit,
    onRevert: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMistake: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onToggle
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("신뢰도 ${item.confidence}/5", style = MaterialTheme.typography.labelSmall)
                    Text("실수 ${item.mistake_count}", style = MaterialTheme.typography.labelSmall)
                    Text("복귀 ${item.revert_count}", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (expanded) {
                if (item.note.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(item.note, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BlackButton("수정", Modifier.weight(1f), onEdit)
                    BlackButton("삭제", Modifier.weight(1f), onDelete)

                    if (status == "ACTIVE") {
                        BlackButton("완료", Modifier.weight(1f), onComplete)
                        BlackButton("실수", Modifier.weight(1f), onMistake)
                    } else {
                        BlackButton("복귀+", Modifier.weight(1f), onRevert)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditItemDialog(
    dialogTitle: String,
    initialTitle: String,
    initialNote: String,
    initialConfidence: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var note by remember { mutableStateOf(initialNote) }
    var confidenceText by remember { mutableStateOf(initialConfidence.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,

        // ✅ 팝업 배경색: 흰색
        containerColor = Color.White,

        title = {
            Text(
                text = dialogTitle,
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목", color = Color.Black) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("내용", color = Color.Black) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )

                OutlinedTextField(
                    value = confidenceText,
                    onValueChange = { confidenceText = it.filter(Char::isDigit) },
                    label = { Text("신뢰도(0~5)", color = Color.Black) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )

                Text(
                    text = "※ 신뢰도는 0~5 사이로 저장됨",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },

        confirmButton = {
            BlackButton(
                text = "저장",
                onClick = {
                    val c = confidenceText.toIntOrNull() ?: initialConfidence
                    onSave(title, note, c)
                }
            )
        },

        dismissButton = {
            BlackButton(
                text = "취소",
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun BlackButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        Text(text)
    }
}