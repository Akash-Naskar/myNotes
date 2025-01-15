package com.akash.mynotes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun saveNotes(context: Context, notes: List<Note>) {
    val sharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
    val jsonString = gson.toJson(notes)
    sharedPreferences.edit().putString("notes", jsonString).apply()
}

fun loadNotes(context: Context): MutableList<Note> {
    val sharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
    val jsonString = sharedPreferences.getString("notes", null) ?: return mutableListOf()
    val type = object : TypeToken<List<Note>>() {}.type
    return gson.fromJson(jsonString, type)
}
