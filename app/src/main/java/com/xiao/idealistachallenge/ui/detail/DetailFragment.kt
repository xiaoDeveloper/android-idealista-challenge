package com.xiao.idealistachallenge.ui.detail

import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.xiao.idealistachallenge.App
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.FavoriteDateFormatter
import com.xiao.idealistachallenge.databinding.FragmentDetailBinding
import com.xiao.idealistachallenge.model.PropertyImage
import com.xiao.idealistachallenge.ui.media.PropertyImagePagerAdapter
import com.xiao.idealistachallenge.ui.media.configurePropertyImagePager
import com.xiao.idealistachallenge.ui.media.displayLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailFragment : Fragment(R.layout.fragment_detail), MenuProvider {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val imagePagerAdapter = PropertyImagePagerAdapter()
    private var displayedImages: List<PropertyImage>? = null
    private var favoriteAtEpochMillis: Long? = null
    private var hasContent = false

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
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        binding.retryButton.setOnClickListener { viewModel.retry() }
        binding.detailDescriptionToggle.setOnClickListener { viewModel.toggleDescriptionExpansion() }
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
        favoriteAtEpochMillis = null
        hasContent = false
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        val favoriteAction = menu.findItem(R.id.action_toggle_favorite)
        favoriteAction.isVisible = hasContent
        if (!hasContent) return

        val savedDate = favoriteAtEpochMillis?.let { epochMillis ->
            getString(R.string.favorite_saved_date, FavoriteDateFormatter.format(epochMillis))
        }
        val isFavorite = savedDate != null
        favoriteAction.isChecked = isFavorite
        favoriteAction.title = if (savedDate == null) {
            getString(R.string.favorite_accessibility_save)
        } else {
            getString(R.string.favorite_accessibility_remove_with_date, savedDate)
        }
        favoriteAction.icon = AppCompatResources.getDrawable(
            requireContext(),
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
        )?.also { icon ->
            DrawableCompat.setTint(
                icon,
                themeColor(
                    if (isFavorite) com.google.android.material.R.attr.colorPrimary
                    else com.google.android.material.R.attr.colorOnSurface,
                ),
            )
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.action_toggle_favorite -> {
            viewModel.toggleFavorite()
            true
        }
        else -> false
    }

    private fun render(state: DetailUiState) = with(binding) {
        hasContent = state is DetailUiState.Content
        favoriteAtEpochMillis = (state as? DetailUiState.Content)?.favoritedAtEpochMillis
        loadingState.isVisible = state is DetailUiState.Loading
        detailContent.isVisible = hasContent
        errorState.isVisible = state is DetailUiState.Error
        when (state) {
            DetailUiState.Loading -> Unit
            is DetailUiState.Error -> {
                errorTitle.setText(state.userFacingError.titleResId)
                errorMessage.setText(state.userFacingError.messageResId)
            }
            is DetailUiState.Content -> bindContent(state)
        }
        requireActivity().invalidateOptionsMenu()
    }

    private fun bindContent(state: DetailUiState.Content) = with(binding) {
        val details = state.details
        val presentation = DetailPresentation(requireContext()).present(details)
        detailPropertyTypeOperation.text = presentation.typeAndOperation
        detailPropertyTypeOperation.isVisible = presentation.typeAndOperation != null
        detailPrice.text = presentation.price
        detailLocation.text = presentation.location
        detailLocation.isVisible = presentation.location != null
        bindFacts(detailPrimaryFacts, presentation.primaryFacts, R.layout.item_detail_primary_fact)
        bindFacts(detailSecondaryFacts, presentation.secondaryFacts, R.layout.item_detail_secondary_fact)
        bindSavedState(state.favoritedAtEpochMillis)
        bindCharacteristics(presentation)
        bindDescription(state)
        bindEnergy(presentation)
        bindImages(details.images)
    }

    private fun bindFacts(group: ChipGroup, facts: List<String>, itemLayout: Int) {
        group.removeAllViews()
        facts.forEach { fact ->
            (layoutInflater.inflate(itemLayout, group, false) as Chip).also { chip ->
                chip.text = fact
                group.addView(chip)
            }
        }
        group.isVisible = facts.isNotEmpty()
    }

    private fun bindSavedState(favoritedAt: Long?) = with(binding) {
        detailSavedState.isVisible = favoritedAt != null
        if (favoritedAt != null) {
            detailSavedState.text = getString(
                R.string.favorite_saved_date,
                FavoriteDateFormatter.format(favoritedAt),
            )
        }
    }

    private fun bindCharacteristics(presentation: DetailPresentation.Content) = with(binding) {
        bindFacts(detailCharacteristicTags, presentation.characteristicTags, R.layout.item_detail_secondary_fact)
        val communityCosts = presentation.communityCosts
        detailCommunityCostsRow.isVisible = communityCosts != null
        if (communityCosts != null) {
            detailCommunityCostsLabel.text = communityCosts.label
            detailCommunityCostsValue.text = communityCosts.value
        }
        detailCharacteristicsSection.isVisible =
            presentation.characteristicTags.isNotEmpty() || communityCosts != null
    }

    private fun bindDescription(state: DetailUiState.Content) = with(binding) {
        val description = state.details.description?.takeIf(String::isNotBlank)
        detailDescriptionTitle.isVisible = description != null
        detailDescription.text = description ?: getString(R.string.property_description_unavailable)
        detailDescription.maxLines = Int.MAX_VALUE
        detailDescription.ellipsize = null
        detailDescriptionToggle.isVisible = state.isDescriptionExpanded
        detailDescriptionToggle.contentDescription = getString(
            if (state.isDescriptionExpanded) {
                R.string.description_collapse_content_description
            } else {
                R.string.description_expand_content_description
            },
        )
        detailDescriptionToggle.setText(
            if (state.isDescriptionExpanded) R.string.description_collapse else R.string.description_expand,
        )
        if (description != null && !state.isDescriptionExpanded) {
            detailDescription.post {
                if (binding.detailDescription.text.toString() == description) {
                    val exceedsPreview = detailDescription.lineCount > DESCRIPTION_PREVIEW_MAX_LINES
                    if (exceedsPreview) {
                        detailDescription.maxLines = DESCRIPTION_PREVIEW_MAX_LINES
                        detailDescription.ellipsize = TextUtils.TruncateAt.END
                        detailDescriptionToggle.isVisible = true
                    }
                }
            }
        }
    }

    private fun bindEnergy(presentation: DetailPresentation.Content) = with(binding) {
        bindEnergyCard(
            detailEnergyConsumptionCard,
            detailEnergyConsumptionLabel,
            detailEnergyConsumptionValue,
            presentation.energyConsumption,
        )
        bindEnergyCard(
            detailEnergyEmissionsCard,
            detailEnergyEmissionsLabel,
            detailEnergyEmissionsValue,
            presentation.energyEmissions,
        )
        detailEnergySection.isVisible =
            presentation.energyConsumption != null || presentation.energyEmissions != null
    }

    private fun bindEnergyCard(
        card: View,
        labelView: android.widget.TextView,
        valueView: android.widget.TextView,
        item: DetailPresentation.LabelValue?,
    ) {
        card.isVisible = item != null
        if (item != null) {
            labelView.text = item.label
            valueView.text = item.value
        }
    }

    private fun bindImages(images: List<PropertyImage>) {
        if (displayedImages == images) return
        displayedImages = images
        imagePagerAdapter.submitImages(images)
        binding.detailImagePager.scrollToPosition(0)
        updateImagePosition(0)
    }

    private fun updateImagePosition(position: Int) = with(binding) {
        val images = displayedImages.orEmpty()
        val total = images.size
        detailImagePosition.isVisible = total > 1
        detailImagePosition.text = if (total > 1) {
            images.getOrNull(position)?.displayLabel(requireContext())?.let { label ->
                getString(R.string.property_image_position_labeled, label, position + 1, total)
            } ?: getString(R.string.property_image_position, position + 1, total)
        } else {
            null
        }
    }

    private fun themeColor(@AttrRes attribute: Int): Int {
        val value = TypedValue()
        requireContext().theme.resolveAttribute(attribute, value, true)
        return if (value.resourceId != 0) ContextCompat.getColor(requireContext(), value.resourceId) else value.data
    }

    private companion object {
        const val ARG_SELECTED_AD_ID = "selectedAdId"
        const val DESCRIPTION_PREVIEW_MAX_LINES = 6
    }
}
