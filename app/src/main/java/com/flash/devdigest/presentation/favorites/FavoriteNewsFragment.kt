package com.flash.devdigest.presentation.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.flash.devdigest.R
import com.flash.devdigest.databinding.FragmentFavoritesBinding
import com.flash.devdigest.presentation.shared.NewsAdapter
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteNewsFragment :
    Fragment(R.layout.fragment_favorites) {

    private val binding: FragmentFavoritesBinding by viewBinding(FragmentFavoritesBinding::bind)
    private val viewModel: FavoriteReposViewModel by viewModels()
    private val adapter = NewsAdapter(enableFavoritesIcon = false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeState()
    }


    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { repo ->
            val action =
                FavoriteNewsFragmentDirections
                    .actionFavoritesToDetails(repo.id)

            findNavController().navigate(action)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        FavoriteNewsUiState.Empty -> {
                            binding.fullScreenLoader.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                        }

                        is FavoriteNewsUiState.Success -> {
                            binding.fullScreenLoader.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter.submitData(PagingData.from(state.repos))
                        }

                        FavoriteNewsUiState.Loading -> {
                            binding.fullScreenLoader.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}