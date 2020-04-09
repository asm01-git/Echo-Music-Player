package com.internshala.echo.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.internshala.echo.R
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.fragments.PlayingSongsFragment

class CaptureBroadast:BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action==Intent.ACTION_NEW_OUTGOING_CALL)
        {
            try {
                MainActivity.Statified.notificationManager?.cancel(1978)
            }
            catch (e: java.lang.Exception){
                e.printStackTrace()
            }
            try{
               if(PlayingSongsFragment.Statified.currentSongHelper?.isPlaying as Boolean)
               {
                   PlayingSongsFragment.Statified.mediaPlayer?.pause()
                   PlayingSongsFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
               }
           }
           catch (e:Exception){
               e.printStackTrace()
           }
        }
        else{
            val tm:TelephonyManager=context?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            if(tm.callState==TelephonyManager.CALL_STATE_RINGING)
            {
                try {
                    MainActivity.Statified.notificationManager?.cancel(1978)
                }
                catch (e: java.lang.Exception){
                    e.printStackTrace()
                }
                try{
                    if(PlayingSongsFragment.Statified.currentSongHelper?.isPlaying as Boolean)
                    {
                        PlayingSongsFragment.Statified.mediaPlayer?.pause()
                        PlayingSongsFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                    }
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

}