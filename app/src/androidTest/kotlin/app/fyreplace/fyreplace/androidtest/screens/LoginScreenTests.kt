package app.fyreplace.fyreplace.androidtest.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import app.fyreplace.fyreplace.ui.screens.LoginScreenPreview
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTests {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() = composeRule.setContent { LoginScreenPreview() }

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
