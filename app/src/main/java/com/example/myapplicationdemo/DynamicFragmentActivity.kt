package com.example.myapplicationdemo

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DynamicFragmentActivity : AppCompatActivity() {
    var myFragment: MyFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dynamic_fragment)

        // 监听系统窗口 inset（注意方法名是 setOnApplyWindowInsetsListener）
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.buttonadd).setOnClickListener({
            // 动态添加MyFragment
            if (myFragment == null) {
                myFragment = MyFragment() // 若未实例化，创建MyFragment实例
            }
            if (!myFragment!!.isAdded) { // 若未添加到Activity，则添加MyFragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, myFragment!!)
                    .commit()
            }
        })

        findViewById<Button>(R.id.buttonremove).setOnClickListener({
            // 动态移除MyFragment
            if( myFragment != null && myFragment!!.isAdded) {
                supportFragmentManager.beginTransaction()
                    .remove(myFragment!!)
                    .commit()
            }
        })

        findViewById<Button>(R.id.button).setOnClickListener({
            // 关闭当前的 Activity，从而返回到上一个界面（MainActivity）
            finish()
        })
    }
}

