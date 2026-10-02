package com.example.ecofeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ecofeed.data.UserPreferences
import com.example.ecofeed.navigation.EcoFeedNavGraph
import com.example.ecofeed.ui.theme.EcoFeedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = UserPreferences(this)
        
        setContent {
            val isDarkMode by prefs.darkModeFlow.collectAsState(initial = false)

            EcoFeedTheme(darkTheme = isDarkMode) {
                EcoFeedNavGraph()
            }
        }
    }
}
