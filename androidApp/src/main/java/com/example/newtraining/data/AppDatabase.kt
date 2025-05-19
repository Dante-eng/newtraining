package com.example.newtraining.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.newtraining.data.dao.*
import com.example.newtraining.data.entity.*
import androidx.compose.runtime.collectAsState

@Database(
    entities = [
        MuscleGroup::class,
        Workout::class,
        Supplement::class,
        Hormone::class,
        Antibiotic::class,
        Vitamin::class,
        DietPlan::class,
        Player::class,
        WorkoutSet::class,
        TrainingPlan::class,
        Item::class,
        Sale::class,
        Payment::class,
        StockMovement::class,
        User::class
    ],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun supplementDao(): SupplementDao
    abstract fun hormoneDao(): HormoneDao
    abstract fun antibioticDao(): AntibioticDao
    abstract fun vitaminDao(): VitaminDao
    abstract fun dietPlanDao(): DietPlanDao
    abstract fun playerDao(): PlayerDao
    abstract fun setDao(): SetDao
    abstract fun trainingPlanDao(): TrainingPlanDao
    abstract fun itemDao(): ItemDao
    abstract fun saleDao(): SaleDao
    abstract fun paymentDao(): PaymentDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "training_database"
                )
                .
                fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 