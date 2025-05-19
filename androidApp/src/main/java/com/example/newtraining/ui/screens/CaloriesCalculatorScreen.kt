package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesCalculatorScreen(navController: NavController) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var selectedActivityLevel by remember { mutableStateOf(0) }
    var isActivityDropdownExpanded by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    val activityLevels = listOf(
        "قليل النشاط (لا تمارين)" to 1.2,
        "خفيف النشاط (1-3 أيام في الأسبوع)" to 1.375,
        "متوسط النشاط (3-5 أيام في الأسبوع)" to 1.55,
        "نشط جداً (6-7 أيام في الأسبوع)" to 1.725,
        "نشاط مكثف (تمارين يومية + عمل بدني)" to 1.9
    )

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
                text = "حساب السعرات",
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("الوزن (كجم)") },
                singleLine = true
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("الطول (سم)") },
                singleLine = true
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("العمر") },
                singleLine = true
            )

            // Gender Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
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

            // Activity Level Dropdown
            ExposedDropdownMenuBox(
                expanded = isActivityDropdownExpanded,
                onExpandedChange = { isActivityDropdownExpanded = !isActivityDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = activityLevels[selectedActivityLevel].first,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("مستوى النشاط") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isActivityDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isActivityDropdownExpanded,
                    onDismissRequest = { isActivityDropdownExpanded = false }
                ) {
                    activityLevels.forEachIndexed { index, (label, _) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedActivityLevel = index
                                isActivityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val weightValue = weight.toDoubleOrNull() ?: 0.0
                    val heightValue = height.toDoubleOrNull() ?: 0.0
                    val ageValue = age.toIntOrNull() ?: 0
                    val activityMultiplier = activityLevels[selectedActivityLevel].second

                    // Harris-Benedict equation
                    val bmr = if (isMale) {
                        66 + (13.7 * weightValue) + (5 * heightValue) - (6.8 * ageValue)
                    } else {
                        655 + (9.6 * weightValue) + (1.8 * heightValue) - (4.7 * ageValue)
                    }
                    
                    // Adjust for activity level
                    val calories = bmr * activityMultiplier
                    
                    result = """
                        معدل الأيض الأساسي (BMR): ${bmr.toInt()} سعرة حرارية
                        مستوى النشاط: ${activityLevels[selectedActivityLevel].first}
                        السعرات الحرارية اليومية المطلوبة: ${calories.toInt()} سعرة حرارية
                    """.trimIndent()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("حساب")
            }

            if (result.isNotEmpty()) {
                Text(
                    text = result,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
} 