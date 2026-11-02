package com.example.financeapp.analytics

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lớp bọc duy nhất cho Analytics + Crashlytics.
 *
 * Luôn log qua đây thay vì gọi thẳng FirebaseAnalytics ở UI/ViewModel:
 * - tên event/param tập trung một chỗ, không gõ sai chuỗi
 * - dễ tắt/đổi provider sau này
 */
@Singleton
class AppAnalytics @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) {

    /** Ghi nhận người dùng đang xem màn hình nào. Gọi trong LaunchedEffect của từng screen. */
    fun logScreen(screenName: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName)
        )
        crashlytics.log("screen: $screenName")
    }

    /** Event tuỳ ý. Tên event chỉ gồm chữ/số/gạch dưới, tối đa 40 ký tự. */
    fun logEvent(name: String, params: Bundle? = null) {
        analytics.logEvent(name, params)
    }

    /** Gắn thuộc tính cho user hiện tại, dùng để phân nhóm trong báo cáo Analytics. */
    fun setUserProperty(key: String, value: String?) {
        analytics.setUserProperty(key, value)
    }

    /** Gắn id người dùng cho cả Analytics lẫn Crashlytics. Truyền null khi đăng xuất. */
    fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId.orEmpty())
    }

    /**
     * Bật/tắt việc thu thập crash ngay trong process đang chạy.
     * Cần cho nút test: nếu không bật trước khi ném exception thì crash bị bỏ qua hoàn toàn.
     */
    fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    /** Báo lỗi đã bắt được (không làm crash app) lên Crashlytics. */
    fun recordError(throwable: Throwable, message: String? = null) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
    }

    object Events {
        const val TRANSACTION_ADDED = "transaction_added"
        const val TRANSACTION_DELETED = "transaction_deleted"
        const val CATEGORY_CREATED = "category_created"
        const val STOCK_VIEWED = "stock_viewed"
        const val NEWS_OPENED = "news_opened"
    }

    object Params {
        const val AMOUNT = "amount"
        const val CATEGORY = "category"
        const val SYMBOL = "symbol"
        const val SECTOR = "sector"
    }
}
