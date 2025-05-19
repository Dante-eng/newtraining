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
import com.example.newtraining.data.entity.DietPlan
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun DietPlanScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val dietPlans by database.dietPlanDao().getAllDietPlans().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var caloriesPer100g by remember { mutableStateOf("") }
    var proteinPer100g by remember { mutableStateOf("") }
    var fatPer100g by remember { mutableStateOf("") }
    var carbsPer100g by remember { mutableStateOf("") }

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
                text = "النظام الغذائي",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Add Component Button
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة مكون")
        }

        // Diet Plans List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dietPlans) { plan ->
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
                                text = plan.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        database.dietPlanDao().deleteDietPlan(plan)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = Color(0xFF6200EE)
                                )
                            }
                        }
                        
                        // Macronutrients info
                        Text(
                            text = "السعرات الحرارية لكل ١٠٠ جرام: ${plan.caloriesPer100g}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "البروتين لكل ١٠٠ جرام: ${plan.proteinPer100g} جرام",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "الدهون لكل ١٠٠ جرام: ${plan.fatPer100g} جرام",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "الكربوهيدرات لكل ١٠٠ جرام: ${plan.carbsPer100g} جرام",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Add Component Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                itemName = ""
                caloriesPer100g = ""
                proteinPer100g = ""
                fatPer100g = ""
                carbsPer100g = ""
            },
            title = { Text("إضافة مكون") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("اسم المكون") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = caloriesPer100g,
                        onValueChange = { caloriesPer100g = it },
                        label = { Text("السعرات الحرارية لكل ١٠٠ جرام") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = proteinPer100g,
                        onValueChange = { proteinPer100g = it },
                        label = { Text("البروتين لكل ١٠٠ جرام") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fatPer100g,
                        onValueChange = { fatPer100g = it },
                        label = { Text("الدهون لكل ١٠٠ جرام") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = carbsPer100g,
                        onValueChange = { carbsPer100g = it },
                        label = { Text("الكربوهيدرات لكل ١٠٠ جرام") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank() && caloriesPer100g.isNotBlank()) {
                            scope.launch {
                                database.dietPlanDao().insertDietPlan(
                                    DietPlan(
                                        name = itemName,
                                        caloriesPer100g = caloriesPer100g,
                                        proteinPer100g = proteinPer100g,
                                        fatPer100g = fatPer100g,
                                        carbsPer100g = carbsPer100g
                                    )
                                )
                                showDialog = false
                                itemName = ""
                                caloriesPer100g = ""
                                proteinPer100g = ""
                                fatPer100g = ""
                                carbsPer100g = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        itemName = ""
                        caloriesPer100g = ""
                        proteinPer100g = ""
                        fatPer100g = ""
                        carbsPer100g = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 