package com.akash.mynotes

import android.content.Context
import android.widget.Toast
import com.akash.mynotes.models.Note
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString

class NoteRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)

    fun saveNotes(notes: List<Note>) {
        sharedPreferences.edit().putString(
            "notes",
            Json.encodeToString(notes)
        ).apply()
    }

    fun loadNotes(): MutableList<Note> {
        var data = mutableListOf<Note>()
        try {
            val jsonString = sharedPreferences.getString("notes", null) ?: return mutableListOf()
            data = decodeFromString(string = jsonString)
        }catch (e: Exception){
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_LONG
            ).show()
        }
        return data;
    }
}
