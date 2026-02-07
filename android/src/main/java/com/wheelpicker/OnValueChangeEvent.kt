package com.wheelpicker

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class OnValueChangeEvent(
    surfaceId: Int,
    viewTag: Int,
    private val eventData: WritableMap
) : Event<OnValueChangeEvent>(surfaceId, viewTag) {

    companion object {
        const val EVENT_NAME = "onValueChange"
    }

    override fun getEventName(): String = EVENT_NAME
    override fun canCoalesce(): Boolean = false

    override fun dispatch(rctEventEmitter: com.facebook.react.uimanager.events.RCTEventEmitter) {
        rctEventEmitter.receiveEvent(viewTag, EVENT_NAME, eventData)
    }
}