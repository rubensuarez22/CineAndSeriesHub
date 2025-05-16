package com.example.cineyhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CommentsAdapter(
    private var items: List<Comment>
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    fun updateItems(new: List<Comment>) {
        items = new
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(v)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar    : ImageView = itemView.findViewById(R.id.ivCommentAvatar)
        private val tvUser      : TextView  = itemView.findViewById(R.id.tvCommentUser)
        private val tvTime      : TextView  = itemView.findViewById(R.id.tvCommentTime)
        private val tvText      : TextView  = itemView.findViewById(R.id.tvCommentText)
        private val ivLike      : ImageView = itemView.findViewById(R.id.ivLike)
        private val tvLikeCount : TextView  = itemView.findViewById(R.id.tvLikeCount)

        fun bind(c: Comment) {
            tvUser.text = c.userName
            // formatea timestamp a “X h atrás”
            tvTime.text = formatTimeAgo(c.timestamp)
            tvText.text = c.text
            tvLikeCount.text = c.likes.toString()

            // avatar genérico o carga real
            Glide.with(ivAvatar.context)
                .load(R.drawable.ic_placeholder_profile)
                .circleCrop()
                .into(ivAvatar)

            ivLike.setOnClickListener {
                // lógica de “like” (incrementar en DB y refrescar contador)
            }
        }

        private fun formatTimeAgo(ts: Long): String {
            val diff = System.currentTimeMillis() - ts
            val hours = diff / (1000 * 60 * 60)
            return if (hours < 1) "Hace unos minutos"
            else if (hours < 24) "Hace ${hours}h"
            else "${hours/24}d atrás"
        }
    }
}
