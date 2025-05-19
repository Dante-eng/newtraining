package com.example.newtraining.ui.screens

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.material3.RadioButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.clickable
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.DietPlan
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import com.example.newtraining.data.entity.MuscleGroup
import com.example.newtraining.data.entity.Workout
import com.example.newtraining.data.entity.WorkoutSet
import androidx.compose.foundation.horizontalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.ButtonDefaults
import com.example.newtraining.data.entity.TrainingPlan
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.google.gson.Gson
import com.example.newtraining.ui.screens.TrainingPlanContent
import com.example.newtraining.ui.screens.MealFood
import com.example.newtraining.ui.screens.WorkoutEntry
import com.example.newtraining.ui.screens.WorkoutDay
import com.example.newtraining.ui.screens.AddedSupplement
import com.example.newtraining.ui.screens.AddedHormone
import com.example.newtraining.ui.screens.AddedAntibiotic
import com.example.newtraining.ui.screens.AddedVitamin
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.content.Context
import com.example.newtraining.util.convertArabicNumberToEnglish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProgressAddScreen(
    navController: NavController,
    playerId: Int,
    planId: Int? = null
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("ذكر") }
    var selectedActivity by remember { mutableStateOf("خامل") }
    var isGenderExpanded by remember { mutableStateOf(false) }
    var isActivityExpanded by remember { mutableStateOf(false) }
    var calculatedCalories by remember { mutableStateOf<Int?>(null) }
    var meals by remember { mutableStateOf(listOf<String>()) }
    val genders = listOf("ذكر", "أنثى")
    val activityLevels = listOf("خامل", "خفيف", "متوسط", "نشيط", "نشيط جداً")
    val mealNames = listOf("الوجبة الأولى", "الوجبة الثانية", "الوجبة الثالثة", "الوجبة الرابعة", "الوجبة الخامسة", "الوجبة السادسة", "الوجبة السابعة", "الوجبة الثامنة", "الوجبة التاسعة")

    // Placeholder diet items (replace with real items from your database or data source)
    val dietItems = listOf("دجاج مشوي", "أرز", "بطاطس", "سلطة", "سمك مشوي", "بيض", "جبن", "تفاح", "موز")
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dietPlans by db.dietPlanDao().getAllDietPlans().collectAsState(initial = emptyList())
    var showAddFoodDialogForMeal by remember { mutableStateOf<Int?>(null) }
    var selectedDietItem by remember { mutableStateOf("") }
    var isDietDropdownExpanded by remember { mutableStateOf(false) }
    var foodWeight by remember { mutableStateOf("") }

    // For each meal, store a list of selected foods (name and weight)
    var mealFoods by remember { mutableStateOf(List(meals.size) { mutableListOf<MealFood>() }) }

    // For dialog: store selected items and their weights
    var selectedDietItems by remember { mutableStateOf(mutableListOf<Pair<String, String>>()) }
    var currentSelectedItem by remember { mutableStateOf("") }
    var currentWeight by remember { mutableStateOf("") }

    // Image picker state
    var playerImages by remember { mutableStateOf(listOf<String>()) } // Use String for image file paths
    fun copyImageToAppStorage(context: Context, uri: Uri): String? {
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
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = copyImageToAppStorage(context, it)
            if (path != null) {
                playerImages = playerImages + path
            }
        }
    }

    fun calculateCalories(weight: Float, height: Float, age: Int, gender: String, activityLevel: String): Int {
        val bmr = when (gender) {
            "ذكر" -> 66.5 + (13.75 * weight) + (5.003 * height) - (6.755 * age)
            else -> 655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
        }
        val activityMultiplier = when (activityLevel) {
            "خامل" -> 1.2
            "خفيف" -> 1.375
            "متوسط" -> 1.55
            "نشيط" -> 1.725
            "نشيط جداً" -> 1.9
            else -> 1.2
        }
        return (bmr * activityMultiplier).toInt()
    }

    // Helper data class for fold
    data class NutritionTotals(val calories: Int, val protein: Int, val fat: Int, val carbs: Int)

    // Calculate total calories, protein, fat, and carbs from all foods in all meals
    val mealTotals = mealFoods.flatten().fold(NutritionTotals(0, 0, 0, 0)) { acc, food ->
        val plan = dietPlans.find { it.name == food.name }
        val weight = convertArabicNumberToEnglish(food.weight).toIntOrNull() ?: 0
        val cal = (weight * (plan?.caloriesPer100g?.toIntOrNull() ?: 0)) / 100
        val protein = (weight * (plan?.proteinPer100g?.toIntOrNull() ?: 0)) / 100
        val fat = (weight * (plan?.fatPer100g?.toIntOrNull() ?: 0)) / 100
        val carbs = (weight * (plan?.carbsPer100g?.toIntOrNull() ?: 0)) / 100
        NutritionTotals(
            acc.calories + cal,
            acc.protein + protein,
            acc.fat + fat,
            acc.carbs + carbs
        )
    }
    val totalMealCalories = mealTotals.calories
    val totalProtein = mealTotals.protein
    val totalFat = mealTotals.fat
    val totalCarbs = mealTotals.carbs
    val remainingCalories = (calculatedCalories ?: 0) - totalMealCalories

    // --- Workout Plan Section ---
    val workoutDayNames = listOf("اليوم الأول", "اليوم الثاني", "اليوم الثالث", "اليوم الرابع", "اليوم الخامس", "اليوم السادس", "اليوم السابع")

    // Data structure for workout entries per day
    var workoutDays by remember { mutableStateOf(mutableListOf<WorkoutDay>()) }

    // Dialog state
    var showAddWorkoutDialogForDay by remember { mutableStateOf<Int?>(null) }
    var selectedMuscleGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showSetPopup by remember { mutableStateOf(false) }
    var selectedSet by remember { mutableStateOf<WorkoutSet?>(null) }
    // Temporary list for new entries in dialog
    var tempWorkoutEntries by remember { mutableStateOf(mutableListOf<WorkoutEntry>()) }

    // DB lists for dropdowns
    val muscleGroups by db.muscleGroupDao().getAllMuscleGroups().collectAsState(initial = emptyList())
    val sets by db.setDao().getAllSets().collectAsState(initial = emptyList())
    val workoutsForSelectedMuscle by db.workoutDao().getWorkoutsForMuscleGroup(selectedMuscleGroup?.id ?: -1).collectAsState(initial = emptyList())

    // Supplement add dialog state
    var showAddSupplementDialog by remember { mutableStateOf(false) }
    var selectedSupplement by remember { mutableStateOf<com.example.newtraining.data.entity.Supplement?>(null) }
    var supplementAmount by remember { mutableStateOf("") }
    var supplementTime by remember { mutableStateOf("") }
    var supplementComment by remember { mutableStateOf("") }
    val supplements by db.supplementDao().getAllSupplements().collectAsState(initial = emptyList())
    // List of added supplements for this plan
    var addedSupplements by remember { mutableStateOf(mutableListOf<AddedSupplement>()) }

    // Hormone add dialog state
    var showAddHormoneDialog by remember { mutableStateOf(false) }
    var selectedHormone by remember { mutableStateOf<com.example.newtraining.data.entity.Hormone?>(null) }
    var hormoneAmount by remember { mutableStateOf("") }
    var hormoneTime by remember { mutableStateOf("") }
    var hormoneComment by remember { mutableStateOf("") }
    val hormones by db.hormoneDao().getAllHormones().collectAsState(initial = emptyList())
    // List of added hormones for this plan
    var addedHormones by remember { mutableStateOf(mutableListOf<AddedHormone>()) }

    // Antibiotic add dialog state
    var showAddAntibioticDialog by remember { mutableStateOf(false) }
    var selectedAntibiotic by remember { mutableStateOf<com.example.newtraining.data.entity.Antibiotic?>(null) }
    var antibioticAmount by remember { mutableStateOf("") }
    var antibioticTime by remember { mutableStateOf("") }
    var antibioticComment by remember { mutableStateOf("") }
    val antibiotics by db.antibioticDao().getAllAntibiotics().collectAsState(initial = emptyList())
    // List of added antibiotics for this plan
    var addedAntibiotics by remember { mutableStateOf(mutableListOf<AddedAntibiotic>()) }

    // Vitamin add dialog state
    var showAddVitaminDialog by remember { mutableStateOf(false) }
    var selectedVitamin by remember { mutableStateOf<com.example.newtraining.data.entity.Vitamin?>(null) }
    var vitaminAmount by remember { mutableStateOf("") }
    var vitaminTime by remember { mutableStateOf("") }
    var vitaminComment by remember { mutableStateOf("") }
    val vitamins by db.vitaminDao().getAllVitamins().collectAsState(initial = emptyList())
    // List of added vitamins for this plan
    var addedVitamins by remember { mutableStateOf(mutableListOf<AddedVitamin>()) }

    // State for full-screen image preview
    var fullScreenImage by remember { mutableStateOf<String?>(null) }

    // State for save confirmation dialog
    var showSaveDialog by remember { mutableStateOf(false) }
    var isPlanComplete by remember { mutableStateOf<Boolean?>(null) }

    val scope = rememberCoroutineScope()

    // Load plan if editing
    val plan by if (planId != null) db.trainingPlanDao().getTrainingPlanById(planId).collectAsState(initial = null) else remember { mutableStateOf(null) }
    var didLoad by remember { mutableStateOf(false) }
    LaunchedEffect(plan) {
        if (!didLoad && plan != null) {
            val content = Gson().fromJson(plan!!.content, TrainingPlanContent::class.java)
            meals = content.meals
            mealFoods = content.mealFoods.map { it.toMutableList() }
            workoutDays = content.workoutDays.toMutableList()
            addedSupplements = content.supplements.toMutableList()
            addedHormones = content.hormones.toMutableList()
            addedAntibiotics = content.antibiotics.toMutableList()
            addedVitamins = content.vitamins.toMutableList()
            playerImages = content.images
            didLoad = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "النظام الكامل",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = "صور اللاعب",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    playerImages.forEachIndexed { idx, imageUri ->
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(4.dp)
                                .clickable { fullScreenImage = imageUri }
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "صورة اللاعب",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            IconButton(
                                onClick = {
                                    playerImages = playerImages.toMutableList().apply { removeAt(idx) }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف الصورة",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("إضافة صورة", textAlign = TextAlign.Center)
                    }
                }
            }
            Text(
                text = "حاسبة السعرات الحرارية",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || "٠١٢٣٤٥٦٧٨٩".contains(c) }) weight = it },
                    label = { Text("الوزن (كجم)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || "٠١٢٣٤٥٦٧٨٩".contains(c) }) height = it },
                    label = { Text("الطول (سم)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { c -> c.isDigit() || "٠١٢٣٤٥٦٧٨٩".contains(c) }) age = it },
                label = { Text("العمر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isGenderExpanded,
                    onExpandedChange = { isGenderExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedGender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الجنس") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isGenderExpanded,
                        onDismissRequest = { isGenderExpanded = false }
                    ) {
                        genders.forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender) },
                                onClick = {
                                    selectedGender = gender
                                    isGenderExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = isActivityExpanded,
                    onExpandedChange = { isActivityExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedActivity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مستوى النشاط") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isActivityExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isActivityExpanded,
                        onDismissRequest = { isActivityExpanded = false }
                    ) {
                        activityLevels.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text(activity) },
                                onClick = {
                                    selectedActivity = activity
                                    isActivityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val w = convertArabicNumberToEnglish(weight).toFloatOrNull()
                    val h = convertArabicNumberToEnglish(height).toFloatOrNull()
                    val a = convertArabicNumberToEnglish(age).toIntOrNull()
                    if (w != null && h != null && a != null) {
                        calculatedCalories = calculateCalories(w, h, a, selectedGender, selectedActivity)
                    } else {
                        calculatedCalories = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("احسب السعرات")
            }
            // Calories summary card (reference only, values will be updated from diet plan)
            if (calculatedCalories != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Text(
                                text = "السعرات المطلوبة:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${calculatedCalories} ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "كالوري",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("إجمالي سعرات الوجبات: $totalMealCalories كالوري", style = MaterialTheme.typography.bodyMedium)
                        Text("السعرات المتبقية: $remainingCalories كالوري", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("اجمالي البروتين: $totalProtein", style = MaterialTheme.typography.bodyMedium)
                        Text("اجمالي الدهون: $totalFat", style = MaterialTheme.typography.bodyMedium)
                        Text("اجمالي الكربوهيدرات: $totalCarbs", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            // --- Diet Plan Section ---
            Text(
                text = "النظام الغذائي",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (meals.size < mealNames.size) {
                        meals = meals + mealNames[meals.size]
                        val newMealFoods = mealFoods.toMutableList()
                        newMealFoods.add(mutableListOf())
                        mealFoods = newMealFoods
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = meals.size < mealNames.size
            ) {
                Text("إضافة وجبة")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meals.forEachIndexed { index, mealName ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mealName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                )
                                FloatingActionButton(
                                    onClick = { showAddFoodDialogForMeal = index },
                                    modifier = Modifier.size(40.dp),
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "إضافة طعام", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                IconButton(
                                    onClick = {
                                        val newMeals = meals.toMutableList().apply { removeAt(index) }
                                        meals = newMeals.mapIndexed { idx, _ -> mealNames[idx] }
                                        // Remove foods for this meal
                                        val newMealFoods = mealFoods.toMutableList().apply { removeAt(index) }
                                        mealFoods = newMealFoods
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف الوجبة")
                                }
                            }
                            // Show foods for this meal
                            mealFoods.getOrNull(index)?.forEach { food ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${food.name} - ${food.weight} جرام", modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        val newMealFoods = mealFoods.toMutableList()
                                        newMealFoods[index] = newMealFoods[index].toMutableList().apply {
                                            remove(food)
                                        }
                                        mealFoods = newMealFoods
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف الطعام")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Add Food Dialog
            if (showAddFoodDialogForMeal != null) {
                AlertDialog(
                    onDismissRequest = {
                        showAddFoodDialogForMeal = null
                        currentSelectedItem = ""
                        currentWeight = ""
                        selectedDietItems = mutableListOf()
                        isDietDropdownExpanded = false
                    },
                    title = { Text("اختر عناصر الطعام وأوزانها") },
                    text = {
                        Column {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                ExposedDropdownMenuBox(
                                    expanded = isDietDropdownExpanded,
                                    onExpandedChange = { isDietDropdownExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = currentSelectedItem,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("اختر الطعام") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDietDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isDietDropdownExpanded,
                                        onDismissRequest = { isDietDropdownExpanded = false }
                                    ) {
                                        dietPlans.forEach { plan ->
                                            DropdownMenuItem(
                                                text = { Text(plan.name) },
                                                onClick = {
                                                    currentSelectedItem = plan.name
                                                    isDietDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                if (currentSelectedItem.isNotBlank()) {
                                    OutlinedTextField(
                                        value = currentWeight,
                                        onValueChange = { if (it.all { c -> c.isDigit() || "٠١٢٣٤٥٦٧٨٩".contains(c) }) currentWeight = it },
                                        label = { Text("الوزن (جرام)") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            if (currentSelectedItem.isNotBlank() && currentWeight.isNotBlank()) {
                                                selectedDietItems = (selectedDietItems + (currentSelectedItem to currentWeight)).toMutableList()
                                                currentSelectedItem = ""
                                                currentWeight = ""
                                            }
                                        },
                                        enabled = currentSelectedItem.isNotBlank() && currentWeight.isNotBlank(),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) { Text("إضافة") }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            // Show selected items
                            selectedDietItems.forEachIndexed { idx, (item, weight) ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$item - $weight جرام", modifier = Modifier.weight(1f))
                                    IconButton(onClick = { selectedDietItems.removeAt(idx) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Add all selectedDietItems to the meal's food list
                                val idx = showAddFoodDialogForMeal!!
                                val newMealFoods = mealFoods.toMutableList()
                                newMealFoods[idx] = (newMealFoods[idx] + selectedDietItems.map { MealFood(it.first, it.second) }).toMutableList()
                                mealFoods = newMealFoods
                                showAddFoodDialogForMeal = null
                                currentSelectedItem = ""
                                currentWeight = ""
                                selectedDietItems = mutableListOf()
                                isDietDropdownExpanded = false
                            },
                            enabled = selectedDietItems.isNotEmpty()
                        ) { Text("إضافة") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showAddFoodDialogForMeal = null
                            currentSelectedItem = ""
                            currentWeight = ""
                            selectedDietItems = mutableListOf()
                            isDietDropdownExpanded = false
                        }) { Text("إلغاء") }
                    }
                )
            }
            // --- Workout Plan Section ---
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "الخطة التدريبية",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    if (workoutDays.size < workoutDayNames.size) {
                        workoutDays = (workoutDays + WorkoutDay(workoutDayNames[workoutDays.size], tempWorkoutEntries.toList())).toMutableList()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = workoutDays.size < workoutDayNames.size
            ) {
                Text("إضافة يوم تدريبي")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                workoutDays.forEachIndexed { index, day ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                )
                                FloatingActionButton(
                                    onClick = { showAddWorkoutDialogForDay = index },
                                    modifier = Modifier.size(40.dp),
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "إضافة تمرين", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                IconButton(
                                    onClick = {
                                        val mutable = workoutDays.toMutableList()
                                        mutable.removeAt(index)
                                        workoutDays = mutable
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف اليوم التدريبي")
                                }
                            }
                            // Display entries for this day, grouped by muscle group
                            if (day.entries.isNotEmpty()) {
                                // Group entries by muscle group
                                val grouped = day.entries.groupBy { it.muscleGroup }
                                Column(Modifier.padding(start = 32.dp, end = 8.dp, bottom = 8.dp)) {
                                    grouped.forEach { (muscle, entries) ->
                                        Text(
                                            text = muscle.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        entries.forEachIndexed { entryIdx, entry ->
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${entry.workout.name} - ${entry.set.name}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = "المجموعات: ${entry.set.setValues}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    // Find the absolute index in day.entries for deletion
                                                    val absIdx = day.entries.indexOf(entry)
                                                    val mutable = workoutDays.toMutableList()
                                                    val updatedDay = day.copy(entries = day.entries.toMutableList().apply { removeAt(absIdx) })
                                                    mutable[index] = updatedDay
                                                    workoutDays = mutable
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف التمرين")
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Add Workout Dialog
            if (showAddWorkoutDialogForDay != null) {
                AlertDialog(
                    onDismissRequest = {
                        showAddWorkoutDialogForDay = null
                        selectedMuscleGroup = null
                        selectedWorkout = null
                        selectedSet = null
                        tempWorkoutEntries = mutableListOf()
                        showSetPopup = false
                    },
                    title = { Text("إضافة تمارين لليوم", textAlign = TextAlign.Center) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Muscle group dropdown
                            var muscleGroupDropdownExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = muscleGroupDropdownExpanded,
                                onExpandedChange = { muscleGroupDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedMuscleGroup?.name ?: "اختر المجموعة العضلية",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المجموعة العضلية") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleGroupDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = muscleGroupDropdownExpanded,
                                    onDismissRequest = { muscleGroupDropdownExpanded = false }
                                ) {
                                    muscleGroups.forEach { group ->
                                        DropdownMenuItem(
                                            text = { Text(group.name) },
                                            onClick = {
                                                selectedMuscleGroup = group
                                                selectedWorkout = null
                                                muscleGroupDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Workout dropdown (after muscle group selected)
                            if (selectedMuscleGroup != null) {
                                var workoutDropdownExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = workoutDropdownExpanded,
                                    onExpandedChange = { workoutDropdownExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedWorkout?.name ?: "اختر التمرين",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("التمرين") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workoutDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = workoutDropdownExpanded,
                                        onDismissRequest = { workoutDropdownExpanded = false }
                                    ) {
                                        workoutsForSelectedMuscle.forEach { workout ->
                                            DropdownMenuItem(
                                                text = { Text(workout.name) },
                                                onClick = {
                                                    selectedWorkout = workout
                                                    workoutDropdownExpanded = false
                                                    showSetPopup = true // Show set popup after workout selection
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            // List of entries added in this dialog
                            if (tempWorkoutEntries.isNotEmpty()) {
                                Text("التمارين المضافة:", style = MaterialTheme.typography.titleSmall)
                                Column(Modifier.padding(start = 8.dp)) {
                                    tempWorkoutEntries.forEachIndexed { idx, entry ->
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    text = "${entry.muscleGroup.name} - ${entry.workout.name} - ${entry.set.name}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "المجموعات: ${entry.set.setValues}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = {
                                                tempWorkoutEntries = tempWorkoutEntries.toMutableList().apply { removeAt(idx) }
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف التمرين")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val idx = showAddWorkoutDialogForDay!!
                                if (tempWorkoutEntries.isNotEmpty()) {
                                    val mutable = workoutDays.toMutableList()
                                    val day = mutable[idx]
                                    val updatedDay = day.copy(entries = day.entries + tempWorkoutEntries)
                                    mutable[idx] = updatedDay
                                    workoutDays = mutable
                                    showAddWorkoutDialogForDay = null
                                    selectedMuscleGroup = null
                                    selectedWorkout = null
                                    selectedSet = null
                                    tempWorkoutEntries = mutableListOf()
                                    showSetPopup = false
                                }
                            },
                            enabled = tempWorkoutEntries.isNotEmpty()
                        ) { Text("إضافة") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showAddWorkoutDialogForDay = null
                            selectedMuscleGroup = null
                            selectedWorkout = null
                            selectedSet = null
                            tempWorkoutEntries = mutableListOf()
                            showSetPopup = false
                        }) { Text("إلغاء") }
                    }
                )
            }
            // Set selection popup
            if (showSetPopup && selectedMuscleGroup != null && selectedWorkout != null) {
                AlertDialog(
                    onDismissRequest = {
                        showSetPopup = false
                        selectedSet = null
                    },
                    title = { Text("اختر المجموعة", textAlign = TextAlign.Center) },
                    text = {
                        var setDropdownExpanded by remember { mutableStateOf(false) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = setDropdownExpanded,
                                onExpandedChange = { setDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSet?.name ?: "اختر المجموعة",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المجموعة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = setDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = setDropdownExpanded,
                                    onDismissRequest = { setDropdownExpanded = false }
                                ) {
                                    sets.forEach { set ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(set.name, style = MaterialTheme.typography.bodyMedium)
                                                    Text(set.setValues, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                            },
                                            onClick = {
                                                selectedSet = set
                                                setDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Show set values below the dropdown if a set is selected
                            if (selectedSet != null) {
                                Text(
                                    text = "المجموعات: ${selectedSet!!.setValues}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (selectedMuscleGroup != null && selectedWorkout != null && selectedSet != null) {
                                    tempWorkoutEntries = (tempWorkoutEntries + WorkoutEntry(selectedMuscleGroup!!, selectedWorkout!!, selectedSet!!)).toMutableList()
                                    // Reset for next entry
                                    selectedWorkout = null
                                    selectedSet = null
                                    showSetPopup = false
                                }
                            },
                            enabled = selectedSet != null
                        ) { Text("إضافة") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            selectedSet = null
                            showSetPopup = false
                        }) { Text("إلغاء") }
                    }
                )
            }
            // After the workout plan section
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "المكملات الغذائية",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showAddSupplementDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة مكمل")
            }
            // Show added supplements
            if (addedSupplements.isNotEmpty()) {
                Column(Modifier.padding(top = 16.dp)) {
                    addedSupplements.forEachIndexed { idx, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.supplement.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        addedSupplements = addedSupplements.toMutableList().apply { removeAt(idx) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف المكمل")
                                    }
                                }
                                Text("الكمية: ${entry.amount}", style = MaterialTheme.typography.bodyMedium)
                                Text("وقت الاستخدام: ${entry.time}", style = MaterialTheme.typography.bodyMedium)
                                if (entry.comment.isNotBlank()) {
                                    Text("ملاحظات: ${entry.comment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            // Hormones section
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "الهرمونات",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showAddHormoneDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة هرمون")
            }
            // Show added hormones
            if (addedHormones.isNotEmpty()) {
                Column(Modifier.padding(top = 16.dp)) {
                    addedHormones.forEachIndexed { idx, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.hormone.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        addedHormones = addedHormones.toMutableList().apply { removeAt(idx) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف الهرمون")
                                    }
                                }
                                Text("الكمية: ${entry.amount}", style = MaterialTheme.typography.bodyMedium)
                                Text("وقت الاستخدام: ${entry.time}", style = MaterialTheme.typography.bodyMedium)
                                if (entry.comment.isNotBlank()) {
                                    Text("ملاحظات: ${entry.comment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            // Antibiotics section
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "المضادات",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showAddAntibioticDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة مضاد")
            }
            // Show added antibiotics
            if (addedAntibiotics.isNotEmpty()) {
                Column(Modifier.padding(top = 16.dp)) {
                    addedAntibiotics.forEachIndexed { idx, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.antibiotic.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        addedAntibiotics = addedAntibiotics.toMutableList().apply { removeAt(idx) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف المضاد")
                                    }
                                }
                                Text("الكمية: ${entry.amount}", style = MaterialTheme.typography.bodyMedium)
                                Text("وقت الاستخدام: ${entry.time}", style = MaterialTheme.typography.bodyMedium)
                                if (entry.comment.isNotBlank()) {
                                    Text("ملاحظات: ${entry.comment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            // Vitamins section
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "الفايتمينات و المعادن",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showAddVitaminDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة فايتمين")
            }
            // Show added vitamins
            if (addedVitamins.isNotEmpty()) {
                Column(Modifier.padding(top = 16.dp)) {
                    addedVitamins.forEachIndexed { idx, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.vitamin.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        addedVitamins = addedVitamins.toMutableList().apply { removeAt(idx) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف الفايتمين")
                                    }
                                }
                                Text("الكمية: ${entry.amount}", style = MaterialTheme.typography.bodyMedium)
                                Text("وقت الاستخدام: ${entry.time}", style = MaterialTheme.typography.bodyMedium)
                                if (entry.comment.isNotBlank()) {
                                    Text("ملاحظات: ${entry.comment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            // At the very end, after all sections
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    showSaveDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("حفظ", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    // Supplement Add Dialog
    if (showAddSupplementDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSupplementDialog = false
                selectedSupplement = null
                supplementAmount = ""
                supplementTime = ""
                supplementComment = ""
            },
            title = { Text("إضافة مكمل", textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Supplement dropdown
                    var supplementDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = supplementDropdownExpanded,
                        onExpandedChange = { supplementDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSupplement?.name ?: "اختر المكمل",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المكمل") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplementDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = supplementDropdownExpanded,
                            onDismissRequest = { supplementDropdownExpanded = false }
                        ) {
                            supplements.forEach { supp ->
                                DropdownMenuItem(
                                    text = { Text(supp.name) },
                                    onClick = {
                                        selectedSupplement = supp
                                        supplementDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = supplementAmount,
                        onValueChange = { supplementAmount = it },
                        label = { Text("الكمية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = supplementTime,
                        onValueChange = { supplementTime = it },
                        label = { Text("وقت الاستخدام") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = supplementComment,
                        onValueChange = { supplementComment = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedSupplement != null && supplementAmount.isNotBlank() && supplementTime.isNotBlank()) {
                            addedSupplements = (addedSupplements + AddedSupplement(selectedSupplement!!, supplementAmount, supplementTime, supplementComment)).toMutableList()
                        }
                        showAddSupplementDialog = false
                        selectedSupplement = null
                        supplementAmount = ""
                        supplementTime = ""
                        supplementComment = ""
                    },
                    enabled = selectedSupplement != null && supplementAmount.isNotBlank() && supplementTime.isNotBlank()
                ) { Text("إضافة") }
            },
            dismissButton = {
                Button(onClick = {
                    showAddSupplementDialog = false
                    selectedSupplement = null
                    supplementAmount = ""
                    supplementTime = ""
                    supplementComment = ""
                }) { Text("إلغاء") }
            }
        )
    }

    // Hormone Add Dialog
    if (showAddHormoneDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddHormoneDialog = false
                selectedHormone = null
                hormoneAmount = ""
                hormoneTime = ""
                hormoneComment = ""
            },
            title = { Text("إضافة هرمون", textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Hormone dropdown
                    var hormoneDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = hormoneDropdownExpanded,
                        onExpandedChange = { hormoneDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedHormone?.name ?: "اختر الهرمون",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الهرمون") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hormoneDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = hormoneDropdownExpanded,
                            onDismissRequest = { hormoneDropdownExpanded = false }
                        ) {
                            hormones.forEach { hormone ->
                                DropdownMenuItem(
                                    text = { Text(hormone.name) },
                                    onClick = {
                                        selectedHormone = hormone
                                        hormoneDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = hormoneAmount,
                        onValueChange = { hormoneAmount = it },
                        label = { Text("الكمية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hormoneTime,
                        onValueChange = { hormoneTime = it },
                        label = { Text("وقت الاستخدام") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hormoneComment,
                        onValueChange = { hormoneComment = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedHormone != null && hormoneAmount.isNotBlank() && hormoneTime.isNotBlank()) {
                            addedHormones = (addedHormones + AddedHormone(selectedHormone!!, hormoneAmount, hormoneTime, hormoneComment)).toMutableList()
                        }
                        showAddHormoneDialog = false
                        selectedHormone = null
                        hormoneAmount = ""
                        hormoneTime = ""
                        hormoneComment = ""
                    },
                    enabled = selectedHormone != null && hormoneAmount.isNotBlank() && hormoneTime.isNotBlank()
                ) { Text("إضافة") }
            },
            dismissButton = {
                Button(onClick = {
                    showAddHormoneDialog = false
                    selectedHormone = null
                    hormoneAmount = ""
                    hormoneTime = ""
                    hormoneComment = ""
                }) { Text("إلغاء") }
            }
        )
    }

    // Antibiotic Add Dialog
    if (showAddAntibioticDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddAntibioticDialog = false
                selectedAntibiotic = null
                antibioticAmount = ""
                antibioticTime = ""
                antibioticComment = ""
            },
            title = { Text("إضافة مضاد", textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Antibiotic dropdown
                    var antibioticDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = antibioticDropdownExpanded,
                        onExpandedChange = { antibioticDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAntibiotic?.name ?: "اختر المضاد",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المضاد") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = antibioticDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = antibioticDropdownExpanded,
                            onDismissRequest = { antibioticDropdownExpanded = false }
                        ) {
                            antibiotics.forEach { antibiotic ->
                                DropdownMenuItem(
                                    text = { Text(antibiotic.name) },
                                    onClick = {
                                        selectedAntibiotic = antibiotic
                                        antibioticDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = antibioticAmount,
                        onValueChange = { antibioticAmount = it },
                        label = { Text("الكمية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = antibioticTime,
                        onValueChange = { antibioticTime = it },
                        label = { Text("وقت الاستخدام") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = antibioticComment,
                        onValueChange = { antibioticComment = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAntibiotic != null && antibioticAmount.isNotBlank() && antibioticTime.isNotBlank()) {
                            addedAntibiotics = (addedAntibiotics + AddedAntibiotic(selectedAntibiotic!!, antibioticAmount, antibioticTime, antibioticComment)).toMutableList()
                        }
                        showAddAntibioticDialog = false
                        selectedAntibiotic = null
                        antibioticAmount = ""
                        antibioticTime = ""
                        antibioticComment = ""
                    },
                    enabled = selectedAntibiotic != null && antibioticAmount.isNotBlank() && antibioticTime.isNotBlank()
                ) { Text("إضافة") }
            },
            dismissButton = {
                Button(onClick = {
                    showAddAntibioticDialog = false
                    selectedAntibiotic = null
                    antibioticAmount = ""
                    antibioticTime = ""
                    antibioticComment = ""
                }) { Text("إلغاء") }
            }
        )
    }

    // Vitamin Add Dialog
    if (showAddVitaminDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddVitaminDialog = false
                selectedVitamin = null
                vitaminAmount = ""
                vitaminTime = ""
                vitaminComment = ""
            },
            title = { Text("إضافة فايتمين", textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Vitamin dropdown
                    var vitaminDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = vitaminDropdownExpanded,
                        onExpandedChange = { vitaminDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVitamin?.name ?: "اختر الفايتمين",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الفايتمين") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vitaminDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = vitaminDropdownExpanded,
                            onDismissRequest = { vitaminDropdownExpanded = false }
                        ) {
                            vitamins.forEach { vitamin ->
                                DropdownMenuItem(
                                    text = { Text(vitamin.name) },
                                    onClick = {
                                        selectedVitamin = vitamin
                                        vitaminDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = vitaminAmount,
                        onValueChange = { vitaminAmount = it },
                        label = { Text("الكمية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = vitaminTime,
                        onValueChange = { vitaminTime = it },
                        label = { Text("وقت الاستخدام") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = vitaminComment,
                        onValueChange = { vitaminComment = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedVitamin != null && vitaminAmount.isNotBlank() && vitaminTime.isNotBlank()) {
                            addedVitamins = (addedVitamins + AddedVitamin(selectedVitamin!!, vitaminAmount, vitaminTime, vitaminComment)).toMutableList()
                        }
                        showAddVitaminDialog = false
                        selectedVitamin = null
                        vitaminAmount = ""
                        vitaminTime = ""
                        vitaminComment = ""
                    },
                    enabled = selectedVitamin != null && vitaminAmount.isNotBlank() && vitaminTime.isNotBlank()
                ) { Text("إضافة") }
            },
            dismissButton = {
                Button(onClick = {
                    showAddVitaminDialog = false
                    selectedVitamin = null
                    vitaminAmount = ""
                    vitaminTime = ""
                    vitaminComment = ""
                }) { Text("إلغاء") }
            }
        )
    }

    // Full-screen image dialog
    if (fullScreenImage != null) {
        Dialog(onDismissRequest = { fullScreenImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fullScreenImage = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullScreenImage,
                    contentDescription = "صورة اللاعب (كاملة)",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Save confirmation dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("تأكيد الحفظ", textAlign = TextAlign.Center) },
            text = { Text("هل الخطة مكتملة؟", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(
                    onClick = {
                        isPlanComplete = true
                        showSaveDialog = false
                        val currentPlan = plan // assign before coroutine
                        scope.launch {
                            val plansCount = db.trainingPlanDao().getPlansCountForPlayer(playerId)
                            val planName = getArabicPlanName(plansCount)
                            val planContent = Gson().toJson(TrainingPlanContent(
                                meals = meals,
                                mealFoods = mealFoods.map { it.toList() },
                                workoutDays = workoutDays.toList(),
                                supplements = addedSupplements.toList(),
                                hormones = addedHormones.toList(),
                                antibiotics = addedAntibiotics.toList(),
                                vitamins = addedVitamins.toList(),
                                images = playerImages,
                                isComplete = true
                            ))
                            if (planId != null && currentPlan != null) {
                                // Update existing plan
                                db.trainingPlanDao().updateTrainingPlan(
                                    currentPlan.copy(
                                        name = planName,
                                        content = planContent,
                                        date = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                // Insert new plan
                                db.trainingPlanDao().insertTrainingPlan(
                                    TrainingPlan(
                                        playerId = playerId,
                                        name = planName,
                                        content = planContent
                                    )
                                )
                            }
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("نعم، مكتملة", color = MaterialTheme.colorScheme.onPrimary) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        isPlanComplete = false
                        showSaveDialog = false
                        val currentPlan = plan // assign before coroutine
                        scope.launch {
                            val plansCount = db.trainingPlanDao().getPlansCountForPlayer(playerId)
                            val planName = getArabicPlanName(plansCount)
                            val planContent = Gson().toJson(TrainingPlanContent(
                                meals = meals,
                                mealFoods = mealFoods.map { it.toList() },
                                workoutDays = workoutDays.toList(),
                                supplements = addedSupplements.toList(),
                                hormones = addedHormones.toList(),
                                antibiotics = addedAntibiotics.toList(),
                                vitamins = addedVitamins.toList(),
                                images = playerImages,
                                isComplete = false
                            ))
                            if (planId != null && currentPlan != null) {
                                // Update existing plan
                                db.trainingPlanDao().updateTrainingPlan(
                                    currentPlan.copy(
                                        name = planName,
                                        content = planContent,
                                        date = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                // Insert new plan
                                db.trainingPlanDao().insertTrainingPlan(
                                    TrainingPlan(
                                        playerId = playerId,
                                        name = planName,
                                        content = planContent
                                    )
                                )
                            }
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("لا، غير مكتملة", color = MaterialTheme.colorScheme.onSecondary) }
            }
        )
    }
}

// Arabic ordinal plan name generator
fun getArabicPlanName(index: Int): String {
    val ordinals = listOf(
        "الأولى", "الثانية", "الثالثة", "الرابعة", "الخامسة", "السادسة", "السابعة", "الثامنة", "التاسعة", "العاشرة"
    )
    val ordinal = if (index in ordinals.indices) ordinals[index] else "${index + 1}"
    return "الخطة التدريبية $ordinal"
} 