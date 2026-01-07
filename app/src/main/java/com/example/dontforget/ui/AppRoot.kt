package com.example.dontforget.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dontforget.ui.screen.HistoryScreen
import com.example.dontforget.ui.screen.ItemsScreen
import com.example.dontforget.ui.screen.RunScreen
import com.example.dontforget.ui.screen.SettingsScreen
import com.example.dontforget.ui.vm.HistoryViewModel
import com.example.dontforget.ui.vm.ItemsViewModel
import com.example.dontforget.ui.vm.RunViewModel
import com.example.dontforget.ui.vm.TodaySummaryViewModel

private enum class Tab { RUN, ITEMS, HISTORY, SETTINGS }

@Composable
fun AppRoot(
    items_vm: ItemsViewModel,
    run_vm: RunViewModel,
    today_vm: TodaySummaryViewModel,
    history_vm: HistoryViewModel,
    analysis_vm: com.example.dontforget.ui.vm.AnalysisViewModel,
    condition_manage_vm: com.example.dontforget.ui.vm.ConditionDefManageViewModel,
    result_manage_vm: com.example.dontforget.ui.vm.ResultDefManageViewModel
) {
    var tab by remember { mutableStateOf(Tab.RUN) }

    val label_style = MaterialTheme.typography.labelLarge.copy(
        fontSize = MaterialTheme.typography.labelLarge.fontSize * 1.5
    )

    val nav_item_colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color.White,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White,
        indicatorColor = Color(0xFF202020)
    )

    Scaffold(
        containerColor = Color(0xFFF2F2F2),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF707070)) {

                NavigationBarItem(
                    selected = tab == Tab.RUN,
                    onClick = { tab = Tab.RUN },
                    label = { Text("연습", style = label_style, color = Color.White) },
                    icon = {},
                    colors = nav_item_colors
                )

                NavigationBarItem(
                    selected = tab == Tab.ITEMS,
                    onClick = { tab = Tab.ITEMS },
                    label = { Text("리스트관리", style = label_style, color = Color.White) },
                    icon = {},
                    colors = nav_item_colors
                )

                NavigationBarItem(
                    selected = tab == Tab.HISTORY,
                    onClick = { tab = Tab.HISTORY },
                    label = { Text("히스토리", style = label_style, color = Color.White) },
                    icon = {},
                    colors = nav_item_colors
                )

                NavigationBarItem(
                    selected = tab == Tab.SETTINGS,
                    onClick = { tab = Tab.SETTINGS },
                    label = { Text("설정", style = label_style, color = Color.White) },
                    icon = {},
                    colors = nav_item_colors
                )
            }
        }
    ) { padding ->
        when (tab) {
            Tab.RUN -> RunScreen(
                vm = run_vm,
                items_vm = items_vm,
                today_vm = today_vm,
                modifier = Modifier.padding(padding)
            )

            Tab.ITEMS -> ItemsScreen(
                vm = items_vm,
                modifier = Modifier.padding(padding)
            )

            Tab.HISTORY -> HistoryScreen(
                vm = history_vm,
                analysis_vm = analysis_vm,
                modifier = Modifier.padding(padding)
            )

            Tab.SETTINGS -> SettingsScreen(
                condition_vm = condition_manage_vm,
                result_vm = result_manage_vm,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun SettingsTestScreen(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("설정(테스트)", color = Color.Black, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("여기서 컨디션항목/연습결과항목 관리로 들어갈 예정", color = Color.Black)
        }
    }
}
