package com.ivan.imusic

import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.ivan.imusic.Adapters.SwipeSongAdapter
import com.ivan.imusic.Constants.Status
import com.ivan.imusic.Service.isPlaying
import com.ivan.imusic.Service.toSong
import com.ivan.imusic.Ui.MainViewModel
import com.ivan.imusic.data.Song
import com.ivan.imusic.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding


    private val mainViewModel : MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter : SwipeSongAdapter

    @Inject
    lateinit var glide : RequestManager

    private var currPlayingSong : Song? = null

    private var playbackState : PlaybackStateCompat?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeToObservers()
        binding.vpSong.adapter  = swipeSongAdapter
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController


        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if(playbackState?.isPlaying == true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])

                }else{
                    currPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener{
            currPlayingSong?.let{
                Log.d("tag", "currplaygin song: ${currPlayingSong}")

                mainViewModel.playOrToggleSong(it,true)
            }
        }



    swipeSongAdapter.setItemClickListener {
       navController
            .navigate(
                R.id.globalActionToSongFragment
            )
    }


        //on nav controller

    navController.addOnDestinationChangedListener{_,destination,_ ->
        when(destination.id){
            R.id.homeFragment->{showBottomBar()}
            R.id.songFragment->{hideBottomBar()}
            else->showBottomBar()
        }
    }
    /*binding.navHostFragment.findNavController()
            .addOnDestinationChangedListener{_,destination,_ ->
                when(destination.id){
                    R.id.homeFragment->{showBottomBar()}
                    R.id.songFragment->{hideBottomBar()}
                    else->showBottomBar()
                }
            }*/
    }


    private fun hideBottomBar(){
        binding.ivCurSongImage.isVisible = false
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible =false
    }

    private fun showBottomBar(){
        binding.ivCurSongImage.isVisible = true
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
    }


    private fun switchViewPagerToCurrentSong(song:Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex!=-1){
            binding.vpSong.currentItem = newItemIndex
            currPlayingSong = song
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let{
                when(it.status){

                    Status.SUCCESS->{
                        it.data?.let{songs->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()){
                                glide.load((currPlayingSong ?: songs[0]).imgUrl)
                                    .into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currPlayingSong?: return@observe )
                        }
                    }

                    Status.LOADING->Unit
                    Status.ERROR -> Unit
                }
            }
        }


        mainViewModel.currPlayingSong.observe(this){
            if(it==null) return@observe

            currPlayingSong = it.toSong()
            glide.load(currPlayingSong?.imgUrl)
                .into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(currPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this){
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.pause_ic else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let{
                when(it.status){
                    Status.ERROR->{
                        Snackbar.make(
                            binding.root,
                            it.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()

                    }

                    else->Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let{
                when(it.status){
                    Status.ERROR->{
                        Snackbar.make(
                            binding.root,
                            it.message ?: "Unexpected Network error occured",
                            Snackbar.LENGTH_LONG
                        ).show()

                    }

                    else->Unit
                }
            }
        }
    }
}