package com.example.myapplicationdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WeekListAdapter(
    private val weekDates: List<LocalDate>, // 7 days
    private val events: List<CalendarEvent>,
    private val onEventClickListener: (CalendarEvent) -> Unit,
    private val onDayClickListener: (LocalDate) -> Unit
) : RecyclerView.Adapter<WeekListAdapter.WeekViewHolder>() {

    inner class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekDayName: TextView = itemView.findViewById(R.id.weekDayName)
        val weekDateText: TextView = itemView.findViewById(R.id.weekDateText)
        val weekLunarText: TextView = itemView.findViewById(R.id.weekLunarText) // New binding
        val dayEventsContainer: LinearLayout = itemView.findViewById(R.id.dayEventsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_week_day_row, parent, false)
        return WeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        val date = weekDates[position]

        // 设置左侧日期信息
        // 1=Mon ... 7=Sun
        val dayNames = arrayOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
        holder.weekDayName.text = dayNames[date.dayOfWeek.value] // java.time dayOfWeek is 1-based
        holder.weekDateText.text = date.format(DateTimeFormatter.ofPattern("MM-dd"))

        // --- 新增：农历绑定 ---
        val lunarStr = LunarUtils.getLunarText(date.year, date.monthValue, date.dayOfMonth)
        holder.weekLunarText.text = lunarStr

        // 全部统一为灰色
        holder.weekLunarText.setTextColor(android.graphics.Color.parseColor("#888888"))

        // --- 处理 "今天" 的高亮逻辑 ---
        val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        if (date == today) {
             // 使用胶囊形背景
             holder.weekDateText.setBackgroundResource(R.drawable.bg_capsule_purple)
             holder.weekDateText.setTextColor(android.graphics.Color.WHITE)
             // 稍微调整padding保证文字居中
             holder.weekDateText.setPadding(12, 4, 12, 4)
        } else {
             holder.weekDateText.background = null
             holder.weekDateText.setTextColor(android.graphics.Color.parseColor("#666666"))
             holder.weekDateText.setPadding(0, 0, 0, 0)
        }

        // --- 绑定日程 ---
        holder.dayEventsContainer.removeAllViews()
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todaysEvents = events.filter { it.date == dateStr }

        if (todaysEvents.isNotEmpty()) {
            todaysEvents.forEach { event ->
                val card = createEventCard(holder.itemView.context, event)
                holder.dayEventsContainer.addView(card)
            }
        } else {
             // 可以添加一个占位，或者什么都不显示，或者"点击添加"
             val hint = TextView(holder.itemView.context)
             hint.text = "+ 点击添加日程"
             hint.textSize = 12f
             hint.setTextColor(android.graphics.Color.LTGRAY)
             hint.setPadding(8, 8, 8, 8)
             hint.setOnClickListener { onDayClickListener(date) }
             holder.dayEventsContainer.addView(hint)
        }

        // 点击整个行也可以触发添加
        holder.itemView.setOnClickListener {
             onDayClickListener(date)
        }
    }

    // 复用之前 DayTimeAdapter 里的卡片创建逻辑 (可以抽离成 Utils，这里简单复制微调)
    private fun createEventCard(context: Context, event: CalendarEvent): View {
        val textView = TextView(context)
        textView.text = "${event.time} ${event.title}"
        textView.textSize = 12f
        textView.setTextColor(android.graphics.Color.parseColor("#4A148C"))
        textView.setBackgroundResource(R.drawable.bg_event_card)
        textView.setPadding(16, 8, 8, 8)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 8)
        textView.layoutParams = params

        textView.setOnClickListener {
            onEventClickListener(event)
        }
        return textView
    }

    override fun getItemCount(): Int = weekDates.size
}
