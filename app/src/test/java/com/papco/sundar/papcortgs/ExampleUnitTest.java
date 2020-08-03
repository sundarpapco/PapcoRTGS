package com.papco.sundar.papcortgs;

import android.text.TextUtils;

import org.junit.Test;

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
}