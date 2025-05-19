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
import com.example.newtraining.data.entity.MuscleGroup
import com.example.newtraining.data.entity.Workout
import com.example.newtraining.data.entity.WorkoutSet
import kotlinx.coroutines.launch

@Composable
fun WorkoutsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val muscleGroups by database.muscleGroupDao().getAllMuscleGroups().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var muscleGroupName by remember { mutableStateOf("") }
    var editingGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    
    // Track expanded state for each muscle group
    val expandedGroups = remember { mutableStateMapOf<Int, Boolean>() }
    
    // Track workout dialog state
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var workoutName by remember { mutableStateOf("") }
    var selectedMuscleGroupId by remember { mutableStateOf<Int?>(null) }
    var editingWorkout by remember { mutableStateOf<Workout?>(null) }

    // Add these state variables
    var showSetsDialog by remember { mutableStateOf(false) }
    var setName by remember { mutableStateOf("") }
    var setValues by remember { mutableStateOf("") }
    val sets by database.setDao().getAllSets().collectAsState(initial = emptyList())

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

        // Add Muscle Group Button
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة مجموعة تمارين")
        }

        // Update the Add Sets Button
        Button(
            onClick = { showSetsDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Text("إضافة مجموعات")
        }

        // Muscle Groups List with Workouts
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First add a header for saved sets if there are any
            if (sets.isNotEmpty()) {
                item {
                    Text(
                        text = "المجموعات المحفوظة",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Add saved sets
                items(sets) { workoutSet ->
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
                                    text = workoutSet.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Delete button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            database.setDao().deleteSet(workoutSet)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف المجموعة",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                            
                            // Display set values
                            Text(
                                text = workoutSet.setValues,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Add a divider between sets and muscle groups
                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Existing muscle groups items
            items(muscleGroups) { group ->
                val isExpanded = expandedGroups[group.id] ?: false
                val workouts by database.workoutDao()
                    .getWorkoutsForMuscleGroup(group.id)
                    .collectAsState(initial = emptyList())

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Muscle Group Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Expand/Collapse Icon
                            IconButton(onClick = { 
                                expandedGroups[group.id] = !isExpanded 
                            }) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = Color(0xFF6200EE)
                                )
                            }
                            
                            // Group Name
                            Text(
                                text = group.name,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            
                            // Action Buttons
                            Row {
                                // Add Workout Button
                                IconButton(
                                    onClick = {
                                        selectedMuscleGroupId = group.id
                                        showWorkoutDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add workout",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                                
                                // Edit Group Button
                                IconButton(
                                    onClick = {
                                        editingGroup = group
                                        muscleGroupName = group.name
                                        showDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit group",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                                
                                // Delete Group Button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            database.muscleGroupDao().deleteMuscleGroup(group)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete group",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                        }
                        
                        // Workouts List (Expandable)
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 48.dp, end = 16.dp, bottom = 16.dp)
                            ) {
                                workouts.forEach { workout ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = workout.name,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row {
                                            // Edit Workout Button
                                            IconButton(
                                                onClick = {
                                                    editingWorkout = workout
                                                    workoutName = workout.name
                                                    showWorkoutDialog = true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit workout",
                                                    tint = Color(0xFF6200EE)
                                                )
                                            }
                                            // Delete Workout Button
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
                    }
                }
            }
        }
    }

    // Dialog for adding/editing muscle group
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                muscleGroupName = ""
                editingGroup = null
            },
            title = { Text(if (editingGroup == null) "العضلة" else "تعديل العضلة", textAlign = TextAlign.Center) },
            text = {
                OutlinedTextField(
                    value = muscleGroupName,
                    onValueChange = { muscleGroupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ادخل اسم المجموعة العضلية") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (muscleGroupName.isNotBlank()) {
                            scope.launch {
                                if (editingGroup != null) {
                                    database.muscleGroupDao().updateMuscleGroup(
                                        editingGroup!!.copy(name = muscleGroupName)
                                    )
                                } else {
                                    database.muscleGroupDao().insertMuscleGroup(
                                        MuscleGroup(name = muscleGroupName)
                                    )
                                }
                                showDialog = false
                                muscleGroupName = ""
                                editingGroup = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(if (editingGroup == null) "إضافة" else "تعديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDialog = false
                        muscleGroupName = ""
                        editingGroup = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog for adding workout
    if (showWorkoutDialog && (selectedMuscleGroupId != null || editingWorkout != null)) {
        AlertDialog(
            onDismissRequest = { 
                showWorkoutDialog = false
                workoutName = ""
                selectedMuscleGroupId = null
                editingWorkout = null
            },
            title = { Text(if (editingWorkout == null) "إضافة تمرين" else "تعديل التمرين", textAlign = TextAlign.Center) },
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
                                if (editingWorkout != null) {
                                    database.workoutDao().updateWorkout(
                                        editingWorkout!!.copy(name = workoutName)
                                    )
                                } else {
                                    database.workoutDao().insertWorkout(
                                        Workout(
                                            muscleGroupId = selectedMuscleGroupId!!,
                                            name = workoutName
                                        )
                                    )
                                }
                                showWorkoutDialog = false
                                workoutName = ""
                                selectedMuscleGroupId = null
                                editingWorkout = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(if (editingWorkout == null) "إضافة" else "تعديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showWorkoutDialog = false
                        workoutName = ""
                        selectedMuscleGroupId = null
                        editingWorkout = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add this dialog at the end of the composable
    if (showSetsDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSetsDialog = false
                setName = ""
                setValues = ""
            },
            title = { Text("إضافة مجموعات", textAlign = TextAlign.Center) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = setName,
                        onValueChange = { setName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اسم المجموعة") },
                        placeholder = { Text("مثال: مجموعة الصدر") },
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = setValues,
                        onValueChange = { setValues = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("المجموعات") },
                        placeholder = { Text("مثال: 15,12,10,8 أو A,B,C") },
                        singleLine = false,
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (setName.isNotBlank() && setValues.isNotBlank()) {
                            scope.launch {
                                database.setDao().insertSet(
                                    WorkoutSet(
                                        name = setName,
                                        setValues = setValues
                                    )
                                )
                                showSetsDialog = false
                                setName = ""
                                setValues = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSetsDialog = false
                        setName = ""
                        setValues = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
} 