package com.example.dontforget.data.entity

enum class ConditionLevel(
    val code: String,
    val label: String
) {
    VERY_BAD("VERY_BAD", "매우나쁨"),
    BAD("BAD", "나쁨"),
    NORMAL("NORMAL", "보통"),
    GOOD("GOOD", "좋음"),
    VERY_GOOD("VERY_GOOD", "매우좋음");

    companion object {
        fun from_code(code: String?): ConditionLevel? {
            if (code.isNullOrBlank()) return null
            return entries.firstOrNull { it.code == code }
        }
    }
}
