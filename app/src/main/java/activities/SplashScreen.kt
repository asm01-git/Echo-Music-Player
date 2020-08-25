package com.internshala.echo.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.internshala.echo.R

class SplashScreen : AppCompatActivity() {

    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        if (permission_check(this@SplashScreen, *permissions))//If all permissions have been granted
        {
            delaybyonesec()
        } else { //Ask for permissions
            ActivityCompat.requestPermissions(this@SplashScreen, permissions, 131)
        }
    }

    fun permission_check(
        context: Context,
        vararg p: String
    ): Boolean//Returns false if any one permission has been denied
    {
        for (permission in p) {
            val res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        reqCode: Int,
        p: Array<out String>,
        grantAccess: IntArray
    ) {
        super.onRequestPermissionsResult(reqCode, p, grantAccess)
        when (reqCode) {
            131 -> {
                if (grantAccess.isNotEmpty() && grantAccess[0] == PackageManager.PERMISSION_GRANTED
                    && grantAccess[1] == PackageManager.PERMISSION_GRANTED && grantAccess[2] == PackageManager.PERMISSION_GRANTED
                    && grantAccess[3] == PackageManager.PERMISSION_GRANTED && grantAccess[4] == PackageManager.PERMISSION_GRANTED
                )
                    delaybyonesec()
                else {
                    val toast = Toast.makeText(
                        this@SplashScreen,
                        "Please grant all permissions to continue",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    this.finish()
                }

                return
            }
            else -> {
                Toast.makeText(this@SplashScreen, "Something went wrong", Toast.LENGTH_SHORT)
                this.finish()
                return

            }
        }
    }

    fun delaybyonesec()//To delay the Splash Screen by one second and kill the activity
    {
        Handler().postDelayed({
            val startAct = Intent(
                this@SplashScreen,
                MainActivity::class.java
            )
            startActivity(startAct)
            this.finish()
        }, 1000)

    }
}

