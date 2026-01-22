# 日历应用演示(Calendar App Demo)

使用 Kotlin 开发的 Android 应用程序。具备完善的日历功能，支持事件管理和农历显示。

## 项目结构

### 📅 日历应用程序(Calendar Application)
日历应用的核心功能实现在以下文件中：

*   **Adapters**: `CalendarAdapter.kt`, `DayTimeAdapter.kt`, `WeekListAdapter.kt` - 负责管理日历网格和列表的显示。
*   **Models**: `CalendarEvent.kt` - 日历事件的数据模型。
*   **Receivers**: `EventAlarmReceiver.kt` - 处理事件的闹钟提醒通知。
*   **Utilities**: `LunarUtils.kt` - 用于农历计算的辅助工具类。
*   **UI/Activities**: `MainActivity.kt`, `ReminderDialogActivity.kt`.

### 📚 教学 / 学习代码
以下文件是我在腾讯菁英班学习过程中的练习和对 Android 组件（Activity、Service、BroadcastReceiver、Fragment）的实验，与主日历功能没有直接关系：

*   **Activities**: `MainActivity2.kt`, `MainActivity3.kt`, `StaticFragmentActivity.kt`, `DynamicFragmentActivity.kt` - 用于测试 Activity 生命周期和导航。
*   **Services**: `MyService.kt` - 服务实现的示例。
*   **Broadcast Receivers**: `DynamicBroadcastReceiver.kt`, `StaticBroadcastReceiver.kt` - 不同广播接收器实现方式的示例。
*   **Fragments**: `MyFragment.kt`.
*   **Reference**: `MainActivity_teach_Reference.txt`.

## 项目运行

1.  在 Android Studio 中打开项目。
2.  同步 Gradle 文件。
3.  运行 MainActivity 以启动日历应用程序。

