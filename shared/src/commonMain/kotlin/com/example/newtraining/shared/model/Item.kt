package com.example.newtraining.shared.model

data class Item(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val type: String,
    val sellPrice: Double? = null
) 