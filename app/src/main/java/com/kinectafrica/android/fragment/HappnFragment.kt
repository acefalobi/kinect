package com.kinectafrica.android.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kinectafrica.android.R

/**
 * A simple [Fragment] subclass.
 */
class HappnFragment : Fragment() {

    private var fragmentView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_happn, container, false)
        return fragmentView
    }

}// Required empty public constructor
