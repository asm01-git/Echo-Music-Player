package com.internshala.echo.fragments
import android.app.Activity
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.activities.MainActivity.Statified as StaticMainActivity
import com.internshala.echo.adapters.MainScreenAdapter
import com.internshala.echo.adapters.QueueAdapter
import com.internshala.echo.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class PlayingSongsFragment :androidx.fragment.app.Fragment() {
        object Statified {
            var mediaPlayer: MediaPlayer? = null
        }
    companion object {
        var currentSongHelper: CurrentSongHelper? = null
        var fetchSongs: ArrayList<Song>? = null
        var currentPosition: Int = 0
        var myActivity: Activity? = null
        //TextViews
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var prevSongView:TextView?=null
        var nextSongView:TextView?=null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var seekBar: SeekBar? = null
        //Favourite widgets
        var fab: ImageButton? = null
        var addedToFav:TextView?=null
        var playlists:ArrayList<String>?=null
        /*Variable for using DB functions*/
        var favoriteContent: EchoDatabase? = null
        //Button Variables
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null

        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        /*Sensor Variables*/
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        /*Shared Preferences file names*/
        var MY_PREFS_SHAKE = SettingsFragment.Statified.MY_PREF_SHAKE
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete() {
            if (currentSongHelper?.isShuffle as Boolean)
                playNext("PlayNextLikeNormalShuffle")

            else if(currentSongHelper?.isLoop as Boolean)
                playNext("PlayNextLoop")

            else
                playNext("PlayNextNormal")
        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true))
                currentPosition += 1
            else if (check.equals("PlayNextLikeNormalShuffle", true))
            {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(
                    fetchSongs?.size?.plus(1) as
                            Int
                )
                currentPosition = randomPosition
            }
            if (currentPosition == fetchSongs?.size)
                currentPosition = 0
            if(!check.equals("PlayNextLoop",true))
                currentSongHelper?.isLoop = false
            getSongData_playSong()

            if (isFavourited()) {
                fab?.setImageResource(R.drawable.favorite_on)
                addedToFav?.text = "Added to Playlists"
            } else {
                fab?.setImageResource(R.drawable.favorite_off)
                addedToFav?.text = "Click to add to playlists"
            }
        }
        fun playRandomSong(_id:Long){
            val songIndexList= fetchSongs?.map { song ->  song.songID}
            currentPosition= songIndexList?.indexOf(_id) as Int
            getSongData_playSong()
        }
        private fun getSongData_playSong(){
            val nextSong = fetchSongs?.get(currentPosition)
            currentSongHelper?.songPath = nextSong?.songData
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songArtist = nextSong?.songArtist
            currentSongHelper?.songId = nextSong?.songID as Long
            currentSongHelper?.currentPosition= currentPosition
            updateTextViews(
                currentSongHelper?.songTitle as String,
                currentSongHelper?.songArtist as String,
                currentPosition
            )
            //Starting playback of next song
            try {
                Statified.mediaPlayer?.reset()
                Statified.mediaPlayer?.setDataSource(
                    myActivity as Context,
                    Uri.parse(currentSongHelper?.songPath)
                )
                Statified.mediaPlayer?.prepare()
                processInformation(Statified.mediaPlayer as MediaPlayer)
                Statified.mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
                makeToast(e.message as String)
            }
            currentSongHelper?.isPlaying=true
        }
        private fun isFavourited():Boolean{
            for(i in 0 until playlists?.size as Int)
                if(favoriteContent?.checkifIdExists(currentSongHelper?.songId!!,
                        playlists!![i]) as Boolean)
                    return true
            return false
        }
        private fun makeToast(message:String){
            Toast.makeText(myActivity,message,Toast.LENGTH_LONG).show()
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            seekBar?.max = finalTime
            startTimeText?.setText(
                String.format(
                    "%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
                )
            )
            endTimeText?.setText(
                String.format(
                    "%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))
                )
            )
            seekBar?.setProgress(startTime)
            Handler().postDelayed(updateSongTime, 1000)
        }
        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = Statified.mediaPlayer?.currentPosition
                startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()))))
                seekBar?.setProgress(getCurrent as Int)
                Handler().postDelayed(this, 1000)
            }
        }
        fun updateTextViews(songTitle: String, songArtist: String,currentPosition:Int) {
            var songTitleUpdated: String = songTitle
            var songArtistUpdated: String = songArtist
            if (songTitle.equals("<unknown>", true))
                songTitleUpdated = "Unknown"
            if (songArtist.equals("<unknown>", true))
                songArtistUpdated = "Unknown"
            songTitleView?.setText(songTitleUpdated)
            songArtistView?.setText(songArtistUpdated)

            //Setting text views of next and previous songs
            if(currentPosition==0){
                nextImageButton?.isEnabled=true
                previousImageButton?.isEnabled=false
                previousImageButton?.alpha=0.5f
                prevSongView?.visibility=View.INVISIBLE
                //Setting next song view
                if(fetchSongs?.size!=1)
                    nextSongView?.text= fetchSongs?.get(currentPosition+1)?.songTitle
            }
            if(currentPosition== fetchSongs?.size?.minus(1)){
                previousImageButton?.isEnabled=true
                nextImageButton?.isEnabled=false
                nextImageButton?.alpha=0.5f
                nextSongView?.visibility=View.INVISIBLE
                //Setting previous song view
                if(fetchSongs?.size!=1)
                    prevSongView?.text=fetchSongs?.get(currentPosition-1)?.songTitle
            }
            if(currentPosition!=0&&currentPosition!=fetchSongs?.size?.minus(1)){
                //Enabling both previous button and next button
                previousImageButton?.isEnabled=true
                previousImageButton?.alpha=1f
                nextImageButton?.isEnabled=true
                nextImageButton?.alpha=1f
                //Making prev and next song views visible
                prevSongView?.visibility=View.VISIBLE
                nextSongView?.visibility=View.VISIBLE
                //Setting both next and previous songs
                nextSongView?.text= fetchSongs?.get(currentPosition+1)?.songTitle
                prevSongView?.text=fetchSongs?.get(currentPosition-1)?.songTitle
            }
        }
       fun generateCheckList(list:Array<CharSequence>):BooleanArray{
            val checkList=BooleanArray(list.size) { i->false}
            for(i in list.indices)
                checkList[i]=favoriteContent?.checkifIdExists(
                    currentSongHelper?.songId as Long,list[i].toString()) as Boolean
            return checkList
        }
        //End of companion object
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_playing_songs, container, false)
        activity?.title="Now Playing"
        setHasOptionsMenu(true)
        seekBar = view?.findViewById(R.id.SeekBar)
        startTimeText = view?.findViewById(R.id.StartTime)
        endTimeText = view?.findViewById(R.id.EndTime)
        playPauseImageButton = view?.findViewById(R.id.PlayPauseButtonSongPlaying)
        nextImageButton = view?.findViewById(R.id.PlayNext)
        previousImageButton = view?.findViewById(R.id.PlayPrev)
        loopImageButton = view?.findViewById(R.id.Loop)
        shuffleImageButton = view?.findViewById(R.id.Shuffle)
        songTitleView = view?.findViewById(R.id.PlaylistTitle)
        songArtistView = view?.findViewById(R.id.SongArtist)
        prevSongView=view?.findViewById(R.id.prev_song_name)
        nextSongView=view?.findViewById(R.id.next_song_name)
        addedToFav=view?.findViewById(R.id.added_to_fav_text)
        fab = view?.findViewById(R.id.fav_button)
