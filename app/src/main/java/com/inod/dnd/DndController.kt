package com.inod.dnd

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DndState(
    val permissionGranted: Boolean = false,
    val active: Boolean = false,
    val filter: Int = NotificationManager.INTERRUPTION_FILTER_ALL,
    val typeLabel: String = "Off",
    val selectedFilter: Int = NotificationManager.INTERRUPTION_FILTER_PRIORITY
)

object DndController {

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences

    private val nm by lazy {
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val _state = MutableStateFlow(DndState())
    val state: StateFlow<DndState> = _state.asStateFlow()

    private var receiverRegistered = false
    private const val KEY_PREFERRED_FILTER = "preferred_filter"
    private val DEFAULT_PREFERRED = NotificationManager.INTERRUPTION_FILTER_PRIORITY

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences("dnd_prefs", Context.MODE_PRIVATE)
        registerPolicyReceiver()
        refresh()
    }

    private val policyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refresh()
        }
    }

    private fun registerPolicyReceiver() {
        if (receiverRegistered) return
        receiverRegistered = true
        val filter = IntentFilter().apply {
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(NotificationManager.ACTION_NOTIFICATION_POLICY_CHANGED)
        }
        appContext.registerReceiver(policyReceiver, filter)
    }

    private fun isPolicyAccessGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            nm.isNotificationPolicyAccessGranted
        } else {
            try {
                nm.notificationPolicy
                true
            } catch (_: SecurityException) {
                false
            }
        }
    }

    fun refresh() {
        val granted = isPolicyAccessGranted()
        val current = if (granted) {
            nm.currentInterruptionFilter
        } else {
            NotificationManager.INTERRUPTION_FILTER_ALL
        }
        val active = granted && current != NotificationManager.INTERRUPTION_FILTER_ALL
        _state.value = DndState(
            permissionGranted = granted,
            active = active,
            filter = current,
            typeLabel = filterLabel(current),
            selectedFilter = prefs.getInt(KEY_PREFERRED_FILTER, DEFAULT_PREFERRED)
        )
    }

    private fun applyInterruptionFilter(filter: Int) {
        try {
            nm.setInterruptionFilter(filter)
        } catch (_: SecurityException) {
            // No policy access, or the device blocked it - just ignore.
        }
    }

    fun setFilter(filter: Int) {
        if (!isPolicyAccessGranted()) return
        if (filter != NotificationManager.INTERRUPTION_FILTER_ALL) {
            prefs.edit().putInt(KEY_PREFERRED_FILTER, filter).apply()
        }
        applyInterruptionFilter(filter)
        refresh()
    }

    fun toggle() {
        if (!isPolicyAccessGranted()) return
        val target = if (_state.value.active) {
            NotificationManager.INTERRUPTION_FILTER_ALL
        } else {
            prefs.getInt(KEY_PREFERRED_FILTER, DEFAULT_PREFERRED)
        }
        applyInterruptionFilter(target)
        refresh()
    }

    fun openPolicySettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun filterLabel(filter: Int): String = when (filter) {
        NotificationManager.INTERRUPTION_FILTER_NONE -> "Total Silence"
        NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "Priority Only"
        NotificationManager.INTERRUPTION_FILTER_ALARMS -> "Alarms Only"
        NotificationManager.INTERRUPTION_FILTER_ALL -> "Off"
        else -> "Unknown"
    }
}
