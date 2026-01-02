package com.example.dontforget.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dontforget.ui.vm.ItemsViewModel
import com.example.dontforget.ui.vm.RunViewModel
import com.example.dontforget.ui.vm.RunViewModel.RunStep

@Composable
fun RunScreen(
    vm: RunViewModel,
    items_vm: ItemsViewModel,
    modifier: Modifier = Modifier
) {
    val in_progress by vm.in_progress.collectAsStateWithLifecycle()
    val current_session_id by vm.current_session_id.collectAsStateWithLifecycle()
    val step by vm.step.collectAsStateWithLifecycle()

    var confirm_start_open by remember { mutableStateOf(false) }
    var resume_dialog_open by remember { mutableStateOf(false) }

    val started_at by vm.current_started_at.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refresh_in_progress() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(16.dp)
    ) {
        when (step) {
            RunStep.HOME -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { confirm_start_open = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("연습 시작", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            RunStep.CONDITION_START -> {
                ConditionStartScreen(
                    vm = vm,
                    sessionId = current_session_id,
                    onSave = {
                        // ✅ save_condition_start 내부에서 RUN_ITEMS로 이동하므로 여기선 아무것도 안 함
                    },
                    onCancel = {
                        vm.go_home()
                    }
                )
            }

            RunStep.RUN_ITEMS -> {
                RunItemsScreen(
                    items_vm = items_vm,
                    sessionId = current_session_id,
                    startedAt = started_at,
                    onFinish = {
                        vm.finish_current_go_summary()
                    },
                    onBack = {
                        vm.go_condition_start()
                    }
                )
            }

            RunStep.FINISH_SUMMARY -> {
                FinishSummaryScreen(
                    items_vm = items_vm,
                    sessionId = current_session_id,
                    onGoTodaySummary = {
                        // TODO: 오늘요약 화면 만들면 여기 연결
                        vm.go_home() // 임시
                    },
                    onGoHome = {
                        // 요약에서 홈으로 빠질 때 세션 정리하고 싶으면 여기서 처리
                        vm.go_home()
                    }
                )
            }
        }
    }

    // 시작 확인 팝업
    if (confirm_start_open) {
        AlertDialog(
            onDismissRequest = { confirm_start_open = false },
            containerColor = Color.White,
            title = { Text("시작할까?", color = Color.Black) },
            text = { Text("연습을 시작하시겠습니까?", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        confirm_start_open = false
                        if (in_progress != null) resume_dialog_open = true
                        else vm.start_new()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("시작") }
            },
            dismissButton = {
                Button(
                    onClick = { confirm_start_open = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("취소") }
            }
        )
    }

    // 진행중 세션 복구 팝업
    if (resume_dialog_open) {
        val sid = in_progress?.session_id
        AlertDialog(
            onDismissRequest = { resume_dialog_open = false },
            containerColor = Color.White,
            title = { Text("진행 중인 연습이 있어", color = Color.Black) },
            text = { Text("이어서 진행할까? 아니면 새로 시작할까?", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        resume_dialog_open = false
                        if (sid != null) vm.resume_existing(sid)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("이어하기") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        resume_dialog_open = false
                        vm.start_new()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("새로하기") }
            }
        )
    }
}
