package com.akash.mynotes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteRepository(private val context: Context) {
    private val gson = Gson()
    private val sharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)

    fun saveNotes(notes: List<Note>) {
        val jsonString = gson.toJson(notes)
        sharedPreferences.edit().putString("notes", jsonString).apply()
    }

    fun loadNotes(): MutableList<Note> {
        val jsonString = sharedPreferences.getString("notes", null) ?: return mutableListOf()
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}
