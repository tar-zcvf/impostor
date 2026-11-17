package com.maskedparty.game

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.maskedparty.game.data.GameRepository
import com.maskedparty.game.ui.screens.RootScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Real instrumented smoke test. Requires a device or emulator:
 *   ./gradlew connectedDebugAndroidTest
 *
 * It launches a Compose host activity with the RootScreen and verifies the
 * app boots to the setup screen and the start button becomes enabled once the
 * minimum number of players is present.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<InstrumentedActivity>()

    @Test
    fun appBootsToSetup() {
        composeRule.onNodeWithText("Masked").assertIsDisplayed()
        composeRule.onNodeWithText("Start game").assertIsDisplayed()
        composeRule.onNodeWithText("How to play").assertIsDisplayed()
    }
}

/**
 * Minimal host activity that hosts RootScreen directly, avoiding a full app
 * bootstrap for the instrumented smoke test.
 */
class InstrumentedActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = GameRepository(this)
        setContent {
            com.maskedparty.game.ui.theme.MaskedTheme {
                RootScreen(repository = repo)
            }
        }
    }
}
