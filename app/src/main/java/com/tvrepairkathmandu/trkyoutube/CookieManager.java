package com.tvrepairkathmandu.trkyoutube;

/** Resolves the java.net/android.webkit CookieManager name collision on legacy builds. */
final class CookieManager {
    private CookieManager() {}
    static android.webkit.CookieManager getInstance() {
        return android.webkit.CookieManager.getInstance();
    }
}
