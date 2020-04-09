package com.internshala.echo.fragments
import android.app.Activity
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.SharedPreferences
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
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.cleveroad.audiovisualization.VisualizerDbmHandler
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_playing_songs.*
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit
/**
 * A simple [Fragment] subclass.
 */
class PlayingSongsFragment :androidx.fragment.app.Fragment() {
    /*Here you may wonder that why did we create two objects namely Statified and
Staticated respectively
* These objects are created as the variables and functions will be used from another
class
* Now, the question is why did we make two different objects and not one single object
* This is because we created the Statified object which contains all the variables and
* the Staticated object which contain all the functions*/
    object Statified {
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekBar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        /*Declaring variable for handling the favorite button*/
        var fab: ImageButton? = null
        /*Variable for using DB functions*/
        var favoriteContent: EchoDatabase? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        /*Sensor Variables*/
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"
        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as
                            Long))))
                seekBar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }
        }
    }
    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"
        fun onSongComplete() {
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songArtist = nextSong?.artist
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long
                    updateTextViews(Statified.currentSongHelper?.songTitle as String,
                        Statified.currentSongHelper?.songArtist as String)
                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context,
                            Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int)
                        as Boolean) {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
            }
        }
        fun updateTextViews(songTitle: String, songArtist: String) {
            var songTitleUpdated:String=songTitle
            var songArtistUpdated:String=songArtist
            if(songTitle.equals("<unknown>",true))
              songTitleUpdated="Unknown"
            if(songArtist.equals("<unknown>",true))
                songArtistUpdated="Unknown"
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)
        }
        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekBar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d: %d",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            Statified.endTimeText?.setText(String.format("%d: %d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )
            Statified.seekBar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }
        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition + 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as
                        Int)
                Statified.currentPosition = randomPosition
            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songId = nextSong?.songID as Long
            updateTextViews(Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String)
            try {
                Statified.mediaPlayer?.reset()
                Statified.mediaPlayer?.prepare()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int)
                        as Boolean) {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
            }
        }
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
        Statified.seekBar = view?.findViewById(R.id.SeekBar)
        Statified.startTimeText = view?.findViewById(R.id.StartTime)
        Statified.endTimeText = view?.findViewById(R.id.EndTime)
        Statified.playPauseImageButton = view?.findViewById(R.id.PlayPauseButtonSongPlaying)
        Statified.nextImageButton = view?.findViewById(R.id.PlayNext)
        Statified.previousImageButton = view?.findViewById(R.id.PlayPrev)
        Statified.loopImageButton = view?.findViewById(R.id.Loop)
        Statified.shuffleImageButton = view?.findViewById(R.id.Shuffle)
        Statified.songTitleView = view?.findViewById(R.id.SongTitle)
        Statified.songArtistView = view?.findViewById(R.id.SongArtist)
/*Linking it with the view*/
        Statified.fab = view?.findViewById(R.id.fav_button)
/*Fading the favorite icon*/
        Statified.fab?.alpha = 0.8f
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }
    override fun onAttach(context: Context) {
        super.onAttach(context as Context)
        Statified.myActivity = context as Activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
/*Here we register the sensor*/
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()
/*When fragment is paused, we remove the sensor to prevent the battery drain*/
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }
    override fun onDestroyView() {
        Statified.audioVisualization?.release()
        super.onDestroyView()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*Sensor service is activate when the fragment is created*/
        Statified.mSensorManager =
            Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
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
        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong() as Long
            Statified.currentPosition = arguments?.getInt("position") as Int
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")
            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String)
        } catch (e: Exception) {
            e.printStackTrace()
        }
/*Here we check whether we came to the song playing fragment via tapping on a song
or by bottom bar*/
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
/*If we came via bottom bar then the already playing media player object is
used*/
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        } else {
/*Else we use the default way*/
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified. playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as
                Context, 0)
        Statified.audioVisualization?.linkTo(visualizationHandler)
        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,
            Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feaure", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,
            Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            Statified.currentSongHelper?.isLoop = false
        }
/*Here we check that if the song playing is a favorite, then we show a red colored
heart indicating favorite else only the heart boundary
* This action is performed whenever a new song is played, hence this will done in
the playNext(), playPrevious() and onSongComplete() methods*/
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as
                    Boolean) {
            Statified.fab?.setImageResource(R.drawable.favorite_on)
        } else {
            Statified.fab?.setImageResource(R.drawable.favorite_off)
        }
    }
    fun clickHandler() {
/*Here we handle the click of the favorite icon
* When the icon was clicked, if it was red in color i.e. a favorite song then we
remove the song from favorites*/
        Statified.fab?.setOnClickListener({
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int)
                        as Boolean) {
                Statified.fab?.setImageResource(R.drawable.favorite_off)
                Statified.favoriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as
                        Int)
/*Toast is prompt message at the bottom of screen indicating that an
action has been performed*/
                Toast.makeText(Statified.myActivity, "Removed from Favorites",
                    Toast.LENGTH_SHORT).show()
            } else {
/*If the song was not a favorite, we then add it to the favorites using
the method we made in our database*/
                Statified.fab?.setImageResource(R.drawable.favorite_on)
                Statified.favoriteContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt(),
                    Statified.currentSongHelper?.songArtist, Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Added to Favorites",
                    Toast.LENGTH_SHORT).show()
            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop =Statified. myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,
                Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }
        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,
                Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        })
        Statified.playPauseImageButton?.setOnClickListener({
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }
    fun playPrevious() {
        Statified.currentPosition = Statified.currentPosition - 1
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songArtist = nextSong?.artist
        Statified.currentSongHelper?.songId = nextSong?.songID as Long
        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String,
            Statified.currentSongHelper?.songArtist as String)
        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as
                    Boolean) {
            Statified.fab?.setImageResource(R.drawable.favorite_on)
        } else {
            Statified.fab?.setImageResource(R.drawable.favorite_off)
        }
    }
    /*This function handles the shake events in order to change the songs when we shake the
    phone*/
    fun bindShakeListener() {
/*The sensor listener has two methods used for its implementation i.e.
OnAccuracyChanged() and onSensorChanged*/
        Statified.mSensorListener = object : SensorEventListener {
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
                        Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
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
        val item:MenuItem=menu.findItem(R.id.redirect)
        item.isVisible=true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.redirect-> {
                Statified.myActivity?.onBackPressed()
                           return false
            }
        }
        return false
    }
}