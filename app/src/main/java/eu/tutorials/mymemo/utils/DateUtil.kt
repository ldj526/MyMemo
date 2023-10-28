package eu.tutorials.mymemo.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun convertTimestampToDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
    val tz = TimeZone.getTimeZone("Asia/Seoul")
    sdf.timeZone = tz
    return sdf.format(date)
}