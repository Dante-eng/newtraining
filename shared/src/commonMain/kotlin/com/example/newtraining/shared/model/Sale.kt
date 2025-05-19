package com.example.newtraining.shared.model

data class Sale(
    val id: Long = 0,
    val itemId: Long,
    val playerId: Long,
    val price: Double,
    val discount: Double = 0.0,
    val paid: Double = 0.0,
    val debt: Double = 0.0,
    val date: Long
) 