package com.papco.sundar.papcortgs.common

import java.util.*

fun parseDate(dateString: String): Calendar {

    val calendar = getCalendarInstance()

    val split = dateString.split("/")
    check(split.size == 3) { "Illegal date format while parsing" }

    //Set the year
    //check(split[2].isDigitsOnly()) { "Illegal date format while parsing" }
    calendar.set(Calendar.YEAR, split[2].toInt())

    //Set the month
    calendar.set(Calendar.MONTH, split[1].toInt() - 1)

    //Set the date
    //check(split[0].isDigitsOnly()) { "Illegal date format while parsing" }
    calendar.set(Calendar.DAY_OF_MONTH, split[0].toInt())

    //Set the default time of midnight 12 that is starting of the day
    calendar.setTime()

    return calendar

}

fun getCalendarInstance(): Calendar =
    Calendar.getInstance(Locale.getDefault())

fun calendarWithTime(time: Long): Calendar =
    getCalendarInstance().also { it.timeInMillis = time }


fun currentTimeInMillie(): Long = Calendar.getInstance(Locale.getDefault()).timeInMillis

fun getMonthNumber(monthAsString: String): Int {

    return when (monthAsString) {
        "January" -> {
            0
        }
        "February" -> {
            1
        }
        "March" -> {
            2
        }
        "April" -> {
            3
        }
        "May" -> {
            4
        }
        "June" -> {
            5
        }
        "July" -> {
            6
        }
        "August" -> {
            7
        }
        "September" -> {
            8
        }
        "October" -> {
            9
        }
        "November" -> {
            10
        }
        "December" -> {
            11
        }
        else -> {
            throw IllegalStateException("Invalid Month while parsing")
        }
    }

}



fun Calendar.getMonthName(): String {

    return when (get(Calendar.MONTH)) {
        0 -> {
            "January"
        }
        1 -> {
            "February"
        }
        2 -> {
            "March"
        }
        3 -> {
            "April"
        }
        4 -> {
            "May"
        }
        5 -> {
            "June"
        }
        6 -> {
            "July"
        }
        7 -> {
            "August"
        }
        8 -> {
            "September"
        }
        9 -> {
            "October"
        }
        10 -> {
            "November"
        }
        11 -> {
            "December"
        }
        else -> {
            throw IllegalStateException("Invalid Month while parsing")
        }
    }

}

fun Calendar.getYear(): Int {
    return this.get(Calendar.YEAR)
}

fun Calendar.getDayName(): String {

    return when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> {
            "SUNDAY"
        }
        Calendar.MONDAY -> {
            "MONDAY"
        }
        Calendar.TUESDAY -> {
            "TUESDAY"
        }
        Calendar.WEDNESDAY -> {
            "WEDNESDAY"
        }
        Calendar.THURSDAY -> {
            "THURSDAY"
        }
        Calendar.FRIDAY -> {
            "FRIDAY"
        }
        Calendar.SATURDAY -> {
            "SATURDAY"
        }
        else -> {
            throw IllegalStateException("Invalid Month while parsing")
        }
    }

}

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

fun Calendar.setTime(
    hour: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0,
    milliSeconds: Int = 0
):Calendar {

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


fun Calendar.asDateString(): String {

    return String.format("%02d/%02d/%04d",
        get(Calendar.DAY_OF_MONTH),
        get(Calendar.MONTH)+1,
        get(Calendar.YEAR))
    //return "${get(Calendar.DAY_OF_MONTH)}/${get(Calendar.MONTH) + 1}/${get(Calendar.YEAR)}"
}

fun Calendar.asDateAndTimeString():String{

    return String.format("%02d/%02d/%04d, %02d:%02d:%02d:%04d",
        get(Calendar.DAY_OF_MONTH),
        get(Calendar.MONTH)+1,
        get(Calendar.YEAR),
        get(Calendar.HOUR_OF_DAY),
        get(Calendar.MINUTE),
        get(Calendar.SECOND),
        get(Calendar.MILLISECOND))

}

fun Calendar.reportTimeStamp():String{

    return String.format("%02d%02d%04d",
        get(Calendar.DAY_OF_MONTH),
        get(Calendar.MONTH)+1,
        get(Calendar.YEAR))
}




