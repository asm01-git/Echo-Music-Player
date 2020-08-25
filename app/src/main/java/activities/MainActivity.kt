package com.internshala.echo.activities

import android.app.Fragment
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.R
import com.internshala.echo.Song
import com.internshala.echo.adapters.NavigationDrawerAdapter
import com.internshala.echo.fragments.MainScreenFragment
import com.internshala.echo.fragments.PlayingSongsFragment
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    var iconArrayList:ArrayList<String> =arrayListOf()
    var imageListForNavDrawer= intArrayOf(R.drawable.navigation_allsongs,R.drawable.navigation_favorites,R.drawable.navigation_settings,R.drawable.navigation_aboutus)
    object Statified {

        var drawerLayout: DrawerLayout? = null
        var notificationManager:NotificationManager?=null
        var songQueue:ArrayList<Song>?=null
    }
    var trackNotificationBuilder:Notification?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Statified.songQueue=ArrayList()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Statified.drawerLayout = findViewById(R.id.drawer_layout)
        iconArrayList.add("All Songs")
        iconArrayList.add("Favourites")
        iconArrayList.add("Settings")
        iconArrayList.add("About Us")
        val toggle = ActionBarDrawerToggle(
            this@MainActivity,
            Statified.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        Statified.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()
        val mainScreenFragment=MainScreenFragment()
            this.supportFragmentManager
                .beginTransaction()
                .add(R.id.frag,mainScreenFragment,"MainScreenFragment")
                .commit()
        var nav_adapter=NavigationDrawerAdapter(iconArrayList,imageListForNavDrawer,this)
        nav_adapter.notifyDataSetChanged()
        var navigation_recycler_view=findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager=LinearLayoutManager(this) as RecyclerView.LayoutManager
        navigation_recycler_view.itemAnimator=DefaultItemAnimator()
        navigation_recycler_view.adapter=nav_adapter
        navigation_recycler_view.setHasFixedSize(true)
        //The following code is to set up the notification Bar
        val intent= Intent(this@MainActivity,MainActivity::class.java)
        val pIntent=PendingIntent.getActivity(this@MainActivity,19,intent,0)
        trackNotificationBuilder=Notification.Builder(this)
            .setContentTitle("Song is Playing")
            .setSmallIcon(R.drawable.echo_logo)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(pIntent)
            .build()
        Statified.notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }

    override fun onStart() {
        super.onStart()
        try {
            Statified.notificationManager?.cancel(1978)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if(PlayingSongsFragment.currentSongHelper?.isPlaying as Boolean){
          Statified.notificationManager?.notify(1978,trackNotificationBuilder)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Statified.notificationManager?.cancel(1978)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

}