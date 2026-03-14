package com.flash.devdigest.presentation.trending

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.flash.devdigest.R
import com.flash.devdigest.databinding.FragmentTrendingNewsBinding
import com.flash.devdigest.presentation.shared.NewsAdapter
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
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
        observeEvents()
        observeSearchField()
    }

    private fun observeSearchField() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.processIntent(
                TrendingNewsIntent.OnSearchQueryChanged(
                    text?.toString().orEmpty()
                )
            )
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.processIntent(TrendingNewsIntent.Refresh)
        }

        adapter.setOnItemClickListener { news ->
            val action =
                TrendingNewsFragmentDirections
                    .actionTrendingToDetails(news)

            findNavController().navigate(action)
        }

        adapter.setOnFavoriteClickListener { news ->
            viewModel.processIntent(TrendingNewsIntent.ToggleFavorite(news))
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.swipeRefresh.isRefreshing = state.isLoading
                    when {
                        state.isLoading && state.news.isEmpty() -> {
                            binding.fullScreenLoader.visibility = View.VISIBLE
                        }

                        else -> {
                            binding.fullScreenLoader.visibility = View.GONE

                            adapter.submitList(state.news)
                        }
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }
}