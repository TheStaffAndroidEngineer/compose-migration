package staff.android.migration

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import staff.android.saveditems.SavedItemsStore

/** Activity-scoped, in-memory state. Configuration changes survive; process death does not. */
// region whole-class
class SavedItemsViewModel : ViewModel() {
    private val store = SavedItemsStore()
    private val mutableState = MutableStateFlow(store.snapshot())
    val state = mutableState.asStateFlow()

    @MainThread
    fun toggleSaved(id: String) {
        store.toggleSaved(id)
        mutableState.value = store.snapshot()
    }

    @MainThread
    fun renameCamera() {
        val title = state.value.items.first { it.id == "camera" }.title
        store.rename("camera", if (title == "Camera") "Camera Pro" else "Camera")
        mutableState.value = store.snapshot()
    }

    @MainThread
    fun reset() {
        store.reset()
        mutableState.value = store.snapshot()
    }
}
// endregion whole-class
