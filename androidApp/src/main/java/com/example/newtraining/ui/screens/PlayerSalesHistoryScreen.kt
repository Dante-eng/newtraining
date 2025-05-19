package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Item
import com.example.newtraining.data.entity.Player
import com.example.newtraining.data.entity.Sale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.*
import com.example.newtraining.ui.screens.formatCurrency
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSalesHistoryScreen(navController: NavController, playerId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val player by db.playerDao().getPlayerById(playerId).collectAsState(initial = null)
    val sales by db.saleDao().getSalesForPlayer(playerId).collectAsState(initial = emptyList())
    val items by db.itemDao().getAllItems().collectAsState(initial = emptyList())
    val payments by db.paymentDao().getPaymentsForPlayer(playerId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل المشتريات", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(player?.fullName ?: "لاعب", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            // Group sales by date (to the day)
            val groupedSales = sales.groupBy { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(it.date)) }
            LazyColumn(modifier = Modifier.weight(1f)) {
                groupedSales.entries.sortedByDescending { it.key }.forEach { (dateStr, salesList) ->
                    val date = dateStr
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3E3F3)
                            )
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(date, fontSize = 16.sp, color = Color(0xFF3700B3), modifier = Modifier.fillMaxWidth())
                                    Spacer(Modifier.height(6.dp))
                                    // Table header (RTL order: المنتج | النوع | الكمية | سعر القطعة | السعر الكلي)
                                    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                                        Text("المنتج", modifier = Modifier.weight(2f), fontSize = 14.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text("النوع", modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text("الكمية", modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text("سعر القطعة", modifier = Modifier.weight(2f), fontSize = 14.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text("السعر الكلي", modifier = Modifier.weight(2f), fontSize = 14.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                    }
                                    salesList.forEach { sale ->
                                        val item = items.find { it.id == sale.itemId }
                                        val qty = (sale.price / (item?.price ?: 1.0)).toInt()
                                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                            Text(item?.name ?: "منتج", modifier = Modifier.weight(2f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                            Text(item?.type ?: "-", modifier = Modifier.weight(1f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                            Text("$qty", modifier = Modifier.weight(1f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                            Text(formatCurrency(item?.price ?: 0.0), modifier = Modifier.weight(2f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                            Text(formatCurrency(sale.price), modifier = Modifier.weight(2f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        }
                                    }
                                    Divider(Modifier.padding(vertical = 6.dp))
                                    // Summary row (RTL order)
                                    val totalPrice = salesList.sumOf { it.price }
                                    val totalPaid = salesList.sumOf { it.paid }
                                    val totalDiscount = salesList.sumOf { it.discount }
                                    val totalDebt = salesList.sumOf { it.debt }
                                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(":الإجمالي", modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFF3700B3), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text(formatCurrency(totalPrice), modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFF3700B3), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text(":المدفوع", modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFF388E3C), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text(formatCurrency(totalPaid), modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFF388E3C), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                    }
                                    Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(":الخصم", modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFF1976D2), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text(formatCurrency(totalDiscount), modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFF1976D2), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text(":الدين", modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFFD32F2F), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                        Text(formatCurrency(totalDebt), modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color(0xFFD32F2F), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            // Payment history section
            if (payments.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3E3F3))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("سجل المدفوعات", fontSize = 16.sp, color = Color(0xFF3700B3), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        payments.forEach { payment ->
                            val date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(payment.date))
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text("${formatCurrency(payment.amount)}", color = Color(0xFF388E3C), fontSize = 15.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                                Text(date, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.weight(2f), textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                            }
                        }
                    }
                }
            }
        }
    }
} 