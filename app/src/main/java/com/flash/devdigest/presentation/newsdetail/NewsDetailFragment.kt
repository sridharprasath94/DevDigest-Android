package com.flash.devdigest.presentation.newsdetail

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.flash.devdigest.R
import com.flash.devdigest.databinding.FragmentNewsDetailBinding
import com.flash.devdigest.domain.model.News
import com.flash.devdigest.presentation.utils.showCenteredSnackBar
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class NewsDetailFragment : Fragment(R.layout.fragment_news_detail) {
    private val binding: FragmentNewsDetailBinding by viewBinding(FragmentNewsDetailBinding::bind)
    private val args: NewsDetailFragmentArgs by navArgs()
    private val viewModel: NewsDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadNews(args.newsId)
        observeState()
        observeEvent()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        NewsDetailUiState.Initial -> {
                        }

                        is NewsDetailUiState.Success -> {
                            bindNews(state.news)
                        }
                    }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(requireContext(), url.toUri())
    }

    private fun observeEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    binding.root.showCenteredSnackBar(event.message)
                }
            }
        }
    }

    private fun bindNews(news: News) {

        with(binding) {

            tvTitle.text = news.title

            tvAuthor.text = buildString {
                append("Author: ")
                append(news.author)
            }

            tvStats.text = buildString {
                append("⭐ ")
                append(news.points)
                append("   |   💬 ")
                append(news.comments)
            }

            tvCreatedAt.text = news.createdAt

            btnNewsLink.setOnClickListener {
                news.url?.let { openUrl(it) }
            }
        }
    }
}