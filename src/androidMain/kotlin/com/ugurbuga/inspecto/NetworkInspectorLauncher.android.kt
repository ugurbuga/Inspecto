package com.ugurbuga.inspecto

import android.content.Context

actual fun launchNetworkInspector(context: Any?) {
    val ctx = (context as? Context) ?: InspectoContextHolder.applicationContext
    if (ctx != null) {
        InspectoActivity.start(ctx)
    } else {
        NetworkInspector.launch()
    }
}
