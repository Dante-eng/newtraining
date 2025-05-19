package com.example.newtraining.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newtraining.data.AppDatabase
import com.example.newtraining.data.entity.Player
import kotlinx.coroutines.launch

class PlayersListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val playerDao = database.playerDao()

    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.updatePlayer(player)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.deletePlayer(player)
        }
    }
} 