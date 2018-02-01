package com.kinectafrica.android.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log

/**
 * Made by acefalobi on 5/15/2017.
 */

class CustomSwipeTextView(internal var context: Context, attrs: AttributeSet) : android.support.v7.widget.AppCompatTextView(context, attrs) {
    private var ttfName: String = ""

    private var tag = javaClass.name

    init {

        for (i in 0 until attrs.attributeCount) {
            Log.i(tag, attrs.getAttributeName(i))

            this.ttfName = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "ttfName")

            init()
        }
    }

    private fun init() {
        val typeface = Typeface.createFromAsset(context.assets, "fonts/" + ttfName)
        setTypeface(typeface)
    }

    fun setFontFromAsset(assetName: String) {
        val typeface = Typeface.createFromAsset(context.assets,assetName)
        setTypeface(typeface)
    }

}
