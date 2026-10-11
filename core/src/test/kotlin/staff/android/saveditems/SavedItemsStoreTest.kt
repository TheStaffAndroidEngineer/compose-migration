package staff.android.saveditems

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SavedItemsStoreTest {
    @Test
    fun `default catalog starts with three saved items`() {
        val state = SavedItemsStore().snapshot()

        assertEquals(
            listOf(
                SavedItem("camera", "Camera", "travel"),
                SavedItem("bag", "Everyday bag", "work"),
                SavedItem("headphones", "Headphones", "music"),
            ),
            state.items,
        )
        assertEquals(3, state.savedCount)
        assertEquals(state.items, state.savedItems)
    }

    @Test
    fun `toggle targets an ID and preserves catalog order and unsaved items`() {
        val store = SavedItemsStore()

        store.toggleSaved("bag")

        val state = store.snapshot()
        assertEquals(listOf("camera", "bag", "headphones"), state.items.map { it.id })
        assertTrue(state.items[0].saved)
        assertFalse(state.items[1].saved)
        assertTrue(state.items[2].saved)
        assertEquals(2, state.savedCount)
        assertEquals(listOf("camera", "headphones"), state.savedItems.map { it.id })

        store.toggleSaved("bag")

        assertEquals(sampleItems(), store.snapshot().items)
        assertEquals(3, store.snapshot().savedCount)
    }

    @Test
    fun `earlier snapshots keep their values through successive changes`() {
        val store = SavedItemsStore()
        val before = store.snapshot()

        store.toggleSaved("camera")
        val afterToggle = store.snapshot()
        store.rename("camera", "Camera Pro")
        val afterRename = store.snapshot()

        assertEquals(SavedItem("camera", "Camera", "travel"), before.items[0])
        assertEquals(SavedItem("camera", "Camera", "travel", saved = false), afterToggle.items[0])
        assertEquals(SavedItem("camera", "Camera Pro", "travel", saved = false), afterRename.items[0])
        assertEquals(3, before.savedCount)
        assertEquals(2, afterToggle.savedCount)
        assertNotSame(before, afterToggle)
        assertNotSame(afterToggle, afterRename)
    }

    @Test
    fun `constructor detaches the caller's mutable list`() {
        val input = sampleItems().toMutableList()
        val store = SavedItemsStore(input)

        input[0] = input[0].copy(title = "Changed outside the store")
        input.removeAt(1)
        input.add(SavedItem("tripod", "Tripod", "travel"))

        assertEquals(sampleItems(), store.snapshot().items)
    }

    @Test
    fun `updates share unchanged item values but replace the changed item`() {
        val store = SavedItemsStore()
        val before = store.snapshot()

        store.rename("camera", "Camera Pro")

        val after = store.snapshot()
        assertNotSame(before.items, after.items)
        assertNotSame(before.items[0], after.items[0])
        assertSame(before.items[1], after.items[1])
        assertSame(before.items[2], after.items[2])
    }

    @Test
    fun `rename trims whitespace and preserves other fields`() {
        val store = SavedItemsStore(listOf(SavedItem("camera", "Camera", "travel", saved = false)))

        store.rename("camera", " \tCamera Pro\n")

        assertEquals(
            SavedItem("camera", "Camera Pro", "travel", saved = false),
            store.snapshot().items.single(),
        )
    }

    @Test
    fun `unknown IDs blank names and unchanged names retain the same state`() {
        val store = SavedItemsStore()
        val before = store.snapshot()

        store.toggleSaved("missing")
        assertSame(before, store.snapshot())
        store.rename("missing", "New title")
        assertSame(before, store.snapshot())
        store.rename("camera", " \t\n")
        assertSame(before, store.snapshot())
        store.rename("camera", " Camera ")
        assertSame(before, store.snapshot())
    }

    @Test
    fun `duplicate IDs are rejected even when item contents differ`() {
        assertThrows(IllegalArgumentException::class.java) {
            SavedItemsStore(
                listOf(
                    SavedItem("camera", "Camera", "travel"),
                    SavedItem("camera", "Another camera", "work", saved = false),
                ),
            )
        }
    }

    @Test
    fun `reset restores the detached initial seed without changing captured history`() {
        val original = listOf(
            SavedItem("custom", "Custom item", "original note", saved = false),
            SavedItem("other", "Other item", "another note"),
        )
        val input = original.toMutableList()
        val store = SavedItemsStore(input)
        input.clear()

        store.toggleSaved("custom")
        store.rename("other", "Renamed item")
        val beforeReset = store.snapshot()
        store.reset()

        assertEquals(original, store.snapshot().items)
        assertTrue(beforeReset.items[0].saved)
        assertEquals("Renamed item", beforeReset.items[1].title)

        store.rename("custom", "Changed after reset")
        store.reset()
        assertEquals(original, store.snapshot().items)
    }

    @Test
    fun `an empty catalog supports all commands and derived values`() {
        val store = SavedItemsStore(emptyList())

        store.toggleSaved("missing")
        store.rename("missing", "New title")
        store.reset()

        assertTrue(store.snapshot().items.isEmpty())
        assertTrue(store.snapshot().savedItems.isEmpty())
        assertEquals(0, store.snapshot().savedCount)
    }
}
