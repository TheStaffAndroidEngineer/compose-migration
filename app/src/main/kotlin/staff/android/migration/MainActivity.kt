package staff.android.migration

import android.os.Bundle
import android.content.res.Configuration
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

// region activity-fields
class MainActivity : ComponentActivity() {
    private val viewModel: SavedItemsViewModel by viewModels()
    private lateinit var composeHost: ComposeView
    private lateinit var viewsList: RecyclerView
    private var renderer = R.id.mode_views
    private var controlsExpanded = false
// endregion activity-fields

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        val lightTheme = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = lightTheme
            isAppearanceLightStatusBars = lightTheme
        }
        ViewCompat.setAccessibilityHeading(findViewById(R.id.screen_title), true)
        val root = findViewById<View>(R.id.root)
        val horizontalPadding = root.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            view.setPadding(
                horizontalPadding + bars.left, bars.top,
                horizontalPadding + bars.right, bars.bottom,
            )
            insets
        }
// region host-setup
        composeHost = findViewById(R.id.compose_host)
        composeHost.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        viewsList = findViewById(R.id.views_list)
        val adapter = ViewsSavedItemsAdapter(viewModel::toggleSaved)
        viewsList.layoutManager = LinearLayoutManager(this)
        viewsList.adapter = adapter
        // Avoid animation duration being mistaken for stale state in the lab.
        viewsList.itemAnimator = null
// endregion host-setup
// region host-controls
        controlsExpanded = savedInstanceState?.getBoolean("controlsExpanded") ?: false
        findViewById<Button>(R.id.lab_controls_toggle).setOnClickListener {
            controlsExpanded = !controlsExpanded
            updateControls()
        }
        findViewById<Button>(R.id.rename_camera).setOnClickListener { viewModel.renameCamera() }
        findViewById<Button>(R.id.reset).setOnClickListener { viewModel.reset() }
        val group = findViewById<RadioGroup>(R.id.renderer_group)
        renderer = savedInstanceState?.getInt("renderer", R.id.mode_views) ?: R.id.mode_views
        group.check(renderer)
        group.setOnCheckedChangeListener { _, checkedId -> showRenderer(checkedId) }
        showRenderer(renderer)
// endregion host-controls

        // region views-observation
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    adapter.submitList(state.items)
                    findViewById<TextView>(R.id.views_saved_count).text =
                        resources.getQuantityString(
                            R.plurals.saved_count, state.savedCount, state.savedCount,
                        )
                    findViewById<View>(R.id.views_empty_hint).visibility =
                        if (state.savedCount == 0) View.VISIBLE else View.GONE
                    val camera = state.items.first { it.id == "camera" }
                    findViewById<TextView>(R.id.source_status).text =
                        resources.getQuantityString(
                            R.plurals.source_status,
                            state.savedCount,
                            camera.title,
                            state.savedCount,
                        )
                }
            }
        }
        // endregion views-observation
    }

// region show-renderer
    private fun showRenderer(id: Int) {
        renderer = id
        // Visibility alone does not dispose a composition.
        // A mode switch is an explicit remount.
        composeHost.disposeComposition()
        val usingViews = id == R.id.mode_views
        findViewById<View>(R.id.views_container).visibility =
            if (usingViews) View.VISIBLE else View.GONE
        composeHost.visibility = if (usingViews) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.mode_hint).setText(
            if (id == R.id.mode_bug) R.string.bug_hint else R.string.live_hint,
        )
        updateControls()
        if (!usingViews) {
            composeHost.setContent {
                SavedItemsTheme {
                    if (id == R.id.mode_bug) FrozenSnapshotRoute(viewModel)
                    else SavedItemsRoute(viewModel)
                }
            }
        }
    }
// endregion show-renderer

    private fun updateControls() {
        findViewById<View>(R.id.lab_controls).visibility =
            if (controlsExpanded) View.VISIBLE else View.GONE
        val mode = getString(when (renderer) {
            R.id.mode_compose -> R.string.compose
            R.id.mode_bug -> R.string.snapshot_bug
            else -> R.string.views
        })
        findViewById<Button>(R.id.lab_controls_toggle).apply {
            text = getString(R.string.lab_controls, mode)
            ViewCompat.setStateDescription(this, if (controlsExpanded) "Expanded" else "Collapsed")
        }
    }

// region save-renderer
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("renderer", renderer)
        outState.putBoolean("controlsExpanded", controlsExpanded)
        super.onSaveInstanceState(outState)
    }
// endregion save-renderer
}
