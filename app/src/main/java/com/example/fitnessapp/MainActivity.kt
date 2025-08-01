package com.example.fitnessapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.fitnessapp.ui.theme.FitnessappTheme
import com.example.fitnessapp.navigation.AppNavigation
import com.example.fitnessapp.views.ThemePreference
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themePref = ThemePreference(this)
        setContent {
            val isDarkMode = remember { mutableStateOf(themePref.isDarkMode) }
            FitnessappTheme(darkTheme = isDarkMode.value) {
                AppNavigation(
                    themePreference = themePref,
                    onThemeChange = {
                        isDarkMode.value = it
                        themePref.isDarkMode = it
                    }
                )
            }
        }
    }
}