package com.maskedparty.game

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

/**
 * JVM (Robolectric) launch smoke test for [MainActivity].
 *
 * Boots the real single-activity app and asserts a [ComposeView] is attached to
 * the window after layout. This runs headlessly (no ActivityScenarioRule, no
 * emulator), so it is part of `test` and CI.
 *
 * Note: Compose *interaction* tests (click-through) require a real display and
 * live as an instrumented test (see MainActivityInstrumentedTest). The complete
 * interactive game flow is covered deterministically in GameViewModelTest.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityComposeTest {

    @Test
    fun mainActivityBootsAndAttachesCompose() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val activity = controller.setup().get()

        // Let Robolectric run pending main-looper work (lifecycle, initial layout).
        ShadowLooper.idleMainLooper()

        val composeView = activity.window?.decorView?.let { findComposeView(it) }
        assertNotNull("Expected a ComposeView in the window hierarchy", composeView)
        ShadowLooper.idleMainLooper()
        assertTrue("ComposeView must be attached to the window", composeView!!.isAttachedToWindow)
    }

    private fun findComposeView(view: View): ComposeView? {
        if (view is ComposeView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findComposeView(view.getChildAt(i))?.let { return it }
            }
        }
        return null
    }
}
