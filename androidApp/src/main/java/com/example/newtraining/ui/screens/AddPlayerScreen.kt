package com.example.newtraining.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Player
import kotlinx.coroutines.launch
import kotlin.random.Random
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var medicalCondition by remember { mutableStateOf("") }
    var pictureUri by remember { mutableStateOf<String?>(null) }

    // Generate unique 5-digit ID
    val uniqueId = remember {
        val random = Random.nextInt(10000, 99999)
        "P$random"
    }

    // Function to copy picked image to app storage
    fun copyImageToAppStorage(context: android.content.Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "player_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = copyImageToAppStorage(context, it)
            if (path != null) {
                pictureUri = path
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Text("رجوع")
            }
            Text(
                text = "إضافة لاعب",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Input Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (pictureUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(pictureUri),
                        contentDescription = "صورة اللاعب",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة صورة",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text("اضغط لإضافة صورة", style = MaterialTheme.typography.bodySmall)

            // Player ID
            OutlinedTextField(
                value = uniqueId,
                onValueChange = { },
                label = { Text("رقم اللاعب") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("الاسم الكامل") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { char -> char.isDigit() } },
                label = { Text("العمر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it.filter { char -> char.isDigit() } },
                label = { Text("الطول (سم)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Gender Selection
            Text("الجنس", modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = isMale,
                    onClick = { isMale = true },
                    label = { Text("ذكر") }
                )
                FilterChip(
                    selected = !isMale,
                    onClick = { isMale = false },
                    label = { Text("أنثى") }
                )
            }

            OutlinedTextField(
                value = medicalCondition,
                onValueChange = { medicalCondition = it },
                label = { Text("الحالة الصحية") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        // Save Button
        Button(
            onClick = {
                val ageInt = age.toIntOrNull() ?: 0
                val heightInt = height.toIntOrNull() ?: 0
                if (fullName.isNotBlank() && ageInt > 0 && heightInt > 0) {
                    val player = Player(
                        uniqueId = uniqueId,
                        fullName = fullName,
                        age = ageInt,
                        height = heightInt,
                        gender = if (isMale) "ذكر" else "أنثى",
                        medicalCondition = medicalCondition,
                        pictureUri = pictureUri
                    )
                    scope.launch {
                        database.playerDao().insertPlayer(player)
                        navController.navigateUp()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            enabled = fullName.isNotBlank() && age.isNotBlank() && height.isNotBlank()
        ) {
            Text("حفظ")
        }
    }
} 