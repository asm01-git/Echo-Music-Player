package com.internshala.echo.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.databases.EchoDatabase
import com.internshala.echo.fragments.PlayingSongsFragment
import kotlin.collections.ArrayList

class MainScreenAdapter (_songsList:ArrayList<Song>, _context: Context,fromFavourites:String,favDatabase:EchoDatabase,playList:String?):
    RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>() {
    var songList:ArrayList<Song>?=null
    var fullSongsList:ArrayList<Song>?=null
    var context:Context?=null
    var fromFavourites:String?=null
    var favDatabase:EchoDatabase?=null
    var playList:String?=null

    init{
        songList=_songsList
        fullSongsList= ArrayList(_songsList)
        context=_context
        this.fromFavourites=fromFavourites
        this.favDatabase=favDatabase
        this.playList=playList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

    val itemView=LayoutInflater.from(parent.context)
    .inflate(R.layout.row_custom_mainscreen_adapter,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObj=songList?.get(position)
        holder.trackTitle?.text=songObj?.songTitle
        holder.trackArtist?.text=songObj?.songArtist
        if(PlayingSongsFragment.currentSongHelper!=null) {
            //Attaching popup menu to the layout container
            val popupMenu = PopupMenu(context, holder.contentHolder)
            popupMenu.menuInflater.inflate(R.menu.menu_song_options, popupMenu.menu)
            if (fromFavourites.equals("fromFavAdapter")) {
                popupMenu.menu.findItem(R.id.add_to_playlist).isVisible=false
                popupMenu.menu.findItem(R.id.song_delete_from_playlist).isVisible=true
            }

            //Setting up item click listener of popup menu
            popupMenu.setOnMenuItemClickListener {
                val songQueue = MainActivity.Statified.songQueue
                when (it.itemId) {
                    R.id.play_next -> {
                        try {
                            val songIdList = songQueue?.map { it.songID }
                            val insertIndex =
                                songIdList?.indexOf(PlayingSongsFragment.currentSongHelper?.songId)
                            songQueue?.add(insertIndex as Int + 1, songObj as Song)
                            makeToast("Song will be played next", context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    R.id.add_to_queue -> {
                       addSongToQueue(songObj as Song)
                    }
                    R.id.add_to_playlist -> {
                        try {
                            showPlaylists(context, songObj as Song)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    R.id.song_delete_from_playlist -> {
                        favDatabase?.deleteFavourite(songObj?.songID as Long, playList as String)
                        songList?.remove(songObj)
                        notifyDataSetChanged()
                    }
                }
                true
            }
            //Popup menu is shown when the layout is clicked for long time
            holder.contentHolder?.setOnLongClickListener {
                popupMenu.show()
                true
            }
        }

        holder.contentHolder?.setOnClickListener {
            if(MainActivity.Statified.songQueue?.isEmpty() as Boolean) {
                val args = Bundle()
                args.putString("songArtist", songObj?.songArtist)
                args.putString("path", songObj?.songData)
                args.putInt("songId", songObj?.songID?.toInt() as Int)
                args.putString("songTitle", songObj.songTitle)
                args.putInt("position", position)
                args.putLong("dateAdded",songObj.dateAdded)
                args.putString("MainBottomBar", null)//Transaction took place by clicking on song, not from bottom Bar
                args.putString("FavBottomBar", null)
                args.putParcelableArrayList("songData", songList)
                val playingSongsFragment = PlayingSongsFragment()
                /*If we click the song while another song is playing in the background or when bottom bar has been formed,
             *Both of them will play at the same time
             * To avoid that, stop the first mediaPlayer object before playing the new one
             *
            if(fromFavourites.equals("fromFavAdapter")){
                if(FavouriteFragment.Statified.nowPlayingBottomBar?.visibility==View.VISIBLE)
                    PlayingSongsFragment.Statified.mediaPlayer?.stop()
            }
            else{
                if(MainScreenFragment.Statified.nowPlayingBottomBar?.visibility==View.VISIBLE)
                    PlayingSongsFragment.Statified.mediaPlayer?.stop()
            }*/
                playingSongsFragment.arguments = args
                MainActivity.Statified.songQueue?.add(songObj)
                (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frag, playingSongsFragment, "NowPlayingFragment")
                    .addToBackStack("PlayingSongsFragment")
                    .commit()
            }
            else {
                addSongToQueue(songObj as Song)
            }
        }
    }

    override fun getItemCount(): Int {
        if(songList==null)
            return 0
        else
            return (songList as ArrayList<Song>).size

    }
    fun getFilter(query:String):Filter{
        return filter
    }
    val filter=object:Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var queriedList=ArrayList<Song>()
            if(constraint.isNullOrEmpty()){
                queriedList.addAll(fullSongsList!!)
            }
            else{
                for(i in 0..fullSongsList?.size as Int-1){
                    var song=fullSongsList?.get(i)
                    if(song?.songTitle?.contains(constraint,true) as Boolean||song.songArtist.contains(constraint,true))
                        queriedList.add(song)
                }
            }
            val filterResults=FilterResults()
            filterResults.values=queriedList
            return filterResults
        }
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            songList?.clear()
            songList?.addAll(results?.values as ArrayList<Song>)
            notifyDataSetChanged()
        }

    }
    fun addSongToQueue(songObj:Song){
        if(!MainActivity.Statified.songQueue!!.contains(songObj as Song)) {
            MainActivity.Statified.songQueue?.add(songObj as Song)
            makeToast("Song added to Queue!", context)
        }
        else
            makeToast("This song is already in the queue!",context)
    }
        fun showPlaylists(context:Context?,song:Song) {
            val list = EchoDatabase.s.playLists
            val playLists =
                list.toArray(Array<CharSequence>(EchoDatabase.s.playLists.size) { i -> list[i] })

            if(list.size==0)
                Toast.makeText(context,"No playlists found!Go to favourites tab and create a playlist!",Toast.LENGTH_SHORT).show()
            else {
                AlertDialog.Builder(PlayingSongsFragment.myActivity)
                    .setTitle("Choose a playlist")
                    .setSingleChoiceItems(playLists as Array<out CharSequence>,-1){dialog, which ->
                        dialog.dismiss()
                        val playListToBeAdded=playLists[which].toString()
                        favDatabase?.storeAsFavorite(song, playListToBeAdded)
                        makeToast("Song added to " + playListToBeAdded + " successfully", context)
                    }
                    .create().show()
            }
        }
    companion object {
        fun makeToast(s:String,context: Context?){
            Toast.makeText(context,s,Toast.LENGTH_SHORT).show()
        }
    }
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        var trackTitle:TextView?=null
        var trackArtist:TextView?=null
        var contentHolder:RelativeLayout?=null
        init {
            trackTitle=view.findViewById(R.id.PlaylistTitle)
            trackArtist=view.findViewById(R.id.SongArtist)
            contentHolder=view.findViewById(R.id.contentHolder)
        }
    }
}