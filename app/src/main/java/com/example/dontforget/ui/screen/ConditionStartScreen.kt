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
import com.example.dontforget.ui.vm.RunViewModel
import com.example.dontforget.ui.vm.RunViewModel.RunStep


@Composable
fun ConditionStartScreen(
    sessionId: Long?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("시작 컨디션 입력", color = Color.Black, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("세션: ${sessionId ?: "없음"}", color = Color.Black)

            Spacer(Modifier.height(16.dp))

            // 일단 입력 UI는 다음 단계에서 “동적 항목”으로 붙일 거라
            // 지금은 예시 필드 2개만 깔아두자
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("몸상태 메모") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("수면 상태 메모") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

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
                    onClick = onSave,
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
