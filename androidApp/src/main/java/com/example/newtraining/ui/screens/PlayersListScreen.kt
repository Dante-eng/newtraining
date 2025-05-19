package com.example.newtraining.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Player
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersListScreen(
    navController: NavController,
    viewModel: PlayersListViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val players by database.playerDao().getAllPlayers().collectAsState(initial = emptyList<Player>())
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var editFullName by remember { mutableStateOf("") }
    var editAge by remember { mutableStateOf("") }
    var editHeight by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf("") }
    var editMedicalCondition by remember { mutableStateOf("") }
    var editPictureUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        editPictureUri = uri
    }

    val filteredPlayers = remember(searchQuery, players) {
        if (searchQuery.isEmpty()) {
            players
        } else {
            players.filter { player ->
                player.fullName.contains(searchQuery, ignoreCase = true) ||
                player.uniqueId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Observe navigation result
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Player>("newPlayer")?.let { newPlayer ->
            scope.launch {
                database.playerDao().insertPlayer(newPlayer)
                navController.currentBackStackEntry?.savedStateHandle?.remove<Player>("newPlayer")
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && selectedPlayer != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل بيانات اللاعب") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (editPictureUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(editPictureUri),
                                contentDescription = "صورة اللاعب",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة صورة",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editAge,
                        onValueChange = { editAge = it },
                        label = { Text("العمر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editHeight,
                        onValueChange = { editHeight = it },
                        label = { Text("الطول") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editGender,
                        onValueChange = { editGender = it },
                        label = { Text("الجنس") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMedicalCondition,
                        onValueChange = { editMedicalCondition = it },
                        label = { Text("الحالة الصحية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPlayer?.let { player ->
                            viewModel.updatePlayer(
                                player.copy(
                                    fullName = editFullName,
                                    age = editAge.toIntOrNull() ?: 0,
                                    height = editHeight.toIntOrNull() ?: 0,
                                    gender = editGender,
                                    medicalCondition = editMedicalCondition,
                                    pictureUri = editPictureUri?.toString() ?: player.pictureUri
                                )
                            )
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قائمة اللاعبين") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_player") }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة لاعب"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("بحث") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") }
            )

            // Players List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPlayers) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("player_details/${player.id}")
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Player Picture
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            ) {
                                if (player.pictureUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(Uri.parse(player.pictureUri)),
                                        contentDescription = "صورة اللاعب",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = player.fullName.firstOrNull()?.toString() ?: "",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                }
                            }

                            // Player Info
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = player.fullName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = player.uniqueId,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Actions
                            Row {
                                IconButton(
                                    onClick = {
                                        selectedPlayer = player
                                        editFullName = player.fullName
                                        editAge = player.age.toString()
                                        editHeight = player.height.toString()
                                        editGender = player.gender
                                        editMedicalCondition = player.medicalCondition
                                        editPictureUri = player.pictureUri?.let { Uri.parse(it) }
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "تعديل",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { 
                                        viewModel.deletePlayer(player)
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف",
                                        modifier = Modifier.size(20.dp)
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