package com.example.myapplicationdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class EventAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EVENT_TITLE") ?: "日程提醒"
        val message = intent.getStringExtra("EVENT_MESSAGE") ?: "您有一个待办事项"
        val eventId = intent.getIntExtra("EVENT_ID", 0)
        val scheduleTime = intent.getStringExtra("SCHEDULE_TIME") ?: ""

        Log.d("EventAlarmReceiver", "Received alarm for event: $title")

        // 1. 发送通知
        // 检查通知权限 (Android 13+)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("EventAlarmReceiver", "No permission to post notifications")
            // 如果没有通知权限，也许还可以弹个 dialog？
            // 但 BroadcastReceiver 不能直接弹窗，需要启动一个 Activity
        } else {
            val notificationBuilder = NotificationCompat.Builder(context, "CALENDAR_CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(eventId, notificationBuilder.build())
        }

        // 2. 启动弹窗 Activity
        // 创建一个全屏透明 Activity 或者 Dialog Activity 来显示弹窗
        val dialogIntent = Intent(context, ReminderDialogActivity::class.java).apply {
            // 修改：去掉 FLAG_ACTIVITY_CLEAR_TASK，防止清除原有任务栈导致 App 退出
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("EVENT_TITLE", title)
            putExtra("EVENT_MESSAGE", message)
            putExtra("SCHEDULE_TIME", scheduleTime)
        }
        context.startActivity(dialogIntent)
    }
}
