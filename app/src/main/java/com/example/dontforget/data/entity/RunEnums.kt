package com.example.dontforget.data.entity

enum class RunStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

enum class RunItemStatus {
    PROCESS,
    COMPLETE
}

enum class ConditionPhase {
    START,
    MID,
    END
}

enum class InputType {
    SCORE,
    TEXT,
    TOGGLE
}