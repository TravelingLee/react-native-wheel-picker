package com.wheelpicker

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.annotations.ReactProp

@ReactModule(name = WheelPickerViewManager.REACT_CLASS)
class WheelPickerViewManager : SimpleViewManager<WheelPickerView>() {

    companion object {
        const val REACT_CLASS = "WheelPickerView"
    }

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(context: ThemedReactContext): WheelPickerView {
        return WheelPickerView(context).apply {

            // 原有最终值回调
            setOnValueChangedListener { index ->
                val eventData = Arguments.createMap().apply { putInt("index", index) }
                val surfaceId = UIManagerHelper.getSurfaceId(context)
                val event = OnValueChangeEvent(surfaceId, id, eventData)
                dispatchEvent(context, event)
            }

            // 新增：实时滚动回调
            setOnValueChangingListener { index ->
                val eventData = Arguments.createMap().apply { putInt("index", index) }
                val surfaceId = UIManagerHelper.getSurfaceId(context)
                val event = OnValueChangingEvent(surfaceId, id, eventData)
                dispatchEvent(context, event)
            }
        }
    }

    // 统一的事件分发方法
    private fun dispatchEvent(context: ThemedReactContext, event: com.facebook.react.uimanager.events.Event<*>) {
        val reactContext = context.reactApplicationContext
        val dispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, event.viewTag)
        dispatcher?.dispatchEvent(event)
    }

    @ReactProp(name = "items")
    fun setItems(view: WheelPickerView, items: ReadableArray?) {
        items?.let {
            val list = mutableListOf<String>()
            for (i in 0 until it.size()) {
                list.add(it.getString(i) ?: "")
            }
            view.setItems(list)
        }
    }

    @ReactProp(name = "selectedIndex")
    fun setSelectedIndex(view: WheelPickerView, index: Int) {
        view.setSelectedIndex(index)
    }

    @ReactProp(name = "unit")
    fun setUnit(view: WheelPickerView, unit: String?) {
        view.setUnit(unit)
    }

    @ReactProp(name = "fontFamily")
    fun setFontFamily(view: WheelPickerView, fontFamily: String?) {
        view.setFontFamily(fontFamily)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return mapOf(
            "onValueChange" to mapOf("registrationName" to "onValueChange"),
            "onValueChanging" to mapOf("registrationName" to "onValueChanging")
        )
    }
}