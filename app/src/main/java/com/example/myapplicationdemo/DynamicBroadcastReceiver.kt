package com.example.myapplicationdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class DynamicBroadcastReceiver : BroadcastReceiver() {
    val TAG="DynamicBroadcastReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "DynamicBroadcastReceiver onReceive")
    }
}


