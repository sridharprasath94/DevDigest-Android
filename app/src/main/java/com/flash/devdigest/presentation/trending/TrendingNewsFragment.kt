package com.flash.devdigest.presentation.trending

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.flash.devdigest.R
import com.flash.devdigest.databinding.FragmentTrendingNewsBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding

@AndroidEntryPoint
class TrendingNewsFragment : Fragment(R.layout.fragment_trending_news) {
    private val binding: FragmentTrendingNewsBinding by viewBinding(FragmentTrendingNewsBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(TrendingNewsFragmentDirections.actionTrendingNewsFragmentToNewsDetailFragment())
        }
    }
}