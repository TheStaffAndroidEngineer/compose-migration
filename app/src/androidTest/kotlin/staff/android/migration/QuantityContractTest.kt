package staff.android.migration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import staff.android.migration.exercise.QuantitySelector
import staff.android.migration.solution.QuantitySelectorSolution

/** Excluded from the normal suite; selected by the project's -Pexercise runner option. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExerciseOnly

abstract class QuantityContractTest {
    @get:Rule
    val compose = createComposeRule()

    @Composable
    protected abstract fun Selector(quantity: Int, onQuantityChange: (Int) -> Unit)

    @Test
    fun clicksRequestAChangeAndWaitForTheParentToAcceptIt() {
        val requests = mutableListOf<Int>()
        compose.setContent {
            SavedItemsTheme { Selector(quantity = 2, onQuantityChange = requests::add) }
        }

        compose.runOnIdle { assertTrue(requests.isEmpty()) }

        compose.onNodeWithTag("increase").performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("2")
        compose.runOnIdle { assertEquals(listOf(3), requests) }

        compose.onNodeWithTag("increase").performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("2")
        compose.runOnIdle { assertEquals(listOf(3, 3), requests) }

        compose.onNodeWithTag("decrease").performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("2")
        compose.runOnIdle { assertEquals(listOf(3, 3, 1), requests) }
    }

    @Test
    fun newParentValuesReplaceTheDisplayAndDriveTheNextRequest() {
        val parentQuantity = mutableIntStateOf(2)
        val requests = mutableListOf<Int>()
        compose.setContent {
            SavedItemsTheme {
                Selector(quantity = parentQuantity.intValue, onQuantityChange = requests::add)
            }
        }
        compose.onNodeWithTag("quantity").assertTextEquals("2")

        compose.runOnIdle { parentQuantity.intValue = 4 }

        compose.onNodeWithTag("quantity").assertTextEquals("4")
        compose.runOnIdle { assertTrue(requests.isEmpty()) }
        compose.onNodeWithTag("decrease").performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("4")
        compose.runOnIdle {
            assertEquals(listOf(3), requests)
            parentQuantity.intValue = 1
        }
        compose.onNodeWithTag("quantity").assertTextEquals("1")
    }

    @Test
    fun boundsDisableTheUnavailableActionAndKeepRequestsWithinZeroToFive() {
        val parentQuantity = mutableIntStateOf(0)
        val requests = mutableListOf<Int>()
        compose.setContent {
            SavedItemsTheme {
                Selector(quantity = parentQuantity.intValue, onQuantityChange = requests::add)
            }
        }

        compose.onNodeWithTag("quantity").assertTextEquals("0")
        compose.onNodeWithTag("decrease")
            .assertContentDescriptionEquals("Decrease quantity")
        compose.onNodeWithTag("increase")
            .assertContentDescriptionEquals("Increase quantity")
        compose.onNodeWithTag("decrease").assertIsNotEnabled()
        compose.onNodeWithTag("increase").assertIsEnabled().performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("0")
        compose.runOnIdle {
            assertEquals(listOf(1), requests)
            parentQuantity.intValue = 5
        }

        compose.onNodeWithTag("quantity").assertTextEquals("5")
        compose.onNodeWithTag("increase").assertIsNotEnabled()
        compose.onNodeWithTag("decrease").assertIsEnabled().performClick()
        compose.onNodeWithTag("quantity").assertTextEquals("5")
        compose.runOnIdle {
            assertEquals(listOf(1, 4), requests)
            assertTrue(requests.all { it in 0..5 })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeParentQuantityIsRejected() {
        compose.setContent { Selector(quantity = -1, onQuantityChange = {}) }
        compose.waitForIdle()
    }

    @Test(expected = IllegalArgumentException::class)
    fun parentQuantityAboveFiveIsRejected() {
        compose.setContent { Selector(quantity = 6, onQuantityChange = {}) }
        compose.waitForIdle()
    }
}

@RunWith(AndroidJUnit4::class)
class ReferenceQuantityContract : QuantityContractTest() {
    @Composable
    override fun Selector(quantity: Int, onQuantityChange: (Int) -> Unit) {
        QuantitySelectorSolution(quantity = quantity, onQuantityChange = onQuantityChange)
    }
}

@ExerciseOnly
@RunWith(AndroidJUnit4::class)
class ExerciseQuantityContract : QuantityContractTest() {
    @Composable
    override fun Selector(quantity: Int, onQuantityChange: (Int) -> Unit) {
        QuantitySelector(quantity = quantity, onQuantityChange = onQuantityChange)
    }
}
