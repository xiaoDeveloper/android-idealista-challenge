package com.xiao.idealistachallenge.ui.media

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil3.dispose
import coil3.load
import coil3.request.crossfade
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.databinding.ItemPropertyImageBinding
import com.xiao.idealistachallenge.model.PropertyImage
import com.xiao.idealistachallenge.model.PropertyImageTag

/**
 * A stable one-page-at-a-time media source. An empty source still renders one accessible
 * placeholder page, while each failed URL preserves its original page position.
 */
class PropertyImagePagerAdapter(
    private val onImageClick: (PropertyImage) -> Unit = {},
) : RecyclerView.Adapter<PropertyImagePagerAdapter.ImageViewHolder>() {
    private var images: List<PropertyImage> = emptyList()

    fun submitImages(items: List<PropertyImage>) {
        images = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = images.size.coerceAtLeast(1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder = ImageViewHolder(
        ItemPropertyImageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onImageClick,
    )

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images.getOrNull(position), position, itemCount)
    }

    class ImageViewHolder(
        private val binding: ItemPropertyImageBinding,
        private val onImageClick: (PropertyImage) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: PropertyImage?, position: Int, total: Int) {
            with(binding) {
            propertyImage.dispose()
            propertyImage.setImageDrawable(null)
            root.setOnClickListener(null)

            if (image == null) {
                showPlaceholder(R.string.property_image_placeholder_content_description)
                return@with
            }

            val imageDescription = image.contentDescription(root, position + 1, total)
            propertyImage.contentDescription = imageDescription
            propertyImage.isVisible = true
            propertyImage.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            propertyImagePlaceholder.isVisible = false
            root.setOnClickListener { onImageClick(image) }
            propertyImage.load(image.url) {
                crossfade(true)
                listener(
                    onError = { _, _ ->
                        showPlaceholder(
                            R.string.property_image_load_failed_content_description,
                            position + 1,
                            total,
                        )
                    },
                    onSuccess = { _, _ ->
                        propertyImage.isVisible = true
                        propertyImagePlaceholder.isVisible = false
                    },
                )
            }
            }
        }

        private fun showPlaceholder(descriptionRes: Int, vararg formatArgs: Any) = with(binding) {
            propertyImage.isVisible = false
            propertyImage.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            propertyImagePlaceholder.contentDescription = root.context.getString(descriptionRes, *formatArgs)
            propertyImagePlaceholder.isVisible = true
        }
    }
}

/** Configures a horizontal pager and reports its settled source position. */
fun RecyclerView.configurePropertyImagePager(onPageSettled: (Int) -> Unit) {
    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    val snapHelper = PagerSnapHelper()
    snapHelper.attachToRecyclerView(this)
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) return
            val snappedView = snapHelper.findSnapView(layoutManager) ?: return
            val position = getChildAdapterPosition(snappedView)
            if (position != RecyclerView.NO_POSITION) onPageSettled(position)
        }
    })
}

fun PropertyImage.displayLabel(context: Context): String? = semanticTag?.let { tag ->
    val tagLabel = when (tag) {
        PropertyImageTag.LIVING_ROOM -> R.string.property_image_tag_living_room
        PropertyImageTag.BEDROOM -> R.string.property_image_tag_bedroom
        PropertyImageTag.KITCHEN -> R.string.property_image_tag_kitchen
        PropertyImageTag.BATHROOM -> R.string.property_image_tag_bathroom
        PropertyImageTag.FACADE -> R.string.property_image_tag_facade
        PropertyImageTag.CORRIDOR -> R.string.property_image_tag_corridor
        PropertyImageTag.COMMUNAL_AREAS -> R.string.property_image_tag_communal_areas
    }
    context.getString(tagLabel)
} ?: localizedName

private fun PropertyImage.contentDescription(view: View, position: Int, total: Int): String {
    val label = displayLabel(view.context)
    return if (label == null) {
        view.context.getString(R.string.property_image_content_description_untagged, position, total)
    } else {
        view.context.getString(
            R.string.property_image_content_description_tagged,
            label,
            position,
            total,
        )
    }
}
