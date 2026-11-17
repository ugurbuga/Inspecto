package com.ugurbuga.inspectodemo

expect fun performOkHttpTestRequest(url: String, onResult: (String) -> Unit)
