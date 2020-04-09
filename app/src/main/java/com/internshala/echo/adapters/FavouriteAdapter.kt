package com.internshala.echo.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.fragments.PlayingSongsFragment

class FavouriteAdapter(_songDetails:ArrayList<Songs>, _context: Context):
    RecyclerView.Adapter<FavouriteAdapter.MyViewHolder>() {
    var songDetails: ArrayList<Songs>? = null
    var context: Context? = null

    init {
        songDetails = _songDetails
        context = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObj = songDetails?.get(position)
        holder.trackTitle?.text = songObj?.songTitle
        holder.trackArtist?.text = songObj?.artist
        holder.contentHolder?.setOnClickListener({
            val playingSongsFragment = PlayingSongsFragment()
            val args = Bundle()
            args.putString("songArtist", songObj?.artist)
            args.putString("path", songObj?.songData)
            args.putInt("songId", songObj?.songID?.toInt() as Int)
            args.putString("songTitle", songObj.songTitle)
            args.putInt("position", position)
            args.putParcelableArrayList("songData", songDetails)
            (context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.frag, playingSongsFragment)
                .commit()
            playingSongsFragment.arguments = args
        })

    }

    override fun getItemCount(): Int {
        if (songDetails == null)
            return 0
        else
            return (songDetails as ArrayList<Songs>).size

    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = view.findViewById(R.id.SongTitle)
            trackArtist = view.findViewById(R.id.SongArtist)
            contentHolder = view.findViewById(R.id.contentHolder)
        }

    }
}
