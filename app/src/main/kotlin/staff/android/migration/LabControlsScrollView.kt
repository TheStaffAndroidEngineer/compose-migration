package staff.android.migration

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/** Keeps the teaching instruments reachable without taking the whole product viewport. */
class LabControlsScrollView(context: Context, attrs: AttributeSet? = null) : ScrollView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maximum = (resources.displayMetrics.heightPixels * 0.38f).toInt()
        val available = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) maximum
            else minOf(maximum, MeasureSpec.getSize(heightMeasureSpec))
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(available, MeasureSpec.AT_MOST))
    }
}
