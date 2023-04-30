package com.ivan.imusic.Service

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.ivan.imusic.data.Song
import com.ivan.imusic.remote.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    var songs = emptyList<MediaMetadataCompat>()


    suspend fun fetchMediaData(){
        state = State.STATE_INITIALIZING

        getAllSongs_()

        state = State.STATE_INITIALIZED
    }

    suspend fun getAllSongs_()= withContext(Dispatchers.IO){
        val allSongs = musicDatabase.getAllSongs()

        //mapping allsongs to mediaMetadatalist
        songs = allSongs.map{song->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST,song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID,song.musicId)
                .putString(METADATA_KEY_TITLE,song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE,song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI,song.imgUrl)
                .putString(METADATA_KEY_MEDIA_URI,song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI,song.imgUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE,song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,song.subtitle)
                .build()
        }
    }


    //convert mediaMetadata to mediaSource
    //final state of music object
    fun asMediaItems()=songs.map{song->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
    }.toMutableList()


    //creating concatenating media Source
    fun asMediaSource(dataSourceFactory:DefaultDataSource.Factory):ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach {song->
            val mediaSource = ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI).toUri()))

            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    private val onReadyListeners = mutableListOf<(Boolean)->Unit>()

    private var state:State = State.STATE_CREATED
        set(value){
            if(value==State.STATE_INITIALIZED || value==State.STATE_ERROR){
                synchronized(onReadyListeners){
                    //only one thread at a time is allowed to get in.
                    field = value
                    onReadyListeners.forEach{listener->
                        listener(state==State.STATE_INITIALIZED)
                    }
                }
            }
            else{
                field= value
            }
        }

    fun whenReady(action:(Boolean)->Unit):Boolean{
        if(state == State.STATE_INITIALIZING || state == State.STATE_CREATED){
            onReadyListeners+=action
            return false;
        }else{
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }
}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}


