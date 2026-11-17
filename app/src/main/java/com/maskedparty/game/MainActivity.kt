package com.maskedparty.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.maskedparty.game.ui.theme.MaskedTheme
import com.maskedparty.game.ui.screens.RootScreen

/**
 * Single-activity host for the whole app (menu -> game -> results -> game-over).
 *
 * Edge-to-edge with the dark party theme; navigation is owned by the view model.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaskedTheme {
                MaterialTheme {
                    RootScreen(
                        repository = (application as MaskedApp).gameRepository
                    )
                }
            }
        }
    }
}
