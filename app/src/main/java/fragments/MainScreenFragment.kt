package com.internshala.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.adapters.MainScreenAdapter
import com.internshala.echo.databases.EchoDatabase
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class MainScreenFragment : Fragment() {

    var playPauseButton: ImageButton? = null
    var songTitleView: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongsLayout: RelativeLayout? = null
    var songsRecyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var mainScreenAdapter:MainScreenAdapter?=null
    var songArtist:String?=null
    var songTitle:String?=null
    var trackPosition:Int=0
    companion object {
        var songsList: ArrayList<Song>? = null
    }
        var favDatabase:EchoDatabase?=null

    object Statified{
        var mediaPlayer:MediaPlayer?=null
        var nowPlayingBottomBar: RelativeLayout? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        activity?.title="All Songs"
        visibleLayout = view?.findViewById(R.id.visibleLayout)
        Statified.nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarMainScreen)
        playPauseButton = view?.findViewById(R.id.PlayPauseButton)
        songTitleView = view?.findViewById(R.id.SongPlaying)
        songsRecyclerView = view?.findViewById(R.id.mainContent)
        noSongsLayout = view?.findViewById(R.id.NoSongsScreen)
        return view
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity=context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        songsList= getSongs()
        //Instantiating favorites database
        favDatabase=PlayingSongsFragment.favoriteContent
        if(favDatabase==null) {
            favDatabase = EchoDatabase(myActivity)
            favDatabase!!.createPlaylistsTable()
        }

        if(songsList==null)
        {
            visibleLayout?.visibility=View.INVISIBLE
            noSongsLayout?.visibility=View.VISIBLE
        }
        else {
            mainScreenAdapter =
                MainScreenAdapter(songsList as ArrayList<Song>, myActivity as Context,
                    "fromMainScreen",favDatabase!!,null)
            val layout = LinearLayoutManager(myActivity)
            songsRecyclerView?.layoutManager = layout
            songsRecyclerView?.itemAnimator = DefaultItemAnimator()
            songsRecyclerView?.adapter = mainScreenAdapter

            var pref_ascending=myActivity?.getSharedPreferences("sort_ascending",Context.MODE_PRIVATE)
            var pref_datewise=myActivity?.getSharedPreferences("sort_datewise",Context.MODE_PRIVATE)
            var isAscendingAllowed=pref_ascending?.getBoolean("sort_ascending",true)
            var isDatewiseAllowed=pref_datewise?.getBoolean("sort_datewise",false)
            if(isAscendingAllowed as Boolean)
            {
                Collections.sort(songsList as ArrayList<Song>,Song.Statified.nameComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            }
            else if(isDatewiseAllowed as Boolean) {
                Collections.sort(songsList as ArrayList<Song>, Song.Statified.dateComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            }
            bottomBarSetup()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val switcher=item.itemId
        if(switcher==R.id.sort_ascending){
            var editor=myActivity?.getSharedPreferences("sort_ascending",Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("sort_ascending",true)
            editor?.putBoolean("sort_datewise",false)
            editor?.apply()
            if(songsList!=null) {
                Collections.sort(songsList as ArrayList<Song>, Song.Statified.nameComparator)
                mainScreenAdapter?.notifyDataSetChanged()
                return false
            }
        }
        else if(switcher==R.id.sort_date){
            var editor=myActivity?.getSharedPreferences("sort_datewise",Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("sort_ascending",false)
            editor?.putBoolean("sort_datewise",true)
            editor?.apply()
            if(songsList!=null) {
                Collections.sort(songsList as ArrayList<Song>, Song.Statified.dateComparator)
                mainScreenAdapter?.notifyDataSetChanged()
                return false
            }
        }
        else if(switcher==R.id.search_icon) {
            try {
                val searchView =MenuItemCompat.getActionView(item) as android.widget.SearchView
                searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        mainScreenAdapter?.getFilter(newText as String)?.filter(newText)
                        return false
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun getSongs(): ArrayList<Song> {

        var arrayList = ArrayList<Song>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songDate = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentArtist = songCursor.getString(songArtist)
                var currentTitle = songCursor.getString(songTitle)
                var currentDate = songCursor.getLong(songDate)
                var currentData = songCursor.getString(songData)
                arrayList.add(
                    Song(
                        currentId,
                        currentDate,
                        currentTitle,
                        currentArtist,
                        currentData
                    )
                )
            }
        }
        return arrayList
    }
    fun bottomBarSetup(){
        try {
            if(PlayingSongsFragment.Statified.mediaPlayer==null)
                Statified.nowPlayingBottomBar?.visibility = View.INVISIBLE
            else {
                Statified.nowPlayingBottomBar?.visibility = View.VISIBLE
                trackPosition =
                    PlayingSongsFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                bottomBarClickHandler()
                songArtist = PlayingSongsFragment.currentSongHelper?.songArtist
                songTitle = PlayingSongsFragment.currentSongHelper?.songTitle
                songTitleView?.text = songTitle

                //On completion of song, invoke onSongComplete() and change songTitle view
                PlayingSongsFragment.Statified.mediaPlayer?.setOnCompletionListener {
                    PlayingSongsFragment.onSongComplete()
                    songTitleView?.text = PlayingSongsFragment.currentSongHelper?.songTitle
                }

                if (PlayingSongsFragment.Statified.mediaPlayer?.isPlaying as Boolean)
                    playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
                else
                    playPauseButton?.setBackgroundResource(R.drawable.play_icon2)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
            makeToast(e.message as String)
        }
    }
    fun bottomBarClickHandler(){
        Statified.nowPlayingBottomBar?.setOnClickListener {
            Statified.mediaPlayer=PlayingSongsFragment.Statified.mediaPlayer
            val args=Bundle()
            val playingSongsFragment=PlayingSongsFragment()
            args.putString("songArtist",songArtist)
            args.putString("songTitle",songTitle)
            args.putString("path",PlayingSongsFragment.currentSongHelper?.songPath)
            args.putLong("songId",PlayingSongsFragment.currentSongHelper?.songId as Long)
            args.putInt("position",PlayingSongsFragment.currentSongHelper?.currentPosition as Int)
            args.putLong("dateAdded",PlayingSongsFragment.currentSongHelper?.dateAdded as Long)
            args.putParcelableArrayList("songData",songsList)
            //To let playingSongFragment know that transaction took place by clicking bottom Bar
            args.putString("MainBottomBar","success")
            playingSongsFragment.arguments=args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frag,playingSongsFragment as Fragment,"NowPlayingFragment")
                ?.addToBackStack("PlayingSongsFragment")
                ?.commit()
        }
        playPauseButton?.setOnClickListener {
            if(PlayingSongsFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                PlayingSongsFragment.Statified.mediaPlayer?.pause()
                trackPosition=PlayingSongsFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon2)
            } else{
                PlayingSongsFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                PlayingSongsFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }
    private fun makeToast(s:String){
        Toast.makeText(myActivity,s,Toast.LENGTH_LONG).show()
    }
}



