package com.xiao.idealistachallenge.ui.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.dispose
import coil3.load
import coil3.request.crossfade
import com.xiao.idealistachallenge.App
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.FavoriteDateFormatter
import com.xiao.idealistachallenge.databinding.FragmentDetailBinding
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailFragment : Fragment(R.layout.fragment_detail) {
    private var _binding: FragmentDetailBinding? = null
    private val binding: FragmentDetailBinding get() = checkNotNull(_binding)

    private val selectedAdId: String by lazy {
        requireArguments().getString(ARG_SELECTED_AD_ID)
            ?.takeIf(String::isNotBlank)
            ?: error("Detail requires a selectedAdId navigation argument")
    }

    private val viewModel: DetailViewModel by viewModels {
        (requireActivity().application as App).container.viewModelFactory { container ->
            DetailViewModel(
                adRepository = container.adRepository,
                favoriteRepository = container.favoriteRepository,
                selectedAdId = selectedAdId,
                dispatcher = Dispatchers.IO,
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)
        binding.retryButton.setOnClickListener { viewModel.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
        viewModel.load()
    }

    override fun onDestroyView() {
        binding.detailImage.dispose()
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: DetailUiState) {
        val currentBinding = _binding ?: return
        currentBinding.loadingState.isVisible = state is DetailUiState.Loading
        currentBinding.detailContent.isVisible = state is DetailUiState.Content
        currentBinding.errorState.isVisible = state is DetailUiState.Error

        when (state) {
            DetailUiState.Loading -> Unit
            is DetailUiState.Error -> {
                currentBinding.errorTitle.setText(state.userFacingError.titleResId)
                currentBinding.errorMessage.setText(state.userFacingError.messageResId)
            }
            is DetailUiState.Content -> bindContent(state)
        }
    }

    private fun bindContent(state: DetailUiState.Content) = with(binding) {
        val details = state.details
        detailPrice.text = getString(
            R.string.detail_price,
            NumberFormat.getNumberInstance(Locale.getDefault()).format(details.price),
        )
        detailDescription.text = details.description?.takeIf(String::isNotBlank)
            ?: getString(R.string.property_description_unavailable)
        bindLocation(details.latitude?.toPlainString(), details.longitude?.toPlainString())
        bindCharacteristics(details.characteristics)
        bindFavorite(state.favoritedAtEpochMillis)
        bindImage(details.imageUrls.firstOrNull())
    }

    private fun bindLocation(latitude: String?, longitude: String?) = with(binding) {
        detailLocation.isVisible = latitude != null && longitude != null
        if (detailLocation.isVisible) {
            detailLocation.text = getString(R.string.property_coordinates, latitude, longitude)
        }
    }

    private fun bindCharacteristics(characteristics: Map<String, String>) = with(binding) {
        val text = characteristics.entries.joinToString("\n") { (name, value) -> "$name: $value" }
        detailCharacteristicsTitle.isVisible = text.isNotBlank()
        detailCharacteristics.isVisible = text.isNotBlank()
        detailCharacteristics.text = text
    }

    private fun bindFavorite(favoritedAtEpochMillis: Long?) = with(binding) {
        val isFavorite = favoritedAtEpochMillis != null
        detailFavoriteButton.isSelected = isFavorite
        detailFavoriteButton.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off,
        )
        detailFavoriteDate.isVisible = isFavorite
        if (favoritedAtEpochMillis != null) {
            val savedDate = getString(
                R.string.favorite_saved_date,
                FavoriteDateFormatter.format(favoritedAtEpochMillis),
            )
            detailFavoriteDate.text = savedDate
            detailFavoriteButton.contentDescription = getString(
                R.string.favorite_accessibility_remove_with_date,
                savedDate,
            )
        } else {
            detailFavoriteButton.contentDescription = getString(R.string.favorite_accessibility_save)
        }
    }

    private fun bindImage(imageUrl: String?) = with(binding) {
        detailImage.dispose()
        detailImage.setImageDrawable(null)
        detailImagePlaceholder.isVisible = true
        detailImage.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        imageUrl?.takeIf(String::isNotBlank)?.let { url ->
            detailImage.load(url) {
                crossfade(true)
                listener(
                    onError = { _, _ -> showImagePlaceholder() },
                    onSuccess = { _, _ -> showImage() },
                )
            }
        }
    }

    private fun showImagePlaceholder() = with(binding) {
        detailImage.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        detailImagePlaceholder.isVisible = true
    }

    private fun showImage() = with(binding) {
        detailImage.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        detailImagePlaceholder.isVisible = false
    }

    private companion object {
        const val ARG_SELECTED_AD_ID = "selectedAdId"
    }
}
