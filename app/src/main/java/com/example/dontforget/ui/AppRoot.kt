package com.example.dontforget.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.dontforget.ui.screen.ItemsScreen
import com.example.dontforget.ui.screen.RunScreen
import com.example.dontforget.ui.vm.ItemsViewModel

private enum class Tab { RUN, ITEMS }

@Composable
fun AppRoot(itemsVm: ItemsViewModel) {
    var tab by remember { mutableStateOf(Tab.RUN) }

    Scaffold(
        containerColor = Color(0xFFF2F2F2), // 밝은 회색
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF707070)
            ) {
                NavigationBarItem(
                    selected = tab == Tab.RUN,
                    onClick = { tab = Tab.RUN },
                    label = {
                        Text("Run", color = Color.White)
                    },
                    icon = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color(0xFF202020) // 선택된 탭 배경 (조금 더 진하게)
                    )
                )

                NavigationBarItem(
                    selected = tab == Tab.ITEMS,
                    onClick = { tab = Tab.ITEMS },
                    label = {
                        Text("Items", color = Color.White)
                    },
                    icon = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color(0xFF202020)
                    )
                )
            }
        }
    ) { padding ->
        when (tab) {
            Tab.RUN -> RunScreen(modifier = Modifier.padding(padding))
            Tab.ITEMS -> ItemsScreen(vm = itemsVm, modifier = Modifier.padding(padding))
        }
    }
}