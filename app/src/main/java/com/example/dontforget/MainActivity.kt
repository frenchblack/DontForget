package com.example.dontforget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.dontforget.data.AppDatabase
import com.example.dontforget.data.entity.CheckItemEntity
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.data.repo.CheckItemRepo
import com.example.dontforget.data.repo.ConditionDefinitionRepo
import com.example.dontforget.data.repo.RunRepo
import com.example.dontforget.ui.AppRoot
import com.example.dontforget.ui.theme.DontForgetTheme
import com.example.dontforget.ui.vm.ItemsViewModel
import com.example.dontforget.ui.vm.ItemsVmFactory
import com.example.dontforget.ui.vm.RunViewModel
import com.example.dontforget.ui.vm.RunVmFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dontforget.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        lifecycleScope.launch {
            // ✅ 체크항목 시드
            val check_item_dao = db.check_item_dao()
            if (check_item_dao.count_all() == 0) {
                check_item_dao.insert_all(seed_check_items())
            }

            // ✅ 컨디션 정의 시드 (니가 쓰는 메서드명 그대로: condision_dao())
            val condition_def_dao = db.condision_dao()
            if (condition_def_dao.count_all() == 0) {
                condition_def_dao.insert_all(seed_condition_definitions())
            }
        }

        // ✅ Items VM
        val repo = CheckItemRepo(db.check_item_dao())
        val factory = ItemsVmFactory(repo)

        // ✅ Run VM (여기서 ConditionDefinitionRepo도 같이 주입)
        val run_repo = RunRepo(
            dao = db.run_dao()
            , condition_dao = db.run_condition_dao()
        )
        val condition_def_repo = ConditionDefinitionRepo(db.condision_dao())
        val run_factory = RunVmFactory(
            repo = run_repo,
            condition_def_repo = condition_def_repo
        )

        setContent {
            DontForgetTheme {
                val items_vm: ItemsViewModel = viewModel(factory = factory)
                val run_vm: RunViewModel = viewModel(factory = run_factory)

                AppRoot(
                    items_vm = items_vm,
                    run_vm = run_vm
                )
            }
        }
    }
}

private fun seed_check_items(): List<CheckItemEntity> {
    val active = listOf(
        CheckItemEntity(
            title = "호흡 압 유지(복압/옆구리 버팀)",
            note = "숨 밀지 말고 버티는 느낌. 소리 전에 지지부터.",
            confidence = 4,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "목 힘 빼기(후두/턱 긴장 체크)",
            note = "턱·혀·목에 힘 들어가면 바로 중지하고 풀기.",
            confidence = 4,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "공명 위치(앞/위로 모으기)",
            note = "소리가 앞으로 모이는데 목은 편한지 체크.",
            confidence = 3,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "성대 접촉(새는 소리/과압 체크)",
            note = "새면 접촉 부족, 눌리면 과압. 중간 찾기.",
            confidence = 3,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "발음/혀 위치 정리",
            note = "혀 뿌리 긴장 줄이고 모음 이동 자연스럽게.",
            confidence = 3,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "미들 믹스 안정(중음 연결)",
            note = "흉성→두성 넘어갈 때 볼륨/압력 급변 방지.",
            confidence = 4,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "고음 진입(상향 접근, 밀지 않기)",
            note = "올릴 때 힘으로 ‘올리는’ 느낌 금지. 위치 이동.",
            confidence = 4,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "끝음 처리(마무리 힘 빼기)",
            note = "끝에서 힘 들어가면 목에 남음. 끝도 가볍게.",
            confidence = 3,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "리듬/박자 정확도(메트로놈 느낌)",
            note = "끌기/급해지기 체크.",
            confidence = 2,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "음정(센터 유지)",
            note = "위아래 흔들리면 호흡/공명 다시 확인.",
            confidence = 3,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "볼륨 조절(작게 해도 유지)",
            note = "작게 부를 때도 지지 유지되는지.",
            confidence = 3,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "워밍업 루틴(가볍게, 짧게)",
            note = "컨디션 나쁜 날은 강도 낮춰서 루틴만.",
            confidence = 5,
            status = "ACTIVE",
            mistake_count = 0,
            revert_count = 0
        )
    )

    val mastered = listOf(
        CheckItemEntity(
            title = "입모양 과한 변형 금지",
            note = "이건 거의 자동화됨.",
            confidence = 5,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "불필요한 호흡 과다 금지",
            note = "들이마시기 과하면 텐션 올라감.",
            confidence = 5,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "턱 앞으로 빼는 습관 교정",
            note = "거울로 체크하면 잘 됨.",
            confidence = 4,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "소리 뭉개짐(모음 흐림) 방지",
            note = "발음 정리가 됨.",
            confidence = 4,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "목에 걸리는 ‘쥐어짜기’ 금지",
            note = "예전보다 확실히 줄었음.",
            confidence = 4,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "호흡-발성 타이밍 맞추기",
            note = "호흡 먼저, 소리 나중.",
            confidence = 4,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        ),
        CheckItemEntity(
            title = "기본 자세(등/목 정렬)",
            note = "자세 잡으면 소화+두통도 같이 좋아짐.",
            confidence = 5,
            status = "MASTERED",
            mistake_count = 0,
            revert_count = 0
        )
    )

    return active + mastered
}

private fun seed_condition_definitions(): List<ConditionDefinitionEntity> {
    return listOf(
        ConditionDefinitionEntity(name = "성대상태", input_type = InputType.LEVEL_5, sort_order = 1, is_active = 1),
        ConditionDefinitionEntity(name = "호흡", input_type = InputType.LEVEL_5, sort_order = 2, is_active = 1),
        ConditionDefinitionEntity(name = "코막힘", input_type = InputType.LEVEL_5, sort_order = 3, is_active = 1),
        ConditionDefinitionEntity(name = "수면상태", input_type = InputType.LEVEL_5, sort_order = 4, is_active = 1),
        ConditionDefinitionEntity(name = "몸상태(메모)", input_type = InputType.TEXT, sort_order = 5, is_active = 1)
    )
}
