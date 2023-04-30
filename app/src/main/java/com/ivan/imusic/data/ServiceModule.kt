package com.ivan.imusic.data

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import com.ivan.imusic.remote.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.sql.DataSource

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    //setting the meta data of music

    @ServiceScoped
    @Provides
    fun provideMusicDatabase() = MusicDatabase()

    @ServiceScoped
    @Provides
    fun provideAudioAttributes()=AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @ServiceScoped
    fun provideWithExoplayer(
      @ApplicationContext  context: Context,
      audioAttributes:AudioAttributes
    )=ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes,true)
        .setHandleAudioBecomingNoisy(true)
        .build()


    @Provides
    @ServiceScoped
    fun provideDataFactory(
       @ApplicationContext context:Context
    )=DefaultDataSource.Factory(context)
}