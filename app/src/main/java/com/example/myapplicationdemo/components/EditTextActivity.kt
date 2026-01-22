package com.example.myapplicationdemo.components

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.widget.EditText
import com.example.myapplicationdemo.BaseActivity
import com.example.myapplicationdemo.R

class EditTextActivity: BaseActivity()  {
    companion object {
        const val TAG = "EditTextActivity"
    }

    override fun setupContent() {
        setContentView(R.layout.activity_edittext)

        // 仅允许英文、数字
        val filter = InputFilter { source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int ->
            if (!source.toString().matches("[A-Za-z0-9]+".toRegex())) {
                return@InputFilter ""
            }
            null
        }
        findViewById<EditText>(R.id.set_filters).filters = arrayOf<InputFilter>(filter)

        val et = findViewById<EditText>(R.id.add_text_changed_listener)
        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (!s.toString().matches("[App]+".toRegex())) {
                    et.error = "只允许输入App"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}