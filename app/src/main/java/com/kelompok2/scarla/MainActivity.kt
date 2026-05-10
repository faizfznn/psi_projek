package com.kelompok2.scarla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kelompok2.scarla.navigation.AppNavigation
import com.kelompok2.scarla.ui.theme.ScarlaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScarlaTheme {
                AppNavigation()
            }
        }
    }
}