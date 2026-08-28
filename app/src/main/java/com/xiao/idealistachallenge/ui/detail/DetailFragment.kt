package com.xiao.idealistachallenge.ui.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xiao.idealistachallenge.App
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.FavoriteDateFormatter
import com.xiao.idealistachallenge.databinding.FragmentDetailBinding
import com.xiao.idealistachallenge.model.PropertyImage
import com.xiao.idealistachallenge.ui.media.PropertyImagePagerAdapter
import com.xiao.idealistachallenge.ui.media.configurePropertyImagePager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailFragment : Fragment(R.layout.fragment_detail) {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val imagePagerAdapter = PropertyImagePagerAdapter()
    private var displayedImages: List<PropertyImage>? = null

    private val selectedAdId: String by lazy {
        requireArguments().getString(ARG_SELECTED_AD_ID)?.takeIf(String::isNotBlank)
            ?: error("Detail requires a selectedAdId navigation argument")
    }
    private val viewModel: DetailViewModel by viewModels {
        (requireActivity().application as App).container.viewModelFactory { container ->
            DetailViewModel(container.adRepository, container.favoriteRepository, selectedAdId, Dispatchers.IO)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)
        binding.retryButton.setOnClickListener { viewModel.retry() }
        binding.detailFavoriteButton.setOnClickListener { viewModel.toggleFavorite() }
        binding.detailImagePager.adapter = imagePagerAdapter
        binding.detailImagePager.configurePropertyImagePager(::updateImagePosition)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.uiState.collect(::render) }
        }
        viewModel.load()
    }

    override fun onDestroyView() {
        binding.detailImagePager.adapter = null
        displayedImages = null
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: DetailUiState) = with(binding) {
        loadingState.isVisible = state is DetailUiState.Loading
        detailContent.isVisible = state is DetailUiState.Content
        errorState.isVisible = state is DetailUiState.Error
        detailFavoriteButton.isEnabled = state is DetailUiState.Content
        when (state) {
            DetailUiState.Loading -> Unit
            is DetailUiState.Error -> {
                errorTitle.setText(state.userFacingError.titleResId)
                errorMessage.setText(state.userFacingError.messageResId)
            }
            is DetailUiState.Content -> bindContent(state)
        }
    }

    private fun bindContent(state: DetailUiState.Content) = with(binding) {
        val details = state.details
        val presentation = DetailPresentation(requireContext()).present(details)
        detailPropertyTypeOperation.text = presentation.typeAndOperation
        detailPropertyTypeOperation.isVisible = presentation.typeAndOperation != null
        detailPrice.text = presentation.price
        // Coordinates from the static response are deliberately not a location label.
        detailLocation.text = null
        detailLocation.isVisible = false
        detailEssentialFacts.text = presentation.essentialFacts.joinToString("\n")
        detailEssentialFacts.isVisible = presentation.essentialFacts.isNotEmpty()
        detailAdditionalCharacteristics.text = presentation.additionalCharacteristics.joinToString("\n")
        detailAdditionalCharacteristicsTitle.isVisible = presentation.additionalCharacteristics.isNotEmpty()
        detailAdditionalCharacteristics.isVisible = presentation.additionalCharacteristics.isNotEmpty()
        val description = details.description?.takeIf(String::isNotBlank)
        detailDescriptionTitle.isVisible = description != null
        detailDescription.text = description ?: getString(R.string.property_description_unavailable)
        detailEnergyConsumption.text = presentation.energyRows.getOrNull(0).orEmpty()
        detailEnergyConsumption.isVisible = presentation.energyRows.isNotEmpty()
        detailEnergyEmissions.text = presentation.energyRows.getOrNull(1).orEmpty()
        detailEnergyEmissions.isVisible = presentation.energyRows.size > 1
        detailEnergyTitle.isVisible = presentation.energyRows.isNotEmpty()
        bindFavorite(state.favoritedAtEpochMillis)
        bindImages(details.images)
    }

    private fun bindImages(images: List<PropertyImage>) {
        if (displayedImages == images) return
        displayedImages = images
        imagePagerAdapter.submitImages(images)
        binding.detailImagePager.scrollToPosition(0)
        updateImagePosition(0)
    }

    private fun updateImagePosition(position: Int) = with(binding) {
        val total = displayedImages.orEmpty().size
        detailImagePosition.isVisible = total > 1
        detailImagePosition.text = if (total > 1) getString(R.string.property_image_position, position + 1, total) else null
    }

    private fun bindFavorite(favoritedAtEpochMillis: Long?) = with(binding) {
        val isFavorite = favoritedAtEpochMillis != null
        detailFavoriteButton.isSelected = isFavorite
        detailFavoriteButton.setImageResource(if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        detailFavoriteDate.isVisible = isFavorite
        if (favoritedAtEpochMillis != null) {
            val savedDate = getString(R.string.favorite_saved_date, FavoriteDateFormatter.format(favoritedAtEpochMillis))
            detailFavoriteDate.text = savedDate
            detailFavoriteButton.contentDescription = getString(R.string.favorite_accessibility_remove_with_date, savedDate)
        } else detailFavoriteButton.contentDescription = getString(R.string.favorite_accessibility_save)
    }

    private companion object { const val ARG_SELECTED_AD_ID = "selectedAdId" }
}
