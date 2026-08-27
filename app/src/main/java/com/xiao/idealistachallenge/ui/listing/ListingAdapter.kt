package com.xiao.idealistachallenge.ui.listing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.FavoriteDateFormatter
import com.xiao.idealistachallenge.databinding.ItemListingBinding
import com.xiao.idealistachallenge.model.PropertyAd
import coil3.dispose
import coil3.load
import coil3.request.crossfade
import java.text.NumberFormat
import java.util.Locale

class ListingAdapter(
    private val onItemClick: (String) -> Unit = {},
    private val onFavoriteClick: (ListingRowUiModel) -> Unit = {},
) : ListAdapter<ListingRowUiModel, ListingAdapter.ListingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder =
        ListingViewHolder(
            binding = ItemListingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onItemClick = onItemClick,
            onFavoriteClick = onFavoriteClick,
        )

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListingViewHolder(
        private val binding: ItemListingBinding,
        private val onItemClick: (String) -> Unit,
        private val onFavoriteClick: (ListingRowUiModel) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: ListingRowUiModel) {
            val ad = row.ad
            binding.root.setOnClickListener { onItemClick(ad.propertyCode) }
            binding.favoriteButton.setOnClickListener { onFavoriteClick(row) }

            binding.listingSummary.text = ad.summary(binding.root.resources)
            binding.listingPrice.text = ad.priceText()
            bindFacts(ad)
            bindFavorite(row)
            bindImage(ad)
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

        private fun bindFavorite(row: ListingRowUiModel) {
            val favoritedAt = row.favoritedAtEpochMillis
            val isFavorite = favoritedAt != null
            binding.favoriteButton.isSelected = isFavorite
            binding.favoriteButton.setImageResource(
                if (isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off,
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

        private fun bindImage(ad: PropertyAd) {
            binding.listingImage.dispose()
            binding.listingImage.setImageDrawable(null)
            showImagePlaceholder()

            val imageUrl = ad.thumbnailUrl?.takeIf { it.isNotBlank() }
                ?: ad.imageUrls.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: return

            binding.listingImage.load(imageUrl) {
                crossfade(true)
                listener(
                    onError = { _, _ -> showImagePlaceholder() },
                    onSuccess = { _, _ -> showImage() },
                )
            }
        }

        private fun showImagePlaceholder() {
            binding.listingImage.importantForAccessibility =
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
            binding.listingImagePlaceholder.isVisible = true
        }

        private fun showImage() {
            binding.listingImage.importantForAccessibility =
                View.IMPORTANT_FOR_ACCESSIBILITY_YES
            binding.listingImagePlaceholder.isVisible = false
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
