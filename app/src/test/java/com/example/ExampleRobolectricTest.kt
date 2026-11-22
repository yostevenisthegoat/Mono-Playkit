package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
  fun `launcher intent adaptive icon and fonts resolve cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pkgInfo = context.packageManager.getPackageInfo(
      context.packageName,
      android.content.pm.PackageManager.GET_ACTIVITIES,
    )
    val activities = pkgInfo.activities?.map { it.name } ?: emptyList()
    assertTrue("MainActivity missing from manifest: $activities", activities.contains(MainActivity::class.java.name))

    val mainAct = pkgInfo.activities?.firstOrNull { it.name == MainActivity::class.java.name }
    assertNotNull(mainAct)
    assertTrue("MainActivity must be exported", mainAct!!.exported)

    assertNotNull(context.getDrawable(R.mipmap.ic_launcher))
    assertNotNull(context.getDrawable(R.mipmap.ic_launcher_round))
    assertNotNull(context.resources.getFont(R.font.space_grotesk))
    assertNotNull(context.resources.getFont(R.font.plus_jakarta_sans))
    assertNotNull(context.resources.getFont(R.font.jetbrains_mono))
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
