package com.papco.sundar.papcortgs.ui.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed class ToastMessage{
    class Message(val message:String): ToastMessage()
    class Resource(@StringRes val resourceId:Int): ToastMessage()
}

open class ToasterState{
    private val _toaster = Channel<ToastMessage>()
    val toaster = _toaster.receiveAsFlow()


    suspend fun toast(toast: ToastMessage) {
        _toaster.send(toast)
    }
}

@Composable
fun Toaster(context: Context,toasterState: ToasterState){
    LaunchedEffect(Unit) {
        toasterState.toaster.collect {toast->
            val message = when(toast){
                is ToastMessage.Message->{
                    toast.message
                }

                is ToastMessage.Resource->{
                    context.getString(toast.resourceId)
                }
            }

            Toast.makeText(context,message, Toast.LENGTH_LONG).show()
        }
    }
}