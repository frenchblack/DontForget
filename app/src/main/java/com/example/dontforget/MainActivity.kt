package com.example.dontforget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dontforget.data.AppDatabase
import com.example.dontforget.data.repo.CheckItemRepo
import com.example.dontforget.ui.AppRoot
import com.example.dontforget.ui.theme.DontForgetTheme
import com.example.dontforget.ui.vm.ItemsViewModel
import com.example.dontforget.ui.vm.ItemsVmFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE check_item ADD COLUMN mistake_count INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE check_item ADD COLUMN revert_count INTEGER NOT NULL DEFAULT 0")
            }
        }
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dontforget.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

        val repo = CheckItemRepo(db.check_item_dao())
        val factory = ItemsVmFactory(repo)

        setContent {
            DontForgetTheme {
                val itemsVm: ItemsViewModel = viewModel(factory = factory)
                AppRoot(itemsVm = itemsVm)
            }
        }
    }
}

//@Composable
//fun AppRoot() {
//    Text(
//        text = "Vocal Checklist App",
//        modifier = Modifier.padding(24.dp)
//    )
//}

//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    DontForgetTheme {
//        Greeting("Android")
//    }
//}