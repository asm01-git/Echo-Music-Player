package com.internshala.echo.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.R

/**
 * A simple [Fragment] subclass.
 */
class AboutUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_about_us, container, false)
        activity?.title="About Us"
        view?.findViewById<RelativeLayout>(R.id.about_us_layout)
        view?.findViewById<ImageView>(R.id.dev_image)
        view?.findViewById<TextView>(R.id.description)
        return view
    }


}
