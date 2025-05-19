package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vitamins")
data class Vitamin(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val dosage: String = "",
    val frequency: String = ""
) 