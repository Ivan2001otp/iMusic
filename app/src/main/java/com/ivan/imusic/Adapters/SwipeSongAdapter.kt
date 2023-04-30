package com.ivan.imusic.Adapters

import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import com.ivan.imusic.R

class SwipeSongAdapter : BaseSongAdapter(R.layout.swipe_item){


   override val differ = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = songs[position]
        holder.itemView.apply{
            val text_ = "${song.title} - ${song.subtitle}"
            this.findViewById<TextView>(R.id.tvPrimary).text = text_

            setOnClickListener {
                onItemClickListener?.let{
                    it(song)
                }
            }
        }
    }
}