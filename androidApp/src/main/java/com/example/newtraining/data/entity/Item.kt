package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val type: String, // supplement, hormone, vitamin, antibiotic, cloth, etc.
    val sellPrice: Double? = null
) 