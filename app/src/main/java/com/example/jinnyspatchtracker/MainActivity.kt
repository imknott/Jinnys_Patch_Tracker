package com.example.jinnyspatchtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.jinnyspatchtracker.ui.MainApp
import com.example.jinnyspatchtracker.ui.theme.JinnysPatchTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JinnysPatchTrackerTheme {
                MainApp()
            }
        }
    }

}
