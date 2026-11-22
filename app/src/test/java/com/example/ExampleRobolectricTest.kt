package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mono PlayKit", appName)
  }

  @Test
  fun `main activity launches and renders mono playkit ui`() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Mono PlayKit").assertIsDisplayed()
    composeTestRule.mainClock.advanceTimeBy(1200L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome to Mono PlayKit").assertIsDisplayed()
  }
}
