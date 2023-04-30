package com.ivan.imusic.Service

import android.support.v4.media.MediaMetadataCompat
import com.ivan.imusic.data.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let{
        Song(
            it.mediaId ?: "",
            it.subtitle.toString(),
            it.iconUri.toString(),
            it.mediaUri.toString(),
            it.title.toString()
        )
    }
}