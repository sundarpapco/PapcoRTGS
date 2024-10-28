package com.papco.sundar.papcortgs.database.pojo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString


/*
This class is a common representation for the Sender and Receiver to display in the UI.
id: Id of the sender or receiver
name: The name of the Sender or receiver to display in the UI
highlightWord: The text which if contained in the name should be highlighted in the UI. Typically this is the search query.
               The matched portion will be displayed in a different color in the UI
disabled: Whether this Item should be rendered as disabled. Will be true in some cases like a selecting a receiver
          again who has already been added to a group thus preventing the user from selecting this receiver again
 */
data class Party(
    val id: Int,
    val name: String,
    val highlightWord:String,
    val disabled:Boolean=false
) {

    /*
   Returns an Annotated String which will have the highlightWord highlighted in the given color.
   If the highlight is not found in the name, then the name is returned simply without any highlighting
    */
    fun highlightedName(color: Color):AnnotatedString{

        if(disabled)
            return AnnotatedString(name)

       val index = name.indexOf(highlightWord, ignoreCase = true)
       if(index==-1 || highlightWord.isBlank())
           return AnnotatedString(name)

       return buildAnnotatedString {
           append(name)
           addStyle(SpanStyle(color), index, index+highlightWord.length)
           toAnnotatedString()
       }
   }
}
