package com.github.livingwithhippos.unchained.search.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.github.livingwithhippos.unchained.R
import com.github.livingwithhippos.unchained.base.UnchainedFragment
import com.github.livingwithhippos.unchained.databinding.FragmentSearchItemBinding
import com.github.livingwithhippos.unchained.plugins.model.ScrapedItem
import com.github.livingwithhippos.unchained.search.model.LinkItem
import com.github.livingwithhippos.unchained.search.model.LinkItemAdapter
import com.github.livingwithhippos.unchained.search.model.LinkItemListener
import com.github.livingwithhippos.unchained.search.viewmodel.SearchItemViewModel
import com.github.livingwithhippos.unchained.utilities.MAGNET_PATTERN
import coil.load
import com.github.livingwithhippos.unchained.utilities.extension.copyToClipboard
import com.github.livingwithhippos.unchained.utilities.extension.openExternalWebPage
import com.github.livingwithhippos.unchained.utilities.extension.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchItemFragment : UnchainedFragment(), LinkItemListener {

    private val args: SearchItemFragmentArgs by navArgs()
    private val viewModel: SearchItemViewModel by viewModels()

    private val magnetPattern = Regex(MAGNET_PATTERN, RegexOption.IGNORE_CASE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSearchItemBinding.inflate(inflater, container, false)

        setup(binding)
        return binding.root
    }

    private fun setup(binding: FragmentSearchItemBinding) {
        val item: ScrapedItem = args.item

        binding.item = item

        binding.linkCaption.setOnClickListener {
            if (item.link != null) context?.openExternalWebPage(item.link)
        }

        val adapter = LinkItemAdapter(this)
        binding.linkList.adapter = adapter

        val links = mutableListOf<LinkItem>()

        item.magnets.forEach {
            links.add(LinkItem(getString(R.string.magnet), it.substringBefore("&"), it))
        }
        item.torrents.forEach { links.add(LinkItem(getString(R.string.torrent), it, it)) }
        item.hosting.forEach { links.add(LinkItem(getString(R.string.hoster), it, it)) }
        adapter.submitList(links)

        // Setup TMDB info
        setupTmdbObservers(binding)
        
        // Load TMDB information for this torrent
        viewModel.loadTmdbInfo(item.name)
    }

    private fun setupTmdbObservers(binding: FragmentSearchItemBinding) {
        viewModel.tmdbLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.tmdbProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            updateLinkListConstraints(binding)
        }

        viewModel.tmdbInfo.observe(viewLifecycleOwner) { tmdbInfo ->
            if (tmdbInfo != null) {
                binding.tmdbInfoLayout.visibility = View.VISIBLE
                setupTmdbInfo(binding, tmdbInfo)
            } else {
                binding.tmdbInfoLayout.visibility = View.GONE
            }
            updateLinkListConstraints(binding)
        }

        viewModel.tmdbError.observe(viewLifecycleOwner) { error ->
            if (error != null && binding.tmdbInfoLayout.visibility != View.VISIBLE) {
                // Only show error if no TMDB info is displayed
                context?.showToast(error)
            }
        }
    }

    private fun updateLinkListConstraints(binding: FragmentSearchItemBinding) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.rootLayout)
        
        val topAnchor = when {
            binding.tmdbInfoLayout.visibility == View.VISIBLE -> R.id.tmdbInfoLayout
            binding.tmdbProgressBar.visibility == View.VISIBLE -> R.id.tmdbProgressBar
            else -> R.id.infoLayout
        }
        
        constraintSet.connect(R.id.linkList, ConstraintSet.TOP, topAnchor, ConstraintSet.BOTTOM, 10)
        constraintSet.applyTo(binding.rootLayout)
    }

    private fun setupTmdbInfo(binding: FragmentSearchItemBinding, tmdbInfo: com.github.livingwithhippos.unchained.data.model.TmdbInfo) {
        // Access the included layout views
        val posterImageView = binding.tmdbInfoLayout.findViewById<android.widget.ImageView>(R.id.iv_poster)
        val titleTextView = binding.tmdbInfoLayout.findViewById<android.widget.TextView>(R.id.tv_title)
        val ratingTextView = binding.tmdbInfoLayout.findViewById<android.widget.TextView>(R.id.tv_rating)
        val mediaTypeTextView = binding.tmdbInfoLayout.findViewById<android.widget.TextView>(R.id.tv_media_type)
        val overviewTextView = binding.tmdbInfoLayout.findViewById<android.widget.TextView>(R.id.tv_overview)

        // Set title
        titleTextView?.text = tmdbInfo.getDisplayTitle()

        // Set rating
        ratingTextView?.text = tmdbInfo.voteAverage?.let { 
            getString(R.string.tmdb_rating_format, it) 
        } ?: getString(R.string.tmdb_no_info)

        // Set media type
        mediaTypeTextView?.text = if (tmdbInfo.mediaType == "movie") {
            getString(R.string.category_movies)
        } else {
            getString(R.string.category_tv)
        }

        // Set overview
        overviewTextView?.text = tmdbInfo.overview ?: getString(R.string.tmdb_no_info)

        // Load poster image
        posterImageView?.let { imageView ->
            val posterUrl = tmdbInfo.getPosterUrl()
            if (posterUrl != null) {
                imageView.load(posterUrl) {
                    crossfade(true)
                    placeholder(R.drawable.icon_movie)
                    error(R.drawable.icon_movie)
                }
            } else {
                imageView.load(R.drawable.icon_movie)
            }
        }
    }

    override fun onClick(item: LinkItem) {
        activityViewModel.downloadSupportedLink(item.link)
    }

    override fun onLongClick(item: LinkItem): Boolean {
        copyToClipboard(getString(R.string.link), item.link)
        context?.showToast(R.string.link_copied)
        return true
    }
}
