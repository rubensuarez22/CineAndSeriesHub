package com.example.cineyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrailersAdapter(
    private var items: List<Trailer>,
    private val onPlayClick: (Trailer) -> Unit
) : RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trailer, parent, false)
        return TrailerViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TrailerViewHolder, position: Int) {
        holder.bind(items[position], onPlayClick)
    }

    fun updateItems(newItems: List<Trailer>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TrailerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivTrailerThumbnail)
        private val ivPlay: ImageView      = itemView.findViewById(R.id.ivPlayOverlay)
        private val tvTitle: TextView      = itemView.findViewById(R.id.tvTrailerTitle)

        fun bind(trailer: Trailer, onPlayClick: (Trailer) -> Unit) {
            // Título
            tvTitle.text = trailer.title

            // Carga la miniatura con Glide
            Glide.with(ivThumbnail.context)
                .load(trailer.thumbnail)
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder_video)
                .error(R.drawable.ic_placeholder_video)
                .into(ivThumbnail)

            // Cuando hagan tap en cualquier parte del ítem o en el play, invoca el callback
            itemView.setOnClickListener { onPlayClick(trailer) }
            ivPlay.setOnClickListener   { onPlayClick(trailer) }
        }
    }
}
