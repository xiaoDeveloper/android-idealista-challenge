package com.xiao.idealistachallenge.ui.media

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.databinding.FragmentFullscreenGalleryBinding
import com.xiao.idealistachallenge.model.PropertyImage
import com.xiao.idealistachallenge.ui.detail.DetailMediaGalleryState

class FullscreenGalleryFragment : Fragment(R.layout.fragment_fullscreen_gallery) {
    private var _binding: FragmentFullscreenGalleryBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val imagePagerAdapter = PropertyImagePagerAdapter()

    private val initialPosition: Int
        get() = requireArguments().getInt(ARG_INITIAL_POSITION)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFullscreenGalleryBinding.bind(view)
        val images = ViewModelProvider(
            findNavController().getBackStackEntry(R.id.detailFragment),
        )[DetailMediaGalleryState::class.java].requireImages()
        check(initialPosition in images.indices) { "Gallery initial position is outside Detail media" }

        binding.fullscreenGalleryPager.adapter = imagePagerAdapter
        binding.fullscreenGalleryPager.configurePropertyImagePager(::showImagePosition)
        binding.galleryClose.setOnClickListener { findNavController().navigateUp() }
        imagePagerAdapter.submitImages(images)
        binding.fullscreenGalleryPager.scrollToPosition(initialPosition)
        showImagePosition(initialPosition)
    }

    override fun onDestroyView() {
        binding.fullscreenGalleryPager.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun showImagePosition(position: Int) {
        val images = ViewModelProvider(
            findNavController().getBackStackEntry(R.id.detailFragment),
        )[DetailMediaGalleryState::class.java].requireImages()
        val image = images[position]
        binding.galleryImagePosition.text = image.displayLabel(requireContext())?.let { label ->
            getString(R.string.property_image_position_labeled, label, position + 1, images.size)
        } ?: getString(R.string.property_image_position, position + 1, images.size)
    }

    companion object {
        const val ARG_INITIAL_POSITION = "initialPosition"
    }
}
