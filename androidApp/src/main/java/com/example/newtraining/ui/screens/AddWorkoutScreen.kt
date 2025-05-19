package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.newtraining.data.entity.Workout
import kotlinx.coroutines.launch

@Composable
fun AddWorkoutScreen(navController: NavController, muscleGroupId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val workouts by database.workoutDao().getWorkoutsForMuscleGroup(muscleGroupId).collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var workoutName by remember { mutableStateOf("") }

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
                text = "التمارين",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Add Workout Button
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة تمرين")
        }

        // Workouts List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workout.name,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                scope.launch {
                                    database.workoutDao().deleteWorkout(workout)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete workout",
                                tint = Color(0xFF6200EE)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for adding new workout
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                workoutName = ""
            },
            title = { Text("إضافة تمرين", textAlign = TextAlign.Center) },
            text = {
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ادخل اسم التمرين") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (workoutName.isNotBlank()) {
                            scope.launch {
                                database.workoutDao().insertWorkout(
                                    Workout(
                                        muscleGroupId = muscleGroupId,
                                        name = workoutName
                                    )
                                )
                                showDialog = false
                                workoutName = ""
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
                        workoutName = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 