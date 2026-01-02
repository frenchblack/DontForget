package com.example.dontforget.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
fun RunItemsScreen(
    items_vm: ItemsViewModel,
    sessionId: Long?,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val active by items_vm.active.collectAsStateWithLifecycle()

    var expanded_item_id by remember { mutableStateOf<Long?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("체크리스트 진행", color = Color.Black, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("세션: ${sessionId ?: "없음"}", color = Color.Black)

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(active, key = { it.item_id }) { item ->
                    RunItemCard(
                        item = item,
                        expanded = expanded_item_id == item.item_id,
                        onToggle = {
                            expanded_item_id =
                                if (expanded_item_id == item.item_id) null else item.item_id
                        },
                        onComplete = { items_vm.to_mastered(item.item_id) },
                        onMistake = { items_vm.add_mistake(item.item_id) }
                    )
                }

                if (active.isEmpty()) {
                    item {
                        Text("ACTIVE 항목이 없습니다.", color = Color.Black)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("뒤로") }

                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) { Text("종료") }
            }
        }
    }
}

@Composable
private fun RunItemCard(
    item: CheckItemEntity,
    expanded: Boolean,
    onToggle: () -> Unit,
    onComplete: () -> Unit,
    onMistake: () -> Unit
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
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) { Text("완료") }

                    Button(
                        onClick = onMistake,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) { Text("실수") }
                }
            }
        }
    }
}
