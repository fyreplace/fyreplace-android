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
class RegisterScreenTests {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.onNodeWithTag("navigation:settings").performClick()
        composeRule.onNodeWithTag("navigation:register").performClick()
    }

    @Test
    fun testUsernameMustHaveCorrectLength() {
        composeRule.onNodeWithTag("register:email")
            .assertIsDisplayed()
            .performTextReplacement("email@example")
        val username = composeRule.onNodeWithTag("register:username")
            .assertIsDisplayed()
        val submit = composeRule.onNodeWithTag("register:submit")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        for (i in 1..2) {
            username.performTextInput("a")
            submit.assertIsNotEnabled()
        }

        username.performTextReplacement("a".repeat(50))
        submit.assertIsEnabled()
    }

    @Test
    fun testEmailMustHaveCorrectLength() {
        composeRule.onNodeWithTag("register:username")
            .assertIsDisplayed()
            .performTextReplacement("some_user")
        val email = composeRule.onNodeWithTag("register:email")
            .assertIsDisplayed()
        val submit = composeRule.onNodeWithTag("register:submit")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        for (i in 1..2) {
            email.performTextInput("@")
            submit.assertIsNotEnabled()
        }

        email.performTextReplacement("@".repeat(254))
        submit.assertIsEnabled()
    }

    @Test
    fun testEmailMustHaveAtSign() {
        composeRule.onNodeWithTag("register:username")
            .assertIsDisplayed()
            .performTextReplacement("some_user")
        val email = composeRule.onNodeWithTag("register:email")
            .assertIsDisplayed()
        val submit = composeRule.onNodeWithTag("register:submit")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        email.performTextReplacement("email")
        submit.assertIsNotEnabled()
        email.performTextInput("email@")
        submit.assertIsEnabled()
    }
}
