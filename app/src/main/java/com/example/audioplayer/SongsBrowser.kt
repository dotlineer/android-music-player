package com.example.audioplayer

import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FilenameFilter
import java.util.ArrayList
import java.util.HashMap


class SongsBrowser {
    internal val PATH = Uri.parse(Environment.getExternalStorageDirectory().getPath()).toString() + "/Music"
    internal val EXTENSION_LENGTH = 4
    private val songsPathsAndTitles = ArrayList<HashMap<String, String>>()

    fun getSongList(): ArrayList<HashMap<String, String>> {
        val dir = File(PATH)

        if (dir.listFiles(FileExtensionFilter()).isNotEmpty()) {
            for (file in dir.listFiles(FileExtensionFilter())) {
                val song = HashMap<String, String>()
                song["songTitle"] = file.getName().substring(0, file.getName().length - EXTENSION_LENGTH)
                song["songPath"] = file.getPath()
                songsPathsAndTitles.add(song)
            }
        }
        return songsPathsAndTitles
    }

    internal inner class FileExtensionFilter : FilenameFilter {
        override fun accept(dir: File, name: String): Boolean {
            return name.endsWith(".mp4") || name.endsWith(".mp3")
        }
    }

}
