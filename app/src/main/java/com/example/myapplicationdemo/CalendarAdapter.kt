package com.example.myapplicationdemo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarAdapter(
    private val daysOfMonth: ArrayList<String>,
    private val onItemListener: OnItemListener,
    private val eventDates: List<String>, // 有日程的日期列表
    private val selectedDay: String, // 当前选中的日期（例如 "20"）
    private val currentMonth: YearMonth
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>()  {

    private val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_calendar_day, parent, false)
        val layoutParams = view.layoutParams

        // 如果是月视图（item数量多于7），设置为正方形
        if (daysOfMonth.size > 7) {
            val width = parent.width
            if (width > 0) {
                 layoutParams.height = width / 7
            } else {
                 val displayMetrics = parent.context.resources.displayMetrics
                 layoutParams.height = displayMetrics.widthPixels / 7
            }
        } else {
             // 周视图或其他情况保持原样或另行处理
             layoutParams.height = (parent.height * 0.166666666).toInt()
        }

        return CalendarViewHolder(view, onItemListener)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysOfMonth[position]
        holder.dayText.text = day

        // 1. 处理 "今天" 的高亮逻辑 (圆形深紫色背景)
        if (day.isNotEmpty()) {
            val date = try {
                currentMonth.atDay(day.toInt())
            } catch (e: Exception) { null }

            if (date != null && date == today) {
                holder.dayText.setBackgroundResource(R.drawable.bg_circle_purple)
                holder.dayText.setTextColor(Color.WHITE)
            } else {
                holder.dayText.background = null
                holder.dayText.setTextColor(Color.parseColor("#333333"))
            }

            // --- 农历显示 ---
            if (date != null) {
                val lunarStr = LunarUtils.getLunarText(date.year, date.monthValue, date.dayOfMonth)
                holder.lunarText.text = lunarStr
                holder.lunarText.visibility = View.VISIBLE

                // 全部统一为灰色
                holder.lunarText.setTextColor(Color.parseColor("#888888"))
            }
        } else {
            holder.dayText.background = null
            holder.lunarText.visibility = View.INVISIBLE
        }

        // 如果不是空日期，且该日期有日程，显示小圆点
        if (day.isNotEmpty() && eventDates.contains(day)) {
            holder.eventIndicator.visibility = View.VISIBLE
        } else {
            holder.eventIndicator.visibility = View.GONE
        }

        // 选中效果：如果当前日期等于选中日期，背景变紫
        // 否则如果不是空日期，背景白色
        // 空日期灰色
        if (day == selectedDay) {
            holder.itemView.setBackgroundColor(Color.parseColor("#BDA7E7"))
        } else if (day.isNotEmpty()) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor("#F5F5F5".toColorInt())
        }
    }

    override fun getItemCount(): Int = daysOfMonth.size

    inner class CalendarViewHolder(
        itemView: View,
        private val onItemListener: OnItemListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val lunarText: TextView = itemView.findViewById(R.id.lunarText) // Add lunarText binding
        val eventIndicator: TextView = itemView.findViewById(R.id.eventIndicator)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val day = daysOfMonth[adapterPosition]
            if (day.isNotEmpty()) {
                onItemListener.onItemClick(adapterPosition, day)
            }
        }
    }

    interface OnItemListener {
        fun onItemClick(position: Int, dayText: String)
    }
}