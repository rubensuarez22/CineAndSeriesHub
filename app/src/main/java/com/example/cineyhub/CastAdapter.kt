package com.example.cineyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CastAdapter(
    private var items: List<Actor>
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateItems(newItems: List<Actor>) {
        items = newItems
        notifyDataSetChanged()
    }

    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivCastProfile)
        private val tvName: TextView    = itemView.findViewById(R.id.tvCastName)

        fun bind(actor: Actor) {
            tvName.text = actor.name
            Glide.with(ivProfile.context)
                .load(actor.photoUrl ?: R.drawable.ic_placeholder_profile)
                .circleCrop()
                .into(ivProfile)
        }
    }
}
