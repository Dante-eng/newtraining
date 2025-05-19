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
import com.example.newtraining.data.entity.Hormone
import kotlinx.coroutines.launch

@Composable
fun HormonesListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val hormones by database.hormoneDao().getAllHormones().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var hormoneName by remember { mutableStateOf("") }
    var hormoneDescription by remember { mutableStateOf("") }
    var editingHormone by remember { mutableStateOf<Hormone?>(null) }

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
                text = "الهرمون",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Add Hormone Button
        Button(
            onClick = { 
                showDialog = true
                editingHormone = null
                hormoneName = ""
                hormoneDescription = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة هرمون")
        }

        // Hormones List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(hormones) { hormone ->
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
                                text = hormone.name,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                // Edit Button
                                IconButton(
                                    onClick = {
                                        editingHormone = hormone
                                        hormoneName = hormone.name
                                        hormoneDescription = hormone.description
                                        showDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit hormone",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                                // Delete Button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            database.hormoneDao().deleteHormone(hormone)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete hormone",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                        }
                        if (hormone.description.isNotBlank()) {
                            Text(
                                text = "الوصف: ${hormone.description}",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Hormone Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                editingHormone = null
                hormoneName = ""
                hormoneDescription = ""
            },
            title = { Text(if (editingHormone == null) "إضافة هرمون" else "تعديل الهرمون", textAlign = TextAlign.Center) },
            text = {
                Column {
                    OutlinedTextField(
                        value = hormoneName,
                        onValueChange = { hormoneName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("اسم الهرمون") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hormoneDescription,
                        onValueChange = { hormoneDescription = it },
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
                        if (hormoneName.isNotBlank()) {
                            scope.launch {
                                if (editingHormone != null) {
                                    database.hormoneDao().updateHormone(
                                        editingHormone!!.copy(
                                            name = hormoneName,
                                            description = hormoneDescription
                                        )
                                    )
                                } else {
                                    database.hormoneDao().insertHormone(
                                        Hormone(
                                            name = hormoneName,
                                            description = hormoneDescription
                                        )
                                    )
                                }
                                showDialog = false
                                editingHormone = null
                                hormoneName = ""
                                hormoneDescription = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(if (editingHormone == null) "إضافة" else "تعديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        editingHormone = null
                        hormoneName = ""
                        hormoneDescription = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 