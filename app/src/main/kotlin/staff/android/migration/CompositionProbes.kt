package staff.android.migration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import staff.android.migration.solution.QuantitySelectorSolution
import staff.android.saveditems.SavedItem

// These isolated teaching components run through CompositionProbesTest.
// They are not additional modes in the saved-items application.

// region note-disclosure
@Composable
fun NoteDisclosure(note: String) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Button(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Hide note" else "Show note")
        }
        if (expanded) Text(note)
    }
}
// endregion note-disclosure

// region disclosure-row
@Composable
private fun DisclosureRow(item: SavedItem) {
    Column(Modifier.testTag("disclosure-${item.id}")) {
        Text(item.title)
        NoteDisclosure(item.note)
    }
}
// endregion disclosure-row

// region keyed-disclosures
@Composable
fun KeyedDisclosures(catalog: List<SavedItem>) {
    LazyColumn {
        items(catalog, key = { it.id }) { item ->
            DisclosureRow(item)
        }
    }
}
// endregion keyed-disclosures

// region positional-disclosures
// Deliberate counterexample: disclosure follows the position.
@Composable
fun PositionalDisclosures(catalog: List<SavedItem>) {
    LazyColumn {
        items(catalog) { item ->
            DisclosureRow(item)
        }
    }
}
// endregion positional-disclosures

// region modifier-order-probe
@Composable
fun ModifierOrderProbe(
    paddingInsideClick: Boolean,
    onClick: () -> Unit,
) {
    val ordered = if (paddingInsideClick) {
        Modifier.clickable(onClick = onClick).padding(24.dp)
    } else {
        Modifier.padding(24.dp).clickable(onClick = onClick)
    }
    Box(
        Modifier.testTag("probe").size(168.dp)
            .background(Color.LightGray),
    ) {
        Box(ordered.size(120.dp).background(Color.DarkGray))
    }
}
// endregion modifier-order-probe

// region quantity-parent
@Composable
fun QuantityParent(acceptRequests: Boolean) {
    var accepted by remember { mutableIntStateOf(2) }
    QuantitySelectorSolution(
        quantity = accepted,
        onQuantityChange = { proposed ->
            if (acceptRequests) accepted = proposed
        },
    )
}
// endregion quantity-parent
