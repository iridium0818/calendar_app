package com.example.myapplicationdemo.components

import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.example.myapplicationdemo.BaseActivity
import com.example.myapplicationdemo.R

class TextViewActivity:BaseActivity() {
    companion object {
        const val TAG = "TextViewActivity"
    }

    override fun setupContent() {
        setContentView(R.layout.activity_textview)

        val spannableString = SpannableString("Hello, This is spannable string!!!")
        // 文本颜色与背景
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(BackgroundColorSpan(Color.YELLOW), 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        // 字体样式
        spannableString.setSpan(StyleSpan(Typeface.BOLD_ITALIC), 12, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        // 字体大小，相对大小，默认大小的 x 倍
        spannableString.setSpan(RelativeSizeSpan(2f), 15, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        findViewById<TextView>(R.id.spannable_textview).text = spannableString

        val htmlText = Html.fromHtml("<b>粗体</b><i>斜体</i><font color='red'>红色文字</font>")
        findViewById<TextView>(R.id.html_textview).text = htmlText
    }
}