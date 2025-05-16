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

    // Dentro de la clase FeaturedMoviesAdapter
    class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivFeaturedMovieImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvFeaturedMovieTitle)
        // Añade referencias a las estrellas
        private val star1: ImageView = itemView.findViewById(R.id.star1_featured)
        private val star2: ImageView = itemView.findViewById(R.id.star2_featured)
        private val star3: ImageView = itemView.findViewById(R.id.star3_featured)
        private val star4: ImageView = itemView.findViewById(R.id.star4_featured)
        private val star5: ImageView = itemView.findViewById(R.id.star5_featured)
        private val starsList: List<ImageView> = listOf(star1, star2, star3, star4, star5)

        fun bind(movie: MovieItem, onItemClick: (MovieItem) -> Unit) {
            // Título (con la corrección para nulos o vacíos)
            tvTitle.text = if (movie.title.isNullOrBlank()) "Sin titulo" else movie.title

            Glide.with(ivPoster.context)
                .load(movie.posterUrl)
                .fitCenter()
                // Considera añadir placeholders y errores para Glide
                .placeholder(R.drawable.ic_placeholder_error)

            itemView.setOnClickListener { onItemClick(movie) }

            // Lógica para mostrar las estrellas de calificación
            // Asume que movie.rating es un Float de 0.0 a 10.0
            val ratingOutOfFive = movie.rating / 2.0f // Convierte a una escala de 0-5

            for (i in starsList.indices) {
                val starImageView = starsList[i]
                if (i < ratingOutOfFive) {
                    // Lógica para estrella llena o media (simplificado a llena vs. borde)
                    if ((ratingOutOfFive - i) >= 0.75f) { // Umbral para estrella llena
                        starImageView.setImageResource(R.drawable.ic_star_filled_base)
                    } else if ((ratingOutOfFive - i) >= 0.25f) { // Umbral para media estrella (si tienes el drawable)
                        // starImageView.setImageResource(R.drawable.ic_star_half_base) // Necesitarías este drawable
                        starImageView.setImageResource(R.drawable.ic_star_border_base) // Por ahora, borde si no es llena completa
                    }
                    else {
                        starImageView.setImageResource(R.drawable.ic_star_border_base)
                    }
                } else {
                    starImageView.setImageResource(R.drawable.ic_star_border_base)
                }
                // El tinte ya está aplicado en el XML (app:tint). Si cambias el src dinámicamente
                // y el tinte no se aplica correctamente, puedes forzarlo aquí:
                // val starColor = ContextCompat.getColor(starImageView.context, R.color.featured_movie_star_tint)
                // DrawableCompat.setTint(starImageView.drawable.mutate(), starColor)
            }
        }
    }
}
