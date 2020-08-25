package com.internshala.echo.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.databases.EchoDatabase
import com.internshala.echo.fragments.FavouriteFragment
import com.internshala.echo.fragments.PlayingSongsFragment
import java.sql.SQLException

class FavouriteAdapter(playLists:ArrayList<String>, _context: Context):
    RecyclerView.Adapter<FavouriteAdapter.MyViewHolder>() {
    var playLists:ArrayList<String>?=null
    var context: Context? = null
    var favDatabase:EchoDatabase?=null

    init {
        this.playLists=playLists
        context = _context
        favDatabase= EchoDatabase(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_fav_adapter, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val playlistName=playLists?.get(position)
        holder.playlistTitle?.text = playlistName
        var isExpanded=false
        holder.expandButton?.setOnClickListener {
            if(isExpanded){
                holder.playlistSongsView?.visibility=View.GONE
                holder.expandButton?.setImageResource(R.drawable.ic_add_black_24dp)
                isExpanded=false
            }
            else{
                holder.playlistSongsView?.adapter=MainScreenAdapter(favDatabase?.queryPlaylist(playlistName as String) as ArrayList<Song>,
                                                                    context as Context,"fromFavAdapter", favDatabase!!,playlistName as String)
                holder.playlistSongsView?.layoutManager=LinearLayoutManager(context)
                holder.playlistSongsView?.itemAnimator=DefaultItemAnimator()
                holder.playlistSongsView?.visibility=View.VISIBLE
                holder.expandButton?.setImageResource(R.drawable.ic_baseline_minimize_24)
                isExpanded=true
            }
        }
        //Setting up pop up menu for playlist
        val songQueue=MainActivity.Statified.songQueue
        val popupMenu=PopupMenu(context,holder.contentHolder)
        popupMenu.menuInflater.inflate(R.menu.menu_song_options,popupMenu.menu)
        //Adding Delete playlist button and hiding add to playlist option
        popupMenu.menu.findItem(R.id.add_to_playlist).isVisible=false
        popupMenu.menu.findItem(R.id.delete_playlist).isVisible=true
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.play_next->{
                    try {
                        val songIdList = songQueue?.map { it.songID }
                        val insertIndex =
                            songIdList?.indexOf(PlayingSongsFragment.currentSongHelper?.songId)
                        songQueue?.addAll(insertIndex as Int+1,
                            favDatabase?.queryPlaylist(playlistName as String)!!
                        )
                        MainScreenAdapter.makeToast(
                            "This playlist will be played next",
                            context
                        )
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                        Log.e("tag",e.cause.toString())
                        MainScreenAdapter.makeToast(e.message as String,context)
                    }
                }
                R.id.add_to_queue->{
                    songQueue?.addAll(favDatabase?.queryPlaylist(playlistName as String)!!)
                    MainScreenAdapter.makeToast(
                        "This playlist will be played next",
                        context)
                }
                R.id.delete_playlist->{
                    AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete playlist?")
                        .setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
                            try {
                                favDatabase?.deletePlaylist(playlistName as String)
                            }
                            catch (e:SQLiteException){
                                e.printStackTrace()
                            }
                            playLists?.remove(playlistName as String)
                            notifyDataSetChanged()
                            MainScreenAdapter.makeToast("Playlist Deleted!",context)
                        }
                }
            }
            true
        }
        holder.contentHolder?.setOnLongClickListener {
            popupMenu.show()
            true
        }
    }
    override fun getItemCount(): Int {
        if (playLists == null)
            return 0
        else
            return (playLists as ArrayList<String>).size

    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playlistTitle: TextView? = null
        var contentHolder: RelativeLayout? = null
        var expandButton:ImageButton?=null
        var playlistSongsView:RecyclerView?=null

        init {
            playlistTitle = view.findViewById(R.id.PlaylistTitle)
            contentHolder = view.findViewById(R.id.contentHolder)
            expandButton=view.findViewById(R.id.expand_playlist_button)
            playlistSongsView=view.findViewById(R.id.playlist_songs_rec_view)
        }

    }
}
