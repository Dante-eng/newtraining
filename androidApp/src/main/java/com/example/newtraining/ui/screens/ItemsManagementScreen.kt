package com.example.newtraining.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Item
import com.example.newtraining.util.convertArabicNumberToEnglish
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val items by db.itemDao().getAllItems().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }

    val filteredItems = if (searchQuery.text.isBlank()) items else items.filter {
        it.name.contains(searchQuery.text, ignoreCase = true) ||
        it.type.contains(searchQuery.text, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المنتجات", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingItem = null
                itemName = ""
                itemPrice = ""
                itemType = ""
                showDialog = true
            }) {
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث عن منتج أو نوع") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems) { item ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontSize = 16.sp, color = Color.Black)
                                Text("السعر: ${item.price} | النوع: ${item.type}", fontSize = 14.sp, color = Color.DarkGray)
                            }
                            IconButton(onClick = {
                                editingItem = item
                                itemName = item.name
                                itemPrice = item.price.toString()
                                itemType = item.type
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                            }
                            IconButton(onClick = {
                                scope.launch { db.itemDao().deleteItem(item) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                editingItem = null
                itemName = ""
                itemPrice = ""
                itemType = ""
            },
            title = { Text(if (editingItem == null) "إضافة منتج" else "تعديل المنتج") },
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("اسم المنتج") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("السعر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = itemType,
                        onValueChange = { itemType = it },
                        label = { Text("النوع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val price = convertArabicNumberToEnglish(itemPrice).toDoubleOrNull() ?: 0.0
                    if (itemName.isNotBlank() && price > 0.0) {
                        scope.launch {
                            if (editingItem == null) {
                                db.itemDao().insertItem(Item(name = itemName, price = price, type = itemType))
                            } else {
                                db.itemDao().updateItem(editingItem!!.copy(name = itemName, price = price, type = itemType))
                            }
                            showDialog = false
                            editingItem = null
                            itemName = ""
                            itemPrice = ""
                            itemType = ""
                        }
                    }
                }) {
                    Text(if (editingItem == null) "إضافة" else "حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    editingItem = null
                    itemName = ""
                    itemPrice = ""
                    itemType = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }
} 