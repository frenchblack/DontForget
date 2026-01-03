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
import com.example.dontforget.ui.vm.RunViewModel

@Composable
fun FinishSummaryScreen(
    run_vm: RunViewModel,
    items_vm: ItemsViewModel,
    sessionId: Long?,
    onGoTodaySummary: () -> Unit,
    onGoHome: () -> Unit
) {
    val active by items_vm.active.collectAsStateWithLifecycle()

    val completed_ids_state = remember(sessionId) {
        if (sessionId != null) items_vm.completed_ids(sessionId) else null
    }

    val completed_ids by if (completed_ids_state != null) {
        completed_ids_state.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val completed_set = remember(completed_ids) { completed_ids.toSet() }

    val success_list = remember(active, completed_set) { active.filter { it.item_id in completed_set } }
    val fail_list = remember(active, completed_set) { active.filter { it.item_id !in completed_set } }

    // ✅ 이 세션에서 "실패로 체크(1회)" 한 항목 id들
    val failed_mark_set = remember(sessionId) { mutableStateOf(setOf<Long>()) }

    // dialog state
    var confirm_open by remember { mutableStateOf(false) }
    var confirm_mode by remember { mutableStateOf("") } // "CANCEL_SUCCESS" / "ADD_FAIL" / "CANCEL_FAIL"
    var target_item by remember { mutableStateOf<CheckItemEntity?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("연습 종료 요약", color = Color.Black, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text("세션: ${sessionId ?: "없음"}", color = Color.Black)

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("✅ 오늘 연습 성공 (${success_list.size})", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                items(success_list, key = { it.item_id }) { item ->
                    SummaryRow(
                        title = item.title,
                        rightText = "성공 ${item.practice_success_count} / 취소 ${item.practice_revert_count}",
                        buttonText = "취소",
                        onButton = {
                            confirm_mode = "CANCEL_SUCCESS"
                            target_item = item
                            confirm_open = true
                        }
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Text("❌ 성공 못한 항목 (${fail_list.size})", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                items(fail_list, key = { it.item_id }) { item ->
                    val is_marked_failed = failed_mark_set.value.contains(item.item_id)

                    SummaryRow(
                        title = item.title,
                        rightText = "실패 ${item.practice_fail_count}",
                        buttonText = if (!is_marked_failed) "실패" else "취소",
                        onButton = {
                            confirm_mode = if (!is_marked_failed) "ADD_FAIL" else "CANCEL_FAIL"
                            target_item = item
                            confirm_open = true
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGoTodaySummary,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) { Text("오늘요약") }

                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("홈") }
            }
        }
    }

    if (confirm_open) {
        val item = target_item

        val title = when (confirm_mode) {
            "CANCEL_SUCCESS" -> "연습성공을 취소할까?"
            "ADD_FAIL" -> "실패로 기록할까?"
            else -> "실패 기록을 취소할까?"
        }

        val msg = when (confirm_mode) {
            "CANCEL_SUCCESS" -> "연습성공을 취소하시겠습니까? (성공취소 +1)\n취소하면 성공 못한 항목으로 이동합니다."
            "ADD_FAIL" -> "연습실패 항목으로 체크하시겠습니까? (실패 +1)\n한 번만 체크할 수 있습니다."
            else -> "실패 체크를 취소하시겠습니까? (실패 -1)\n다시 실패 버튼이 활성화됩니다."
        }

        AlertDialog(
            onDismissRequest = { confirm_open = false },
            containerColor = Color.White,
            title = { Text(title, color = Color.Black) },
            text = { Text(msg, color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        if (item != null && sessionId != null) {
                            when (confirm_mode) {
                                "CANCEL_SUCCESS" -> {
                                    // ✅ 성공취소 +1 & COMPLETE 해제 -> 자동으로 fail_list로 내려감
                                    items_vm.practice_revert(session_id = sessionId, item_id = item.item_id)
                                    run_vm.mark_cancel(sessionId, item.item_id)
                                }

                                "ADD_FAIL" -> {
                                    // ✅ 실패 +1, 그리고 이 세션에서는 더 못 누르게 마킹
                                    items_vm.add_practice_fail(item.item_id)
                                    run_vm.mark_fail(sessionId, item.item_id)
                                    failed_mark_set.value = failed_mark_set.value + item.item_id
                                }

                                "CANCEL_FAIL" -> {
                                    // ✅ 실패 -1, 다시 실패 버튼 활성화
                                    items_vm.sub_practice_fail(item.item_id)
                                    run_vm.clear_one(sessionId, item.item_id)
                                    failed_mark_set.value = failed_mark_set.value - item.item_id
                                }
                            }
                        }

                        confirm_open = false
                        target_item = null
                        confirm_mode = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) { Text("확인") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        confirm_open = false
                        target_item = null
                        confirm_mode = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("취소") }
            }
        )
    }
}

@Composable
private fun SummaryRow(
    title: String,
    rightText: String,
    buttonText: String,
    onButton: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.Black, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(rightText, color = Color.Black, style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = onButton,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) { Text(buttonText) }
        }
    }
}