/*Fading the favorite icon*/
        fab?.alpha = 0.8f
        glView = view?.findViewById(R.id.visualizer_view)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
/*Here we register the sensor*/
        mSensorManager?.registerListener(mSensorListener,
            mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        audioVisualization?.onPause()
        super.onPause()
/*When fragment is paused, we remove the sensor to prevent the battery drain*/
        mSensorManager?.unregisterListener(mSensorListener)
    }
    override fun onDestroyView() {
        audioVisualization?.release()
        super.onDestroyView()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*Sensor service is activate when the fragment is created*/
        mSensorManager =
            myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
/*Default values*/
        mAcceleration = 0.0f
/*We take earth's gravitational value to be default, this will give us good
results*/
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
/*Here we call the function*/
        bindShakeListener()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
/*Initialising the database*/
        favoriteContent = StaticMainActivity.favDatabase

        if(!EchoDatabase.s.playListsStored){
            favoriteContent!!.createPlaylistsTable()
        }
        playlists = EchoDatabase.s.playLists

        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop = false
        currentSongHelper?.isShuffle = false
        var _path: String? = null
        var _songTitle: String?=null
        var _songArtist: String?=null
        var _songId: Long = 0
        var _dateAdded:Long?=0
        var fromFavBottomBar:String?=null
        var fromMainBottomBar:String?= null
        try {
                fetchSongs = MainActivity.Statified.songQueue
                _path = arguments?.getString("path")
                _songTitle = arguments?.getString("songTitle")
                _songArtist = arguments?.getString("songArtist")
                _songId = arguments?.getInt("songId")?.toLong() as Long
                _dateAdded=arguments?.getLong("dateAdded")
                fromFavBottomBar = arguments?.getString("FavBottomBar")
                fromMainBottomBar=arguments?.getString("MainBottomBar")

            //If the control is not from the bottom bar,then the song will be the first in the queue
                if(fromFavBottomBar!=null||fromMainBottomBar!=null)
                    currentPosition=arguments?.getInt("position") as Int
                else
                    currentPosition=0

                currentSongHelper?.songPath = _path
                currentSongHelper?.songTitle = _songTitle
                currentSongHelper?.songArtist = _songArtist
                currentSongHelper?.songId = _songId
                currentSongHelper?.currentPosition = currentPosition
                currentSongHelper?.dateAdded=_dateAdded
                updateTextViews(
                    currentSongHelper?.songTitle as String,
                    currentSongHelper?.songArtist as String,
                    currentPosition
                )
        } catch (e: Exception) {
            e.printStackTrace()
            MainScreenAdapter.makeToast(e.message as String, myActivity)
        }
/*Here we check whether we came to the song playing fragment via tapping on a song
or by bottom bar*/
        if (fromFavBottomBar != null) {
/*If we came via bottom bar then the already playing media player object is
used*/
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        }
        //Now check the same for MainScreen Bottom Bar
        else if(fromMainBottomBar!=null)
        {
            Statified.mediaPlayer=MainScreenFragment.Statified.mediaPlayer
        }
        else {
/*Else we use the default way*/
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(_path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        processInformation(Statified.mediaPlayer as MediaPlayer)

        if (currentSongHelper?.isPlaying as Boolean) {
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
             playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }
        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as
                Context, 0)
        audioVisualization?.linkTo(visualizationHandler)
        var prefs = myActivity?.getSharedPreferences("feature",
            Context.MODE_PRIVATE)
        var isShuffleAllowed = prefs?.getBoolean(MY_PREFS_SHUFFLE, false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            currentSongHelper?.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var isLoopAllowed = prefs?.getBoolean(MY_PREFS_LOOP, false)
        if (isLoopAllowed as Boolean) {
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = true
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            currentSongHelper?.isLoop = false
        }
        if(isFavourited()) {
            fab?.setImageResource(R.drawable.favorite_on)
            addedToFav?.text="Added to Playlists"
        }
         else {
            fab?.setImageResource(R.drawable.favorite_off)
            addedToFav?.text="Click to add to playlists"
        }
    }
    fun clickHandler() {
        fab?.setOnClickListener {
            try {
                showPlaylists()
            }
            catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(myActivity,e.message as String,Toast.LENGTH_LONG).show()
            }
        }
        shuffleImageButton?.setOnClickListener {
            var editor =
                myActivity?.getSharedPreferences("feature", Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper?.isShuffle as Boolean) {
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isShuffle = false
                editor?.putBoolean(MY_PREFS_SHUFFLE, false)
                editor?.apply()
            } else {
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editor?.putBoolean(MY_PREFS_SHUFFLE, true)
                editor?.apply()
                editor?.putBoolean(MY_PREFS_LOOP, false)
                editor?.apply()
            }
        }
        if(nextImageButton?.isEnabled as Boolean) {
            nextImageButton?.setOnClickListener {
                if (currentSongHelper?.isShuffle as Boolean) {
                    playNext("PlayNextLikeNormalShuffle")
                } else {
                    playNext("PlayNextNormal")
                    currentSongHelper?.isLoop=false
                    loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                }
            }
        }
        if(previousImageButton?.isEnabled as Boolean) {
            previousImageButton?.setOnClickListener {
                if (currentSongHelper?.isLoop as Boolean) {
                    loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                }
                playPrevious()
            }
        }
        loopImageButton?.setOnClickListener {
            var editor =
                myActivity?.getSharedPreferences("feature", Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editor?.putBoolean(MY_PREFS_LOOP, false)
                editor?.apply()
            } else {
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editor?.putBoolean(MY_PREFS_SHUFFLE, false)
                editor?.apply()
                editor?.putBoolean(MY_PREFS_LOOP, true)
                editor?.apply()
            }
        }
        playPauseImageButton?.setOnClickListener {
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                currentSongHelper?.isPlaying = false
                playPauseImageButton?.setBackgroundResource(R.drawable.play_icon2)
            } else {
                Statified.mediaPlayer?.start()
                currentSongHelper?.isPlaying = true
                playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }
    fun playPrevious() {
        currentPosition -= 1
        if (currentPosition == -1) {
            currentPosition = (fetchSongs?.size as Int) -1
        }
        if (currentSongHelper?.isPlaying as Boolean) {
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper?.isLoop = false
        getSongData_playSong()

        if(isFavourited()) {
            fab?.setImageResource(R.drawable.favorite_on)
            addedToFav?.text="Added to Playlists"
        }
        else {
            fab?.setImageResource(R.drawable.favorite_off)
            addedToFav?.text="Click to add to playlists"
        }
    }
    /*This function handles the shake events in order to change the songs when we shake the
    phone*/
    fun bindShakeListener() {
/*The sensor listener has two methods used for its implementation i.e.
OnAccuracyChanged() and onSensorChanged*/
        mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
/*We do noot need to check or work with the accuracy changes for the
sensor*/
            }
            override fun onSensorChanged(event: SensorEvent) {
/*We need this onSensorChanged function
* This function is called when there is a new sensor event*/
/*The sensor event has 3 dimensions i.e. the x, y and z in which the
changes can occur*/
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
/*Now lets see how we calculate the changes in the acceleration*/
/*Now we shook the phone so the current acceleration will be the first to
start with*/
                mAccelerationLast = mAccelerationCurrent
/*Since we could have moved the phone in any direction, we calculate the
Euclidean distance to get the normalized distance*/
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z *
                        z).toDouble())).toFloat()
/*Delta gives the change in acceleration*/
                val delta = mAccelerationCurrent - mAccelerationLast
/*Here we calculate thelower filter
* The written below is a formula to get it*/
                mAcceleration = mAcceleration * 0.9f + delta
/*We obtain a real number for acceleration
* and we check if the acceleration was noticeable, considering 12 here*/
                if (mAcceleration > 12) {
/*If the accel was greater than 12 we change the song, given the fact
our shake to change was active*/
                    val prefs =
                        myActivity?.getSharedPreferences("feature", Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean(MY_PREFS_SHAKE, false)
                    if (isAllowed as Boolean) {
                        playNext("PlayNextNormal")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.song_playing_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item1:MenuItem=menu.findItem(R.id.redirect)
        item1.isVisible=true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.redirect-> {
                myActivity?.onBackPressed()
                           return true
            }
            R.id.song_queue->{
                //Creating a popup window view
                val popupView=RelativeLayout(myActivity)
                popupView.layoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
                popupView.setBackgroundColor(Color.WHITE)
                val queueRecyclerView=RecyclerView(myActivity!!)
                queueRecyclerView.layoutParams=RecyclerView.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
                popupView.addView(queueRecyclerView)
                //Use the above view to create popup window and show it
                val popupWindow=PopupWindow(popupView,LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,true)
                popupWindow.showAtLocation(view,Gravity.CENTER,0,0)
                //Now create the adapter and link it to the above recycler view
                queueRecyclerView.adapter=QueueAdapter(MainActivity.Statified.songQueue as ArrayList<Song>, myActivity as Context)
                queueRecyclerView.layoutManager=LinearLayoutManager(myActivity)
                queueRecyclerView.itemAnimator=DefaultItemAnimator()
                return true
            }
        }
        return true
    }
    private fun showPlaylists(){
        val list = playlists
        val playLists =
            list?.toArray(Array<CharSequence>(EchoDatabase.s.playLists.size) { i -> list[i] })

        if(list?.size==0)
            Toast.makeText(context,"No playlists found!Go to favourites tab and create a playlist!",Toast.LENGTH_SHORT).show()
        else {
            AlertDialog.Builder(myActivity)
                .setTitle("Choose a playlist")
                .setSingleChoiceItems(playLists as Array<out CharSequence>,-1){dialog, which ->
                    dialog.dismiss()
                    val playListToBeAdded=playLists[which].toString()
                    val currentSong= Song(currentSongHelper?.songId as Long,currentSongHelper?.dateAdded as Long,
                        currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String,
                        currentSongHelper?.songPath as String)

                    favoriteContent?.storeAsFavorite(currentSong, playListToBeAdded)
                    //Changing UI elements
                    fab?.setImageResource(R.drawable.favorite_on)
                    addedToFav?.text="Added to Playlists"
                    MainScreenAdapter.makeToast(
                        "Song added to " + playListToBeAdded + " successfully", context
                    )
                }
                .create().show()
        }
    }
}