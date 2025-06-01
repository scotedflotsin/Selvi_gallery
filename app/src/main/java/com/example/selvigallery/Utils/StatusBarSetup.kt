package com.example.selvigallery.Utils

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.selvigallery.R

class StatusBarSetup {
    fun setUpStatusBar(context: Context) {
        // Ensure the context is an AppCompatActivity to access the window
        if (context is AppCompatActivity) {
            val window = context.window

            // Set the status bar color
            // Use ContextCompat for backward compatibility and cleaner syntax
            window.statusBarColor = ContextCompat.getColor(context, R.color.black)
            window.navigationBarColor = ContextCompat.getColor(context, R.color.violet)

        }
    }
}