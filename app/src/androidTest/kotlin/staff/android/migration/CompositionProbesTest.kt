package staff.android.migration

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import staff.android.saveditems.SavedItem

@RunWith(AndroidJUnit4::class)
class CompositionProbesTest {
    @get:Rule val compose = createComposeRule()

    private val camera = SavedItem("camera", "Camera", "travel")
    private val bag = SavedItem("bag", "Everyday bag", "work")

    @Test fun observableLocalStateOpensAndClosesTheNote() {
        compose.setContent { SavedItemsTheme { NoteDisclosure("travel") } }
        compose.onNodeWithText("travel").assertDoesNotExist()
        compose.onNodeWithText("Show note").performClick()
        compose.onNodeWithText("travel").assertIsDisplayed()
        compose.onNodeWithText("Hide note").performClick()
        compose.onNodeWithText("travel").assertDoesNotExist()
    }

    @Test fun keyedReorderingKeepsTheCameraNoteExpanded() {
        val catalog = mutableStateOf(listOf(camera, bag))
        compose.setContent { SavedItemsTheme { KeyedDisclosures(catalog.value) } }
        expandCamera()
        compose.runOnIdle { catalog.value = listOf(bag, camera) }
        compose.onNodeWithText("travel").assertIsDisplayed()
        compose.onNodeWithText("work").assertDoesNotExist()
    }

    @Test fun positionalReorderingMovesExpansionToTheBag() {
        val catalog = mutableStateOf(listOf(camera, bag))
        compose.setContent { SavedItemsTheme { PositionalDisclosures(catalog.value) } }
        expandCamera()
        compose.runOnIdle { catalog.value = listOf(bag, camera) }
        compose.onNodeWithText("work").assertIsDisplayed()
        compose.onNodeWithText("travel").assertDoesNotExist()
    }

    @Test fun clickableBeforePaddingIncludesTheOuterBand() {
        checkClickBounds(paddingInsideClick = true, expectedAfterBand = 1)
    }

    @Test fun paddingBeforeClickableExcludesTheOuterBand() {
        checkClickBounds(paddingInsideClick = false, expectedAfterBand = 0)
    }

    @Test fun acceptingParentPublishesThreeAfterTheRequest() {
        compose.setContent { SavedItemsTheme { QuantityParent(acceptRequests = true) } }
        compose.onNodeWithTag("increase").performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("3")
    }

    @Test fun rejectingParentKeepsTwoAfterTheRequest() {
        compose.setContent { SavedItemsTheme { QuantityParent(acceptRequests = false) } }
        compose.onNodeWithTag("increase").performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("2")
    }

    private fun expandCamera() {
        compose.onNode(
            hasText("Show note") and hasAnyAncestor(hasTestTag("disclosure-camera")),
        ).performClick()
        compose.onNodeWithText("travel").assertIsDisplayed()
    }

    private fun checkClickBounds(paddingInsideClick: Boolean, expectedAfterBand: Int) {
        var clicks = 0
        compose.setContent {
            ModifierOrderProbe(paddingInsideClick = paddingInsideClick) { clicks++ }
        }
        // Tap at 8/168 of the width: inside the 24dp band, outside the 120dp core.
        compose.onNodeWithTag("probe").performTouchInput {
            click(Offset(center.x * (8f / 84f), center.y))
        }
        compose.runOnIdle { assertEquals(expectedAfterBand, clicks) }
        compose.onNodeWithTag("probe").performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(expectedAfterBand + 1, clicks) }
    }
}
