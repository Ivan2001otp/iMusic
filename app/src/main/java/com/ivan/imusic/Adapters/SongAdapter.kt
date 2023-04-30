package com.ivan.imusic.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.RequestManager
import com.ivan.imusic.R
import com.ivan.imusic.data.Song
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide:RequestManager)
    : BaseSongAdapter(R.layout.list_item) {

  override val differ = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = songs[position]
        holder.itemView.apply{
            this.findViewById<TextView>(R.id.tvPrimary).text = song.title
            this.findViewById<TextView>(R.id.tvSecondary).text = song.subtitle

            glide.load(song.imgUrl)
                .into(findViewById(R.id.ivItemImage))

            setOnClickListener {
                onItemClickListener?.let{
                    it(song)
                }
            }

        }
    }
}