package com.example.cineyhub

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.widget.addTextChangedListener


// Tus data classes (MovieItem, TmdbMovieDto, TmdbMovie) y TmdbService deben estar definidas.
// Asumo que MovieItem es Serializable o Parcelable para pasarla en Intents.

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFeatured: RecyclerView
    private lateinit var rvGrid: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var searchCard: CardView
    private lateinit var ivSearchIcon: ImageView
    private lateinit var etSearchQuery: EditText


    private lateinit var featuredAdapter: MoviesGridAdapter
    private lateinit var gridAdapter: MoviesGridAdapter

    private var allPopularMovies: List<MovieItem> = emptyList()



    private val homeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    // Define un TAG para los logs
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //Ajuste edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        //Toolbar
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).let { tb -> // <--- TIPO CORRECTO AQUÍ
            setSupportActionBar(tb) // Ahora 'tb' es del tipo correcto (androidx.appcompat.widget.Toolbar)
            tb.inflateMenu(R.menu.home_menu)
            tb.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_favorites -> {
                        startActivity(Intent(this, FavoritesActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }

        //Views

        rvFeatured    = findViewById(R.id.rvFeaturedMovies)
        rvGrid        = findViewById(R.id.rvMoviesGrid)
        pbLoading     = findViewById(R.id.pbHomeLoading)
        etSearchQuery = findViewById(R.id.etSearchQuery)
        ivSearchIcon  = findViewById(R.id.ivSearchIcon)
        searchCard    = findViewById(R.id.searchBarCard)

        //Adapters

        featuredAdapter = MoviesGridAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("movie", movie)
            }
            startActivity(intent)
        }

        rvFeatured.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFeatured.adapter = featuredAdapter

        gridAdapter = MoviesGridAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("movie", movie)
            }
            startActivity(intent)
        }

        rvGrid.layoutManager = GridLayoutManager(this, 2)
        rvGrid.adapter = gridAdapter

        // cuando hagan click en la card, ponemos el foco en el EditText
        searchCard.setOnClickListener {
            etSearchQuery.requestFocus()
        }

        // cada vez que cambie el texto, filtramos

        etSearchQuery.addTextChangedListener { editable ->
            val q = editable.toString().trim()
            Log.d(TAG, "🔍 Query: '$q'")
            val filtered = if (q.isEmpty()) {
                allPopularMovies
            } else {
                allPopularMovies.filter {
                    it.title.contains(q, ignoreCase = true)
                }
            }
            Log.d(TAG, "🎬 Filtradas: ${filtered.size} de ${allPopularMovies.size}")
            gridAdapter.updateItems(filtered)
        }

        // al pulsar el icono, si hay texto lo borramos
        ivSearchIcon.setOnClickListener {
            if (etSearchQuery.text.isNotEmpty()) {
                etSearchQuery.text.clear()
            } else {
                etSearchQuery.requestFocus()
            }
        }


        loadHomeData()
    }


    private fun loadHomeData() {
        pbLoading.visibility = View.VISIBLE

        homeScope.launch {
            val service = Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbService::class.java)

            val apiKey = BuildConfig.TMDB_API_KEY

            Log.d(TAG, "Usando TMDB API Key: $apiKey")

            var nowPlayingList: List<MovieItem>? = null

            // 1. Obtener películas "Now Playing"
            try {
                Log.d(TAG, "Intentando obtener películas 'Now Playing'...")
                val nowPlayingResponse = withContext(Dispatchers.IO) {
                    service.getNowPlaying(apiKey).results // Asumiendo que .results es List<MovieDto>
                }
                nowPlayingList = nowPlayingResponse.toMovieItems() // Tu función de extensión
                Log.i(TAG, "Películas 'Now Playing' obtenidas: ${nowPlayingList.size} ítems")
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException al obtener 'Now Playing': Código ${e.code()} - ${e.message()}", e)
                if (e.code() == 401) {
                    showToast("Error 401 (Now Playing): API Key inválida. Revisa tu clave API v3.")
                } else {
                    showToast("Error HTTP (Now Playing) ${e.code()}: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception al obtener 'Now Playing': ${e.localizedMessage}", e)
                showToast("Error de red (Now Playing): ${e.localizedMessage}")
            }

            // 2. Obtener películas "Popular"
            try {
                Log.d(TAG, "Intentando obtener películas 'Popular'...")
                val popularResponse = withContext(Dispatchers.IO) {
                    service.getPopular(apiKey).results // Asumiendo que .results es List<MovieDto>
                }

                val popularList = popularResponse.toMovieItems()

                allPopularMovies = popularList
                Log.i(TAG, "Películas 'Popular' obtenidas: ${allPopularMovies.size} ítems")
                gridAdapter.updateItems(allPopularMovies)
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException al obtener 'Popular': Código ${e.code()} - ${e.message()}", e)
                if (e.code() == 401) {
                    showToast("Error 401 (Popular): API Key inválida. Revisa tu clave API v3.")
                } else {
                    showToast("Error HTTP (Popular) ${e.code()}: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception al obtener 'Popular': ${e.localizedMessage}", e)
                showToast("Error de red (Popular): ${e.localizedMessage}")
            }

            // 3. Actualizar UI si los datos se obtuvieron
            nowPlayingList?.let {
                featuredAdapter.updateItems(it)
            }
            allPopularMovies?.let {
                gridAdapter.updateItems(it)
            }

            // Si no se pudo cargar ninguna lista, muestra un mensaje genérico o mantén los toasts específicos.
            if (nowPlayingList == null && allPopularMovies == null) {
                Log.w(TAG, "No se pudieron cargar datos de películas.")
                // Podrías mostrar un Toast general aquí si lo deseas y si no se mostraron ya los específicos.
            }

            pbLoading.visibility = View.GONE
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        homeScope.cancel()
    }
}

// These extension functions should be defined at the top level of your Kotlin file,
// or within an object, if not already.
// The 'private' modifier restricts them to this file, which is fine.

/**
 * Maps a list of TmdbMovieDto (Data Transfer Objects from TMDB API for now playing/popular)
 * to a list of MovieItem (your app's UI model).
 */
private fun List<MovieDto>.toMovieItems(): List<MovieItem> =
    this.mapNotNull { dto -> // Use mapNotNull to skip any items that might be invalid
        MovieItem(
            id          = dto.id.toString(),
            title       = dto.title ?: "No Title", // Provide defaults for nullable fields
            posterUrl   = dto.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "",
            rating      = dto.vote_average.toFloat(),
            overview    = dto.overview ?: "",
            releaseDate = dto.release_date ?: ""
        )
    }

/**
 * Maps a list of TmdbMovie (potentially for search results or a different API structure)
 * to a list of MovieItem.
 */
private fun List<TmdbMovie>.mapToMovieItems(): List<MovieItem> =
    this.mapNotNull { tmdb -> // Use mapNotNull to skip any items that might be invalid
        MovieItem(
            id          = tmdb.id.toString(),
            title       = tmdb.title ?: tmdb.name.orEmpty().ifEmpty { "No Title" }, // Handle both title and name
            posterUrl   = tmdb.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "",
            rating      = tmdb.vote_average.toFloat(),
            overview    = "", // Search results might not have detailed overview here
            releaseDate = ""  // Search results might not have release date here
        )
    }
