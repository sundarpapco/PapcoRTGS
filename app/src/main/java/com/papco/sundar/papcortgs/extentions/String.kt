package com.papco.sundar.papcortgs.extentions

import android.util.Range
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

fun String.highlightWord(highlightWord:String,color:Color):AnnotatedString{

    val index = indexOf(highlightWord, ignoreCase = true)
    if(index==-1 || highlightWord.isBlank())
        return AnnotatedString(this)

    return buildAnnotatedString {
        append(this@highlightWord)
        addStyle(SpanStyle(color), index, index+highlightWord.length)
        toAnnotatedString()
    }
}

fun String.highlightRange(range:Range<Int>,color:Color):AnnotatedString{

    if(range.upper==0 && range.lower==0)
        return AnnotatedString(this)

    return buildAnnotatedString {
        append(this@highlightRange)
        addStyle(SpanStyle(color), range.lower, range.upper)
        toAnnotatedString()
    }
}