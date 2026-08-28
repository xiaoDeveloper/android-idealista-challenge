package com.xiao.idealistachallenge.ui.listing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.FavoriteDateFormatter
import com.xiao.idealistachallenge.databinding.ItemListingBinding
import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyHighlight
import com.xiao.idealistachallenge.ui.media.PropertyImagePagerAdapter
import com.xiao.idealistachallenge.ui.media.configurePropertyImagePager
import java.text.NumberFormat
import java.util.Locale

class ListingAdapter(
    private val onItemClick: (String) -> Unit = {},
    private val onFavoriteClick: (ListingRowUiModel) -> Unit = {},
) : ListAdapter<ListingRowUiModel, ListingAdapter.ListingViewHolder>(DIFF_CALLBACK) {

    private val pagePositions = mutableMapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder =
        ListingViewHolder(
            binding = ItemListingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onItemClick = onItemClick,
            onFavoriteClick = onFavoriteClick,
            onPageSettled = { propertyCode, position -> pagePositions[propertyCode] = position },
        )

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val row = getItem(position)
        holder.bind(row, pagePositions[row.ad.propertyCode] ?: 0)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<ListingRowUiModel>,
        currentList: MutableList<ListingRowUiModel>,
    ) {
        pagePositions.keys.retainAll(currentList.mapTo(mutableSetOf()) { it.ad.propertyCode })
    }

    class ListingViewHolder(
        private val binding: ItemListingBinding,
        private val onItemClick: (String) -> Unit,
        private val onFavoriteClick: (ListingRowUiModel) -> Unit,
        private val onPageSettled: (String, Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var boundPropertyCode: String? = null
        private val mediaAdapter = PropertyImagePagerAdapter {
            boundPropertyCode?.let(onItemClick)
        }

        init {
            binding.listingImagePager.adapter = mediaAdapter
            binding.listingImagePager.configurePropertyImagePager { position ->
                boundPropertyCode?.let { propertyCode ->
                    onPageSettled(propertyCode, position)
                    showImagePosition(position)
                }
            }
        }

        fun bind(row: ListingRowUiModel, restoredPagePosition: Int) {
            val ad = row.ad
            boundPropertyCode = ad.propertyCode
            binding.root.setOnClickListener { onItemClick(ad.propertyCode) }
            binding.favoriteButton.setOnClickListener { onFavoriteClick(row) }

            binding.listingPrice.text = ad.priceText()
            bindFacts(ad)
            bindHighlights(ad)
            binding.listingSummary.text = ad.summary(binding.root.resources)
            bindFavorite(row)
            bindMedia(ad, restoredPagePosition)
        }

        private fun bindFacts(ad: PropertyAd) {
            val facts = buildList {
                ad.sizeSquareMeters?.let { add(binding.root.resources.getString(R.string.property_size, it)) }
                ad.rooms?.let {
                    add(binding.root.resources.getQuantityString(R.plurals.property_rooms, it, it))
                }
                ad.bathrooms?.let {
                    add(binding.root.resources.getQuantityString(R.plurals.property_bathrooms, it, it))
                }
            }
            binding.listingFacts.isVisible = facts.isNotEmpty()
            binding.listingFacts.text = facts.joinToString(" · ")
        }

        private fun bindHighlights(ad: PropertyAd) {
            val highlightLabels = ad.highlights.mapNotNull { highlight ->
                when (highlight) {
                    PropertyHighlight.EXTERIOR -> binding.root.resources.getString(R.string.property_exterior)
                    PropertyHighlight.AIR_CONDITIONING -> binding.root.resources.getString(R.string.property_air_conditioning)
                    PropertyHighlight.STORAGE_ROOM -> binding.root.resources.getString(R.string.property_storage_room)
                    PropertyHighlight.INCLUDED_PARKING -> binding.root.resources.getString(R.string.property_parking_included)
                }
            }
            binding.listingHighlights.isVisible = highlightLabels.isNotEmpty()
            binding.listingHighlights.text = highlightLabels.joinToString(" · ")
        }

        private fun bindFavorite(row: ListingRowUiModel) {
            val favoritedAt = row.favoritedAtEpochMillis
            val isFavorite = favoritedAt != null
            binding.favoriteButton.isSelected = isFavorite
            binding.favoriteButton.setImageResource(
                if (isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border,
            )

            if (favoritedAt == null) {
                binding.favoriteDate.isVisible = false
                binding.favoriteButton.contentDescription =
                    binding.root.context.getString(R.string.favorite_accessibility_save)
                return
            }

            val date = FavoriteDateFormatter.format(favoritedAt)
            val savedDate = binding.root.context.getString(R.string.favorite_saved_date, date)
            binding.favoriteDate.text = savedDate
            binding.favoriteDate.isVisible = true
            binding.favoriteButton.contentDescription = binding.root.context.getString(
                R.string.favorite_accessibility_remove_with_date,
                savedDate,
            )
        }

        private fun bindMedia(ad: PropertyAd, restoredPagePosition: Int) {
            val images = ad.listingImages()
            mediaAdapter.submitImages(images)
            binding.listingImagePosition.isVisible = images.size > 1
            if (images.size > 1) {
                val position = restoredPagePosition.coerceIn(0, images.lastIndex)
                binding.listingImagePager.scrollToPosition(position)
                showImagePosition(position)
            } else {
                binding.listingImagePager.scrollToPosition(0)
            }
        }

        private fun showImagePosition(position: Int) {
            val total = mediaAdapter.itemCount
            if (total <= 1) return
            binding.listingImagePosition.text = binding.root.context.getString(
                R.string.property_image_position,
                position + 1,
                total,
            )
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListingRowUiModel>() {
            override fun areItemsTheSame(
                oldItem: ListingRowUiModel,
                newItem: ListingRowUiModel,
            ): Boolean = oldItem.ad.propertyCode == newItem.ad.propertyCode

            override fun areContentsTheSame(
                oldItem: ListingRowUiModel,
                newItem: ListingRowUiModel,
            ): Boolean = oldItem == newItem
        }
    }
}

private fun PropertyAd.summary(resources: android.content.res.Resources): String {
    val type = propertyType?.takeIf { it.isNotBlank() }
        ?: resources.getString(R.string.property_type_fallback)
    val location = listOf(address, district, municipality)
        .mapNotNull { it?.takeIf(String::isNotBlank) }
        .joinToString(", ")
        .ifBlank { resources.getString(R.string.property_location_fallback) }
    return "$type, $location"
}

private fun PropertyAd.priceText(): String {
    val number = NumberFormat.getNumberInstance(Locale.getDefault()).format(price)
    return listOf(number, currencySuffix?.takeIf { it.isNotBlank() })
        .filterNotNull()
        .joinToString(" ")
}
