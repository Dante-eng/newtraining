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
import com.example.newtraining.data.entity.Antibiotic
import kotlinx.coroutines.launch

@Composable
fun AntibioticsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val antibiotics by database.antibioticDao().getAllAntibiotics().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var antibioticName by remember { mutableStateOf("") }
    var antibioticDescription by remember { mutableStateOf("") }
    var editingAntibiotic by remember { mutableStateOf<Antibiotic?>(null) }

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
                text = "المضادات",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Add Antibiotic Button
        Button(
            onClick = { 
                showDialog = true
                editingAntibiotic = null
                antibioticName = ""
                antibioticDescription = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة مضاد")
        }

        // Antibiotics List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(antibiotics) { antibiotic ->
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
                                text = antibiotic.name,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                // Edit Button
                                IconButton(
                                    onClick = {
                                        editingAntibiotic = antibiotic
                                        antibioticName = antibiotic.name
                                        antibioticDescription = antibiotic.description
                                        showDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit antibiotic",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                                // Delete Button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            database.antibioticDao().deleteAntibiotic(antibiotic)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete antibiotic",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                        }
                        if (antibiotic.description.isNotBlank()) {
                            Text(
                                text = "الوصف: ${antibiotic.description}",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Antibiotic Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                editingAntibiotic = null
                antibioticName = ""
                antibioticDescription = ""
            },
            title = { Text(if (editingAntibiotic == null) "إضافة مضاد" else "تعديل المضاد", textAlign = TextAlign.Center) },
            text = {
                Column {
                    OutlinedTextField(
                        value = antibioticName,
                        onValueChange = { antibioticName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("اسم المضاد") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = antibioticDescription,
                        onValueChange = { antibioticDescription = it },
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
                        if (antibioticName.isNotBlank()) {
                            scope.launch {
                                if (editingAntibiotic != null) {
                                    database.antibioticDao().updateAntibiotic(
                                        editingAntibiotic!!.copy(
                                            name = antibioticName,
                                            description = antibioticDescription
                                        )
                                    )
                                } else {
                                    database.antibioticDao().insertAntibiotic(
                                        Antibiotic(
                                            name = antibioticName,
                                            description = antibioticDescription
                                        )
                                    )
                                }
                                showDialog = false
                                editingAntibiotic = null
                                antibioticName = ""
                                antibioticDescription = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(if (editingAntibiotic == null) "إضافة" else "تعديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        editingAntibiotic = null
                        antibioticName = ""
                        antibioticDescription = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 