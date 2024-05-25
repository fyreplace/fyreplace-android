package app.fyreplace.fyreplace.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import app.fyreplace.fyreplace.MainActivity
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import org.junit.Rule
import org.junit.Test

class NavigationTests {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testInitialScreenIsFeed() {
        for (destination in Destination.entries) {
            val node = composeRule.onNodeWithTag("screen:$destination")
            if (destination == Destination.FEED) {
                node.assertIsDisplayed()
            } else {
                node.assertIsNotDisplayed()
            }
        }
    }

    @Test
    fun testNavigationIsComplete() {
        for (destination in Destination.entries) {
            val node = composeRule.onNodeWithTag("navigation:$destination")

            if (destination.replacement == null) {
                node.assertIsDisplayed()
            } else if (node.isNotDisplayed()) {
                composeRule.onNodeWithTag("navigation:${destination.replacement}").performClick()
                node.assertIsDisplayed()
            }
        }
    }

    @Test
    fun testNavigationWorks() {
        for (destination in Destination.entries) {
            composeRule.onNodeWithTag("navigation:$destination").performClick()
            composeRule.onNodeWithTag("screen:$destination").assertIsDisplayed()
        }
    }
}
