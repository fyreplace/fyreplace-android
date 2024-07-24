package app.fyreplace.fyreplace.androidtest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import app.fyreplace.fyreplace.MainActivity
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTests {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() = hiltRule.inject()

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

            if (destination.parent == null) {
                node.assertIsDisplayed()
            } else if (node.isNotDisplayed()) {
                composeRule.onNodeWithTag("navigation:${destination.parent}").performClick()
                node.assertIsDisplayed()
            }
        }
    }

    @Test
    fun testNavigationShowsCorrectScreen() {
        for (destination in Destination.entries) {
            composeRule.onNodeWithTag("navigation:$destination").performClick()
            val node = composeRule.onNodeWithTag("screen:$destination")

            if (destination.visible()) {
                node.assertIsDisplayed()
            } else {
                node.assertIsNotDisplayed()
            }
        }
    }
}
