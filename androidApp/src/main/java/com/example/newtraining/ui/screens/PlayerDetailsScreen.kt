package com.example.newtraining.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Player
import com.example.newtraining.data.entity.TrainingPlan
import com.example.newtraining.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailsScreen(
    navController: NavController,
    playerId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val player by db.playerDao().getPlayerById(playerId).collectAsState(initial = null)
    val trainingPlans by db.trainingPlanDao().getTrainingPlansForPlayer(playerId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل اللاعب") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val currentPlayer = player
            if (currentPlayer != null) {
                FloatingActionButton(onClick = {
                    navController.navigate(Screen.PlayerProgressAdd.createRoute(currentPlayer.id))
                }) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة خطة تدريبية")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            player?.let { currentPlayer ->
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                ) {
                    if (currentPlayer.pictureUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(currentPlayer.pictureUri)),
                            contentDescription = "صورة اللاعب",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentPlayer.fullName.firstOrNull()?.toString() ?: "",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player Name
                Text(
                    text = currentPlayer.fullName,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // First Row: Age, Height, Gender
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoColumn("العمر", "${currentPlayer.age} سنة")
                    InfoColumn("الطول", "${currentPlayer.height} سم")
                    InfoColumn("الجنس", currentPlayer.gender)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Second Row: Medical Condition and ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoColumn("الحالة الصحية", currentPlayer.medicalCondition)
                    InfoColumn("رقم التعريف", currentPlayer.uniqueId)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Training Plans Section
                Text(
                    text = "الخطط التدريبية",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trainingPlans) { plan ->
                        TrainingPlanItem(
                            plan = plan,
                            onPlanClick = {
                                navController.navigate(Screen.TrainingPlanDetails.createRoute(plan.id))
                            },
                            onDelete = {
                                scope.launch {
                                    db.trainingPlanDao().deleteTrainingPlan(plan)
                                }
                            }
                        )
                    }
                }
            } ?: run {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun TrainingPlanItem(
    plan: TrainingPlan,
    onPlanClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(plan.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlanClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف الخطة",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
} 