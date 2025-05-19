package com.example.newtraining.shared.repository

import com.example.newtraining.shared.model.Player
import com.example.newtraining.shared.db.AppDatabase
import com.example.newtraining.shared.db.Player as DbPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerRepositoryImpl(private val database: AppDatabase) : PlayerRepository {
    override suspend fun getAllPlayers(): List<Player> = withContext(Dispatchers.Default) {
        database.playerQueries.selectAll().executeAsList().map { it.toPlayer() }
    }

    override suspend fun getPlayerById(id: Long): Player? = withContext(Dispatchers.Default) {
        database.playerQueries.selectById(id).executeAsOneOrNull()?.toPlayer()
    }

    override suspend fun insertPlayer(player: Player): Long = withContext(Dispatchers.Default) {
        database.playerQueries.insertPlayer(
            uniqueId = player.uniqueId,
            fullName = player.fullName,
            age = player.age,
            height = player.height,
            gender = player.gender,
            medicalCondition = player.medicalCondition,
            pictureUri = player.pictureUri
        )
        database.playerQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updatePlayer(player: Player) = withContext(Dispatchers.Default) {
        database.playerQueries.updatePlayer(
            uniqueId = player.uniqueId,
            fullName = player.fullName,
            age = player.age,
            height = player.height,
            gender = player.gender,
            medicalCondition = player.medicalCondition,
            pictureUri = player.pictureUri,
            id = player.id
        )
    }

    override suspend fun deletePlayer(id: Long) = withContext(Dispatchers.Default) {
        database.playerQueries.deleteById(id)
    }
}

private fun DbPlayer.toPlayer() = Player(
    id = id,
    uniqueId = uniqueId,
    fullName = fullName,
    age = age,
    height = height,
    gender = gender,
    medicalCondition = medicalCondition,
    pictureUri = pictureUri
) 