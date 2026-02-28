package com.monkey.lucifer.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("lucifer_settings", Context.MODE_PRIVATE)

    private val _realTimeSpeakEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("real_time_speak", true)
    )
    val realTimeSpeakEnabled: StateFlow<Boolean> = _realTimeSpeakEnabled

    private val _pushToTalkEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("push_to_talk", false)
    )
    val pushToTalkEnabled: StateFlow<Boolean> = _pushToTalkEnabled

    fun setRealTimeSpeakEnabled(enabled: Boolean) {
        _realTimeSpeakEnabled.value = enabled
        sharedPreferences.edit {
            putBoolean("real_time_speak", enabled)
        }
    }

    fun setPushToTalkEnabled(enabled: Boolean) {
        _pushToTalkEnabled.value = enabled
        sharedPreferences.edit {
            putBoolean("push_to_talk", enabled)
        }
    }
}


