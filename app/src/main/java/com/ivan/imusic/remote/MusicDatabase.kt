package com.ivan.imusic.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.ivan.imusic.Constants.Others.SONG_COLLECTION
import com.ivan.imusic.data.Song
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val collectionSong = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs():List<Song>{

        return try{
            collectionSong.get().await()
                .toObjects(Song::class.java)
        }catch (e:Exception){
            emptyList()
        }
    }
}