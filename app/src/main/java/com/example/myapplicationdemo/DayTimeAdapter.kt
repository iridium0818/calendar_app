package com.example.myapplicationdemo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayTimeAdapter(
    private val selectedDate: String, // YYYY-MM-DD
    private val events: List<CalendarEvent>,
    private val onEventClickListener: (CalendarEvent) -> Unit,
    private val onSlotClickListener: (String) -> Unit // returns "HH:mm"
) : RecyclerView.Adapter<DayTimeAdapter.DayTimeViewHolder>() {

    private val hours = (0..23).toList()

    inner class DayTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeLabel: TextView = itemView.findViewById(R.id.timeLabel)
        val eventsContainer: FrameLayout = itemView.findViewById(R.id.eventsContainer)
        val eventList: LinearLayout = itemView.findViewById(R.id.eventList)
        val addEventHint: TextView = itemView.findViewById(R.id.addEventHint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayTimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_time_slot, parent, false)
        return DayTimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayTimeViewHolder, position: Int) {
        val hour = hours[position]
        val timeStr = String.format("%02d:00", hour)
        holder.timeLabel.text = timeStr

        // 清空旧视图
        holder.eventList.removeAllViews()

        // 筛选某小时的日程 (简单匹配 time string startsWith "HH")
        // 假设 events 已经过滤为今天的了
        val hourPrefix = String.format("%02d", hour)
        val eventsInHour = events.filter { it.time.startsWith(hourPrefix) }

        if (eventsInHour.isNotEmpty()) {
            holder.addEventHint.visibility = View.GONE
            eventsInHour.forEach { event ->
                val cardView = createEventCard(holder.itemView.context, event)
                holder.eventList.addView(cardView)
            }
        } else {
            // Restore visibility when empty
            holder.addEventHint.visibility = View.VISIBLE
        }

        // 点击空白处添加日程
        holder.eventsContainer.setOnClickListener {
            onSlotClickListener(timeStr)
        }
    }

    private fun createEventCard(context: Context, event: CalendarEvent): View {
        val textView = TextView(context)
        textView.text = "${event.time} ${event.title}"
        textView.textSize = 12f
        textView.setTextColor(android.graphics.Color.parseColor("#4A148C")) // 深紫色文字
        textView.setBackgroundResource(R.drawable.bg_event_card)
        textView.setPadding(16, 8, 8, 8)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 4)
        textView.layoutParams = params

        textView.setOnClickListener {
            onEventClickListener(event)
        }
        return textView
    }

    override fun getItemCount(): Int = hours.size
}
