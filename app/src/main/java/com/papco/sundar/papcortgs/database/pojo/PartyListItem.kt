package com.papco.sundar.papcortgs.database.pojo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface PartyListItem{
    var id:Int
    var name:String
    var searchText:String
    var disabled:Boolean

    fun highlightedName(color: Color):AnnotatedString{

        if(disabled)
            return AnnotatedString(name)

        val index = name.indexOf(searchText, ignoreCase = true)
        if(index==-1 || searchText.isBlank())
            return AnnotatedString(name)

        return buildAnnotatedString {
            append(name)
            addStyle(SpanStyle(color), index, index+searchText.length)
            toAnnotatedString()
        }
    }
}

fun Flow<List<PartyListItem>>.filterWith(queryFlow:Flow<String>):Flow<List<PartyListItem>>{
    return this.combine(queryFlow) { receivers, query ->

        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank())
            receivers
        else
            receivers.filter { it.name.contains(trimmedQuery, true) }
    }
}