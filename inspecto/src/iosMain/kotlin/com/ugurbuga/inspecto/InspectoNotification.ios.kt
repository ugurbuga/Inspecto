package com.ugurbuga.inspecto

import platform.Foundation.NSLock
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionDestructive
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionCustomDismissAction
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual object InspectoNotification {
    private val recentRequests = mutableListOf<String>()
    private const val MAX_RECENT_REQUESTS = 5
    private val lock = NSLock()

    actual fun initialize() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val openAction = UNNotificationAction.actionWithIdentifier(
            identifier = "OPEN_ACTION",
            title = "OPEN",
            options = 0u
        )
        val clearAction = UNNotificationAction.actionWithIdentifier(
            identifier = "CLEAR_ACTION",
            title = "CLEAR",
            options = UNNotificationActionOptionDestructive
        )
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = "INSPECTO_TRAFFIC_CATEGORY",
            actions = listOf(openAction, clearAction),
            intentIdentifiers = listOf<String>(),
            options = UNNotificationCategoryOptionCustomDismissAction
        )
        center.setNotificationCategories(setOf(category))
    }

    actual fun showServiceCallNotification(entry: InspectoLogEntry) {
        initialize()
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            if (settings?.authorizationStatus == 2L /* UNAuthorizationStatusAuthorized */) {
                val symbol = if (entry.isSuccess) "✓" else "✕"
                val line = "$symbol ${entry.statusCode} ${entry.method} ${entry.path} (${entry.durationMs}ms)"
                lock.lock()
                try {
                    recentRequests.add(0, line)
                    if (recentRequests.size > MAX_RECENT_REQUESTS) {
                        recentRequests.removeAt(recentRequests.size - 1)
                    }
                } finally {
                    lock.unlock()
                }

                val bodyText = lock.let {
                    it.lock()
                    try {
                        recentRequests.joinToString("\n")
                    } finally {
                        it.unlock()
                    }
                }

                val content = UNMutableNotificationContent().apply {
                    setTitle("Inspecto Traffic Monitor")
                    setBody(bodyText)
                    setCategoryIdentifier("INSPECTO_TRAFFIC_CATEGORY")
                }
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, false)
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = "inspecto_ongoing_notification",
                    content = content,
                    trigger = trigger
                )
                center.addNotificationRequest(request, null)
            }
        }
    }

    actual fun requestPermission(onResult: (Boolean) -> Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            if (granted) {
                initialize()
            }
            onResult(granted)
        }
    }
}
