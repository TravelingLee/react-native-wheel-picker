package com.wheelpicker

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class OnValueChangingEvent(
    surfaceId: Int,
    viewTag: Int,
    private val eventData: WritableMap
) : Event<OnValueChangingEvent>(surfaceId, viewTag) {

    companion object {
        const val EVENT_NAME = "onValueChanging"
    }

    override fun getEventName(): String = EVENT_NAME
    override fun canCoalesce(): Boolean = false

    override fun dispatch(rctEventEmitter: com.facebook.react.uimanager.events.RCTEventEmitter) {
        rctEventEmitter.receiveEvent(viewTag, EVENT_NAME, eventData)
    }
}