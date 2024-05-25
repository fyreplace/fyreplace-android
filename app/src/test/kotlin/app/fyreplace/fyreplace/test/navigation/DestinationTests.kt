package app.fyreplace.fyreplace.test.navigation

import app.fyreplace.fyreplace.ui.views.navigation.Destination
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationTests {
    @Test
    fun testDestinationsAreUnique() {
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
    fun testEssentialsAreFewer() {
        assertTrue(Destination.essentials.size < Destination.entries.size)
    }

    @Test
    fun testReplacementsDoNotLoop() {
        for (destination in Destination.entries) {
            assertNotEquals(destination, destination.replacement)
            assertNotEquals(destination, destination.replacement?.replacement)
        }
    }
}
