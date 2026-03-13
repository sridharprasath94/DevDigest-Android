package com.flash.devdigest.presentation.trending

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.flash.devdigest.R
import com.flash.devdigest.databinding.FragmentTrendingNewsBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrendingNewsFragment : Fragment(R.layout.fragment_trending_news) {
    private val binding: FragmentTrendingNewsBinding by viewBinding(FragmentTrendingNewsBinding::bind)
    private val viewModel: TrendingNewsViewModel by viewModels()
    private val adapter = NewsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { _ ->
            val action =
                TrendingNewsFragmentDirections
                    .actionTrendingNewsFragmentToNewsDetailFragment()

            findNavController().navigate(action)
        }

//        adapter.setOnFavoriteClickListener { repo ->
//            viewModel.toggleFavorite(repo)
//        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    Log.d("TrendingNewsFragment", "State updated: $state")
                    when {
                        state.isLoading && state.news.isEmpty() -> {
                            binding.fullScreenLoader.visibility = View.VISIBLE
                            adapter.submitList(emptyList())
                        }

                        state.error != null -> {
                            binding.fullScreenLoader.visibility = View.GONE
                            adapter.submitList(emptyList())
                            Toast.makeText(
                                requireContext(),
                                when (state.error) {
                                    UiError.NetworkUnavailable -> "Network unavailable. Please check your connection."
                                    UiError.RateLimitExceeded -> "API rate limit exceeded. Please try again later."
                                    UiError.InvalidRequest -> "Invalid request. Please try again."
                                    UiError.Unknown -> "An unknown error occurred. Please try again."
                                },
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            Log.d("TrendingNewsFragment", "Displaying news: ${state.news.size} items")
                            binding.fullScreenLoader.visibility = View.GONE
                            adapter.submitList(state.news) {
                                binding.recyclerView.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        }

    }
}