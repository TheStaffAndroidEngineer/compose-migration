package staff.android.migration

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.RadioGroup

/** Prevents word fragments in the teaching controls at large system font sizes. */
class RendererChoices(context: Context, attrs: AttributeSet? = null) : RadioGroup(context, attrs) {
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (resources.configuration.fontScale >= 1.4f) {
            orientation = VERTICAL
            for (index in 0 until childCount) {
                val child = getChildAt(index)
                child.layoutParams = (child.layoutParams as LayoutParams).apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    weight = 0f
                }
            }
        }
    }
}
