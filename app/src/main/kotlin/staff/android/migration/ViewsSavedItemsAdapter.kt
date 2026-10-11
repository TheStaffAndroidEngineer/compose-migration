package staff.android.migration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import staff.android.saveditems.SavedItem

// region adapter-header
class ViewsSavedItemsAdapter(private val onToggle: (String) -> Unit) :
    ListAdapter<SavedItem, ViewsSavedItemsAdapter.Holder>(Diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_saved, parent, false),
        )

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position))
// endregion adapter-header

// region adapter-holder-diff
    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.product_title)
        private val note = view.findViewById<TextView>(R.id.product_note)
        private val icon = view.findViewById<ImageView>(R.id.product_icon)
        private val toggle = view.findViewById<Button>(R.id.toggle_saved)

        init {
            icon.clipToOutline = true
            if (view.resources.configuration.fontScale >= 1.4f) {
                val density = view.resources.displayMetrics.density
                view.findViewById<LinearLayout>(R.id.product_layout).orientation =
                    LinearLayout.VERTICAL
                icon.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (148 * density).toInt(),
                )
                icon.scaleType = ImageView.ScaleType.FIT_CENTER
                val details = view.findViewById<LinearLayout>(R.id.product_details)
                details.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = (16 * density).toInt() }
            }
        }

        fun bind(item: SavedItem) {
            title.text = item.title
            note.text = itemView.context.getString(R.string.note, item.note)
            icon.setImageResource(productIcon(item.id))
            toggle.setText(
                if (item.saved) R.string.remove
                else R.string.save,
            )
            toggle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (item.saved) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline,
                0, 0, 0,
            )
            toggle.contentDescription = itemView.context.getString(
                if (item.saved) R.string.remove_item
                else R.string.save_item,
                item.title,
            )
            // Bind stable identity. A position can change
            // during a diff.
            toggle.setOnClickListener { onToggle(item.id) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<SavedItem>() {
        override fun areItemsTheSame(
            oldItem: SavedItem, newItem: SavedItem,
        ) = oldItem.id == newItem.id
        override fun areContentsTheSame(
            oldItem: SavedItem, newItem: SavedItem,
        ) = oldItem == newItem
    }
}
// endregion adapter-holder-diff
