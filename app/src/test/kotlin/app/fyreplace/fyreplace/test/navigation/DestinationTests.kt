package app.fyreplace.fyreplace.test.navigation

import app.fyreplace.fyreplace.ui.views.navigation.Destination
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DestinationTests {
    @Test
    fun `Destinations are unique`() {
        for (first in Destination.entries) {
            for (second in Destination.entries.filterNot { it == first }) {
                assertNotEquals(first.route, second.route)
                assertNotEquals(first.activeIcon, second.activeIcon)
                assertNotEquals(first.inactiveIcon, second.inactiveIcon)
                assertNotEquals(first.labelRes, second.labelRes)
                assertNotEquals(first.content, second.content)
            }
        }
    }

    @Test
    fun `Destinations do not loop`() {
        for (destination in Destination.entries) {
            assertNotEquals(destination, destination.parent)
            assertNotEquals(destination, destination.parent?.parent)
        }
    }
}
