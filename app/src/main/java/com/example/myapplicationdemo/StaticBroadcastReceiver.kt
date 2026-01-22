package com.example.myapplicationdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StaticBroadcastReceiver: BroadcastReceiver() {
    val TAG="StaticBroadcastReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "StaticBroadcastReceiver onReceive")
    }
}