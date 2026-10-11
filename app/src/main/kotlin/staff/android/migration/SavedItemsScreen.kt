package staff.android.migration

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import staff.android.saveditems.SavedItem
import staff.android.saveditems.SavedItemsState

// region observed-route
@Composable
fun SavedItemsRoute(viewModel: SavedItemsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SavedItemsScreen(state = state, onToggle = viewModel::toggleSaved)
}
// endregion observed-route

// region frozen-route
/** Deliberately broken: caching a value is not subscribing to its source. */
@Composable
fun FrozenSnapshotRoute(viewModel: SavedItemsViewModel) {
    val frozen = remember { viewModel.state.value }
    SavedItemsScreen(state = frozen, onToggle = viewModel::toggleSaved)
}
// endregion frozen-route

// region stateless-screen
@Composable
fun SavedItemsScreen(state: SavedItemsState, onToggle: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("saved-items"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.headline),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    pluralStringResource(
                        R.plurals.saved_count, state.savedCount, state.savedCount,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.savedCount == 0) {
                    Text(
                        stringResource(R.string.saved_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        items(state.items, key = { it.id }) { item ->
            SavedItemRow(item, onToggle)
        }
    }
}
// endregion stateless-screen

// region saved-row
@Composable
private fun SavedItemRow(item: SavedItem, onToggle: (String) -> Unit) {
    val largeText = LocalDensity.current.fontScale >= 1.4f
    Column(Modifier.fillMaxWidth().testTag("item-${item.id}")) {
        if (largeText) {
            ProductPhoto(item.id, Modifier.fillMaxWidth().height(148.dp))
            Spacer(Modifier.height(16.dp))
            SavedItemDetails(item, onToggle)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProductPhoto(item.id, Modifier.size(112.dp))
                Box(Modifier.weight(1f)) { SavedItemDetails(item, onToggle) }
            }
        }
        HorizontalDivider(
            Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ProductPhoto(id: String, modifier: Modifier) {
    Image(
        painterResource(productIcon(id)), contentDescription = null,
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F4F6)),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun SavedItemDetails(item: SavedItem, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(item.title, style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.note, item.note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val action = stringResource(
            if (item.saved) R.string.remove_item else R.string.save_item, item.title,
        )
        OutlinedButton(
            onClick = { onToggle(item.id) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                .heightIn(min = 48.dp).semantics { contentDescription = action },
        ) {
            Icon(
                painterResource(
                    if (item.saved) R.drawable.ic_heart_filled
                    else R.drawable.ic_heart_outline,
                ),
                contentDescription = null, modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(if (item.saved) R.string.remove else R.string.save))
        }
    }
}
// endregion saved-row

// region product-icon
internal fun productIcon(id: String): Int = when (id) {
    "camera" -> R.drawable.product_camera
    "bag" -> R.drawable.product_bag
    else -> R.drawable.product_headphones
}
// endregion product-icon
