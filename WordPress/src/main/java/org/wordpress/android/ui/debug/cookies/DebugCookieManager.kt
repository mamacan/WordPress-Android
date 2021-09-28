package org.wordpress.android.ui.debug.cookies

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.wordpress.android.util.BuildConfigWrapper
import java.net.CookieManager
import javax.inject.Inject

/**
 * This class wraps a [CookieStore][java.net.CookieStore] and a [SharedPreferences][android.content.SharedPreferences],
 * and syncs [DebugCookie]s between them.
 *
 * Note: this class was not designed with production use in mind, and because of that, it makes several assumptions
 * about the format of the cookies. If we ever need to use this for anything other than manually setting debug cookies,
 * then we should consider introducing our own [CookieStore][java.net.CookieStore] implementation instead.
 *
 * @param context The [Context][android.content.Context] from which the
 * [SharedPreferences][android.content.SharedPreferences] will be built.
 * @param cookieManager The [CookieManager][java.net.CookieManager] from which the [CookieStore][java.net.CookieStore]
 * will be retrieved.
 */
class DebugCookieManager internal constructor(
    private val httpCookieManager: CookieManager,
    private val webViewCookieManager: android.webkit.CookieManager,
    private val preferences: SharedPreferences,
    private val gson: Gson,
    private val buildConfig: BuildConfigWrapper
) {
    @Inject constructor(
        context: Context,
        cookieManager: CookieManager,
        buildConfig: BuildConfigWrapper
    ) : this(
            cookieManager,
            android.webkit.CookieManager.getInstance(),
            context.getSharedPreferences(DEBUG_COOKIE_PREFERENCES, MODE_PRIVATE),
            GsonBuilder().serializeNulls().create(),
            buildConfig
    )

    fun sync() {
        if (buildConfig.isDebugSettingsEnabled()) {
            getAll().forEach {
                httpCookieManager.add(it)
                webViewCookieManager.add(it)
            }
        }
    }

    fun getAll() = preferences.all.values.mapNotNull { DebugCookie.decode(gson, it as? String?) }

    fun add(cookie: DebugCookie) {
        httpCookieManager.add(cookie)
        webViewCookieManager.add(cookie)
        preferences.add(cookie)
    }

    fun remove(cookie: DebugCookie) {
        httpCookieManager.remove(cookie)
        webViewCookieManager.remove(cookie)
        preferences.remove(cookie)
    }

    private fun CookieManager.add(cookie: DebugCookie) = cookieStore.add(cookie.toURI(), cookie.toHttpCookie())

    private fun CookieManager.remove(cookie: DebugCookie) = cookieStore.remove(cookie.toURI(), cookie.toHttpCookie())

    private fun android.webkit.CookieManager.remove(cookie: DebugCookie) =
            setCookie(cookie.oldRfcDomain, cookie.copy(value = null).headerValue)

    private fun android.webkit.CookieManager.add(cookie: DebugCookie) =
            setCookie(cookie.oldRfcDomain, cookie.headerValue)

    private fun SharedPreferences.add(cookie: DebugCookie) = edit { putString(cookie.key, cookie.encode(gson)) }

    private fun SharedPreferences.remove(cookie: DebugCookie) = edit { remove(cookie.key) }

    companion object {
        private const val DEBUG_COOKIE_PREFERENCES = "debug-cookie-preferences"
    }
}
