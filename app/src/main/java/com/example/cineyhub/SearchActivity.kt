package com.example.cineyhub

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.cineyhub.MovieItem
import com.example.cineyhub.MoviesGridAdapter
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var etSearch: EditText
    private lateinit var rvResults: RecyclerView
    private lateinit var adapter: MoviesGridAdapter
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvNoResults: TextView
    // Coroutine scope for search requests
    private val searchScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        pbLoading   = findViewById(R.id.pbSearchLoading)
        tvNoResults = findViewById(R.id.tvNoResults)
        etSearch    = findViewById(R.id.etSearchQuery)
        rvResults   = findViewById(R.id.rvSearchResults)
        adapter = MoviesGridAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("movie", movie)   // MovieItem implementa Parcelable
            }
            startActivity(intent)
        }


        rvResults.layoutManager = GridLayoutManager(this, 2)
        rvResults.adapter = adapter

        etSearch.addTextChangedListener(object: TextWatcher {
            private var searchJob: Job? = null

            override fun afterTextChanged(s: Editable?) {
                // Debounce: esperar 300ms tras última pulsación
                searchJob?.cancel()
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    adapter.updateItems(emptyList())
                    return
                }
                searchJob = searchScope.launch {
                    delay(300)
                                      runOnUiThread {
                                            pbLoading.visibility = View.VISIBLE
                                           tvNoResults.visibility = View.GONE
                                          }
                                      performSearch(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        searchScope.cancel()
    }

    private suspend fun performSearch(query: String) {
        // Mostrar loader si tienes uno...
        withContext(Dispatchers.IO) {
            try {
                val service = Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TmdbService::class.java)

                val response = service.searchMovies(apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI2N2E0Y2ZhODA2NWQxMGI5NTNkOTQwNWIzMjJlMjE5NCIsIm5iZiI6MS43NDczMzQ1MzA0ODg5OTk4ZSs5LCJzdWIiOiI2ODI2MzU4MmUxYzA3ZDY4ODkyZDJiYjMiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.kGPv6P3kI6bKcCB_bt4d2_VLUr0YDcQszDNRbe5ddJ8", query = query)
                val movies = response.results.map {
                    MovieItem(
                        id = it.id.toString(),
                        title = it.title ?: it.name ?: "",
                        posterUrl = "https://image.tmdb.org/t/p/w500${it.poster_path}",
                        rating = it.vote_average.toFloat(),
                        overview =  "",
                        releaseDate = ""
                    )
                }
                
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    adapter.updateItems(movies)
                    tvNoResults.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                // TODO: mostrar error al usuario
            }
        }
    }
}
