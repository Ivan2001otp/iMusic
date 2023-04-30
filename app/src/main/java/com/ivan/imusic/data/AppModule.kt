package com.ivan.imusic.data

import android.content.Context
import android.media.MediaMetadataRetriever
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.MediaMetadata
import com.ivan.imusic.Adapters.SwipeSongAdapter
import com.ivan.imusic.R
import com.ivan.imusic.Service.MusicServiceConnection
import com.ivan.imusic.remote.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    )=MusicServiceConnection(context)


    @Singleton
    @Provides
    fun providesSwipeSongAdapter() = SwipeSongAdapter()

    @Singleton
    @Provides
    fun provideGlideInstance(
       @ApplicationContext context : Context
    ) = Glide.with(context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.music_img)
                .error(R.drawable.ic_baseline_library_music_24)
                .diskCacheStrategy(DiskCacheStrategy.DATA)

        )
}