package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemId: Int,
    val playerId: Int,
    val price: Double,
    val discount: Double = 0.0,
    val paid: Double = 0.0,
    val debt: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playerId: Int,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val saleId: Int? = null // Optional: link to a specific sale if needed
)

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemId: Int,
    val type: String, // "IN" for purchase, "OUT" for sale
    val quantity: Int,
    val date: Long = System.currentTimeMillis(),
    val referenceId: Int? = null, // saleId or purchaseId
    val note: String? = null,
    val purchasePrice: Double? = null
) 