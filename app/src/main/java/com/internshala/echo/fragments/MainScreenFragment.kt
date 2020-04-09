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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.adapters.MainScreenAdapter
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class MainScreenFragment : Fragment() {

    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var mainScreenAdapter:MainScreenAdapter?=null
    var artist:String?=null
    var title:String?=null
    var trackPosition:Int=0
    var songsList:ArrayList<Songs>?=null
    object Statified{
        var mediaPlayer:MediaPlayer?=null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        activity?.title="All Songs"
        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visibleLayout)
        nowPlayingBottomBar = view?.findViewById<RelativeLayout>(R.id.hiddenBarMainScreen)
        playPauseButton = view?.findViewById<ImageButton>(R.id.PlayPauseButton)
        songTitle = view?.findViewById<TextView>(R.id.SongPlaying)
        recyclerView = view?.findViewById<RecyclerView>(R.id.mainContent)
        noSongs = view?.findViewById<RelativeLayout>(R.id.NoSongsScreen)
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        songsList= getSongs()
        if(songsList==null)
        {
            visibleLayout?.visibility=View.INVISIBLE
            noSongs?.visibility=View.VISIBLE
        }
        else {

            mainScreenAdapter =
                MainScreenAdapter(songsList as ArrayList<Songs>, myActivity as Context)
            val layout = LinearLayoutManager(myActivity)
            recyclerView?.layoutManager = layout
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = mainScreenAdapter

            var pref_ascending=myActivity?.getSharedPreferences("sort_ascending",Context.MODE_PRIVATE)
            var pref_datewise=myActivity?.getSharedPreferences("sort_datewise",Context.MODE_PRIVATE)
            var isAscendingAllowed=pref_ascending?.getBoolean("sort_ascending",true)
            var isDatewiseAllowed=pref_datewise?.getBoolean("sort_datewise",false)
            if(isAscendingAllowed as Boolean)
            {Collections.sort(songsList as ArrayList<Songs>,Songs.Statified.nameComparator)
               mainScreenAdapter?.notifyDataSetChanged()
            }
            else if(isDatewiseAllowed as Boolean) {
                Collections.sort(songsList as ArrayList<Songs>, Songs.Statified.dateComparator)
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
                Collections.sort(songsList as ArrayList<Songs>, Songs.Statified.nameComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        else if(switcher==R.id.sort_date){
            var editor=myActivity?.getSharedPreferences("sort_datewise",Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("sort_ascending",false)
            editor?.putBoolean("sort_datewise",true)
            editor?.apply()
            if(songsList!=null) {
                Collections.sort(songsList as ArrayList<Songs>, Songs.Statified.dateComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun getSongs(): ArrayList<Songs> {

        var arrayList = ArrayList<Songs>()
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
                    Songs(
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
            bottomBarClickHandler()
            artist = PlayingSongsFragment.Statified.currentSongHelper?.songArtist
            title = PlayingSongsFragment.Statified.currentSongHelper?.songTitle
            songTitle?.setText(title)
            PlayingSongsFragment.Statified.mediaPlayer?.setOnCompletionListener({
                PlayingSongsFragment.Staticated.onSongComplete()
            })
            if (PlayingSongsFragment.Statified.mediaPlayer?.isPlaying as Boolean)
                nowPlayingBottomBar?.visibility = View.VISIBLE
            else
                nowPlayingBottomBar?.visibility = View.INVISIBLE
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer=PlayingSongsFragment.Statified.mediaPlayer
            val args=Bundle()
            val playingSongsFragment=PlayingSongsFragment()
            args.putString("songArtist",artist)
            args.putString("songTitle",title)
            args.putString("path",PlayingSongsFragment.Statified.currentSongHelper?.songPath)
            args.putLong("songId",PlayingSongsFragment.Statified.currentSongHelper?.songId as Long)
            args.putInt("position",PlayingSongsFragment.Statified.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData",songsList)
            args.putString("MainBottomBar","success")
            playingSongsFragment.arguments=args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frag,playingSongsFragment as Fragment)
                ?.addToBackStack("PlayingSongsFragment")
                ?.commit()

        })
        playPauseButton?.setOnClickListener({
            if(PlayingSongsFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                PlayingSongsFragment.Statified.mediaPlayer?.pause()
                trackPosition=PlayingSongsFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon2)
            }
            else{
                PlayingSongsFragment.Statified.mediaPlayer?.seekTo(trackPosition as Int)
                PlayingSongsFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }
}



