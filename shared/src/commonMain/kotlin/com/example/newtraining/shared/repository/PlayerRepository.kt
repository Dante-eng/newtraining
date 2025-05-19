package com.example.newtraining.shared.repository

import com.example.newtraining.shared.model.Player

interface PlayerRepository {
    suspend fun getAllPlayers(): List<Player>
    suspend fun getPlayerById(id: Long): Player?
    suspend fun insertPlayer(player: Player): Long
    suspend fun updatePlayer(player: Player)
    suspend fun deletePlayer(id: Long)
} 