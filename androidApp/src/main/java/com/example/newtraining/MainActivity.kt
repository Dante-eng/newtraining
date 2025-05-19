package com.example.newtraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.newtraining.ui.navigation.AppNavigation
import com.example.newtraining.ui.theme.NewTrainingTheme
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.newtraining.data.entity.User
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewTrainingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = this
                    val db = remember { com.example.newtraining.data.AppDatabase.getDatabase(context) }
                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            if (db.userDao().getAllUsers().isEmpty()) {
                                db.userDao().insert(User(username = "hassan", password = "H12345h@"))
                            }
                        }
                    }
                    com.example.newtraining.ui.navigation.AppNavigation()
                }
            }
        }
    }
}