package com.akash.mynotes

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.akash.mynotes.models.Note
import com.akash.mynotes.ui.theme.MyNotesTheme
import java.util.UUID


class MainActivity : ComponentActivity() {

    private var popupView: View? = null
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private var isPopupVisible by mutableStateOf(false)
    private var currentNote: Note? by mutableStateOf(null)

    private var floatingNote: Note? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Check and request overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            ContextCompat.startActivity(this, intent, null)
        }

        val noteRepository = NoteRepository(this)
        setContent {
            MyNotesTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Notes") })
                    },
                    floatingActionButton = {
                        NotesActionButton() {
                            currentNote = null
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    MainScreen(
                        Modifier
                            .fillMaxWidth()
                            .padding(paddingValues),
                        noteRepository
                    )

                    if (currentNote == null) {
                        EditNoteDialog(
                            anote = currentNote,
                            onDismiss = { currentNote = null },
                            onSave = {
                                currentNote = it
                                var notes = noteRepository.loadNotes()
                                notes.add(currentNote!!)
                                noteRepository.saveNotes(
                                    notes
                                )
                            }
                        )
                    }
                }

//                if (isPopupVisible) {
//                    FloatingNote(
//                        activity = this,
//                        onClose = {
//                            if (floatingNote != null) {
//                                saveNote(floatingNote!!)
//                            }
//                        }
//                    )
//                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        showFloatingNote()
    }

    private fun showFloatingNote() {
        if (!Settings.canDrawOverlays(this)) {
            Log.d("floating_note_error", "Overlay permission not granted")
            Toast.makeText(this, "Overlay permission required", Toast.LENGTH_LONG).show()
            return
        }

        isPopupVisible = true
        floatingNote = currentNote
        Log.d("MainActivity", "showFloatingNote: $floatingNote")

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        popupView = ComposePopupView(this).apply {
            setContent {
                FloatingNote(this@MainActivity) {
                    if (isPopupVisible) {
                        isPopupVisible = false
                        removePopupView()
                    }
                }
            }
        }.also {
            windowManager.addView(it, params)
        }
    }

    private fun removePopupView() {
        popupView?.let {
            windowManager.removeView(it)
            popupView = null
            floatingNote?.let {
                currentNote = it
            }
        }
    }

    override fun onResume() {
        super.onResume()
        removePopupView()
        isPopupVisible = false
    }

    @Composable
    fun FloatingNote(activity: MainActivity, onClose: () -> Unit) {
        var isTransparent by remember { mutableStateOf(false) }
        var note by remember { mutableStateOf(floatingNote ?: Note(UUID.randomUUID(), "", "")) }
        var isMaximized by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (isMaximized) it.fillMaxSize() else it.wrapContentSize() }
                .background(
                    if (isSystemInDarkTheme()) Color(0xFF424242) else Color.White,
                    RoundedCornerShape(8.dp)
                )
                .alpha(if (isTransparent) 0.6f else 1f)
                .clickable { isTransparent = !isTransparent }
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Floating Note",
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontSize = 14.sp)
                    )
                    Row {
                        IconButton(onClick = { isMaximized = !isMaximized }) {
                            Icon(
                                if (isMaximized) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                "Maximize note"
                            )
                        }

                        IconButton(onClick = { onClose() }) {
                            Icon(Icons.Filled.Close, "close")
                        }
                    }
                }
                TextField(
                    value = note.title,
                    onValueChange = {
                        note = note.copy(title = it)
                        floatingNote = note
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    label = { Text("Title") },
                    textStyle = TextStyle(fontSize = 14.sp)
                )
                TextField(
                    value = note.text,
                    onValueChange = {
                        note = note.copy(text = it)
                        floatingNote = note
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    label = { Text("Note") },
                    textStyle = TextStyle(fontSize = 14.sp)
                )
            }
        }
    }
}

class ComposePopupView(context: Context) : ViewGroup(context) {
    private val composeView = ComposeView(context)

    init {
        addView(composeView)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        composeView.layout(0, 0, r - l, b - t)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(500, widthMeasureSpec)
        val height = resolveSize(500, heightMeasureSpec)
        composeView.measure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}

// MainScreen.kt for listing of notes
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    noteRepository: NoteRepository
) {
    val context = LocalContext.current
    val notes = noteRepository.loadNotes()



    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (notes.isEmpty()) {
            Text("No notes found. Add some notes.")
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes) { nnote ->
                NoteItem(
                    nnote,
                    onSave = {
                        noteRepository.saveNotes(notes)
                    },
                    onDelete = {
                        noteRepository.saveNotes(
                            notes.filter { it.title != nnote.title }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onDelete: (Note) -> Unit,
    onSave: (Note) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    note.title.take(20) + (if (note.title.length > 20) "..." else ""),
                    style = TextStyle(fontWeight = FontWeight.SemiBold)
                )
                Row {
                    IconButton(onClick = { onSave(note) }) {
                        Icon(Icons.Default.Done, "Save Note")
                    }
                    IconButton(onClick = { onDelete(note) }) {
                        Icon(Icons.Default.Delete, "Delete Note")
                    }
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Text(text = note.text)
            }
        }
    }
}






