package com.ivan.imusic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivan.imusic.Adapters.SongAdapter
import com.ivan.imusic.Constants.Status
import com.ivan.imusic.Ui.MainViewModel
import com.ivan.imusic.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter:SongAdapter

    private var _binding:FragmentHomeBinding ?= null
    private val binding
        get()=_binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        val view =  binding!!.root
        return view.rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())
            .get(MainViewModel::class.java)

        setUpRecyclerView()
        subscribeToObservers()

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun setUpRecyclerView() = binding!!.rvAllSongs.apply{
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext())
        adapter = songAdapter
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems
            .observe(viewLifecycleOwner){
                when(it.status){
                    Status.SUCCESS -> {
                        binding!!.allSongsProgressBar.isVisible = false
                        it.data?.let{
                          songAdapter.songs = it
                        }
                    }
                    Status.LOADING ->{
                        binding!!.allSongsProgressBar.isVisible = true
                    }
                    Status.ERROR -> Unit
                }
            }
    }


    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

}