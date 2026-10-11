package staff.android.saveditems

// region state-trace
fun main() {
    val store = SavedItemsStore()
    val captured = store.snapshot()
    printState("captured", captured)
    store.rename("camera", "Camera Pro")
    printState("after rename", store.snapshot())
    store.toggleSaved("camera")
    printState("after remove", store.snapshot())
    printState("captured again", captured)
}

private fun printState(label: String, state: SavedItemsState) {
    val camera = state.items.first { it.id == "camera" }
    println(
        "$label: title=${camera.title}; " +
            "saved=${camera.saved}; count=${state.savedCount}",
    )
}
// endregion state-trace
