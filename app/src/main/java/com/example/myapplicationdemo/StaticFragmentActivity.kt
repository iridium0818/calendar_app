package com.example.myapplicationdemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationdemo.MyFragment
import com.example.myapplicationdemo.R

class StaticFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 加载 activity_static_fragment.xml 布局
        setContentView(R.layout.activity_static_fragment)

        // 关闭当前的 Activity，从而返回到上一个界面（MainActivity）
        val returnButton: Button = findViewById(R.id.button)
        returnButton.setOnClickListener {
            finish()
        }
    }
}


