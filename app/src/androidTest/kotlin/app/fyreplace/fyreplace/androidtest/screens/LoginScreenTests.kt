package app.fyreplace.fyreplace.androidtest.screens

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import app.fyreplace.fyreplace.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class LoginScreenTests {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.onNodeWithTag("navigation:settings").performClick()
        composeRule.onNodeWithTag("navigation:login").performClick()
    }

    @Test
    fun testIdentifierMustHaveCorrectLength() {
        val identifier = composeRule.onNodeWithTag("login:identifier")
            .assertIsDisplayed()
        val submit = composeRule.onNodeWithTag("login:submit")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        for (i in 1..2) {
            identifier.performTextInput("a")
            submit.assertIsNotEnabled()
        }

        identifier.performTextReplacement("a".repeat(254))
        submit.assertIsEnabled()
    }
}
