package com.leschnitzky.dailyshiba.utils

import android.view.View
import android.view.ViewConfiguration


abstract class OnDoubleClickListener : View.OnClickListener {
    private val tapHandler: TapHandler = TapHandler()
    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)
    override fun onClick(v: View) {
        tapHandler.cancelSingleTap(v)
        if (tapHandler.isDoubleTap) {
            onDoubleClick(v)
        } else {
            tapHandler.performSingleTap(v)
        }
    }

    private inner class TapHandler : Runnable {
        val isDoubleTap: Boolean
            get() {
                val tapTime = System.currentTimeMillis()
                val doubleTap = tapTime - lastTapTime < TIME_OUT
                lastTapTime = tapTime
                return doubleTap
            }

        fun performSingleTap(v: View) {
            view = v
            v.postDelayed(this, TIME_OUT.toLong())
        }

        fun cancelSingleTap(v: View) {
            view = null
            v.removeCallbacks(this)
        }

        override fun run() {
            if (view != null) {
                onSingleClick(view)
            }
        }

        private var view: View? = null
        private var lastTapTime: Long = 0
    }

    companion object {
        private val TIME_OUT = ViewConfiguration.getDoubleTapTimeout()
    }
}