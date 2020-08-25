package com.internshala.echo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieAnimationView
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.fragments.PlayingSongsFragment
import kotlinx.android.synthetic.main.custom_row_queue.view.*

class QueueAdapter(list:ArrayList<Song>,context:Context) :RecyclerView.Adapter<QueueAdapter.QueueItemViewHolder>(){
    var list:ArrayList<Song>?=null
    var context:Context?=null
    var currentSongHelper:CurrentSongHelper?=null
    init {
        this.list=list
        this.context=context
        this.currentSongHelper=PlayingSongsFragment.currentSongHelper
    }
    class QueueItemViewHolder(v: View): RecyclerView.ViewHolder(v) {

        var contentHolder:RelativeLayout?=null
        var artistView:TextView?=null
        var titleView:TextView?=null
        var animation:LottieAnimationView?=null
        init {
            contentHolder=v.findViewById(R.id.contentHolder)
            artistView=v.findViewById(R.id.song_artist_view)
            titleView=v.findViewById(R.id.song_title_view)
            animation=v.findViewById(R.id.animation_now_playing)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueItemViewHolder {
        val itemView=LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_row_queue,parent,false)
        return QueueItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list?.size as Int
    }

    override fun onBindViewHolder(holder: QueueItemViewHolder, position: Int) {
        val song=list?.get(position)
        holder.titleView?.setText(song?.songTitle)
        holder.artistView?.setText(song?.songArtist)
        //Checking if the song is the one currently being played
        if(song?.songID==currentSongHelper?.songId) {

            holder.animation?.visibility=View.VISIBLE

            if(currentSongHelper?.isPlaying as Boolean)
            {
                holder.animation?.playAnimation()
                holder.animation?.loop(true)
            }
        }
        //Handling click event of song in queue
        holder.contentHolder?.setOnClickListener {
            if(PlayingSongsFragment.currentSongHelper?.isLoop as Boolean)
                MainScreenAdapter.makeToast("This song is on loop!",context)
            else {
                PlayingSongsFragment.playRandomSong(song?.songID as Long)
                holder.animation?.playAnimation()

            }
        }
    }
}