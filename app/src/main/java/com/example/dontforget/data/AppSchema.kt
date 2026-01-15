package com.example.dontforget.data

object AppSchema {
    // ✅ Room DB schema version (AppDatabase.version 과 반드시 동일)
    const val DB_VERSION = 8

    // ✅ Export/Import schema version
    // 원칙: DB 변경이 “백업 포맷에도 영향” 있으면 같이 올리고,
    // 영향 없으면 그대로 둬도 됨.
    const val EXPORT_VERSION = DB_VERSION
}
