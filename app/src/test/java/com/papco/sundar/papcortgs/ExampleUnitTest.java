package com.papco.sundar.papcortgs;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import org.junit.Test;

import static java.sql.DriverManager.println;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void equalsTest(){
        String oldName="a sakthivel";
        String newName="a sakthivel";
        boolean result= oldName.equals(newName);
        assertEquals(true,result);
    }

    @Test
    public void measureTextTest(){

        TextPaint textPaint=new TextPaint();
        textPaint.setTextSize(10f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(Typeface.create("Arial",Typeface.NORMAL));

        String w="w";
        String captialW="W";
        float smallLength=textPaint.measureText(w);
        float capitalLength=textPaint.measureText(captialW);
        println(Float.toString(smallLength));
        println(Float.toString(capitalLength));

    }


}