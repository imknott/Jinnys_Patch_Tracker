package com.knottlabs.jinnyspatchtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.knottlabs.jinnyspatchtracker.ui.MainApp
import com.knottlabs.jinnyspatchtracker.ui.theme.JinnysPatchTrackerTheme

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
