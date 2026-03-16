package com.flash.devdigest.presentation.shared

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.flash.devdigest.R
import com.flash.devdigest.databinding.RowNewsBinding
import com.flash.devdigest.domain.model.News
import kotlin.time.Clock
import kotlin.time.Instant

class NewsAdapter(private val enableFavoritesIcon: Boolean = true) :
    PagingDataAdapter<News, NewsAdapter.NewsViewHolder>(DiffCallback) {

    private var onItemClick: ((News) -> Unit)? = null
    private var onFavoriteClick: ((News) -> Unit)? = null

    class NewsViewHolder(
        private val binding: RowNewsBinding,
        private val onItemClick: ((News) -> Unit)?,
        private val onFavoriteClick: ((News) -> Unit)?,
        private val enableFavoritesIcon: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            binding.newsTitle.text = news.title
            binding.newsAuthor.text = buildString {
                append("by ")
                append(news.author)
            }
            binding.newsPoints.text = buildString {
                append(news.points)
                append(" points")
            }
            binding.newsComments.text = buildString {
                append(news.comments)
                append(" comments")
            }
            binding.newsDate.text = formatDate(news.createdAt)

            if (enableFavoritesIcon) {
                val iconRes =
                    if (news.isFavorite)
                        R.drawable.ic_star_filled
                    else
                        R.drawable.ic_star_outline

                val colorFilter = if (news.isFavorite) {
                    binding.root.context.getColor(R.color.star_filled)
                } else {
                    binding.root.context.getColor(R.color.star_outline)
                }

                binding.ivFavorite.setImageResource(iconRes)
                binding.ivFavorite.setColorFilter(colorFilter)

                binding.ivFavorite.setOnClickListener {
                    onFavoriteClick?.invoke(news)
                }
            } else {
                binding.ivFavorite.visibility = android.view.View.GONE
            }


            binding.root.setOnClickListener {
                onItemClick?.invoke(news)
            }
        }


        private fun formatDate(date: String): String {
            val instant = Instant.parse(date)
            val now = Clock.System.now()
            val duration = now - instant

            return when {
                duration.inWholeHours < 1 -> "${duration.inWholeMinutes}m ago"
                duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
                else -> "${duration.inWholeDays}d ago"
            }
        }
    }

    fun setOnItemClickListener(listener: (News) -> Unit) {
        onItemClick = listener
    }

    fun setOnFavoriteClickListener(listener: (News) -> Unit) {
        onFavoriteClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = RowNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding, onItemClick, onFavoriteClick, enableFavoritesIcon)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = getItem(position) ?: return
        holder.bind(news)
    }


    companion object DiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
}