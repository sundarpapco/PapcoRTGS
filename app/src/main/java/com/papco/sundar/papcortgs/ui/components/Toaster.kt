package com.papco.sundar.papcortgs.ui.components

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.papco.sundar.papcortgs.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.lang.Exception

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
fun Toaster(context: Context,toasts: Flow<ToastMessage>){
    LaunchedEffect(Unit) {
        toasts.collect {toast->
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

fun Exception.toastMessage(): ToastMessage {
    return this.message?.let{
        ToastMessage.Message(it)
    } ?: ToastMessage.Resource(R.string.unknown_error)
}