package com.internshala.echo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.internshala.echo.R
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.fragments.AboutUsFragment
import com.internshala.echo.fragments.FavouriteFragment
import com.internshala.echo.fragments.MainScreenFragment
import com.internshala.echo.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.zip.Inflater

class NavigationDrawerAdapter(_contentList:ArrayList<String>,_getImages:IntArray,_context:Context): RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>()
{
    var contentList:ArrayList<String>?=null
    var getImages:IntArray?=null
    var context:Context?=null
    init {
        contentList=_contentList
        getImages=_getImages
        context=_context
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
       var itemView= LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_nav_view,parent,false)
        var abc=NavViewHolder(itemView)
        return abc
    }

    override fun getItemCount(): Int {
         return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {

        holder.get_icon?.setBackgroundResource(getImages?.get(position) as Int)
        holder.get_text?.setText(contentList?.get(position))
        holder.get_rel_layout?.setOnClickListener {
            when (position) {
                0 -> {
                    val mainScreenFragment=MainScreenFragment()
                    (context as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frag,mainScreenFragment as Fragment)
                        .commit()
                }
                1 -> {
                    val favFragment=FavouriteFragment()
                    (context as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frag,favFragment as Fragment)
                        .commit()
                }
                2 -> {
                    val settingsFragment=SettingsFragment()
                    (context as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frag,settingsFragment as Fragment)
                        .commit()
                }
                3 -> {
                    val aboutUsFragment=AboutUsFragment()
                    (context as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frag,aboutUsFragment)
                        .commit()
                }
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        }

    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var get_icon: ImageView? = null
        var get_text: TextView? = null
        var get_rel_layout: RelativeLayout? = null

        init
        {
            get_icon=itemView.findViewById(R.id.nav_drawer_icon)
            get_text=itemView.findViewById(R.id.nav_drawer_text)
            get_rel_layout=itemView.findViewById(R.id.nav_drawer_row)
        }

    }
}