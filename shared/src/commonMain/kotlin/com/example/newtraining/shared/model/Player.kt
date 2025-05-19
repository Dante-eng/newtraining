package com.example.newtraining.shared.model

data class Player(
    val id: Long = 0,
    val uniqueId: String,
    val fullName: String,
    val age: Int,
    val height: Int,
    val gender: String,
    val medicalCondition: String,
    val pictureUri: String? = null
) 