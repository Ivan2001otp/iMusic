package com.ivan.imusic.Callbacks

import android.app.Service
import android.os.Build
import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.ivan.imusic.Service.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
): Player.Listener{


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if(playbackState == Player.STATE_READY){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                musicService.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            }
        }
    }


    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if(!playWhenReady){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                musicService.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            }
        }
    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService,"Poor internet connection",Toast.LENGTH_SHORT)
            .show()
    }
}