package com.example.financeapp

import android.app.Application
import com.example.financeapp.utils.PreferencesManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FinanceApp : Application() {

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Mặc định chỉ gửi crash của bản release, tránh làm nhiễu dashboard khi đang dev.
        // Riêng khi bấm "Tạo crash thử" trong Settings, cờ debug được bật để report
        // đang chờ vẫn được gửi đi ở lần mở app kế tiếp.
        crashlytics.isCrashlyticsCollectionEnabled =
            !BuildConfig.DEBUG || preferencesManager.isCrashlyticsDebugEnabled()

        crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
    }
}
