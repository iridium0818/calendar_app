package com.example.myapplicationdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReminderDialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_dialog)

        // 设置点击外部不取消，必须点确认
        setFinishOnTouchOutside(false)

        val title = intent.getStringExtra("EVENT_TITLE") ?: "日程提醒"
        val message = intent.getStringExtra("EVENT_MESSAGE") ?: "您有一个日程即将开始"
        val scheduleTime = intent.getStringExtra("SCHEDULE_TIME") ?: "时间未知"

        findViewById<TextView>(R.id.tvDialogTitle).text = title
        findViewById<TextView>(R.id.tvDialogMessage).text = message
        findViewById<TextView>(R.id.tvScheduleTime).text = scheduleTime

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            // 这里可以添加停止铃声/震动的逻辑
            finish()
        }
    }
}
