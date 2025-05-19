package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "antibiotics")
data class Antibiotic(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = ""
) 