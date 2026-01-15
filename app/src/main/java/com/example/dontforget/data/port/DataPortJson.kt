package com.example.dontforget.data.port

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object DataPortJson {
    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    fun to_json(bundle: ExportBundle): String = gson.toJson(bundle)

    fun from_json(text: String): ExportBundle =
        gson.fromJson(text, ExportBundle::class.java)
}
