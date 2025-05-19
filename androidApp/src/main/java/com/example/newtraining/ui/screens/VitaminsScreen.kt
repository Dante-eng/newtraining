package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Vitamin
import kotlinx.coroutines.launch

@Composable
fun VitaminsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val vitamins by database.vitaminDao().getAllVitamins().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var vitaminName by remember { mutableStateOf("") }
    var vitaminDescription by remember { mutableStateOf("") }
    var editingVitamin by remember { mutableStateOf<Vitamin?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with back button
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
                text = "الفايتمينات و المعادن",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Add Vitamin Button
        Button(
            onClick = { 
                showDialog = true
                editingVitamin = null
                vitaminName = ""
                vitaminDescription = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة فايتمين")
        }

        // Vitamins List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vitamins) { vitamin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = vitamin.name,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                // Edit Button
                                IconButton(
                                    onClick = {
                                        editingVitamin = vitamin
                                        vitaminName = vitamin.name
                                        vitaminDescription = vitamin.description
                                        showDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit vitamin",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                                // Delete Button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            database.vitaminDao().deleteVitamin(vitamin)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete vitamin",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                        }
                        if (vitamin.description.isNotBlank()) {
                            Text(
                                text = "الوصف: ${vitamin.description}",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Vitamin Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                editingVitamin = null
                vitaminName = ""
                vitaminDescription = ""
            },
            title = { Text(if (editingVitamin == null) "إضافة فايتمين" else "تعديل الفايتمين", textAlign = TextAlign.Center) },
            text = {
                Column {
                    OutlinedTextField(
                        value = vitaminName,
                        onValueChange = { vitaminName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("اسم الفايتمين") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = vitaminDescription,
                        onValueChange = { vitaminDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("الوصف") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (vitaminName.isNotBlank()) {
                            scope.launch {
                                if (editingVitamin != null) {
                                    database.vitaminDao().updateVitamin(
                                        editingVitamin!!.copy(
                                            name = vitaminName,
                                            description = vitaminDescription
                                        )
                                    )
                                } else {
                                    database.vitaminDao().insertVitamin(
                                        Vitamin(
                                            name = vitaminName,
                                            description = vitaminDescription
                                        )
                                    )
                                }
                                showDialog = false
                                editingVitamin = null
                                vitaminName = ""
                                vitaminDescription = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(if (editingVitamin == null) "إضافة" else "تعديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        editingVitamin = null
                        vitaminName = ""
                        vitaminDescription = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 