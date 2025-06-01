package com.example.selvigallery.Utils

import android.view.View
import android.view.animation.AlphaAnimation

    fun View.setFadeClickListener(onClick: () -> Unit) {
        this.setOnClickListener {
            val animation = AlphaAnimation(0.3f, 1.0f)
            animation.duration = 200
            this.startAnimation(animation)
            onClick()
        }
    }

