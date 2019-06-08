package com.example.audioplayer

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.IOException




class MainActivity : AppCompatActivity() {

    private var mp: MediaPlayer = MediaPlayer()
    private val songsInDir = SongsBrowser().getSongList()
    private var indexOfSongInDir = 0
    private var canPlay: Boolean = false
    private var timeOfCurrentSong = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPermissions()

        val btnPlayPause = findViewById(R.id.btnPlayPause) as Button
        btnPlayPause.setOnClickListener {
            btnPlayPauseClicked()
        }

        val btnPlayPrev = findViewById(R.id.btnPlayPrev) as Button
        btnPlayPrev.setOnClickListener {
            playPrevSong()
        }

        val btnPlayNext = findViewById(R.id.btnPlayNext) as Button
        btnPlayNext.setOnClickListener {
            playNextSong()
        }

        initPlayerWithSongAndSettings()

        volumeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val volumeNum = progress / 100.0f
                        mp.setVolume(volumeNum, volumeNum)
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )

        progressBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mp.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )

        Thread(Runnable {
            while (mp != null) {
                try {
                    val msg = Message()
                    msg.what = mp.currentPosition
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()
    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what
            progressBar.progress = currentPosition
            tvElapsedTime.text = getTime(currentPosition)
            tvLeftTime.text = getTime(timeOfCurrentSong - currentPosition)
        }
    }

    fun initPlayerWithSongAndSettings() {
        if (songsInDir.size >= 1) {
            canPlay = true

            mp.setOnCompletionListener({ mp ->
                increaseSongIndex()
                playSong(indexOfSongInDir)
            })

            mp.setDataSource(songsInDir.get(0).get("songPath"))
            mp.setVolume(0.5f, 0.5f)
            mp.prepare()
            timeOfCurrentSong = mp.duration
            progressBar.max = timeOfCurrentSong
            mp.setVolume(0.5f, 0.5f)
        }
        else {
            canPlay = false
            Toast.makeText(this, "No songs found in " + Uri.parse(Environment.getExternalStorageDirectory().getPath()).toString() + "/Music", Toast.LENGTH_LONG).show()
        }

    }

    fun getTime(time: Int): String {
        val minutes = time / 1000 / 60
        val seconds = time / 1000 % 60
        val sTime = minutes.toString() + ":" + (if (seconds < 10) "0" else "") + seconds.toString()
        return sTime
    }

    fun btnPlayPauseClicked() {
        if (canPlay) {
            if (mp.isPlaying) {
                mp.pause()
                btnPlayPause.setBackgroundResource(R.drawable.fa_play)
            }
            else {
                mp.start()
                btnPlayPause.setBackgroundResource(R.drawable.fa_pause)
                setCurrentSongData()
            }
        }
    }

    fun playNextSong() {
        if (canPlay) {
            increaseSongIndex()
            playSong(indexOfSongInDir)
        }
    }

    fun playPrevSong() {
        if (canPlay) {
            decreaseSongIndex()
            playSong(indexOfSongInDir)
        }
    }

    fun getPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 9111)
        }
    }


    fun setCurrentSongData() {
        val songTitle = songsInDir.get(indexOfSongInDir).get("songTitle")
        val songTitleLabel = findViewById<TextView>(R.id.tvSongTitle)
        songTitleLabel.setText(songTitle)
    }


    fun playSong(songIndex: Int) {
        if (canPlay) {
            try {
                mp.reset()
                mp.setDataSource(songsInDir.get(songIndex).get("songPath"))

                mp.prepare()
                timeOfCurrentSong = mp.duration
                progressBar.max = timeOfCurrentSong

                mp.start()

                val songTitle = songsInDir.get(songIndex).get("songTitle")
                val songTitleLabel = findViewById<TextView>(R.id.tvSongTitle)
                songTitleLabel.setText(songTitle)

            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun increaseSongIndex() {
        if (indexOfSongInDir < songsInDir.size - 1) {
            indexOfSongInDir += 1
        } else {
            indexOfSongInDir = 0
        }
    }

    fun decreaseSongIndex() {
        if (indexOfSongInDir > 0) {
            indexOfSongInDir -= 1
        } else {
            indexOfSongInDir = songsInDir.size - 1
        }
    }

}
