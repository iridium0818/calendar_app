# 日历应用演示(Calendar App Demo)

使用 Kotlin 开发的 Android 应用程序。具备完善的日历功能，支持事件管理和农历显示。

## 项目结构

### 📅 日历应用程序(Calendar Application)
日历应用的核心功能实现在以下文件中：

*   **Adapters**: `CalendarAdapter.kt`, `DayTimeAdapter.kt`, `WeekListAdapter.kt` - Manage the display of calendar grids and lists.
*   **Models**: `CalendarEvent.kt` - Data model for calendar events.
*   **Receivers**: `EventAlarmReceiver.kt` - Handles alarm notifications for events.
*   **Utilities**: `LunarUtils.kt` - Helper utilities for Lunar calendar calculations.
*   **UI/Activities**: `MainActivity.kt`, `ReminderDialogActivity.kt`.

### 📚 教学 / 学习代码
以下文件是我在腾讯菁英班学习过程中的练习和对 Android 组件（Activity、Service、BroadcastReceiver、Fragment）的实验，与主日历功能没有直接关系：

*   **Activities**: `MainActivity2.kt`, `MainActivity3.kt`, `StaticFragmentActivity.kt`, `DynamicFragmentActivity.kt` - Used for testing activity lifecycles and navigation.
*   **Services**: `MyService.kt` - Service implementation examples.
*   **Broadcast Receivers**: `DynamicBroadcastReceiver.kt`, `StaticBroadcastReceiver.kt` - Examples of different broadcast receiver implementations.
*   **Fragments**: `MyFragment.kt`.
*   **Reference**: `MainActivity_teach_Reference.txt`.

## Getting Started

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Sync with Gradle files.
4.  Run `MainActivity` to start the Calendar application.

