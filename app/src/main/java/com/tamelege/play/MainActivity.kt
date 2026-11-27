package com.tamelege.play

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var nowPlayingText: TextView

    private val mediaPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Keep access to the selected document after the app is reopened.
            try {
                contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some document providers do not offer persistable permissions.
            }
            playMedia(selectedUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        nowPlayingText = findViewById(R.id.nowPlayingText)

        findViewById<MaterialButton>(R.id.chooseMediaButton).setOnClickListener {
            mediaPicker.launch(arrayOf("audio/*", "video/*"))
        }
        findViewById<MaterialButton>(R.id.playButton).setOnClickListener {
            player?.play()
        }
        findViewById<MaterialButton>(R.id.pauseButton).setOnClickListener {
            player?.pause()
        }
        findViewById<MaterialButton>(R.id.stopButton).setOnClickListener {
            player?.stop()
            player?.clearMediaItems()
            nowPlayingText.text = "Playback stopped"
        }

        initializePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
        }
    }

    private fun playMedia(uri: Uri) {
        val exoPlayer = player ?: return
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected media"
        nowPlayingText.text = "Now playing: $name"

        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        // Release resources when the activity is no longer visible.
        // Background audio playback will be added in the next development stage.
        playerView.player = null
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        if (player == null) initializePlayer()
    }

    override fun onDestroy() {
        playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }
}
