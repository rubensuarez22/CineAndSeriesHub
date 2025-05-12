package com.example.cineyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FeaturedMoviesAdapter(
    private val items: List<MovieItem>,
    private val onItemClick: (MovieItem) -> Unit
) : RecyclerView.Adapter<FeaturedMoviesAdapter.FeaturedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_movie, parent, false)
        return FeaturedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivFeaturedMovieImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvFeaturedMovieTitle)

        fun bind(movie: MovieItem, onItemClick: (MovieItem) -> Unit) {
            tvTitle.text = movie.title
            Glide.with(ivPoster.context)
                .load(movie.posterUrl)
                .centerCrop()
                .into(ivPoster)

            itemView.setOnClickListener { onItemClick(movie) }
        }
    }
}
