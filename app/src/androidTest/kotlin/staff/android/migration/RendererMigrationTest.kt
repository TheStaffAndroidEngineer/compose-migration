package staff.android.migration

import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RendererMigrationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openTeachingControls() {
        onView(withId(R.id.lab_controls_toggle)).perform(click())
    }

    @Test
    fun renameInViewsIsVisibleWhenComposeMounts() {
        onView(withId(R.id.rename_camera)).perform(click())
        awaitViewsAction("Remove Camera Pro")

        onView(withId(R.id.mode_compose)).perform(click())

        compose.onNodeWithText("Camera Pro").assertIsDisplayed()
        compose.onNodeWithContentDescription("Remove Camera Pro").assertIsDisplayed()
        assertStoreStatus("Camera Pro", savedCount = 3)

        // The same mounted composition must observe a later external command.
        onView(withId(R.id.rename_camera)).perform(click())
        compose.onNodeWithText("Camera").assertIsDisplayed()
        compose.onNodeWithText("Camera Pro").assertDoesNotExist()
        assertStoreStatus("Camera", savedCount = 3)
    }

    @Test
    fun mountedComposeFollowsBothRenameDirections() {
        onView(withId(R.id.mode_compose)).perform(click())
        compose.onNodeWithText("Camera").assertIsDisplayed()

        onView(withId(R.id.rename_camera)).perform(click())
        compose.onNodeWithText("Camera Pro").assertIsDisplayed()
        compose.onNodeWithText("Camera").assertDoesNotExist()

        onView(withId(R.id.rename_camera)).perform(click())
        compose.onNodeWithText("Camera").assertIsDisplayed()
        compose.onNodeWithText("Camera Pro").assertDoesNotExist()
        assertStoreStatus("Camera", savedCount = 3)
    }

    @Test
    fun removeInComposeUpdatesTheHeaderAndViewsAction() {
        onView(withId(R.id.rename_camera)).perform(click())
        onView(withId(R.id.mode_compose)).perform(click())

        compose.onNodeWithContentDescription("Remove Camera Pro").performClick()

        compose.onNodeWithText("2 saved items").assertIsDisplayed()
        compose.onNodeWithContentDescription("Save Camera Pro").assertIsDisplayed()
        assertStoreStatus("Camera Pro", savedCount = 2)

        onView(withId(R.id.mode_views)).perform(click())

        awaitViewsAction("Save Camera Pro")
        assertStoreStatus("Camera Pro", savedCount = 2)
    }

    @Test
    fun frozenRouteKeepsItsSnapshotUntilItIsMountedAgain() {
        onView(withId(R.id.mode_bug)).perform(click())
        compose.onNodeWithText("Camera").assertIsDisplayed()

        onView(withId(R.id.rename_camera)).perform(click())

        assertStoreStatus("Camera Pro", savedCount = 3)
        compose.onNodeWithText("Camera").assertIsDisplayed()
        compose.onNodeWithText("Camera Pro").assertDoesNotExist()

        // Hiding teaching instruments does not repair or remount the shopper screen.
        onView(withId(R.id.lab_controls_toggle)).perform(click())
        compose.onNodeWithText("Camera").assertIsDisplayed()
        onView(withId(R.id.lab_controls_toggle)).perform(click())
        assertStoreStatus("Camera Pro", savedCount = 3)
        compose.onNodeWithText("Camera").assertIsDisplayed()

        onView(withId(R.id.mode_compose)).perform(click())
        compose.onNodeWithText("Camera Pro").assertIsDisplayed()
        onView(withId(R.id.mode_bug)).perform(click())

        compose.onNodeWithText("Camera Pro").assertIsDisplayed()
        compose.onNodeWithText("Camera").assertDoesNotExist()
    }

    @Test
    fun activityRecreationRetainsTheSelectedModeAndViewModelState() {
        onView(withId(R.id.rename_camera)).perform(click())
        onView(withId(R.id.mode_compose)).perform(click())
        compose.onNodeWithContentDescription("Remove Camera Pro").performClick()
        compose.onNodeWithText("2 saved items").assertIsDisplayed()

        // Recreating an Activity exercises configuration-change retention, not process death.
        compose.activityRule.scenario.recreate()

        onView(withId(R.id.mode_compose)).check(matches(isChecked()))
        compose.onNodeWithText("Camera Pro").assertIsDisplayed()
        compose.onNodeWithText("2 saved items").assertIsDisplayed()
        compose.onNodeWithContentDescription("Save Camera Pro").assertIsDisplayed()
        assertStoreStatus("Camera Pro", savedCount = 2)
    }

    private fun assertStoreStatus(title: String, savedCount: Int) {
        val expected = compose.activity.resources.getQuantityString(R.plurals.source_status, savedCount, title, savedCount)
        onView(withId(R.id.source_status)).check(matches(withText(expected)))
    }

    private fun awaitViewsAction(description: String) {
        // ListAdapter computes diffs off the main thread. Wait for the bound, visible
        // action rather than assuming that main-thread idleness completes the diff.
        compose.waitUntil(timeoutMillis = 5_000) {
            var found = false
            compose.activityRule.scenario.onActivity { activity ->
                val candidates = arrayListOf<View>()
                activity.findViewById<RecyclerView>(R.id.views_list).findViewsWithText(
                    candidates,
                    description,
                    View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION,
                )
                found = candidates.any {
                    it.isShown && it.contentDescription?.toString() == description
                }
            }
            found
        }
        onView(withContentDescription(description)).check(matches(isDisplayed()))
    }
}
