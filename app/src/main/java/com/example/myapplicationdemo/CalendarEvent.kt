package com.example.myapplicationdemo

import android.content.Context
import androidx.room.*
import java.util.Date

// 1. 定义日程表 (Entity)
@Entity(tableName = "events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,          // 日程标题
    val description: String?,   // 详情
    val date: String,           // 日期，格式 YYYY-MM-DD
    val time: String,           // 时间，格式 HH:mm
    val isCompleted: Boolean = false, // 新增字段：是否完成
    val reminderMinutes: Int = 15 // 提醒提前分钟数，默认为15
)

// 2. 定义操作接口 (DAO)
@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE date = :date")
    suspend fun getEventsByDate(date: String): List<CalendarEvent>

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<CalendarEvent>

    @Insert
    suspend fun insert(event: CalendarEvent): Long // Change to return Long (rowIt) for ID retrieval if needed

    @Update
    suspend fun update(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)
}

// 3. 定义数据库 (Database)
@Database(entities = [CalendarEvent::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calendar_database"
                )
                .fallbackToDestructiveMigration() // 简单处理：版本升级时清除数据
                .allowMainThreadQueries() // 作业演示方便，允许在主线程查库(实际开发不建议)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}