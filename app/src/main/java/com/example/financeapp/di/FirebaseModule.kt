package com.example.financeapp.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Cung cấp các instance Firebase cho toàn app.
 *
 * Lưu ý: FirebaseApp được khởi tạo tự động bởi FirebaseInitProvider
 * (ContentProvider do plugin google-services merge vào manifest),
 * nên KHÔNG cần gọi FirebaseApp.initializeApp() thủ công.
 *
 * Từ BoM 32.5.0 trở lên, API KTX đã gộp vào artifact chính:
 * dùng `com.google.firebase.Firebase` chứ KHÔNG phải `com.google.firebase.ktx.Firebase`.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = Firebase.crashlytics
}
