package com.example.dontforget

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.dontforget.data.AppDatabase
import com.example.dontforget.data.entity.CheckItemEntity
import com.example.dontforget.data.entity.ConditionDefinitionEntity
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.data.entity.ResultDefinitionEntity
import com.example.dontforget.data.repo.CheckItemRepo
import com.example.dontforget.data.repo.ConditionDefinitionRepo
import com.example.dontforget.data.repo.HistoryRepo
import com.example.dontforget.data.repo.ResultRepo
import com.example.dontforget.data.repo.RunRepo
import com.example.dontforget.ui.AppRoot
import com.example.dontforget.ui.theme.DontForgetTheme
import com.example.dontforget.ui.vm.AnalysisVmFactory
import com.example.dontforget.ui.vm.HistoryViewModel
import com.example.dontforget.ui.vm.HistoryVmFactory
import com.example.dontforget.ui.vm.ItemsViewModel
import com.example.dontforget.ui.vm.ItemsVmFactory
import com.example.dontforget.ui.vm.RunViewModel
import com.example.dontforget.ui.vm.RunVmFactory
import com.example.dontforget.ui.vm.TodaySummaryViewModel
import com.example.dontforget.ui.vm.TodaySummaryVmFactory
import kotlinx.coroutines.launch

private const val DEV_SEED_MODE = false
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
            if(DEV_SEED_MODE)  {
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

                // ✅ 결과 정의 시드 (여기도 코루틴 안에서!)
                val result_def_dao = db.result_definition_dao()
                if (result_def_dao.count_all() == 0) {
                    result_def_dao.insert_all(seed_result_definitions())
                }
                Log.d("DF_SEED", "before seed: sessions=${db.run_dao().count_sessions()}, run_items=${db.run_dao().count_run_items()}")
                // ✅ ✅ 여기 추가 (런 더미)
                seed_run_dummy_data_if_empty(db)

                Log.d("DF_SEED", "after seed: sessions=${db.run_dao().count_sessions()}, run_items=${db.run_dao().count_run_items()}")


                val run_item_count = db.run_dao().count_run_items()
                android.util.Log.d("DF", "run_item count = $run_item_count")

                val recent = db.run_dao().get_recent_run_items()
                android.util.Log.d("DF", "recent run_item size = ${recent.size}")
                recent.forEach { android.util.Log.d("DF", "run_item: $it") }
            }
        }


        // ✅ Items VM
        val repo = CheckItemRepo(
            dao = db.check_item_dao(),
            progress_dao = db.run_item_progress_dao()
        )
        val factory = ItemsVmFactory(repo)

        // ✅ Run VM (여기서 ConditionDefinitionRepo도 같이 주입)
        val run_repo = RunRepo(
            dao = db.run_dao(),
            condition_dao = db.run_condition_dao(),
            check_item_dao = db.check_item_dao(),
            progress_dao = db.run_item_progress_dao()
        )
        val condition_def_repo = ConditionDefinitionRepo(db.condision_dao())
        val run_factory = RunVmFactory(
            repo = run_repo,
            condition_def_repo = condition_def_repo
        )

        val result_repo = ResultRepo(
            def_dao = db.result_definition_dao(),
            summary_dao = db.run_summary_dao()
        )

        val today_factory = TodaySummaryVmFactory(
            run_repo = run_repo,
            condition_def_repo = condition_def_repo,
            result_repo = result_repo
        )

        // ✅ History VM
        val history_repo = HistoryRepo(
            run_dao = db.run_dao(),
            run_condition_dao = db.run_condition_dao(),
            run_summary_dao = db.run_summary_dao(),
            condition_def_dao = db.condision_dao(),
            result_def_dao = db.result_definition_dao()
        )
        val analysis_repo = com.example.dontforget.data.repo.AnalysisRepo(db.analysisDao())

        val history_factory = HistoryVmFactory(
            repo = history_repo,
            analysis_repo = analysis_repo
        )

        val condition_manage_repo = com.example.dontforget.data.repo.ConditionDefManageRepo(db.condision_dao())
        val condition_manage_factory = com.example.dontforget.ui.vm.ConditionDefManageVmFactory(condition_manage_repo)

        val result_manage_repo = com.example.dontforget.data.repo.ResultDefManageRepo(db.result_definition_dao())
        val result_manage_factory = com.example.dontforget.ui.vm.ResultDefManageVmFactory(result_manage_repo)

        val data_port_repo = com.example.dontforget.data.repo.DataPortRepo(db)

        setContent {
            DontForgetTheme {
                val items_vm: ItemsViewModel = viewModel(factory = factory)
                val run_vm: RunViewModel = viewModel(factory = run_factory)
                val today_vm: TodaySummaryViewModel = viewModel(factory = today_factory)
                val history_vm: HistoryViewModel = viewModel(factory = history_factory)
                val analysis_vm = ViewModelProvider(
                    this,
                    AnalysisVmFactory(analysis_repo)
                )[com.example.dontforget.ui.vm.AnalysisViewModel::class.java]
                val condition_manage_vm: com.example.dontforget.ui.vm.ConditionDefManageViewModel =
                    viewModel(factory = condition_manage_factory)

                val result_manage_vm: com.example.dontforget.ui.vm.ResultDefManageViewModel =
                    viewModel(factory = result_manage_factory)

                AppRoot(
                    items_vm = items_vm,
                    run_vm = run_vm,
                    today_vm = today_vm,
                    history_vm = history_vm,
                    analysis_vm = analysis_vm,
                    condition_manage_vm = condition_manage_vm,
                    result_manage_vm = result_manage_vm,
                    data_port_repo = data_port_repo
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

private fun seed_result_definitions(): List<ResultDefinitionEntity> {
    return listOf(
        ResultDefinitionEntity(name = "호흡", input_type = InputType.LEVEL_5, sort_order = 1, is_active = 1),
        ResultDefinitionEntity(name = "톤", input_type = InputType.LEVEL_5, sort_order = 2, is_active = 1),
        ResultDefinitionEntity(name = "고음", input_type = InputType.LEVEL_5, sort_order = 3, is_active = 1),
        ResultDefinitionEntity(name = "중음", input_type = InputType.LEVEL_5, sort_order = 4, is_active = 1),
        ResultDefinitionEntity(name = "저음", input_type = InputType.LEVEL_5, sort_order = 5, is_active = 1),
        ResultDefinitionEntity(name = "음정", input_type = InputType.LEVEL_5, sort_order = 6, is_active = 1),
        ResultDefinitionEntity(name = "박자", input_type = InputType.LEVEL_5, sort_order = 7, is_active = 1),
        ResultDefinitionEntity(name = "끝음처리", input_type = InputType.LEVEL_5, sort_order = 8, is_active = 1),
        ResultDefinitionEntity(name = "만족도", input_type = InputType.LEVEL_5, sort_order = 9, is_active = 1),
        ResultDefinitionEntity(name = "메모", input_type = InputType.TEXT, sort_order = 10, is_active = 1)
    )
}
