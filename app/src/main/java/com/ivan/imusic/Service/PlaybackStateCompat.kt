package com.ivan.imusic.Service

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

inline val PlaybackStateCompat.isPrepared
    get() = state == PlaybackStateCompat.STATE_PAUSED ||
            state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_BUFFERING


inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_BUFFERING

inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                    state==PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackStateCompat.currentPlaybackPosition : Long
    get() = if(state == PlaybackStateCompat.STATE_PLAYING){
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta*playbackSpeed)).toLong()
    }else position