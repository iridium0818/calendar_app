package com.example.myapplicationdemo

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.myapplicationdemo.EventAlarmReceiver
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater

class MainActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    private enum class ViewMode { MONTH, WEEK, DAY }

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate
    private lateinit var db: AppDatabase
    private var currentViewMode = ViewMode.MONTH

    // 月视图底部显示日程的容器
    private lateinit var monthEventsContainer: android.widget.LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        requestNotificationPermission()

        // 初始化数据库
        db = AppDatabase.getDatabase(this)

        // 初始化视图
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearText)

        selectedDate = LocalDate.now(ZoneId.of("Asia/Shanghai"))

        // 查找或创建底部容器（如果xml没加，需要先加xml）
        // 假设我们在 xml 已经加了一个 id 为 monthEventsListContainer 的 ScrollView/LinearLayout
        monthEventsContainer = findViewById(R.id.monthEventsListContainer) // Need to add to XML first

        findViewById<Button>(R.id.btnPrevMonth).setOnClickListener {
            changeDate(-1)
        }

        findViewById<Button>(R.id.btnNextMonth).setOnClickListener {
            changeDate(1)
        }

        setupViewToggleButtons()

        findViewById<FloatingActionButton>(R.id.fabAddEvent).setOnClickListener {
            showAddEventDialog()
        }

        updateCalendarView()
    }

    private fun setupViewToggleButtons() {
        val btnMonth = findViewById<Button>(R.id.btnViewMonth)
        val btnWeek = findViewById<Button>(R.id.btnViewWeek)
        val btnDay = findViewById<Button>(R.id.btnViewDay)

        val onClickListener = android.view.View.OnClickListener { view ->
            when (view.id) {
                R.id.btnViewMonth -> currentViewMode = ViewMode.MONTH
                R.id.btnViewWeek -> currentViewMode = ViewMode.WEEK
                R.id.btnViewDay -> currentViewMode = ViewMode.DAY
            }
            updateCalendarView()
        }

        btnMonth.setOnClickListener(onClickListener)
        btnWeek.setOnClickListener(onClickListener)
        btnDay.setOnClickListener(onClickListener)
    }

    private fun changeDate(amount: Long) {
        selectedDate = when (currentViewMode) {
            ViewMode.MONTH -> selectedDate.plusMonths(amount)
            ViewMode.WEEK -> selectedDate.plusWeeks(amount)
            ViewMode.DAY -> selectedDate.plusDays(amount)
        }
        updateCalendarView()
    }

    private fun updateCalendarView() {
        monthYearText.text = monthYearFromDate(selectedDate)

        // 更新表头可见性：只有月视图需要 grid header
        findViewById<android.view.View>(R.id.weekHeader).visibility =
            if (currentViewMode == ViewMode.MONTH) android.view.View.VISIBLE else android.view.View.GONE

        // 控制底部日程列表的显示：仅月视图需要
         findViewById<android.view.View>(R.id.monthEventsScrollView).visibility =
            if (currentViewMode == ViewMode.MONTH) android.view.View.VISIBLE else android.view.View.GONE

        // 更新按钮状态颜色 (简单的视觉反馈)
        updateToggleButtonStyles()

        lifecycleScope.launch {
            val allEvents = db.eventDao().getAllEvents()

            when (currentViewMode) {
                ViewMode.MONTH -> setupMonthView(allEvents)
                ViewMode.WEEK -> setupWeekView(allEvents)
                ViewMode.DAY -> setupDayView(allEvents)
            }

            // 如果是月视图，更新底部的列表
            if (currentViewMode == ViewMode.MONTH) {
                 updateMonthEventsList()
            }
        }
    }

    private fun updateToggleButtonStyles() {
        val btnMonth = findViewById<Button>(R.id.btnViewMonth)
        val btnWeek = findViewById<Button>(R.id.btnViewWeek)
        val btnDay = findViewById<Button>(R.id.btnViewDay)

        val activeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#876BCA")) // 深色选中
        val inactiveColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDA7E7")) // 浅色未选中

        btnMonth.backgroundTintList = if (currentViewMode == ViewMode.MONTH) activeColor else inactiveColor
        btnWeek.backgroundTintList = if (currentViewMode == ViewMode.WEEK) activeColor else inactiveColor
        btnDay.backgroundTintList = if (currentViewMode == ViewMode.DAY) activeColor else inactiveColor
    }

    private fun setupMonthView(allEvents: List<CalendarEvent>) {
        val daysInMonth = daysInMonthArray(selectedDate)
        val eventDates = allEvents.mapNotNull { event ->
             try {
                val date = LocalDate.parse(event.date, DateTimeFormatter.ISO_LOCAL_DATE)
                if (date.year == selectedDate.year && date.month == selectedDate.month) {
                    date.dayOfMonth.toString()
                } else null
            } catch (_: Exception) { null }
        }

        // Pass current selectedDate's YearMonth so adapter knows which days are real vs empty padding
        val currentYearMonth = YearMonth.from(selectedDate)
        val calendarAdapter = CalendarAdapter(daysInMonth, this@MainActivity, eventDates, selectedDate.dayOfMonth.toString(), currentYearMonth)
        calendarRecyclerView.layoutManager = GridLayoutManager(this@MainActivity, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun setupWeekView(allEvents: List<CalendarEvent>) {
        val weekDates = ArrayList<LocalDate>()
        var current = getWeekStartDate(selectedDate)
        for (i in 0 until 7) {
            weekDates.add(current)
            current = current.plusDays(1)
        }

        // 筛选本周日程
        // 为了简单，直接传所有 events 给 Adapter，让 Adapter 也就是 WeekListAdapter 内部去 filter
        // 或者在这里 filter 也行，但 WeekListAdapter 设计时接收了完整 list 方便处理
        // 考虑到性能，这里最好先 filter 一下范围，但作业 demo 数据量小，直接传 full list 也没问题

        val adapter = WeekListAdapter(weekDates, allEvents,
            onEventClickListener = { event -> showAddEventDialog(eventToEdit = event) },
            onDayClickListener = { date ->
                selectedDate = date
                showAddEventDialog()
            }
        )
        calendarRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        calendarRecyclerView.adapter = adapter
    }

    private fun setupDayView(allEvents: List<CalendarEvent>) {
        // 筛选当天的日程
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todaysEvents = allEvents.filter { it.date == dateStr }

        val adapter = DayTimeAdapter(dateStr, todaysEvents,
            onEventClickListener = { event -> showAddEventDialog(eventToEdit = event) },
            onSlotClickListener = { timeStr ->
                showAddEventDialog(preFilledTime = timeStr)
            }
        )
        calendarRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        calendarRecyclerView.adapter = adapter
    }

    // 获取当前日期所在周的周日（起始日）
    private fun getWeekStartDate(date: LocalDate): LocalDate {
        val dayOfWeek = date.dayOfWeek.value // 1=Mon ... 7=Sun
        // 我们视周日为一周开始 (7) -> 减 0 天, 周一(1) -> 减 1 天
        val daysToSubtract = if (dayOfWeek == 7) 0L else dayOfWeek.toLong()
        return date.minusDays(daysToSubtract)
    }

    private fun daysInWeekArray(date: LocalDate): ArrayList<String> {
        val days = ArrayList<String>()
        var current = getWeekStartDate(date)
        for (i in 0 until 7) {
            days.add(current.dayOfMonth.toString())
            current = current.plusDays(1)
        }
        return days
    }

    private fun daysInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value // 1=Mon, .. 7=Sun

        // 调整：让周日为第一列 (根据 activity_main.xml 的表头)
        // java.time 的 dayOfWeek 1是周一。如果表头是 日一二三...
        // 周日(7) -> index 0, 周一(1) -> index 1
        val startSpace = if (dayOfWeek == 7) 0 else dayOfWeek

        for (i in 1..42) {
            if (i <= startSpace || i > daysInMonth + startSpace) {
                daysInMonthArray.add("")
            } else {
                daysInMonthArray.add((i - startSpace).toString())
            }
        }
        return daysInMonthArray
    }

    private fun monthYearFromDate(date: LocalDate): String {
        val pattern = if (currentViewMode == ViewMode.DAY) "yyyy-MM-dd" else "yyyy-MM"
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return date.format(formatter)
    }

    // 注意：onItemClick 是 Month View 的 CalendarAdapter 的回调
    override fun onItemClick(position: Int, dayText: String) {
        if (dayText.isNotEmpty()) {
            val year = selectedDate.year
            val month = selectedDate.monthValue
            val oldSelectedDate = selectedDate // 保存旧选中日期以便判断是否同一天
            selectedDate = LocalDate.of(year, month, dayText.toInt())
            updateCalendarView() // 刷新选中状态 （包括底部列表）
        }
    }

    // 新增：更新月视图底部的日程列表
    private fun updateMonthEventsList() {
        monthEventsContainer.removeAllViews()

        // 重新添加标题 "今日日程" (因为它被 removeAllViews 删掉了)
        val titleView = TextView(this)
        titleView.text = "今日日程"
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        titleView.setTextColor(android.graphics.Color.BLACK)
        titleView.textSize = 16f
        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 24) // margin bottom
        titleView.layoutParams = params
        monthEventsContainer.addView(titleView)

        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        lifecycleScope.launch {
            val events = db.eventDao().getEventsByDate(dateStr)

            if (events.isEmpty()) {
                 val emptyText = TextView(this@MainActivity)
                 emptyText.text = "暂无日程"
                 emptyText.setPadding(32, 32, 32, 32)
                 emptyText.textSize = 14f
                 emptyText.setTextColor(android.graphics.Color.GRAY)
                 monthEventsContainer.addView(emptyText)
            } else {
                 events.forEach { event ->
                     val itemView = createCheckableEventItem(event)
                     monthEventsContainer.addView(itemView)
                 }
            }
        }
    }

    private fun createCheckableEventItem(event: CalendarEvent): android.view.View {
         val view = LayoutInflater.from(this).inflate(R.layout.item_event_list_checkable, monthEventsContainer, false)

         val checkboxContainer = view.findViewById<android.view.View>(R.id.checkboxContainer)
         val checkboxIcon = view.findViewById<android.view.View>(R.id.checkboxIcon)
         val tvTime = view.findViewById<TextView>(R.id.tvEventTime)
         val tvTitle = view.findViewById<TextView>(R.id.tvEventTitle)
         val textContainer = view.findViewById<android.view.View>(R.id.textContainer)

         // tvTime.text = event.time // 不再显示，布局里已经 gone
         tvTitle.text = event.title

         // 使用一个可变变量来保存当前的最新状态
         var currentEvent = event

         // 设置初始状态
         updateCheckableItemStyle(currentEvent.isCompleted, checkboxContainer, checkboxIcon, tvTime, tvTitle)

         // 点击Checkbox区域：切换状态
         checkboxContainer.setOnClickListener {
             val newStatus = !currentEvent.isCompleted
             // 更新数据库
             lifecycleScope.launch {
                 val updatedEvent = currentEvent.copy(isCompleted = newStatus)
                 db.eventDao().update(updatedEvent)

                 // 【核心修复】：更新本地持有的事件对象，以便下次点击时基于新状态取反
                 currentEvent = updatedEvent

                 // 【修复点】: 必须 UI 上也更新状态，包括图标可见性
                 updateCheckableItemStyle(newStatus, checkboxContainer, checkboxIcon, tvTime, tvTitle)

                 // 如果为了保证排序等，可以 updateMonthEventsList()，但这里为了交互流畅，直接更 UI
             }
         }

         // 点击文字区域：编辑
         // 【修改点】：直接打开编辑详情弹窗，复用 dialog_add_event
         textContainer.setOnClickListener {
             // 传递最新的 event，确保编辑的是当前状态
             showAddEventDialog(eventToEdit = currentEvent)
         }

         return view
    }

    private fun updateCheckableItemStyle(
        isCompleted: Boolean,
        container: android.view.View,
        icon: android.view.View,
        tvTime: TextView?, // Change to Nullable
        tvTitle: TextView
    ) {
         if (isCompleted) {
             // 选中状态：紫色实心背景，勾选可见

             // 手动设置背景色为紫色，圆角
             val shape = android.graphics.drawable.GradientDrawable()
             shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
             shape.cornerRadius = dpToPx(6f)
             shape.setColor(android.graphics.Color.parseColor("#876BCA"))
             container.background = shape

             icon.visibility = android.view.View.VISIBLE

             // 文字变灰 + 删除线
             tvTime?.setTextColor(android.graphics.Color.GRAY)
             tvTitle.setTextColor(android.graphics.Color.GRAY)
             tvTitle.paintFlags = tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
             tvTime?.let { it.paintFlags = it.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG }
         } else {
             // 未选中状态：灰色边框，中间空白
             container.setBackgroundResource(R.drawable.selector_checkbox_bg)
             container.isSelected = false
             icon.visibility = android.view.View.GONE

             // 文字正常
             tvTime?.setTextColor(android.graphics.Color.parseColor("#876BCA"))
             tvTitle.setTextColor(android.graphics.Color.parseColor("#333333"))
             tvTitle.paintFlags = tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
             tvTime?.let { it.paintFlags = it.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv() }
         }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun deleteEvent(event: CalendarEvent) {
        lifecycleScope.launch {
            db.eventDao().delete(event)
            cancelEventAlarm(event) // 取消提醒
            Toast.makeText(this@MainActivity, "日程已删除", Toast.LENGTH_SHORT).show()
            updateCalendarView() // 刷新视图
        }
    }

    private fun showAddEventDialog(preFilledTime: String? = null, eventToEdit: CalendarEvent? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 关闭按钮逻辑
        dialogView.findViewById<android.view.View>(R.id.btnCloseDialog).setOnClickListener {
            dialog.dismiss()
        }

        // 绑定视图控件
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etTitle = dialogView.findViewById<EditText>(R.id.etEventTitle)
        val btnSelectDate = dialogView.findViewById<android.view.View>(R.id.btnSelectDate)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate)
        val btnSelectTime = dialogView.findViewById<android.view.View>(R.id.btnSelectTime)
        val tvSelectedTime = dialogView.findViewById<TextView>(R.id.tvSelectedTime)
        val etDesc = dialogView.findViewById<EditText>(R.id.etEventDesc)
        val btnSelectRemind = dialogView.findViewById<android.view.View>(R.id.btnSelectRemind)

        // New views for reminder
        val tvNoRemind = dialogView.findViewById<TextView>(R.id.tvNoRemind)
        val layoutRemindDetail = dialogView.findViewById<android.view.View>(R.id.layoutRemindDetail)
        val tvRemindValue = dialogView.findViewById<TextView>(R.id.tvRemindValue)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // ================= 数据初始化逻辑 =================

        if (eventToEdit != null) {
            // --- 编辑模式 ---
            tvDialogTitle.text = "编辑日程" // 修改标题

            // 填充现有数据
            etTitle.setText(eventToEdit.title)
            etDesc.setText(eventToEdit.description ?: "")

            // 填充日期
            val dateParts = eventToEdit.date.split("-")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            val tempDateObj = LocalDate.of(year, month, day)

            val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate) // 重新获取引用确保安全
            tvSelectedDate.text = eventToEdit.date

            // 填充时间
            val tvSelectedTime = dialogView.findViewById<TextView>(R.id.tvSelectedTime)
            tvSelectedTime.text = eventToEdit.time

            // 改变按钮文字
            btnSave.text = "更新"

            // 添加删除按钮 (如果布局支持，或者我们在代码里动态加)
            // 简单起见，我们把"取消"按钮改成删除? 或者在 Cancel 旁边加一个 Delete?
            // 由于 dialog_add_event.xml 里没有预留删除按钮，我们在这里动态修改 btnCancel 的行为或者添加一个新按钮
            // 方案：让"取消"变成"删除"，如果用户只想取消，点击对话框外或者返回键即可。
            // 或者更好点，把 "取消" 改为 "删除"，并把文字变红。
            btnCancel.text = "删除"
            btnCancel.setTextColor(android.graphics.Color.WHITE) // 修改为白色以适应红色背景
            btnCancel.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF5252")) // 红色背景
            btnCancel.setOnClickListener {
                lifecycleScope.launch {
                    deleteEvent(eventToEdit) // reuse existing delete logic
                    dialog.dismiss()
                    // 这里可能需要 updateMonthEventsList()，deleteEvent 里面已经调了 updateCalendarView，所以应该没问题
                }
            }

            // 需要更新局部变量，供保存逻辑使用 (Kotlin 闭包)
            // 注意：下面的 var tempDate 是定义在 if 块外的，我们需要确保它们被正确赋值
            // 这里的逻辑稍微有点绕，因为原来的代码是先初始化 defaults 再设置 click listeners
            // 我们需要重构一下变量初始化的顺序。
        } else {
            // --- 新建模式 ---
            tvDialogTitle.text = "新建日程"
            // btnCancel 保持默认 "取消" 行为
            btnCancel.text = "取消"
            btnCancel.setTextColor(android.graphics.Color.parseColor("#666666"))
            btnCancel.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D7D7D7"))
        }

        // =================重新组织变量初始化=================

        // 默认日期：如果是编辑模式，用日程日期；否则用当前选中日期
        var tempDate = if (eventToEdit != null) {
            LocalDate.parse(eventToEdit.date, DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            selectedDate
        }
        tvSelectedDate.text = tempDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // 默认时间：北京时间，24小时制 HH:mm
        var tempTimeStr = if (eventToEdit != null) {
            eventToEdit.time
        } else {
            preFilledTime ?: java.time.LocalTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        tvSelectedTime.text = tempTimeStr

        // 默认提醒
        var reminderMinutes = eventToEdit?.reminderMinutes ?: 15

        fun updateRemindUI() {
            if (reminderMinutes == 0) {
                tvNoRemind.visibility = android.view.View.VISIBLE
                layoutRemindDetail.visibility = android.view.View.GONE
            } else {
                tvNoRemind.visibility = android.view.View.GONE
                layoutRemindDetail.visibility = android.view.View.VISIBLE
                tvRemindValue.text = reminderMinutes.toString()
            }
        }

        // 检查是否过期，过期则强制不提醒
        fun checkEventPassed() {
            val parts = tempTimeStr.split(":")
            val dateTime = java.time.LocalDateTime.of(tempDate, java.time.LocalTime.of(parts[0].toInt(), parts[1].toInt()))
            // 加1分钟宽容度，避免新建日程时默认时间(当前分钟)被判定为过期
            if (dateTime.plusMinutes(1).isBefore(java.time.LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
                reminderMinutes = 0
                btnSelectRemind.isEnabled = false
                tvNoRemind.text = "已过期(不提醒)"
            } else {
                btnSelectRemind.isEnabled = true
                if (tvNoRemind.text == "已过期(不提醒)") {
                    tvNoRemind.text = "不提醒"
                    // 如果从已过期变回未来时间，且当前是0，恢复默认15?
                    // 暂时不恢复，保持0，让用户自己选，避免用户特意选了不提醒又被重置
                }
            }
            updateRemindUI()
        }

        // 初始检查
        checkEventPassed()

        // --- 事件监听 (保持不变) ---

        // 1. 日期选择
        btnSelectDate.setOnClickListener {
            val dpd = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                tempDate = LocalDate.of(year, month + 1, dayOfMonth)
                tvSelectedDate.text = tempDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                checkEventPassed() // 检查过期
            }, tempDate.year, tempDate.monthValue - 1, tempDate.dayOfMonth)
            dpd.show()
        }

        // 2. 时间选择
        btnSelectTime.setOnClickListener {
            val parts = tempTimeStr.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val tpd = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                tempTimeStr = String.format("%02d:%02d", selectedHour, selectedMinute)
                tvSelectedTime.text = tempTimeStr
                checkEventPassed() // 检查过期
            }, hour, minute, true)
            tpd.show()
        }

        // 3. 提醒选择 (下拉滚动 -> 这里用列表弹窗模拟)
        btnSelectRemind.setOnClickListener {
            val options = arrayOf("不提醒", "5 分钟", "10 分钟", "15 分钟", "30 分钟", "1 小时")
            val values = arrayOf(0, 5, 10, 15, 30, 60)

            AlertDialog.Builder(this)
                .setTitle("设置提醒时间")
                .setItems(options) { _, which ->
                    reminderMinutes = values[which]
                    updateRemindUI() // Update composed UI
                }
                .show()
        }

        // 4. 取消 / 删除
        // 如果是新建模式，btnCancel 点击是 dismiss (已在上面处理了编辑模式的覆写，这里只处理默认)
        if (eventToEdit == null) {
            btnCancel.setOnClickListener {
                 dialog.dismiss()
            }
        }

        // 5. 保存 / 更新
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDesc.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "请输入日程标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (eventToEdit == null) {
                    // === 增逻辑 ===
                    val newEvent = CalendarEvent(
                        title = title,
                        description = description,
                        date = tempDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        time = tempTimeStr,
                        reminderMinutes = reminderMinutes
                    )

                    // 插入数据库
                    val id = db.eventDao().insert(newEvent)
                    val savedEvent = newEvent.copy(id = id.toInt())

                    // 设置提醒
                    if (reminderMinutes > 0) scheduleEventAlarm(savedEvent, reminderMinutes)

                    Toast.makeText(this@MainActivity, "日程已保存", Toast.LENGTH_SHORT).show()
                } else {
                    // === 更新逻辑 ===
                    val updatedEvent = eventToEdit.copy(
                        title = title,
                        description = description,
                        date = tempDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        time = tempTimeStr,
                        reminderMinutes = reminderMinutes
                    )

                    db.eventDao().update(updatedEvent)
                    // 更新提醒 (先取消旧的，再设新的)
                    cancelEventAlarm(eventToEdit) // 其实 scheduleEventAlarm 也有覆盖功能，但稳妥起见
                    if (reminderMinutes > 0) scheduleEventAlarm(updatedEvent, reminderMinutes)

                    Toast.makeText(this@MainActivity, "日程已更新", Toast.LENGTH_SHORT).show()
                }

                updateCalendarView()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // --- 提醒功能实现 ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "日程提醒"
            val descriptionText = "显示日程的定时提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CALENDAR_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun scheduleEventAlarm(event: CalendarEvent, reminderMinutes: Int = 0) {
        try {
            val dateParts = event.date.split("-")
            val timeParts = event.time.split(":")
            if(dateParts.size != 3 || timeParts.size != 2) return

            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Shanghai"))
            calendar.set(year, month - 1, day, hour, minute, 0)

            // 获取日程发生的准确时间
            val eventTimeInMillis = calendar.timeInMillis

            // 减去提醒时间 (例如提前15分钟)
            calendar.add(java.util.Calendar.MINUTE, -reminderMinutes)

            // 逻辑修改：
            // 1. 如果日程时间(eventTimeInMillis)本身已经过了，则完全不提醒
            // 2. 如果日程时间还没到，但提醒时间点(calendar.timeInMillis)已经过了（即处于提醒窗口内），
            //    AlarmManager 设置过去的时间会立即触发，符合 "就要有提醒弹窗" 的需求

            if (eventTimeInMillis <= System.currentTimeMillis()) return

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, EventAlarmReceiver::class.java).apply {
                putExtra("EVENT_TITLE", event.title)
                putExtra("EVENT_MESSAGE", "日程提醒: ${event.title} (即将开始)")
                putExtra("EVENT_ID", event.id)
                putExtra("SCHEDULE_TIME", event.time)
            }

            // 使用 event.id 作为 requestCode，保证每个事件的 PendingIntent 唯一
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                event.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 设置精准闹钟
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(alarmManager.canScheduleExactAlarms()){
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                     alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelEventAlarm(event: CalendarEvent) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, EventAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
