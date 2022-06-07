package com.papco.sundar.papcortgs.common

import java.util.*

fun getCalendarInstance(): Calendar =
    Calendar.getInstance(Locale.getDefault())

fun calendarWithTime(time: Long): Calendar =
    getCalendarInstance().also { it.timeInMillis = time }


fun Calendar.setDateAndTime(
    year: Int = 2019,
    month: Int = 0,
    day: Int = 1,
    hour: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0,
    milliSeconds: Int = 0
): Calendar {

    set(Calendar.YEAR, year)
    set(Calendar.MONTH, month)
    set(Calendar.DAY_OF_MONTH, day)
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minutes)
    set(Calendar.SECOND, seconds)
    set(Calendar.MILLISECOND, milliSeconds)
    return this
}


fun Calendar.dayId(): Long {

    return getCalendarInstance().setDateAndTime(
        get(Calendar.YEAR),
        get(Calendar.MONTH),
        get(Calendar.DAY_OF_MONTH)
    ).timeInMillis
}





