package com.papco.sundar.papcortgs.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun <T> ResultEffect(
    bus: ResultEventBus,
    key: String,
    clearOnReceive: Boolean = true,
    onReceive: (T) -> Unit
) {
    LaunchedEffect(bus.channelMap[key]) {
        bus.getResultFlow(key)?.collect {
            @Suppress("UNCHECKED_CAST") onReceive(it as T)
            if (clearOnReceive)
                bus.clearResultFlow(key)
        }
    }
}


@Composable
fun rememberResultEventBus(): ResultEventBus {
    return rememberSaveable(saver = ResultEventBus.Saver) {
        ResultEventBus()
    }
}

class ResultEventBus(
    initialKeys: List<String> = emptyList()
) {
    companion object {
        val Saver = Saver<ResultEventBus, List<String>>(
            save = { it.channelMap.keys.toList() }, // Save only the keys
            restore = { ResultEventBus(it) }
        )
    }

    private val _channelMap: MutableMap<String, Channel<Any>> = initialKeys.associateWith {
        Channel<Any>(capacity = BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
    }.toMutableMap()
    val channelMap: Map<String, Any?> = _channelMap

    fun getResultFlow(key: String): Flow<Any>? {
        return _channelMap[key]?.receiveAsFlow()
    }

    fun send(key: String, value: Any) {

        if (_channelMap[key] == null)
            _channelMap[key] = Channel(capacity = BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)

        _channelMap[key]?.trySend(value)
    }

    fun clearResultFlow(key: String) {
        _channelMap.remove(key)
    }


}