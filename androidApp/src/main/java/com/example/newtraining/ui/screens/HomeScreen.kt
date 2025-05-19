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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.example.newtraining.data.AppDatabase
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Close

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val allPlans by db.trainingPlanDao().getAllTrainingPlans().collectAsState(initial = emptyList())
    val incompletePlans = allPlans.filter { plan ->
        try {
            val content = com.google.gson.Gson().fromJson(plan.content, com.example.newtraining.ui.screens.TrainingPlanContent::class.java)
            content.isComplete == false
        } catch (e: Exception) {
            false
        }
    }
    val players by db.playerDao().getAllPlayers().collectAsState(initial = emptyList())
    val playerNameMap = remember(players) { players.associateBy({ it.id }, { it.fullName }) }
    var showNotifications by remember { mutableStateOf(false) }

    val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    val oldPlans = allPlans.filter { plan -> now - plan.date > oneMonthMillis }

    // Only notify if the most recent plan for each player is older than a month
    val lastPlansByPlayer = allPlans
        .groupBy { it.playerId }
        .mapNotNull { (_, plans) -> plans.maxByOrNull { it.date } }
    val playersWithOldLastPlan = lastPlansByPlayer.filter { plan -> now - plan.date > oneMonthMillis }

    val items by db.itemDao().getAllItems().collectAsState(initial = emptyList())
    val stockMovements by db.stockMovementDao().getAllMovements().collectAsState(initial = emptyList())
    val itemsWithStock = items.map { item ->
        val inQty = stockMovements.filter { it.itemId == item.id && it.type == "IN" }.sumOf { it.quantity }
        val outQty = stockMovements.filter { it.itemId == item.id && it.type == "OUT" }.sumOf { it.quantity }
        val currentStock = inQty - outQty
        item to currentStock
    }
    val lowStockItems = itemsWithStock.filter { (_, stock) -> stock < 5 }
    val hasNotifications = incompletePlans.isNotEmpty() || playersWithOldLastPlan.isNotEmpty() || lowStockItems.isNotEmpty()

    // State to track dismissed notifications (by plan id)
    var dismissedNotificationIds by remember { mutableStateOf(setOf<Int>()) }
    val visibleIncompletePlans = incompletePlans.filter { it.id !in dismissedNotificationIds }
    val visibleOldPlans = playersWithOldLastPlan.filter { it.id !in dismissedNotificationIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row with title and notification icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التدريب",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(vertical = 32.dp)
            )
            BadgedBox(
                badge = {
                    if (hasNotifications) {
                        Badge {}
                    }
                }
            ) {
                IconButton(onClick = { showNotifications = true }) {
                    Icon(Icons.Default.Notifications, contentDescription = "الإشعارات")
                }
            }
        }

        // Centered buttons in the remaining space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("playerList") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("قائمة اللاعبين")
                }

                Button(
                    onClick = { navController.navigate("plans") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("الخطط")
                }

                Button(
                    onClick = { navController.navigate("calories_calculator") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("حساب السعرات")
                }

                Button(
                    onClick = { navController.navigate("finance") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("الإدارة المالية")
                }
            }
        }
    }
    // Notification Dialog
    if (showNotifications) {
        Dialog(onDismissRequest = { showNotifications = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 300.dp, max = 380.dp)
                ) {
                    Text(
                        "الإشعارات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    // Section: Incomplete Plans
                    if (visibleIncompletePlans.isNotEmpty()) {
                        Text("خطط غير مكتملة:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        visibleIncompletePlans.forEach { plan ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Box {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("الخطة: ${plan.name}", fontWeight = FontWeight.Bold)
                                        Text("اللاعب: ${playerNameMap[plan.playerId] ?: "لاعب مجهول"}", style = MaterialTheme.typography.bodySmall)
                                        Button(
                                            onClick = {
                                                navController.navigate("player_progress_add/${plan.playerId}?planId=${plan.id}")
                                                showNotifications = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("إكمال الخطة")
                                        }
                                    }
                                    IconButton(
                                        onClick = { dismissedNotificationIds = dismissedNotificationIds + plan.id },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "إغلاق الإشعار")
                                    }
                                }
                            }
                        }
                    }

                    // Section: Old Plans
                    if (visibleOldPlans.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("خطط تجاوزت الشهر:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        visibleOldPlans.forEach { plan ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Box {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("الخطة: ${plan.name}", fontWeight = FontWeight.Bold)
                                        Text("اللاعب: ${playerNameMap[plan.playerId] ?: "لاعب مجهول"}", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "تاريخ الإنشاء: ${
                                                SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(plan.date))
                                            }",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Button(
                                            onClick = {
                                                navController.navigate("player_progress_add/${plan.playerId}?planId=${plan.id}")
                                                showNotifications = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("عرض الخطة")
                                        }
                                    }
                                    IconButton(
                                        onClick = { dismissedNotificationIds = dismissedNotificationIds + plan.id },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "إغلاق الإشعار")
                                    }
                                }
                            }
                        }
                    }

                    // Section: Store Low Stock
                    if (lowStockItems.isNotEmpty()) {
                        Text("منتجات المخزن أوشكت على النفاد:", fontWeight = FontWeight.SemiBold, color = Color(0xFFD32F2F))
                        Spacer(Modifier.height(8.dp))
                        lowStockItems.forEach { (item, stock) ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${item.name} (${item.type})", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
                                    Text("الكمية المتبقية: $stock", color = Color(0xFFD32F2F), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showNotifications = false },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
} 