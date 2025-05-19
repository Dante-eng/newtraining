package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscle_groups")
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
) 