package com.papco.sundar.papcortgs.common

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(msg:String,duration:Int=Toast.LENGTH_SHORT){

    Toast.makeText(requireContext(),msg,duration).show()

}