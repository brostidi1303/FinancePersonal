package com.example.financeapp.di

import com.example.financeapp.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefsManager: PreferencesManager // ✅ Inject trực tiếp manager vào
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // ✅ Lấy token thật từ SharedPreferences
        val token = prefsManager.getAuthToken()

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}