package com.example.myapplicationdemo

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MyService: Service() {
    val TAG="MyService"
    // 提供一个局部 Binder 用于客户端绑定并获取 Service 实例
    inner class LocalBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG,"ServiceLifeCycle: onBind")
        // 返回 binder，允许客户端获取 Service 实例并进行交互
        return binder
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"ServiceLifeCycle: onCreate")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"ServiceLifeCycle: onDestroy")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"ServiceLifeCycle: onStartCommand")
        // 如果需要让服务继续运行，返回START_STICKY
        return START_STICKY
    }
}