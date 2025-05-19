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
import com.example.newtraining.data.entity.Supplement
import kotlinx.coroutines.launch

@Composable
fun SupplementsListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val supplements by database.supplementDao().getAllSupplements().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var supplementName by remember { mutableStateOf("") }
    var supplementDescription by remember { mutableStateOf("") }
    var editingSupplement by remember { mutableStateOf<Supplement?>(null) }

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
                text = "المكملات",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Add Supplement Button
        Button(
            onClick = { 
                showDialog = true
                editingSupplement = null
                supplementName = ""
                supplementDescription = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة مكمل")
        }

        // Supplements List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(supplements) { supplement ->
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
                                text = supplement.name,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                // Edit Button
                                IconButton(
                                    onClick = {
                                        editingSupplement = supplement
                                        supplementName = supplement.name
                                        supplementDescription = supplement.description
                                        showDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit supplement",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                                // Delete Button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            database.supplementDao().deleteSupplement(supplement)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete supplement",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                        }
                        if (supplement.description.isNotBlank()) {
                            Text(
                                text = "الوصف: ${supplement.description}",
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Supplement Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                editingSupplement = null
                supplementName = ""
                supplementDescription = ""
            },
            title = { Text(if (editingSupplement == null) "إضافة مكمل" else "تعديل المكمل", textAlign = TextAlign.Center) },
            text = {
                Column {
                    OutlinedTextField(
                        value = supplementName,
                        onValueChange = { supplementName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("اسم المكمل") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = supplementDescription,
                        onValueChange = { supplementDescription = it },
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
                        if (supplementName.isNotBlank()) {
                            scope.launch {
                                if (editingSupplement != null) {
                                    database.supplementDao().updateSupplement(
                                        editingSupplement!!.copy(
                                            name = supplementName,
                                            description = supplementDescription
                                        )
                                    )
                                } else {
                                    database.supplementDao().insertSupplement(
                                        Supplement(
                                            name = supplementName,
                                            description = supplementDescription
                                        )
                                    )
                                }
                                showDialog = false
                                editingSupplement = null
                                supplementName = ""
                                supplementDescription = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(if (editingSupplement == null) "إضافة" else "تعديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        editingSupplement = null
                        supplementName = ""
                        supplementDescription = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 