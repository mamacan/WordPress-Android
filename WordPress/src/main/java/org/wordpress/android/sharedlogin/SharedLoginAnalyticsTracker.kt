package org.wordpress.android.sharedlogin

import org.wordpress.android.analytics.AnalyticsTracker.Stat
import org.wordpress.android.sharedlogin.SharedLoginAnalyticsTracker.ErrorType.Companion.ERROR_TYPE
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper
import javax.inject.Inject

class SharedLoginAnalyticsTracker @Inject constructor(
    private val analyticsTracker: AnalyticsTrackerWrapper
) {
    fun trackLoginStart() = analyticsTracker.track(Stat.SHARED_LOGIN_START)

    fun trackLoginSuccess() = analyticsTracker.track(Stat.SHARED_LOGIN_SUCCESS)

    fun trackLoginFailed(errorType: ErrorType) =
            analyticsTracker.track(Stat.SHARED_LOGIN_FAILED, mapOf(ERROR_TYPE to errorType.value))

    sealed class ErrorType(val value: String) {
        object WPNotLoggedInError : ErrorType("wp_not_logged_in_error")

        object NullLoginDataError : ErrorType("null_login_data_error")

        object QueryLoginDataError : ErrorType("query_login_data_error")

        companion object {
            const val ERROR_TYPE = "error_type"
        }
    }
}
