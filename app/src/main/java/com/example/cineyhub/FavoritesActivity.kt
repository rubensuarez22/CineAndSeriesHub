package com.example.cineyhub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cineyhub.dataAplication.Companion.prefs
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {
    private lateinit var rvFav: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: MoviesGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // Vistas
        rvFav      = findViewById(R.id.rvFavorites)
        pbLoading  = ProgressBar(this).apply { visibility = View.VISIBLE }
        tvEmpty    = TextView(this).apply {
            text = "No tienes favoritos aún"
            visibility = View.GONE
            setTextColor(resources.getColor(android.R.color.darker_gray))
        }

        // Añádelo al layout (o ponlo en XML si prefieres)
        (findViewById<View>(R.id.fav_root) as? ViewGroup)?.apply {
            addView(pbLoading)
            addView(tvEmpty)
        }

        // Adapter
        adapter = MoviesGridAdapter(emptyList()) { movie ->
            startActivity(Intent(this, DetailActivity::class.java)
                .putExtra("movie", movie))
        }
        rvFav.layoutManager = GridLayoutManager(this, 2)
        rvFav.adapter = adapter

        // Carga favoritos
        loadFavorites()
    }

    private fun loadFavorites() {
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val poster = doc.getString("posterUrl") ?: return@mapNotNull null
                    // Si guardaste rating o id, recupéralos aquí
                    MovieItem(
                        id = doc.id,
                        title = title,
                        posterUrl = poster,
                        rating = 0f,
                        overview = TODO(),
                        releaseDate = TODO(),
                    )
                }
                pbLoading.visibility = View.GONE
                if (list.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    adapter.updateItems(list)
                }
            }
            .addOnFailureListener {
                pbLoading.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            }
    }
}
