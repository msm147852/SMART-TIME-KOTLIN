package com.smarttime.app.core.network

import com.smarttime.app.BuildConfig

/**
 * Shared API endpoint configuration.
 * Keep credentials and provider secrets on the server; never embed API keys in the APK.
 */
object ApiConfig {
    const val BASE_URL: String = BuildConfig.API_BASE_URL
}
