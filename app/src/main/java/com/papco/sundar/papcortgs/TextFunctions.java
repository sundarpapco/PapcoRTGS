package com.papco.sundar.papcortgs;

import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

public class TextFunctions {

    public static SpannableString getHighlitedString(String source, String toHighlight,int highlightColour) {

        int searchStart = 0;
        int searchLength = toHighlight.length();
        int sourceLength = source.length();
        SpannableString resultString=new SpannableString(source);
        source=source.toLowerCase();
        toHighlight=toHighlight.toLowerCase();
        int currentIndex = source.indexOf(toHighlight, searchStart);

        while(currentIndex!=-1){

            resultString.setSpan(new BackgroundColorSpan(highlightColour),currentIndex,currentIndex+searchLength,0);
            searchStart=currentIndex+searchLength;
            if(searchStart>sourceLength)
                break;

            currentIndex=source.indexOf(toHighlight,searchStart);

        }

        return resultString;

    }

}
