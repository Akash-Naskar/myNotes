package com.akash.mynotes

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.akash.mynotes.models.Note
import java.util.UUID

@Composable
fun EditNoteDialog(
    anote: Note?,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var note = anote ?: Note(UUID.randomUUID(), "", "")
    var currentNoteTitle by remember { mutableStateOf(note.title) }
    var currentNoteText by remember { mutableStateOf(note.text) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(currentNoteTitle.ifEmpty { "Edit Note" }) },
        text = {
            Column {
                TextField(
                    value = currentNoteTitle,
                    onValueChange = { currentNoteTitle = it },
                    label = { Text("Title") }
                )
                TextField(
                    value = currentNoteText,
                    onValueChange = { currentNoteText = it },
                    label = { Text("Note") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(note.copy(title = currentNoteTitle, text = currentNoteText))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}