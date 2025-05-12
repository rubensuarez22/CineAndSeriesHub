package com.example.cineyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MoviesGridAdapter(
    private val items: List<MovieItem>,
    private val onItemClick: (MovieItem) -> Unit
) : RecyclerView.Adapter<MoviesGridAdapter.GridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tvMovieRatingValue)

        fun bind(movie: MovieItem, onItemClick: (MovieItem) -> Unit) {
            tvTitle.text = movie.title
            tvRating.text = movie.rating.toString()
            Glide.with(ivPoster.context)
                .load(movie.posterUrl)
                .centerCrop()
                .into(ivPoster)

            itemView.setOnClickListener { onItemClick(movie) }
        }
    }
}

