package com.ivan.imusic.Service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.ivan.imusic.Callbacks.MusicNotificationListener
import com.ivan.imusic.Callbacks.MusicPlayerEventListener
import com.ivan.imusic.Callbacks.MusicPlayerPreparer
import com.ivan.imusic.Constants.Others.MEDIA_ROOT_ID
import com.ivan.imusic.Constants.Others.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

//this is done to achieve music play even when the device is in sleep.
private const val SERVICE_TAG = "service_tag"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
/* note that you cannot use @inject into private variables*/
    @Inject
   lateinit var dataSourceFactory: DefaultDataSource.Factory

   @Inject
   lateinit var exoplayer:ExoPlayer

    @Inject
   lateinit var firebaseMusicSource:FirebaseMusicSource

   //to implement service in coroutine
   var isForegroundService = false

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    //current session that can be used to communicate to the service
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var  mediaSessionConnector:MediaSessionConnector
    private lateinit var musicNotificationManager: MusicNotificationManager
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private var currPlayingSong:MediaMetadataCompat? = null

    private var isPlayerInitialize = false


    companion object {
        var currSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        //fetch the songs
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }


        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)
            ?.let{
                PendingIntent.getActivity(this,0,it,PendingIntent.FLAG_IMMUTABLE)
            }

        //set the media session to service
        mediaSession=MediaSessionCompat(this,SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive=true
        }

        //set the connector btw media session and exoplayer running in service
//        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
//            setPlayer(exoplayer)
//        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager= MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicNotificationListener(this)
        ){
            currSongDuration = exoplayer.duration
        }

        val musicPlayerPreparer= MusicPlayerPreparer(firebaseMusicSource){
            currPlayingSong = it
            preparePlayer(firebaseMusicSource.songs,it,true)
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlayerPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoplayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoplayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoplayer)

    }

    //this is created to  connect the notification with musicMetadataCompat
    private inner class MusicQueueNavigator:TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }

    }

    private fun preparePlayer(
        songs:List<MediaMetadataCompat>,
        itemToPlay:MediaMetadataCompat?,
        playNow:Boolean
    ){
        val currSongIndex = if(currPlayingSong==null) 0 else songs.indexOf(itemToPlay)

        //set the selected music configuration to exoplayer
        exoplayer.setMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoplayer.seekTo(currSongIndex,0L)
        exoplayer.playWhenReady = playNow

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoplayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoplayer.removeListener(musicPlayerEventListener)
        exoplayer.release()
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
            when(parentId){
                MEDIA_ROOT_ID->{
                 val resultsSend =   firebaseMusicSource.whenReady {isInitialized->
                        if(isInitialized) {
                            result.sendResult(firebaseMusicSource.asMediaItems())
                            if (!isPlayerInitialize && firebaseMusicSource.songs.isNotEmpty()) {
                                preparePlayer(
                                    firebaseMusicSource.songs,
                                    firebaseMusicSource.songs[0],
                                    false
                                )
                                isPlayerInitialize = true
                            }
                        }else{
                                mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                                result.sendResult(null)
                            }
                    }
                    if(!resultsSend){
                        result.detach()
                    }
                }
            }
    }
}