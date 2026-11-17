package com.maskedparty.game

import android.app.Application
import com.maskedparty.game.data.GameRepository

/**
 * Application entry point.
 *
 * Wires the single [GameRepository] instance so screens and view models share
 * one source of truth for player names and high scores.
 */
class MaskedApp : Application() {

    /** Process-wide repository instance (kept out of DI frameworks on purpose: the app is small). */
    val gameRepository: GameRepository by lazy { GameRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MaskedApp
            private set
    }
}
