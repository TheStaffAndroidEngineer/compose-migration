package staff.android.migration.solution

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** A controlled input: only its parent can accept a new quantity. Valid input: 0..5. */
@Composable
fun QuantitySelectorSolution(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    require(quantity in 0..5)
    Row {
        Button(
            onClick = { onQuantityChange(quantity - 1) }, enabled = quantity > 0,
            modifier = Modifier.testTag("decrease")
                .semantics { contentDescription = "Decrease quantity" },
        ) { Text("−") }
        Text(quantity.toString(), Modifier.testTag("quantity"))
        Button(
            onClick = { onQuantityChange(quantity + 1) }, enabled = quantity < 5,
            modifier = Modifier.testTag("increase")
                .semantics { contentDescription = "Increase quantity" },
        ) { Text("+") }
    }
}
