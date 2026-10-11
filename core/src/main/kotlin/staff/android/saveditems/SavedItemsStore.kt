package staff.android.saveditems

// region state-model
data class SavedItem(
    val id: String,
    val title: String,
    val note: String,
    val saved: Boolean = true,
)

data class SavedItemsState(val items: List<SavedItem>) {
    val savedCount: Int
        get() = items.count { it.saved }

    val savedItems: List<SavedItem>
        get() = items.filter { it.saved }
}
// endregion state-model

// region sample-items
fun sampleItems(): List<SavedItem> = listOf(
    SavedItem(id = "camera", title = "Camera", note = "travel"),
    SavedItem(id = "bag", title = "Everyday bag", note = "work"),
    SavedItem(id = "headphones", title = "Headphones", note = "music"),
)
// endregion sample-items

/**
 * A small state owner shared by the Views and Compose screens.
 *
 * Calls must be serialized by the caller; this class does not provide synchronization.
 * Each update replaces the state rather than changing an earlier snapshot. The input
 * list is copied, while immutable item values can safely be shared. Consumers must
 * honor the read-only collection API: Kotlin's List is not a runtime mutation barrier.
 */
// region store-public
class SavedItemsStore(seed: List<SavedItem> = sampleItems()) {
    private val initialState = SavedItemsState(seed.toList()).also { state ->
        require(state.items.map { it.id }.distinct().size == state.items.size) {
            "Saved item IDs must be unique"
        }
    }
    private var current = initialState

    fun snapshot(): SavedItemsState = current

    fun toggleSaved(id: String) {
        updateItem(id) { item -> item.copy(saved = !item.saved) }
    }

    fun rename(id: String, title: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return

        updateItem(id) { item ->
            if (item.title == normalizedTitle) item else item.copy(title = normalizedTitle)
        }
    }

    fun reset() {
        current = initialState
    }
// endregion store-public

// region store-update
    private fun updateItem(id: String, transform: (SavedItem) -> SavedItem) {
        val index = current.items.indexOfFirst { it.id == id }
        if (index < 0) return

        val previousItem = current.items[index]
        val nextItem = transform(previousItem)
        if (nextItem == previousItem) return

        current = current.copy(
            items = current.items.mapIndexed { itemIndex, item ->
                if (itemIndex == index) nextItem else item
            },
        )
    }
}
// endregion store-update
