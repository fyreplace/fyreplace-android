package app.fyreplace.fyreplace.androidtest.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import app.fyreplace.fyreplace.ui.screens.RegisterScreenPreview
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterScreenTests {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() = composeRule.setContent { RegisterScreenPreview() }

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
