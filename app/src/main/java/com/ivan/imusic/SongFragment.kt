package com.ivan.imusic

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.ivan.imusic.Constants.Status
import com.ivan.imusic.Service.isPlaying
import com.ivan.imusic.Service.toSong
import com.ivan.imusic.Ui.MainViewModel
import com.ivan.imusic.Ui.SongViewModel
import com.ivan.imusic.data.Song
import com.ivan.imusic.databinding.FragmentSongBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {
    private var _binding:FragmentSongBinding ?= null
    private val binding
        get() = _binding

    @Inject
    lateinit var glide:RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel : SongViewModel by viewModels()

    private var currPlayingSong: Song?= null

    private var playbackState:PlaybackStateCompat ?= null
    private var shouldUpdateSeekbar = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSongBinding.inflate(layoutInflater,container,false)
        val view = binding!!.root
        return view.rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())
            .get(MainViewModel::class.java)
        subscribeToObservers()

        binding!!.ivPlayPauseDetail.setOnClickListener {
            currPlayingSong?.let{
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        binding!!.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPrevious()
        }

        binding!!.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }


        binding!!.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekbar: SeekBar?,progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
               shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {

                seekbar?.let{
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }

        })

    }


    private fun updateSongAndTitle(song : Song){
        val text_:String = "${song.title} - ${song.subtitle}"
        binding!!.tvSongName.text = text_
        glide.load(song.imgUrl)
            .into(binding!!.ivSongImage)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let{result->
                when(result.status){
                    Status.SUCCESS->{
                       result.data?.let{songs->
                           if(currPlayingSong == null && songs.isNotEmpty()){
                               currPlayingSong = songs[0]
                               updateSongAndTitle(songs[0])
                           }
                       }
                    }
                    else->Unit
                }
            }
        }

        mainViewModel.currPlayingSong.observe(viewLifecycleOwner){
            if(it==null)return@observe
            currPlayingSong = it.toSong()
            updateSongAndTitle(currPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState=it
            binding!!.ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.pause_ic else R.drawable.ic_play
            )
            binding!!.seekBar.progress = it?.position?.toInt() ?: 0

        }

        songViewModel.currPlayerPosition.observe(viewLifecycleOwner){
            if(shouldUpdateSeekbar){
                binding!!.seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }

        songViewModel.currSongDuration.observe(viewLifecycleOwner){
            binding!!.seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss",Locale.getDefault())
            binding!!.tvSongDuration.text = dateFormat.format(it)
        }

    }

    private fun setCurPlayerTimeToTextView(ms:Long){
        val dateformat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding!!.tvCurTime.text = dateformat.format(ms)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

}