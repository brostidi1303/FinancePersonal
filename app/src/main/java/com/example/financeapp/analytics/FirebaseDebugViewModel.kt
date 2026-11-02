package com.example.financeapp.analytics

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import com.example.financeapp.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Chỉ phục vụ việc kiểm tra Firebase đã chạy đúng chưa.
 * Màn hình gọi nó được bọc trong `if (BuildConfig.DEBUG)`.
 */
@HiltViewModel
class FirebaseDebugViewModel @Inject constructor(
    private val analytics: AppAnalytics,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    /** Bắn 1 event tự đặt tên để soi trong DebugView. */
    fun sendTestEvent() {
        analytics.logEvent(
            "debug_test_event",
            bundleOf("source" to "settings_screen", "ts" to System.currentTimeMillis())
        )
    }

    /** Gửi 1 non-fatal lên Crashlytics — app KHÔNG chết, report vào mục "Non-fatals". */
    fun sendNonFatal() {
        enableCollectionForTesting()
        analytics.recordError(
            IllegalStateException("Test non-fatal từ FinanceApp"),
            message = "Người dùng bấm nút test non-fatal trong Settings"
        )
    }

    /**
     * Làm app crash thật để kiểm tra Crashlytics.
     * Crashlytics 20.x đẩy report đi ngay trong lúc xử lý crash; nếu lúc đó mất mạng
     * thì report nằm chờ và được gửi ở lần mở app kế tiếp.
     */
    fun triggerTestCrash(): Nothing {
        enableCollectionForTesting()
        throw RuntimeException("Test Crash — FinanceApp Crashlytics")
    }

    /**
     * Ghi cờ vào prefs (cho các lần chạy sau) VÀ bật ngay trong process hiện tại,
     * nếu không thì crash/non-fatal đầu tiên sẽ bị Crashlytics bỏ qua.
     */
    private fun enableCollectionForTesting() {
        preferencesManager.setCrashlyticsDebugEnabled(true)
        analytics.setCollectionEnabled(true)
    }
}
