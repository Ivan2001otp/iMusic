package com.ivan.imusic.Ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivan.imusic.Constants.Others.UPDATE_PLAYER_POSITION_INTERVAL
import com.ivan.imusic.Service.MusicService
import com.ivan.imusic.Service.MusicServiceConnection
import com.ivan.imusic.Service.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject
constructor(musicServiceConnection: MusicServiceConnection) : ViewModel(){

    private val playbackState = musicServiceConnection.playbackState

    private val _currSongDuration = MutableLiveData<Long>()
    val currSongDuration : LiveData<Long> = _currSongDuration

    private val _currPlayerPosition = MutableLiveData<Long>()
    val currPlayerPosition:LiveData<Long> = _currPlayerPosition

    init{
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch{
            while(true){
                val pos = playbackState.value?.currentPlaybackPosition
                if(currPlayerPosition.value!=pos){
                    _currPlayerPosition.postValue(pos!!)
                    _currSongDuration.postValue(MusicService.currSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

}