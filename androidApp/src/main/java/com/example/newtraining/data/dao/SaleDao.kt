package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Sale
import com.example.newtraining.data.entity.Payment
import kotlinx.coroutines.flow.Flow
import com.example.newtraining.data.entity.StockMovement

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE playerId = :playerId ORDER BY date DESC")
    fun getSalesForPlayer(playerId: Int): Flow<List<Sale>>

    @Insert
    suspend fun insertSale(sale: Sale)

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)
}

@Dao
interface PaymentDao {
    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE playerId = :playerId ORDER BY date DESC")
    fun getPaymentsForPlayer(playerId: Int): Flow<List<Payment>>
}

@Dao
interface StockMovementDao {
    @Insert
    suspend fun insertMovement(movement: StockMovement)

    @Query("SELECT * FROM stock_movements WHERE itemId = :itemId ORDER BY date DESC")
    fun getMovementsForItem(itemId: Int): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements ORDER BY date DESC")
    fun getAllMovements(): Flow<List<StockMovement>>
} 