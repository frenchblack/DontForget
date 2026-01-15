package com.example.dontforget.data.port

import com.example.dontforget.data.entity.*

data class ExportBundle(
    val app_id: String = "dontforget",
    val export_version: Int = 1,
    val db_schema_version: Int,
    val created_at: Long,
    val counts: Map<String, Int>,
    val tables: ExportTables
)

data class ExportTables(
    val check_item: List<CheckItemEntity> = emptyList(),
    val condition_definition: List<ConditionDefinitionEntity> = emptyList(),
    val result_definition: List<ResultDefinitionEntity> = emptyList(),

    val run_session: List<RunSessionEntity> = emptyList(),
    val run_item: List<RunItemEntity> = emptyList(),
    val run_item_progress: List<RunItemProgressEntity> = emptyList(),
    val run_condition: List<RunConditionEntity> = emptyList(),
    val run_summary: List<RunSummaryEntity> = emptyList()
)

data class PreviewCounts(
    val counts: Map<String, Int>
)

data class ImportResult(
    val file_name: String?,
    val uri_text: String,
    val counts: Map<String, Int>
)
