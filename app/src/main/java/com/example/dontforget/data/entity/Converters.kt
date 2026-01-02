package com.example.dontforget.data.db

import androidx.room.TypeConverter
import com.example.dontforget.data.entity.ConditionPhase
import com.example.dontforget.data.entity.InputType
import com.example.dontforget.data.entity.RunItemStatus
import com.example.dontforget.data.entity.RunStatus

class Converters {

    @TypeConverter
    fun run_status_to_string(v: RunStatus): String = v.name

    @TypeConverter
    fun string_to_run_status(v: String): RunStatus = RunStatus.valueOf(v)

    @TypeConverter
    fun run_item_status_to_string(v: RunItemStatus): String = v.name

    @TypeConverter
    fun string_to_run_item_status(v: String): RunItemStatus = RunItemStatus.valueOf(v)

    @TypeConverter
    fun condition_phase_to_string(v: ConditionPhase): String = v.name

    @TypeConverter
    fun string_to_condition_phase(v: String): ConditionPhase = ConditionPhase.valueOf(v)

    @TypeConverter
    fun input_type_to_string(v: InputType): String = v.name

    @TypeConverter
    fun string_to_input_type(v: String): InputType = InputType.valueOf(v)
}
