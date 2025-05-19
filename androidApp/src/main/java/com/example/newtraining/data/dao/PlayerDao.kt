package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY fullName ASC")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players WHERE id = :playerId")
    fun getPlayerById(playerId: Int): Flow<Player?>
} 