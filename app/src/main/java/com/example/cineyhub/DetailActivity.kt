package com.example.cineyhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cineyhub.dataAplication.Companion.prefs
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActivity : AppCompatActivity() {

    // Vistas
    private lateinit var toolbar: Toolbar
    private lateinit var ivPoster: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMeta: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvOverview: TextView
    private lateinit var rvCast: RecyclerView
    private lateinit var rvTrailers: RecyclerView
    private lateinit var btnWatchlist: Button
    private lateinit var rvComments: RecyclerView
    private lateinit var etNewComment: EditText
    private lateinit var btnPostComment: ImageView

    // Adaptadores
    private lateinit var castAdapter: CastAdapter
    private lateinit var trailersAdapter: TrailersAdapter
    private lateinit var commentsAdapter: CommentsAdapter

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // TMDB
    private lateinit var tmdbService: TmdbService
    private val detailScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Datos de la película
    private lateinit var movie: MovieItem
    private var isInWatchlist = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Inicializa Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // Inicializa Retrofit/TMDB
        tmdbService = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)

        // Bind de vistas
        toolbar        = findViewById(R.id.toolbar_detail)
        ivPoster       = findViewById(R.id.ivDetailPoster)
        tvTitle        = findViewById(R.id.tvDetailTitle)
        tvMeta         = findViewById(R.id.tvDetailMeta)
        tvRating       = findViewById(R.id.tvDetailRating)
        tvOverview     = findViewById(R.id.tvDetailOverview)
        rvCast         = findViewById(R.id.rvCast)
        rvTrailers     = findViewById(R.id.rvTrailers)
        btnWatchlist   = findViewById(R.id.btnWatchlist)
        rvComments     = findViewById(R.id.rvComments)
        etNewComment   = findViewById(R.id.etNewComment)
        btnPostComment = findViewById(R.id.btnPostComment)

        // Toolbar con back
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Recupera MovieItem pasado por Intent
        movie = intent.getParcelableExtra("movie")
            ?: throw IllegalArgumentException("MovieItem faltante en el Intent")

        // Carga datos básicos antes de la llamada a TMDB
        tvTitle.text  = movie.title
        tvOverview.text = movie.overview

        // Inicializa adapters vacíos
        castAdapter     = CastAdapter(emptyList())
        trailersAdapter = TrailersAdapter(emptyList()) { trailer ->
            // Abrir YouTube con ACTION_VIEW
            val ytUrl = "https://www.youtube.com/watch?v=${trailer.videoKey}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ytUrl)))
        }
        commentsAdapter = CommentsAdapter(emptyList())

        // Setup de RecyclerViews
        rvCast.layoutManager     = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCast.adapter           = castAdapter
        rvTrailers.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTrailers.adapter       = trailersAdapter
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter       = commentsAdapter

        // Carga comentarios en tiempo real
        loadComments()

        // Botón de enviar comentario
        btnPostComment.setOnClickListener { postComment() }

        // Check / toggle watchlist
        checkWatchlist()
        btnWatchlist.setOnClickListener { toggleWatchlist() }

        // Finalmente, carga detalles ricos (reparto + trailers + resto de datos)
        loadDetailFromTmdb()
    }

    private fun loadDetailFromTmdb() {
        detailScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    tmdbService.getMovieDetail(movie.id, BuildConfig.TMDB_API_KEY)
                }

                // 1) Poster, título, meta, rating, overview…
                Glide.with(this@DetailActivity)
                    .load("https://image.tmdb.org/t/p/w500${resp.poster_path}")
                    .centerCrop()
                    .into(ivPoster)

                tvTitle.text  = resp.title
                val year      = resp.release_date?.take(4).orEmpty()
                val runtime   = resp.runtime ?: 0
                tvMeta.text   = "$year • ${runtime}m"
                tvRating.text = "⭐ ${resp.vote_average}"
                tvOverview.text = resp.overview.orEmpty()

                // 2) Reparto (siempre con safe-call y default vacío)
                val actors = resp.credits
                    ?.cast
                    ?.map { dto ->
                        Actor(
                            name     = dto.name,
                            photoUrl = dto.profile_path
                                ?.let { "https://image.tmdb.org/t/p/w200$it" }
                                .orEmpty()
                        )
                    }
                    .orEmpty()
                castAdapter.updateItems(actors)

                // 3) Trailers
                val trailers = resp.videos
                    ?.results
                    ?.filter { it.site == "YouTube" && it.type == "Trailer" }
                    ?.map { dto ->
                        Trailer(
                            title     = dto.name,
                            videoKey  = dto.key,
                            thumbnail = "https://img.youtube.com/vi/${dto.key}/0.jpg"
                        )
                    }
                    .orEmpty()
                trailersAdapter.updateItems(trailers)

            } catch (e: Exception) {
                Log.e("DetailActivity", "Error cargando detalles", e)
                Toast.makeText(
                    this@DetailActivity,
                    "No se pudo cargar detalles: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    private fun checkWatchlist() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("watchlist").document(movie.id)
            .get()
            .addOnSuccessListener {
                isInWatchlist = it.exists()
                btnWatchlist.text = if (isInWatchlist) "En Por Ver" else "+ Agregar a Por Ver"
            }
    }

    private fun toggleWatchlist() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(uid)
            .collection("watchlist").document(movie.id)

        if (isInWatchlist) {
            ref.delete().addOnSuccessListener {
                isInWatchlist = false
                btnWatchlist.text = "+ Agregar a Por Ver"
            }
        } else {
            ref.set(mapOf(
                "title"     to movie.title,
                "posterUrl" to movie.posterUrl
            )).addOnSuccessListener {
                isInWatchlist = true
                btnWatchlist.text = "En Por Ver"
            }
        }
    }

    private fun loadComments() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("movies").document(movie.id)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snaps, _ ->
                val list = snaps?.map { doc ->
                    Comment(
                        id        = doc.id,
                        userId    = doc.getString("userId") ?: "",
                        userName  = doc.getString("userName") ?: "Anon",
                        text      = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        likes     = (doc.getLong("likes") ?: 0L).toInt()
                    )
                } ?: emptyList()
                commentsAdapter.updateItems(list)
            }
    }

    private fun postComment() {
        val uid  = auth.currentUser?.uid ?: return
        val name = prefs.getNombre() ?: "Anon"   // tu shared prefs helper
        val text = etNewComment.text.toString().trim()
        if (text.isEmpty()) return

        val c = Comment(
            userId    = uid,
            userName  = name,
            text      = text,
            timestamp = System.currentTimeMillis(),
            likes     = 0
        )
        db.collection("movies").document(movie.id)
            .collection("comments")
            .add(c)
            .addOnSuccessListener { etNewComment.text.clear() }
    }

    override fun onDestroy() {
        super.onDestroy()
        detailScope.cancel()
    }
}
