package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.TrainingPlan
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.newtraining.ui.screens.TrainingPlanContent
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.Share
import android.os.Build
import android.provider.MediaStore
import android.content.ContentValues
import java.io.OutputStream
import java.io.FileInputStream
import android.graphics.Color as AndroidColor
import com.example.newtraining.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanDetailsScreen(
    navController: NavController,
    planId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val plan by db.trainingPlanDao().getTrainingPlanById(planId).collectAsState(initial = null)
    var isEditing by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }

    LaunchedEffect(plan) {
        plan?.let {
            editedContent = it.content
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل الخطة التدريبية") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        plan?.let {
                            navController.navigate(
                                Screen.PlayerProgressAdd.createRoute(
                                    it.playerId
                                )
                            )
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل")
                    }
                    IconButton(onClick = {
                        plan?.let {
                            generateAndSharePdf(context, it)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة")
                    }
                }
            )
        }
    ) { paddingValues ->
        plan?.let { currentPlan ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(currentPlan.date))

                Text(
                    text = currentPlan.name,
                        style = MaterialTheme.typography.headlineMedium.copy(textDirection = TextDirection.ContentOrRtl),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                )

                Text(
                    text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                )

                if (isEditing) {
                    OutlinedTextField(
                        value = editedContent,
                        onValueChange = { editedContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = { Text("محتوى الخطة") },
                        minLines = 10
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                db.trainingPlanDao().updateTrainingPlan(
                                    currentPlan.copy(content = editedContent)
                                )
                                isEditing = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("حفظ التغييرات")
                    }
                } else {
                        val gson = remember { Gson() }
                        val content = remember(currentPlan.content) {
                            gson.fromJson(currentPlan.content, TrainingPlanContent::class.java)
                        }
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            // Images
                            if (content.images.isNotEmpty()) {
                                Text(
                                    text = "صور اللاعب",
                                    style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Start
                                )
                                Row(Modifier.horizontalScroll(rememberScrollState())) {
                                    content.images.forEach { img ->
                                        AsyncImage(
                                            model = img,
                                            contentDescription = "صورة اللاعب",
                                            modifier = Modifier.size(100.dp).padding(4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            // Workouts
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = "الأيام التدريبية",
                                        style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    content.workoutDays.forEach { day ->
                                        Text(
                                            text = day.name,
                                            style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 2.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        day.entries.forEach { entry ->
                                            Text(
                                                text = "• ${entry.muscleGroup.name}: ${entry.workout.name} (${entry.set.name}: ${entry.set.setValues})",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                                                modifier = Modifier.padding(start = 24.dp, bottom = 2.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                            // Meals
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = "الوجبات",
                                        style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    content.meals.forEachIndexed { idx, meal ->
                                        Text(
                                            text = meal,
                                            style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 2.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        content.mealFoods.getOrNull(idx)?.forEach { food ->
                                            Text(
                                                text = "• ${food.name} : ${food.weight} جرام",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                                                modifier = Modifier.padding(start = 24.dp, bottom = 2.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                            // Supplements
                            if (content.supplements.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            text = "المكملات الغذائية",
                                            style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        content.supplements.forEach { supp ->
                                            Text(
                                                text = "• ${supp.supplement.name}: ${supp.amount}، وقت الاستخدام: ${supp.time}${if (supp.comment.isNotBlank()) "، ملاحظات: ${supp.comment}" else ""}",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                                                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                            // Hormones
                            if (content.hormones.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            text = "الهرمونات",
                                            style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        content.hormones.forEach { hormone ->
                                            Text(
                                                text = "• ${hormone.hormone.name}: ${hormone.amount}، وقت الاستخدام: ${hormone.time}${if (hormone.comment.isNotBlank()) "، ملاحظات: ${hormone.comment}" else ""}",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                                                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                            // Antibiotics
                            if (content.antibiotics.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            text = "المضادات الحيوية",
                                            style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        content.antibiotics.forEach { anti ->
                                            Text(
                                                text = "• ${anti.antibiotic.name}: ${anti.amount}، وقت الاستخدام: ${anti.time}${if (anti.comment.isNotBlank()) "، ملاحظات: ${anti.comment}" else ""}",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                                                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                            // Vitamins
                            if (content.vitamins.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            text = "الفيتامينات والمعادن",
                                            style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.tertiary),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        content.vitamins.forEach { vit ->
                                            Text(
                                                text = "• ${vit.vitamin.name}: ${vit.amount}، وقت الاستخدام: ${vit.time}${if (vit.comment.isNotBlank()) "، ملاحظات: ${vit.comment}" else ""}",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                                                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                            // Completion status
                            Spacer(modifier = Modifier.height(16.dp))
                    Text(
                                text = if (content.isComplete) "الحالة: مكتملة" else "الحالة: غير مكتملة",
                                style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.ContentOrRtl, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(top = 12.dp)
                    )
                        }
                    }
                }
            }
        } ?: run {
            CircularProgressIndicator()
        }
    }
}

// --- PDF Generation and Sharing ---
fun generateAndSharePdf(context: android.content.Context, plan: com.example.newtraining.data.entity.TrainingPlan) {
    try {
        val gson = Gson()
        val content = gson.fromJson(plan.content, TrainingPlanContent::class.java)
        val pdfDocument = PdfDocument()
        val paint = Paint()
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        var y = 40
        val xStart = 550 // Start from right for RTL
        paint.textAlign = Paint.Align.RIGHT
        // Plan Name
        paint.color = AndroidColor.BLACK
        canvas.drawText(plan.name, xStart.toFloat(), y.toFloat(), paint)
        y += 30
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        // Date
        canvas.drawText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(plan.date)), xStart.toFloat(), y.toFloat(), paint)
        y += 30
        // Images (just show count)
        if (content.images.isNotEmpty()) {
            canvas.drawText("صور اللاعب: ${content.images.size}", xStart.toFloat(), y.toFloat(), paint)
            y += 25
        }
        // --- Workouts Card ---
        val cardPadding = 12
        val cardMargin = 10
        val cardHeightStart = y - cardPadding
        paint.color = AndroidColor.parseColor("#E3F2FD") // Light blue
        canvas.drawRect(30f, cardHeightStart.toFloat(), 565f, (y + 80 + content.workoutDays.sumOf { 30 + it.entries.size * 18 }).toFloat(), paint)
        y += cardPadding
        paint.color = AndroidColor.parseColor("#1976D2") // Blue header
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("الأيام التدريبية", xStart.toFloat() - 10, y.toFloat(), paint)
        y += 25
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = AndroidColor.BLACK
        content.workoutDays.forEach { day ->
            canvas.drawText(day.name, xStart.toFloat() - 20, y.toFloat(), paint)
            y += 20
            day.entries.forEach { entry ->
                val entryText = "• ${entry.muscleGroup.name}: ${entry.workout.name} (${entry.set.name}: ${entry.set.setValues})"
                canvas.drawText(entryText, xStart.toFloat() - 40, y.toFloat(), paint)
                y += 18
            }
        }
        y += cardPadding + cardMargin
        // --- Meals Card ---
        val mealsCardStart = y - cardPadding
        paint.color = AndroidColor.parseColor("#FFF8E1") // Light yellow
        canvas.drawRect(30f, mealsCardStart.toFloat(), 565f, (y + 80 + content.meals.sumOf { 30 + (content.mealFoods.getOrNull(content.meals.indexOf(it))?.size ?: 0) * 18 }).toFloat(), paint)
        y += cardPadding
        paint.color = AndroidColor.parseColor("#FFA000") // Amber header
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("الوجبات", xStart.toFloat() - 10, y.toFloat(), paint)
        y += 25
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = AndroidColor.BLACK
        content.meals.forEachIndexed { idx, meal ->
            canvas.drawText(meal, xStart.toFloat() - 20, y.toFloat(), paint)
            y += 20
            content.mealFoods.getOrNull(idx)?.forEach { food ->
                val foodText = "• ${food.name} : ${food.weight} جرام"
                canvas.drawText(foodText, xStart.toFloat() - 40, y.toFloat(), paint)
                y += 18
            }
        }
        y += cardPadding + cardMargin
        // --- Supplements Card ---
        if (content.supplements.isNotEmpty()) {
            val suppCardStart = y - cardPadding
            paint.color = AndroidColor.parseColor("#E8F5E9") // Light green
            canvas.drawRect(30f, suppCardStart.toFloat(), 565f, (y + 60 + content.supplements.size * 18).toFloat(), paint)
            y += cardPadding
            paint.color = AndroidColor.parseColor("#388E3C") // Green header
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("المكملات الغذائية", xStart.toFloat() - 10, y.toFloat(), paint)
            y += 25
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = AndroidColor.BLACK
            content.supplements.forEach { supp ->
                val suppText = "• ${supp.supplement.name}: ${supp.amount}، وقت الاستخدام: ${supp.time}${if (supp.comment.isNotBlank()) "، ملاحظات: ${supp.comment}" else ""}"
                canvas.drawText(suppText, xStart.toFloat() - 20, y.toFloat(), paint)
                y += 18
            }
            y += cardPadding + cardMargin
        }
        // --- Hormones Card ---
        if (content.hormones.isNotEmpty()) {
            val hormoneCardStart = y - cardPadding
            paint.color = AndroidColor.parseColor("#F3E5F5") // Light purple
            canvas.drawRect(30f, hormoneCardStart.toFloat(), 565f, (y + 60 + content.hormones.size * 18).toFloat(), paint)
            y += cardPadding
            paint.color = AndroidColor.parseColor("#8E24AA") // Purple header
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("الهرمونات", xStart.toFloat() - 10, y.toFloat(), paint)
            y += 25
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = AndroidColor.BLACK
            content.hormones.forEach { hormone ->
                val hormoneText = "• ${hormone.hormone.name}: ${hormone.amount}، وقت الاستخدام: ${hormone.time}${if (hormone.comment.isNotBlank()) "، ملاحظات: ${hormone.comment}" else ""}"
                canvas.drawText(hormoneText, xStart.toFloat() - 20, y.toFloat(), paint)
                y += 18
            }
            y += cardPadding + cardMargin
        }
        // --- Antibiotics Card ---
        if (content.antibiotics.isNotEmpty()) {
            val antiCardStart = y - cardPadding
            paint.color = AndroidColor.parseColor("#FFEBEE") // Light red
            canvas.drawRect(30f, antiCardStart.toFloat(), 565f, (y + 60 + content.antibiotics.size * 18).toFloat(), paint)
            y += cardPadding
            paint.color = AndroidColor.parseColor("#D32F2F") // Red header
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("المضادات الحيوية", xStart.toFloat() - 10, y.toFloat(), paint)
            y += 25
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = AndroidColor.BLACK
            content.antibiotics.forEach { anti ->
                val antiText = "• ${anti.antibiotic.name}: ${anti.amount}، وقت الاستخدام: ${anti.time}${if (anti.comment.isNotBlank()) "، ملاحظات: ${anti.comment}" else ""}"
                canvas.drawText(antiText, xStart.toFloat() - 20, y.toFloat(), paint)
                y += 18
            }
            y += cardPadding + cardMargin
        }
        // --- Vitamins Card ---
        if (content.vitamins.isNotEmpty()) {
            val vitCardStart = y - cardPadding
            paint.color = AndroidColor.parseColor("#E1F5FE") // Light cyan
            canvas.drawRect(30f, vitCardStart.toFloat(), 565f, (y + 60 + content.vitamins.size * 18).toFloat(), paint)
            y += cardPadding
            paint.color = AndroidColor.parseColor("#0288D1") // Cyan header
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("الفيتامينات والمعادن", xStart.toFloat() - 10, y.toFloat(), paint)
            y += 25
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = AndroidColor.BLACK
            content.vitamins.forEach { vit ->
                val vitText = "• ${vit.vitamin.name}: ${vit.amount}، وقت الاستخدام: ${vit.time}${if (vit.comment.isNotBlank()) "، ملاحظات: ${vit.comment}" else ""}"
                canvas.drawText(vitText, xStart.toFloat() - 20, y.toFloat(), paint)
                y += 18
            }
            y += cardPadding + cardMargin
        }
        // Completion status
        paint.color = AndroidColor.BLACK
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (content.isComplete) "الحالة: مكتملة" else "الحالة: غير مكتملة", xStart.toFloat(), y.toFloat(), paint)
        pdfDocument.finishPage(page)
        // Save PDF to cache for sharing
        val cacheFile = File(context.cacheDir, "training_plan_${plan.id}.pdf")
        pdfDocument.writeTo(FileOutputStream(cacheFile))
        // Save PDF to Downloads
        val fileName = "training_plan_${plan.id}.pdf"
        var savedToDownloads = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collection, contentValues)
            if (itemUri != null) {
                resolver.openOutputStream(itemUri)?.use { outStream: OutputStream ->
                    FileInputStream(cacheFile).use { inStream: FileInputStream ->
                        inStream.copyTo(outStream)
                    }
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)
                savedToDownloads = true
            }
        } else {
            // For Android 9 and below, write directly to Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloadsDir, fileName)
            FileInputStream(cacheFile).use { inStream: FileInputStream ->
                FileOutputStream(outFile).use { outStream: FileOutputStream ->
                    inStream.copyTo(outStream)
                }
            }
            savedToDownloads = true
        }
        pdfDocument.close()
        if (savedToDownloads) {
            Toast.makeText(context, "تم حفظ الملف في مجلد التنزيلات", Toast.LENGTH_LONG).show()
        }
        // Share intent
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            cacheFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الخطة كملف PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "حدث خطأ أثناء إنشاء أو مشاركة ملف PDF", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
} 