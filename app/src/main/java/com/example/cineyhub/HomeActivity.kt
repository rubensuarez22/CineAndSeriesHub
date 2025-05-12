package com.example.cineyhub

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar as Toolbar)
        toolbar.setNavigationOnClickListener {
            // TODO: abrir menú de navegación
        }

        // Featured movies (horizontal carousel)
        val rvFeatured = findViewById<RecyclerView>(R.id.rvFeaturedMovies)
        rvFeatured.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFeatured.adapter = FeaturedMoviesAdapter(
            items = emptyList(),           // TODO: sustituir con lista real
            onItemClick = { movie ->
                // TODO: lanzar DetailActivity con datos de 'movie'
            }
        )

        // Search placeholder click → lanzar búsqueda
        val tvSearch = findViewById<TextView>(R.id.tvSearchPlaceholder)
        tvSearch.setOnClickListener {
            // TODO: navegar a pantalla SearchActivity o mostrar diálogo de búsqueda
            // startActivity(Intent(this, SearchActivity::class.java))
        }

        // Movies grid (2 columnas)
        val rvGrid = findViewById<RecyclerView>(R.id.rvMoviesGrid)
        rvGrid.layoutManager = GridLayoutManager(this, 2)
        rvGrid.adapter = MoviesGridAdapter(
            items = emptyList(),           // TODO: sustituir con lista real
            onItemClick = { movie ->
                // TODO: navegar a DetailActivity
            }
        )
    }
}