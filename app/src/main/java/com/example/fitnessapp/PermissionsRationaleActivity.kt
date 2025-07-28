package com.example.fitnessapp

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class PermissionsRationaleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "This app uses Health Connect to sync your workout data. Your data is handled securely and only used for fitness tracking."
        setContentView(textView)
    }
}