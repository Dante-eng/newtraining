package com.example.newtraining.shared.repository

import com.example.newtraining.shared.model.Sale

interface SaleRepository {
    suspend fun getAllSales(): List<Sale>
    suspend fun getSaleById(id: Long): Sale?
    suspend fun getSalesByPlayerId(playerId: Long): List<Sale>
    suspend fun insertSale(sale: Sale): Long
    suspend fun updateSale(sale: Sale)
    suspend fun deleteSale(id: Long)
} 