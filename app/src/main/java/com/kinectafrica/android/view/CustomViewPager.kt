package com.kinectafrica.android.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller

/**
 * Made by acefalobi on 4/2/2017.
 */

class CustomViewPager : ViewPager {

    constructor(context: Context) : super(context) {
        setCustomScroller()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setCustomScroller()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =// Never allow swiping to switch between pages
            false

    private fun setCustomScroller() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, CustomScroller(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private inner class CustomScroller internal constructor(context: Context) : Scroller(context, DecelerateInterpolator()) {

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 350)
        }
    }
}
