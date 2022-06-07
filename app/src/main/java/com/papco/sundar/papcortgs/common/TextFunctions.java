package com.papco.sundar.papcortgs.common;

import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class TextFunctions {

    public static final String TAG_RECEIVER_ACC_NUMBER="<RecAccNumber>";
    public static final String TAG_RECEIVER_ACC_NAME="<RecAccName>";
    public static final String TAG_AMOUNT="<TransAmount>";
    public static final String TAG_RECEIVER_BANK="<RecBank>";
    public static final String TAG_RECEIVER_IFSC="<RecIfsc>";
    public static final String TAG_SENDER_NAME="<SenAccName>";


    public static void removeAllForegroundSpan(Editable source){

        for(ForegroundColorSpan span:source.getSpans(0,source.length(),ForegroundColorSpan.class))
            source.removeSpan(span);

    }

    public static void markAllTagsWithSpan(Editable source){

        markTag(source,TAG_RECEIVER_ACC_NAME);
        markTag(source,TAG_RECEIVER_ACC_NUMBER);
        markTag(source,TAG_AMOUNT);
        markTag(source,TAG_RECEIVER_BANK);
        markTag(source,TAG_RECEIVER_IFSC);
        markTag(source,TAG_SENDER_NAME);

    }

    private static void markTag(Editable source,String tag){

        String sourceString=source.toString();
        int searchStart = 0;
        int searchLength = tag.length();
        int sourceLength = source.length();
        int currentIndex = sourceString.indexOf(tag, searchStart);

        while(currentIndex!=-1){

            source.setSpan(new ForegroundColorSpan(Color.BLUE),currentIndex,currentIndex+searchLength,0);
            searchStart=currentIndex+searchLength;
            if(searchStart>sourceLength)
                break;

            currentIndex=sourceString.indexOf(tag,searchStart);

        }

    }

    public static String getDefaultMessageFormat(){

        String format="We have done RTGS transfer of "+
                TextFunctions.TAG_AMOUNT +
                " on your account " +
                TextFunctions.TAG_RECEIVER_ACC_NAME +
                ". Kindly acknowledge the same. - PAPCO OFFSET";

        return format;
    }



    public static SpannableString getHighlightedString(String source, String toHighlight, int highlightColour) {

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
