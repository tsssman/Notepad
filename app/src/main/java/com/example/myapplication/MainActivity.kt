package com.example.simplenotepad

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotepadScreen()
        }
    }
} // <--- КЛАСС MainActivity ЗАКРЫТ

@Composable
fun NotepadScreen() {
    val context = LocalContext.current

    // Состояние текста
    var noteText by remember { mutableStateOf("") }
    val fileName = "notes.txt"

    // --- ФУНКЦИИ ЛОГИКИ ---

    fun saveNotes() {
        try {
            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(noteText.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadNotes() {
        try {
            val inputStream = context.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            reader.close()
            inputStream.close()
            noteText = stringBuilder.toString().trimEnd('\n') // trimEnd для удаления лишнего перевода строки
        } catch (e: FileNotFoundException) {
            // Файл не найден - нормально
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ЛАЙФСИКЛ (Автозагрузка и Автосохранение) ---

    // Загружаем заметки при первом запуске Composable
    LaunchedEffect(Unit) {
        loadNotes()
    }

    // Автосохранение при сворачивании (onPause)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                saveNotes()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- ВЕРСТКА ИНТЕРФЕЙСА ---

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Поле ввода
        TextField(
            value = noteText,
            onValueChange = { newText -> noteText = newText },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            placeholder = { Text("Введите текст...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ряд кнопок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    saveNotes()
                    Toast.makeText(context, "Сохранено!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }

            Button(
                onClick = {
                    loadNotes()
                    Toast.makeText(context, "Загружено!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Загрузить")
            }

            Button(
                onClick = {
                    noteText = ""
                    Toast.makeText(context, "Очищено!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Очистить")
            }
        } // Закрывается Row

    } // Закрывается Column

} // Закрывается функция NotepadScreen