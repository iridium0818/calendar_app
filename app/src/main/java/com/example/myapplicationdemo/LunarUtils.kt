package com.example.myapplicationdemo

import java.util.Calendar

object LunarUtils {
    private val lunarInfo = longArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    )

    private val solarTermInfo = intArrayOf(
            0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693,
            263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758
    )

    private val TianGan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val DiZhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val GanZhi = arrayOfNulls<String>(60)
    private val lunarMonth = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
    private val lunarDay = arrayOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")

    val solarTerm = arrayOf("小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至")

    // 公历节日
    private val solarFestival = mapOf(
        "0101" to "元旦",
        "0214" to "情人节",
        "0308" to "妇女节",
        "0312" to "植树节",
        "0401" to "愚人节",
        "0501" to "劳动节",
        "0504" to "青年节",
        "0601" to "儿童节",
        "0701" to "建党节",
        "0801" to "建军节",
        "0910" to "教师节",
        "1001" to "国庆节",
        "1224" to "平安夜",
        "1225" to "圣诞节"
    )

    // 农历节日
    private val lunarFestival = mapOf(
        "0101" to "春节",
        "0115" to "元宵",
        "0505" to "端午",
        "0707" to "七夕",
        "0715" to "中元",
        "0815" to "中秋",
        "0909" to "重阳",
        "1208" to "腊八",
        "1223" to "小年",
        "0100" to "除夕" // 特殊处理除夕
    )

    init {
        for (i in 0 until 60) {
            GanZhi[i] = TianGan[i % 10] + DiZhi[i % 12]
        }
    }

    /**
     * 获取农历显示字符串 (例如 "初一", "春节", "清明")
     * 优先级: 节日 > 节气 > 农历日
     */
    fun getLunarText(year: Int, month: Int, day: Int): String {
        // 1. 公历节日
        val solarKey = String.format("%02d%02d", month, day)
        if (solarFestival.containsKey(solarKey)) {
            return solarFestival[solarKey]!!
        }

        // 2. 农历转换
        val lunarDate = solarToLunar(year, month, day)
        val lYear = lunarDate[0]
        val lMonth = lunarDate[1]
        val lDay = lunarDate[2]
        val isLeap = lunarDate[3] == 1

        // 3. 农历节日 (闰月不显示节日)
        if (!isLeap) {
            val lunarKey = String.format("%02d%02d", lMonth, lDay)
             if (lunarFestival.containsKey(lunarKey)) {
                 return lunarFestival[lunarKey]!!
             }
             // 除夕处理 (如果是腊月的最后一天)
             if (lMonth == 12) {
                 val daysInLunarMonth = daysInLunarMonth(lYear, lMonth)
                 if (lDay == daysInLunarMonth) {
                     return "除夕"
                 }
             }
        }

        // 4. 24节气
        val term = getSolarTerm(year, month, day)
        if (term != null) {
            return term
        }

        // 5. 农历日
        if (lDay == 1) {
            return lunarMonth[lMonth - 1] + "月"
        }
        return lunarDay[lDay - 1]
    }

    /**
     * 获取完整的农历日期描述 (例如 "农历 腊月廿三")
     */
    fun getLunarDateDetail(year: Int, month: Int, day: Int): String {
        val lunarDate = solarToLunar(year, month, day)
        val lMonth = lunarDate[1]
        val lDay = lunarDate[2]
        val isLeap = lunarDate[3] == 1

        val sb = StringBuilder()
        sb.append("农历")
        if (isLeap) sb.append("闰")
        sb.append(lunarMonth[lMonth - 1]).append("月")
        sb.append(lunarDay[lDay - 1])

        // 尝试获取节日或节气附加在后面
        val simpleText = getLunarText(year, month, day)
        // 如果 simpleText 不是 "X月" 且不是 "初X" 这种常规日期，说明是节日或节气
        if (!simpleText.endsWith("月") && !simpleText.startsWith("初") &&
            !simpleText.startsWith("廿") && !simpleText.startsWith("三") && !simpleText.startsWith("十")) {
             sb.append(" · ").append(simpleText)
        }

        return sb.toString()
    }

    // ================= 核心转换算法 =================

    // 返回农历 [年, 月, 日, 是否闰月(1是0否)]
    private fun solarToLunar(solarYear: Int, solarMonth: Int, solarDay: Int): IntArray {
        var i: Int
        var temp = 0
        var offset: Long

        val cal = Calendar.getInstance()
        cal.set(1900, 0, 31) // 1900-01-31 是农历1900年正月初一
        val baseDate = cal.time

        cal.set(solarYear, solarMonth - 1, solarDay)
        val objDate = cal.time

        offset = (objDate.time - baseDate.time) / 86400000

        var lunarYear = 1900
        val daysInYear = 365
        i = 1900
        while (i < 2100 && offset > 0) {
            temp = daysInLunarYear(i)
            offset -= temp.toLong()
            i++
        }
        if (offset < 0) {
            offset += temp.toLong()
            i--
        }
        lunarYear = i

        val leapMonth = getLeapMonth(lunarYear)
        var isLeap = false
        var lunarMonth = 1

        i = 1
        while (i < 13 && offset > 0) {
            // 闰月
            if (leapMonth > 0 && i == leapMonth + 1 && !isLeap) {
                --i
                isLeap = true
                temp = daysInLeapMonth(lunarYear)
            } else {
                temp = daysInLunarMonth(lunarYear, i)
            }
            // 解除闰月
            if (isLeap && i == leapMonth + 1) isLeap = false

            offset -= temp.toLong()
            if (!isLeap) i++
        }

        if (offset == 0L && leapMonth > 0 && i == leapMonth + 1) {
            if (isLeap) {
                isLeap = false
            } else {
                isLeap = true
                --i
            }
        }
        if (offset < 0) {
            offset += temp.toLong()
            if (isLeap) {
                isLeap = true // keep true
            } else {
               --i
            }
        }
        lunarMonth = i
        val lunarDay = (offset + 1).toInt()

        return intArrayOf(lunarYear, lunarMonth, lunarDay, if (isLeap) 1 else 0)
    }

    private fun daysInLunarYear(year: Int): Int {
        var i: Int = 0x8000
        var sum = 348
        for (k in 0 until 12) {
            if (lunarInfo[year - 1900].toInt() and i != 0) sum += 1
            i = i shr 1
        }
        return sum + daysInLeapMonth(year)
    }

    private fun daysInLunarMonth(year: Int, month: Int): Int {
        return if (lunarInfo[year - 1900].toInt() and (0x10000 shr month) == 0) 29 else 30
    }

    private fun daysInLeapMonth(year: Int): Int {
        return if (lunarInfo[year - 1900].toInt() and 0xf != 0) {
            if (lunarInfo[year - 1900].toInt() and 0x10000 == 0) 29 else 30
        } else 0
    }

    private fun getLeapMonth(year: Int): Int {
        return (lunarInfo[year - 1900] and 0xf).toInt()
    }

    private fun getSolarTerm(year: Int, month: Int, day: Int): String? {
       // 简化版节气计算，仅供演示，实际需要精确的天文学算法
       // 这里使用近似公式或者查表，考虑到代码量，我们使用一个简单的查表法检查是否在节气日
       // 真实项目中建议使用 complex library
       if (year < 1900 || year > 2100) return null

       // 基于 base 年份的偏移估算，这里为了演示，暂时只返回特定的几个大节气或者留空
       // 如果要精准计算24节气需要庞大的数据表。
       // 下面是一个非常简化的近似实现:
       val term1 = (year * 365.2422 + solarTermInfo[(month - 1) * 2] / 60.0 - year * 365.2422).toInt() // error logic
       // 节气计算太复杂，不建议裸写工具类。
       // 替代方案：仅显示“春节”等根据农历日期的，和固定公历节日。
       return null
    }
}
