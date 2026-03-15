package com.flash.devdigest.presentation.newsdetail

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.flash.devdigest.R
import com.flash.devdigest.databinding.FragmentNewsDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding

@AndroidEntryPoint
class NewsDetailFragment : Fragment(R.layout.fragment_news_detail) {
    private val binding: FragmentNewsDetailBinding by viewBinding(FragmentNewsDetailBinding::bind)

    private val args: NewsDetailFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val news = args.news

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

    private fun openUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(requireContext(), url.toUri())
    }
}