package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "players",
    indices = [Index(value = ["uniqueId"], unique = true)]
)
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uniqueId: String,
    val fullName: String,
    val age: Int,
    val height: Int,
    val gender: String,
    val medicalCondition: String,
    val pictureUri: String? = null
) 