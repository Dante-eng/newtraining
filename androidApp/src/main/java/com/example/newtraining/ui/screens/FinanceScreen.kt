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
import com.example.newtraining.data.entity.Payment
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import com.example.newtraining.ui.screens.formatCurrency
import com.example.newtraining.util.convertArabicNumberToEnglish
import java.util.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.first
import com.example.newtraining.data.entity.StockMovement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val items by db.itemDao().getAllItems().collectAsState(initial = emptyList())
    val players by db.playerDao().getAllPlayers().collectAsState(initial = emptyList())
    val sales by db.saleDao().getAllSales().collectAsState(initial = emptyList())

    // Only show players with at least one sale
    val playersWithSales = players.filter { player ->
        sales.any { it.playerId == player.id }
    }
    // Calculate total debt per player
    val playerDebts = playersWithSales.associateWith { player ->
        sales.filter { it.playerId == player.id }.sumOf { it.debt }
    }

    // State for new sale dialog
    var showSaleDialog by remember { mutableStateOf(false) }
    // State for pay debt dialog
    var payDebtPlayer by remember { mutableStateOf<Player?>(null) }
    var payDebtAmount by remember { mutableStateOf("") }
    var payDebtError by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المالية", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSaleDialog = true }) {
                Text("+")
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
            // My Purchases button
            Button(
                onClick = { navController.navigate("my_purchases") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text("المخزن")
            }
            Spacer(Modifier.height(8.dp))
            Text("اللاعبون والديون", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(playersWithSales) { player ->
                    val debt = playerDebts[player] ?: 0.0
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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                player.fullName,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate("player_sales_history/${player.id}") }
                            )
                            Text("الدين: ${formatCurrency(debt)}", fontSize = 14.sp, color = Color.Red)
                            if (debt > 0.0) {
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { payDebtPlayer = player }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                                    Text("سداد الدين", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaleDialog) {
        NewSaleDialog(
            navController = navController,
            onDismiss = { showSaleDialog = false },
            db = db,
            players = players,
            items = db.itemDao().getAllItems().collectAsState(initial = emptyList()).value,
            onSaleAdded = { showSaleDialog = false }
        )
    }

    // Pay Debt Dialog
    if (payDebtPlayer != null) {
        val player = payDebtPlayer!!
        val totalDebt = playerDebts[player] ?: 0.0
        AlertDialog(
            onDismissRequest = {
                payDebtPlayer = null
                payDebtAmount = ""
                payDebtError = ""
            },
            title = { Text("سداد الدين للاعب: ${player.fullName}") },
            text = {
                Column {
                    Text("الدين الحالي: ${formatCurrency(totalDebt)}", color = Color.Red)
                    OutlinedTextField(
                        value = payDebtAmount,
                        onValueChange = { payDebtAmount = it; payDebtError = "" },
                        label = { Text("المبلغ المدفوع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (payDebtError.isNotEmpty()) {
                        Text(payDebtError, color = Color.Red, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val payAmount = convertArabicNumberToEnglish(payDebtAmount).toDoubleOrNull() ?: 0.0
                    if (payAmount <= 0.0) {
                        payDebtError = "أدخل مبلغًا صحيحًا"
                        return@TextButton
                    }
                    if (payAmount > totalDebt) {
                        payDebtError = "المبلغ أكبر من الدين الحالي"
                        return@TextButton
                    }
                    scope.launch {
                        // Pay oldest debts first
                        val playerSales = db.saleDao().getSalesForPlayer(player.id).first().filter { it.debt > 0 }.sortedBy { it.date }
                        var remaining = payAmount
                        for (sale in playerSales) {
                            if (remaining <= 0) break
                            val pay = minOf(sale.debt, remaining)
                            db.saleDao().updateSale(
                                sale.copy(
                                    paid = sale.paid + pay,
                                    debt = sale.debt - pay
                                )
                            )
                            // Record payment for this sale
                            db.paymentDao().insertPayment(
                                Payment(
                                    playerId = player.id,
                                    amount = pay,
                                    saleId = sale.id
                                )
                            )
                            remaining -= pay
                        }
                        // Optionally, record a summary payment for the dialog
                        // db.paymentDao().insertPayment(Payment(playerId = player.id, amount = payAmount))
                        payDebtPlayer = null
                        payDebtAmount = ""
                        payDebtError = ""
                    }
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = {
                    payDebtPlayer = null
                    payDebtAmount = ""
                    payDebtError = ""
                }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun NewSaleDialog(
    navController: NavController,
    onDismiss: () -> Unit,
    db: AppDatabase,
    players: List<Player>,
    items: List<Item>,
    onSaleAdded: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var selectedItems by remember { mutableStateOf(mutableListOf<Pair<Item, String>>()) } // Item and quantity
    var discount by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf("") }
    var expandedPlayer by remember { mutableStateOf(false) }
    var expandedItem by remember { mutableStateOf(false) }
    var selectedItemToAdd by remember { mutableStateOf<Item?>(null) }
    var itemQuantity by remember { mutableStateOf("") }

    // Live calculation inside the composable
    val total = selectedItems.sumOf { (item, qtyStr) ->
        val qty = qtyStr.toIntOrNull() ?: 1
        (item.sellPrice ?: item.price) * qty
    }
    val discountValue = convertArabicNumberToEnglish(discount).toDoubleOrNull() ?: 0.0
    val paidValue = convertArabicNumberToEnglish(paid).toDoubleOrNull() ?: 0.0
    val debt = total - discountValue - paidValue

    AlertDialog(
        modifier = Modifier.width(1000.dp),
        onDismissRequest = onDismiss,
        title = { Text("عملية بيع جديدة") },
        text = {
            Column {
                // Player Dropdown
                Box {
                    OutlinedButton(onClick = { expandedPlayer = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedPlayer?.fullName ?: "اختر اللاعب")
                    }
                    DropdownMenu(expanded = expandedPlayer, onDismissRequest = { expandedPlayer = false }) {
                        players.forEach { player ->
                            DropdownMenuItem(text = { Text(player.fullName) }, onClick = {
                                selectedPlayer = player
                                expandedPlayer = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Add Item with quantity
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedItem = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedItemToAdd?.name ?: "اختر المنتج")
                        }
                        DropdownMenu(expanded = expandedItem, onDismissRequest = { expandedItem = false }) {
                            items.forEach { item ->
                                DropdownMenuItem(text = { Text(item.name) }, onClick = {
                                    selectedItemToAdd = item
                                    expandedItem = false
                                })
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        label = { Text("الكمية") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (selectedItemToAdd != null && (itemQuantity.toIntOrNull() ?: 0) > 0) {
                            selectedItems = selectedItems.toMutableList().apply { add(selectedItemToAdd!! to itemQuantity) }
                            selectedItemToAdd = null
                            itemQuantity = ""
                        }
                    }) {
                        Text("إضافة")
                    }
                }
                // List of selected items as a table
                if (selectedItems.isNotEmpty()) {
                    val headerColor = Color(0xFFE3E3F3)
                    val rowAltColor = Color(0xFFF7F7FB)
                    val borderColor = Color(0xFFBDBDBD)
                    val shape = RoundedCornerShape(8.dp)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .border(1.dp, borderColor, shape)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(headerColor)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("اسم المنتج و نوعه", modifier = Modifier.weight(3f).padding(horizontal = 10.dp), fontSize = 14.sp, color = Color(0xFF3700B3), textAlign = TextAlign.Right)
                                Text("سعر القطعة", modifier = Modifier.weight(2f).padding(horizontal = 8.dp), fontSize = 14.sp, color = Color(0xFF3700B3), textAlign = TextAlign.Left)
                                Text("السعر الكلي", modifier = Modifier.weight(2f).padding(horizontal = 8.dp), fontSize = 14.sp, color = Color(0xFF3700B3), textAlign = TextAlign.Left)
                                Spacer(Modifier.width(40.dp))
                            }
                            selectedItems.forEachIndexed { idx, (item, qty) ->
                                val qtyInt = qty.toIntOrNull() ?: 1
                                val unitPrice = item.sellPrice ?: item.price
                                val itemTotal = unitPrice * qtyInt
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 0) Color.Transparent else rowAltColor)
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${item.name} (${item.type}) × $qtyInt", modifier = Modifier.weight(3f).padding(horizontal = 10.dp), fontSize = 14.sp, textAlign = TextAlign.Right, maxLines = 2)
                                    Text(formatCurrency(unitPrice), modifier = Modifier.weight(2f).padding(horizontal = 8.dp), fontSize = 14.sp, textAlign = TextAlign.Left, maxLines = 1)
                                    Text(formatCurrency(itemTotal), modifier = Modifier.weight(2f).padding(horizontal = 8.dp), fontSize = 14.sp, textAlign = TextAlign.Left, maxLines = 1)
                                    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                                        IconButton(onClick = {
                                            selectedItems = selectedItems.toMutableList().apply { removeAt(idx) }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("الخصم الكلي") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = paid,
                    onValueChange = { paid = it },
                    label = { Text("المدفوع") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Prominent total and debt display
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3E3F3))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي: ${formatCurrency(total)}",
                            color = Color(0xFF3700B3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "المتبقي كدين: ${formatCurrency(if (debt > 0) debt else 0.0)}",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Left
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedPlayer != null && selectedItems.isNotEmpty()) {
                    scope.launch {
                        // Distribute discount and paid proportionally to each item
                        var remainingDiscount = discountValue
                        var remainingPaid = paidValue
                        selectedItems.forEachIndexed { idx, (item, qtyStr) ->
                            val qty = qtyStr.toIntOrNull() ?: 1
                            val unitPrice = item.sellPrice ?: item.price
                            val itemTotal = unitPrice * qty
                            val itemDiscount = if (idx == selectedItems.lastIndex) remainingDiscount else (discountValue * (itemTotal / total)).coerceAtMost(remainingDiscount)
                            val itemPaid = if (idx == selectedItems.lastIndex) remainingPaid else (paidValue * (itemTotal / total)).coerceAtMost(remainingPaid)
                            val itemDebt = itemTotal - itemDiscount - itemPaid
                            db.saleDao().insertSale(
                                Sale(
                                    itemId = item.id,
                                    playerId = selectedPlayer!!.id,
                                    price = itemTotal,
                                    discount = itemDiscount,
                                    paid = itemPaid,
                                    debt = if (itemDebt > 0) itemDebt else 0.0
                                )
                            )
                            // Insert StockMovement (OUT)
                            db.stockMovementDao().insertMovement(
                                StockMovement(
                                    itemId = item.id,
                                    type = "OUT",
                                    quantity = qty,
                                    note = "Sale to player ${selectedPlayer!!.fullName}"
                                )
                            )
                            remainingDiscount -= itemDiscount
                            remainingPaid -= itemPaid
                        }
                        onSaleAdded()
                    }
                }
            }) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
} 