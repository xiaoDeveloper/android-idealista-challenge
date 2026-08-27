package com.xiao.idealistachallenge.ui.listing

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
import com.xiao.idealistachallenge.databinding.FragmentListingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListingFragment : Fragment(R.layout.fragment_listing) {

    private var _binding: FragmentListingBinding? = null
    private val binding: FragmentListingBinding
        get() = checkNotNull(_binding)

    private val viewModel: ListingViewModel by viewModels {
        (requireActivity().application as App).container.viewModelFactory { container ->
            ListingViewModel(
                adRepository = container.adRepository,
                favoriteRepository = container.favoriteRepository,
                dispatcher = Dispatchers.IO,
            )
        }
    }

    private var listingAdapter: ListingAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentListingBinding.bind(view)
        listingAdapter = ListingAdapter()
        binding.listingRecyclerView.adapter = listingAdapter
        binding.retryButton.setOnClickListener { viewModel.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
        viewModel.load()
    }

    override fun onDestroyView() {
        binding.listingRecyclerView.adapter = null
        listingAdapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: ListingUiState) {
        val currentBinding = _binding ?: return
        currentBinding.loadingState.isVisible = state is ListingUiState.Loading
        currentBinding.listingContent.isVisible = state is ListingUiState.Content
        currentBinding.emptyState.isVisible = state is ListingUiState.Empty
        currentBinding.errorState.isVisible = state is ListingUiState.Error

        when (state) {
            ListingUiState.Loading,
            ListingUiState.Empty,
            -> Unit

            is ListingUiState.Content -> {
                currentBinding.resultsCount.text = resources.getQuantityString(
                    R.plurals.results_count,
                    state.rows.size,
                    state.rows.size,
                )
                listingAdapter?.submitList(state.rows)
            }

            is ListingUiState.Error -> {
                currentBinding.errorTitle.setText(state.userFacingError.titleResId)
                currentBinding.errorMessage.setText(state.userFacingError.messageResId)
            }
        }
    }
}
