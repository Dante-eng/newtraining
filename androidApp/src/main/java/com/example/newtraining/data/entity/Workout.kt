package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscleGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("muscleGroupId")]
)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val muscleGroupId: Int,
    val name: String
) 