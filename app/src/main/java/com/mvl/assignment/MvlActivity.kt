package com.mvl.assignment

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mvl.assignment.ui.navigation.AppNavHost
import com.mvl.assignment.ui.theme.MvlTheme
import dagger.hilt.android.AndroidEntryPoint

/** The app's single Activity: hosts the Compose navigation graph with all screens. */
@AndroidEntryPoint
class MvlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The theme is always light, so system-bar icons must stay dark even in system dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            MvlTheme {
                AppNavHost()
            }
        }
    }
}
