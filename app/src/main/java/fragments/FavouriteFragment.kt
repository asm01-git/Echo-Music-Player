package com.internshala.echo.fragments


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.activities.MainActivity.Statified as StaticMainActivity
import com.internshala.echo.adapters.FavouriteAdapter
import com.internshala.echo.adapters.MainScreenAdapter
import com.internshala.echo.databases.EchoDatabase
import com.internshala.echo.fragments.FavouriteFragment.Statified.nowPlayingBottomBar

/**
 * A simple [Fragment] subclass.
 */
class FavouriteFragment : Fragment() {

    var myActivity: Activity? = null
    var noFavorites: RelativeLayout? = null

    var playPauseButton: ImageButton? = null
    var addPlaylist:FloatingActionButton?=null
    var songTitle: TextView? = null
    var playlistsRecyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    /*This variable will be used for database instance*/
    var favoriteContent: EchoDatabase? = null
    var favoriteAdapter:FavouriteAdapter?=null
    /*Variable to store favorites*/
    object Statified {
        var mediaPlayer: MediaPlayer? = null
        var nowPlayingBottomBar: RelativeLayout? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        setHasOptionsMenu(true)
        activity?.title="Favourites"
        noFavorites = view?.findViewById(R.id.NoFavouritesScreen)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarScreenFav)
        songTitle = view.findViewById(R.id.SongPlayingFav)
        playPauseButton = view.findViewById(R.id.PlayPauseButtonFav)
        addPlaylist=view?.findViewById(R.id.add_playlist_button)
        playlistsRecyclerView = view.findViewById(R.id.fav_list)
        return view
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent= StaticMainActivity.favDatabase

        playlistsRecyclerView?.adapter=FavouriteAdapter(EchoDatabase.s.playLists,myActivity as Context)
        playlistsRecyclerView?.layoutManager=LinearLayoutManager(myActivity)
        playlistsRecyclerView?.itemAnimator=DefaultItemAnimator()

        addPlaylist?.setOnClickListener {
            createNewPlaylistDialog()
        }
        bottomBarSetup()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isNotFirstTime",true)
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item1=menu.findItem(R.id.sort)
        item1.isVisible=false
    }
    /*As the name suggests, this function is used to fetch the songs present in your phones
    and returns the arraylist of the same*/
    fun bottomBarSetup() {
        try {
            if (PlayingSongsFragment.Statified.mediaPlayer==null)
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            else{
                nowPlayingBottomBar?.visibility = View.VISIBLE
                trackPosition = PlayingSongsFragment.Statified.mediaPlayer?.currentPosition as Int
                bottomBarClickHandler()
                songTitle?.setText(PlayingSongsFragment.currentSongHelper?.songTitle)
                PlayingSongsFragment.Statified.mediaPlayer?.setOnCompletionListener {

                    PlayingSongsFragment.onSongComplete()
                    songTitle?.setText(PlayingSongsFragment.currentSongHelper?.songTitle)
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            MainScreenAdapter.makeToast(e.message as String,context)
        }
    }
    /*The bottomBarClickHandler() function is used to handle the click events on the bottom
    bar*/
    fun bottomBarClickHandler() {
/*We place a click listener on the bottom bar*/
        nowPlayingBottomBar?.setOnClickListener {
            /*Using the same media player object*/
            Statified.mediaPlayer = PlayingSongsFragment.Statified.mediaPlayer
            val playingSongsFragment = PlayingSongsFragment()
            var args = Bundle()
            args.putString("songArtist", PlayingSongsFragment.currentSongHelper?.songArtist)
            args.putString("songTitle", PlayingSongsFragment.currentSongHelper?.songTitle)
            args.putString("path", PlayingSongsFragment.currentSongHelper?.songPath)
            args.putInt("songId", PlayingSongsFragment.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", PlayingSongsFragment.currentSongHelper?.currentPosition as Int)
            args.putLong("dateAdded",PlayingSongsFragment.currentSongHelper?.dateAdded as Long)
            args.putParcelableArrayList("songData", PlayingSongsFragment.fetchSongs)
            args.putString("FavBottomBar", "success")
            playingSongsFragment.arguments = args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frag, playingSongsFragment as Fragment,"NowPlayingFragment")
                ?.addToBackStack("PlayingSongsFragment")
                ?.commit()
        }
        playPauseButton?.setOnClickListener {
            if (PlayingSongsFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                PlayingSongsFragment.Statified.mediaPlayer?.pause()
                trackPosition = PlayingSongsFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon2)
            } else {
                PlayingSongsFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                PlayingSongsFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }
    fun createNewPlaylistDialog(){
        var editText=TextInputEditText(context)
        editText.setHint("Enter name of new playlist")
        val lp=LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10,10,10,10)
        editText.layoutParams=lp
        AlertDialog.Builder(context)
            .setView(editText)
            .setTitle("Create New Playlist")
            .setPositiveButton("Confirm") { dialog, which ->
                val enteredText=editText.text.toString()
                favoriteContent?.createPlayList(enteredText)
                favoriteAdapter?.notifyDataSetChanged()
            }
            .create().show()
    }
    /*The below function is used to search the favorites and display
    fun display_favorites_by_searching() {
        if (favoriteContent?.checkSize() as Int > 0) {
            refreshList = ArrayList()
            getListfromDatabase = favoriteContent?.queryPlaylist()
            val fetchListfromDevice = getSongsFromPhone()
            if (fetchListfromDevice != null) {

                for (i in 0..fetchListfromDevice?.size - 1) {

                    for (j in 0..getListfromDatabase?.size as Int - 1) {

                        if (getListfromDatabase?.get(j)?.songID ===
                            fetchListfromDevice?.get(i)?.songID) {

                            refreshList?.add((getListfromDatabase as ArrayList<Song>)
                                    [j])
                        }
                    }
                }
            }
/*If refresh list is null we display that there are no favorites*/
            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            } else {
/*Else we setup our recycler view for displaying the favorite songs*/
                val favoriteAdapter = FavouriteAdapter(refreshList as ArrayList<Song>,
                    myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        } else {
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }
    }*/
}