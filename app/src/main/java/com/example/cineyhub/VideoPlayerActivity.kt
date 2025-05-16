package com.example.cineyhub

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VIDEO_URL = "videoUrl"
    }

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        // Habilita la flecha de “Back”
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        videoView = findViewById(R.id.vvTrailer)

        // Recupera la URL
        val url = intent.getStringExtra(EXTRA_VIDEO_URL)
            ?: throw IllegalArgumentException("Se esperaba EXTRA_VIDEO_URL")

        // Prepara VideoView con controles
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.setVideoURI(Uri.parse(url))
        videoView.setOnPreparedListener {
            it.isLooping = false
            videoView.start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
