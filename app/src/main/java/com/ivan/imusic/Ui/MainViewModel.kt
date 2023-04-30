package com.ivan.imusic.Ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ivan.imusic.Constants.Others.MEDIA_ROOT_ID
import com.ivan.imusic.Constants.Resource
import com.ivan.imusic.Service.MusicServiceConnection
import com.ivan.imusic.Service.isPlayEnabled
import com.ivan.imusic.Service.isPlaying
import com.ivan.imusic.Service.isPrepared
import com.ivan.imusic.data.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val  musicServiceConnection: MusicServiceConnection
) : ViewModel(){

    //get all the mediaitems in the form of list of songs

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currPlayingSong = musicServiceConnection.currPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init{
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)

                val items = children.map{
                    Song(
                        it.mediaId!!,
                        it.description.subtitle.toString(),
                        it.description.iconUri.toString(),
                        it.description.mediaUri.toString(),
                        it.description.title.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }


    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPrevious(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos:Long){
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem:Song,toggle:Boolean=false){

        val isPrepared = playbackState.value?.isPrepared ?: false


        Log.d("tag", "isPrepared value : $isPrepared")
        Log.d("tag", "isprepared value 2: ${playbackState.value?.isPrepared}")


        if(isPrepared && mediaItem.musicId ==
                currPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){

            playbackState.value?.let{playbackState->
                when{
                    playbackState.isPlaying->{
                        Log.d("tag", "playOrToggleSong: isplaying inside")

                        if(toggle){
                            Log.d("tag", "playOrToggleSong: isPlaying and toogle if")
                            musicServiceConnection.transportControls.pause()}
                    }
                    playbackState.isPlayEnabled-> {
                        Log.d("tag", "playOrToggleSong: isPlayEnabled")
                        musicServiceConnection.transportControls.play()}
                    else->{
                        Log.d("tag", "playOrToggleSong: nothing")

                        Unit}
                }
            }
        }else{
            Log.d("tag", "mediaConnection: transport Controls - else block")

            musicServiceConnection.transportControls.playFromMediaId(mediaItem.musicId,null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,object : MediaBrowserCompat.SubscriptionCallback(){})
    }
}