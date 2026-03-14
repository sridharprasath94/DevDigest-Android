package com.flash.devdigest.presentation.trending

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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
        setupSearchButton()
        observeSearchField()
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString()
            if (query.isNotBlank()) {
                viewModel.processIntent(TrendingNewsIntent.Search(query))
            } else {
                viewModel.processIntent(TrendingNewsIntent.ClearSearch)
            }
        }
    }

    private fun observeSearchField() {
        binding.etSearch.doAfterTextChanged { text ->
            val query = text?.toString().orEmpty()

            if (query.isBlank()) {
                viewModel.processIntent(TrendingNewsIntent.ClearSearch)
            } else {
                viewModel.processIntent(TrendingNewsIntent.Search(query))
            }
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, event ->

            val isKeyboardEnter =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN

            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                isKeyboardEnter
            ) {
                val query = binding.etSearch.text.toString()

                if (query.isNotBlank()) {
                    viewModel.processIntent(TrendingNewsIntent.Search(query))
                } else {
                    viewModel.processIntent(TrendingNewsIntent.ClearSearch)
                }

                true
            } else {
                false
            }
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter

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