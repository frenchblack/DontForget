package com.example.dontforget.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RunScreen(modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Text("Run (연습 실행 화면)")
        Spacer(Modifier.height(8.dp))
        Text("- 오늘 컨디션 입력")
        Text("- 체크리스트 ✅/⚠️/❌")
        Text("- 저장")
    }
}