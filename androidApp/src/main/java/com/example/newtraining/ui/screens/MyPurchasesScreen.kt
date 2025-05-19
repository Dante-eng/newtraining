package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Item
import androidx.compose.ui.unit.LayoutDirection
import com.example.newtraining.data.entity.Payment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import androidx.compose.ui.platform.LocalContext
import com.example.newtraining.util.convertArabicNumberToEnglish
import com.example.newtraining.data.entity.StockMovement
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPurchasesScreen(navController: NavController, adminPlayerId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val player by db.playerDao().getPlayerById(adminPlayerId).collectAsState(initial = null)
    val sales by db.saleDao().getSalesForPlayer(adminPlayerId).collectAsState(initial = emptyList())
    val items by db.itemDao().getAllItems().collectAsState(initial = emptyList())
    val payments by db.paymentDao().getPaymentsForPlayer(adminPlayerId).collectAsState(initial = emptyList())
    val stockMovements by db.stockMovementDao().getAllMovements().collectAsState(initial = emptyList())
    val itemsWithStock = items.map { item ->
        val inQty = stockMovements.filter { it.itemId == item.id && it.type == "IN" }.sumOf { it.quantity }
        val outQty = stockMovements.filter { it.itemId == item.id && it.type == "OUT" }.sumOf { it.quantity }
        val currentStock = inQty - outQty
        item to currentStock
    }

    var payDebtDialog by remember { mutableStateOf(false) }
    var payDebtAmount by remember { mutableStateOf("") }
    var payDebtError by remember { mutableStateOf("") }
    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var purchaseQuantity by remember { mutableStateOf("") }
    var purchaseNote by remember { mutableStateOf("") }
    var purchaseError by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }

    val totalDebt = sales.sumOf { it.debt }

    // Low stock notification
    val lowStockItems = itemsWithStock.filter { (_, stock) -> stock < 5 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المخزن", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            if (totalDebt > 0.0) {
                FloatingActionButton(onClick = { payDebtDialog = true }) {
                    Text("سداد الدين")
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
            // Low stock notification (now inside the Column)
            if (lowStockItems.isNotEmpty()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E0)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "تنبيه: بعض المنتجات أوشكت على النفاد!",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(Modifier.height(8.dp))
                            lowStockItems.forEach { (item, stock) ->
                                Text(
                                    "${item.name} (${item.type}): الكمية المتبقية $stock",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }
            }
            Text("المخزون الحالي", fontSize = 16.sp, color = Color(0xFF3700B3), modifier = Modifier.fillMaxWidth())
            Button(onClick = { showAddPurchaseDialog = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                Text("إضافة شراء (إدخال مخزون)", color = Color.White)
            }
            // Inventory Table (RTL)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(Modifier.fillMaxWidth()) {
                    // Table Header
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE3E3F3))
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اسم المنتج", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("النوع", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("سعر الشراء", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("سعر البيع", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("المخزون الحالي", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    // Table Rows
                    itemsWithStock.forEachIndexed { idx, (item, stock) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if (idx % 2 == 0) Color.Transparent else Color(0xFFF7F7FB))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.name, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                            Text(item.type, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text(formatCurrency(item.price), modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text(formatCurrency(item.sellPrice ?: 0.0), modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text(stock.toString(), modifier = Modifier.weight(1.5f), color = if (stock > 0) Color(0xFF388E3C) else Color.Red, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
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

    // Pay Debt Dialog
    if (payDebtDialog) {
        AlertDialog(
            onDismissRequest = {
                payDebtDialog = false
                payDebtAmount = ""
                payDebtError = ""
            },
            title = { Text("سداد الدين") },
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
                        val playerSales = db.saleDao().getSalesForPlayer(adminPlayerId).first().filter { it.debt > 0 }.sortedBy { it.date }
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
                            db.paymentDao().insertPayment(
                                Payment(
                                    playerId = adminPlayerId,
                                    amount = pay,
                                    saleId = sale.id
                                )
                            )
                            remaining -= pay
                        }
                        payDebtDialog = false
                        payDebtAmount = ""
                        payDebtError = ""
                    }
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = {
                    payDebtDialog = false
                    payDebtAmount = ""
                    payDebtError = ""
                }) { Text("إلغاء") }
            }
        )
    }

    // Add Purchase Dialog
    if (showAddPurchaseDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddPurchaseDialog = false
                itemName = ""
                itemType = ""
                selectedItem = null
                purchaseQuantity = ""
                purchaseNote = ""
                purchaseError = ""
                purchasePrice = ""
                sellPrice = ""
            },
            title = { Text("إضافة شراء (إدخال مخزون)") },
            text = {
                Column {
                    // Item name with autocomplete
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { name ->
                            itemName = name
                            selectedItem = items.find { it.name == name }
                            if (selectedItem != null) {
                                itemType = selectedItem!!.type
                            }
                        },
                        label = { Text("اسم المنتج") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    // Suggestions
                    val suggestions = items.filter { it.name.contains(itemName, ignoreCase = true) && itemName.isNotBlank() && it.name != itemName }
                    if (suggestions.isNotEmpty()) {
                        Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Column {
                                suggestions.forEach { suggestion ->
                                    Text(
                                        suggestion.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                itemName = suggestion.name
                                                itemType = suggestion.type
                                                selectedItem = suggestion
                                            }
                                            .padding(8.dp),
                                        color = Color(0xFF3700B3)
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = itemType,
                        onValueChange = { if (selectedItem == null) itemType = it },
                        label = { Text("النوع") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedItem == null
                    )
                    OutlinedTextField(
                        value = purchaseQuantity,
                        onValueChange = { purchaseQuantity = it; purchaseError = "" },
                        label = { Text("الكمية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("سعر الشراء للوحدة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it },
                        label = { Text("سعر البيع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = purchaseNote,
                        onValueChange = { purchaseNote = it },
                        label = { Text("ملاحظة (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (purchaseError.isNotEmpty()) {
                        Text(purchaseError, color = Color.Red, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = convertArabicNumberToEnglish(purchaseQuantity).toIntOrNull() ?: 0
                    val price = convertArabicNumberToEnglish(purchasePrice).toDoubleOrNull() ?: 0.0
                    val name = itemName.trim()
                    val type = itemType.trim()
                    val sell = convertArabicNumberToEnglish(sellPrice).toDoubleOrNull() ?: 0.0
                    if (name.isBlank() || qty <= 0 || price <= 0.0 || type.isBlank() || sell <= 0.0) {
                        purchaseError = "أدخل اسم المنتج، النوع، الكمية، سعر الشراء وسعر البيع صحيحين"
                        return@TextButton
                    }
                    scope.launch {
                        val item = items.find { it.name == name }
                            ?: run {
                                val newId = db.itemDao().insertItem(Item(name = name, price = price, type = type, sellPrice = sell)).toInt()
                                db.itemDao().getItemById(newId)!!
                            }
                        // If existing item, update sell price if changed
                        if (item.sellPrice != sell) {
                            db.itemDao().updateItem(item.copy(sellPrice = sell))
                        }
                        db.stockMovementDao().insertMovement(
                            StockMovement(
                                itemId = item.id,
                                type = "IN",
                                quantity = qty,
                                note = purchaseNote.takeIf { it.isNotBlank() },
                                purchasePrice = price
                            )
                        )
                        showAddPurchaseDialog = false
                        itemName = ""
                        itemType = ""
                        selectedItem = null
                        purchaseQuantity = ""
                        purchaseNote = ""
                        purchaseError = ""
                        purchasePrice = ""
                        sellPrice = ""
                    }
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddPurchaseDialog = false
                    itemName = ""
                    itemType = ""
                    selectedItem = null
                    purchaseQuantity = ""
                    purchaseNote = ""
                    purchaseError = ""
                    purchasePrice = ""
                    sellPrice = ""
                }) { Text("إلغاء") }
            }
        )
    }
}

// Helper composable for dropdown
@Composable
fun DropdownMenuBox(
    items: List<Item>,
    selectedItem: Item?,
    onItemSelected: (Item) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedItem?.name ?: label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(item.name) }, onClick = {
                    onItemSelected(item)
                    expanded = false
                })
            }
        }
    }
} 