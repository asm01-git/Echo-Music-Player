package com.internshala.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.internshala.echo.R

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {
var myActivity:Activity?=null
    var shakeSwitch:Switch?=null
    object Statified{
        var MY_PREF_SHAKE="ShakeFeature"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_settings, container, false)
        activity?.title="Settings"
        shakeSwitch=view?.findViewById(R.id.shakeswitch)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val prefs=myActivity?.getSharedPreferences("feature",Context.MODE_PRIVATE)
        val isAllowed=prefs?.getBoolean(Statified.MY_PREF_SHAKE,false)
        if(isAllowed as Boolean)
            shakeSwitch?.isChecked=true
        else
            shakeSwitch?.isChecked=false
       shakeSwitch?.setOnCheckedChangeListener { compoundButton, state->
           if(state) {
               val editor=myActivity?.getSharedPreferences("feature",Context.MODE_PRIVATE)?.edit()
               editor?.putBoolean(Statified.MY_PREF_SHAKE,true)
               editor?.apply()
           } else{
               val editor=myActivity?.getSharedPreferences("feature",Context.MODE_PRIVATE)?.edit()
               editor?.putBoolean(Statified.MY_PREF_SHAKE,false)
               editor?.apply()
           }
       }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity=context as Activity
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
    }
}
