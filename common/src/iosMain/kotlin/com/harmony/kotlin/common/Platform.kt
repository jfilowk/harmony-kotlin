package com.harmony.kotlin.common

actual object Platform {
    actual val IS_JVM: Boolean
        get() = false
    actual val IS_NATIVE: Boolean
        get() = true
}