package staff.android.migration.exercise

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Deliberately defective independent exercise. The parent owns the accepted quantity.
 * Valid input is 0..5. Buttons should request a new value, not invent accepted state.
 * Keep this signature and the test tags; repair the implementation.
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    var displayed by remember { mutableIntStateOf(quantity) }
    Row {
        Button(
            onClick = { displayed-- }, enabled = displayed > 0,
            modifier = Modifier.testTag("decrease"),
        ) { Text("−") }
        Text(displayed.toString(), Modifier.testTag("quantity"))
        Button(
            onClick = { displayed++ }, enabled = displayed < 5,
            modifier = Modifier.testTag("increase"),
        ) { Text("+") }
    }
}
