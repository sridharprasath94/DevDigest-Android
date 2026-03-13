package com.flash.devdigest.presentation.trending

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.flash.devdigest.R
import com.flash.devdigest.TrendingFragmentDirections
import com.flash.devdigest.databinding.FragmentTrendingBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding

@AndroidEntryPoint
class TrendingFragment : Fragment(R.layout.fragment_trending) {
    private val binding: FragmentTrendingBinding by viewBinding(FragmentTrendingBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(TrendingFragmentDirections.actionTrendingFragmentToNewsDetailFragment())
        }
    }
}