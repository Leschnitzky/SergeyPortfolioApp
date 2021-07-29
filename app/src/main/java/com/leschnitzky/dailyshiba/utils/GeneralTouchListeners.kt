package com.leschnitzky.dailyshiba.utils

import android.view.MotionEvent
import android.view.View
import java.util.*

class GeneralTouchListener(
    val onDoubleClickListener: OnDoubleClickListener,
    val onSwipeTouchListener: OnSwipeTouchListener
) : View.OnTouchListener {
    private var startClickTime: Long = 0

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_UP -> {
                onDoubleClickListener.onClick(v!!)
            }
        }
        onSwipeTouchListener.onTouch(v!!,event!!)

        return true
    }
}