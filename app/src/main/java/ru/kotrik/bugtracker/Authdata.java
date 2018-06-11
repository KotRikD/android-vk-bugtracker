package ru.kotrik.bugtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by kotoriku on 11.04.2018.
 */

public class Authdata {

    public static final String PREF_MAIN = "bt";

    public static final String PREF_COOKIES = "bt_cookies";
    public static final String PREF_USERID = "bt_user_id";
    public static final String PREF_TOKEN_VK = "bt_token_vk";
    public static final String PREF_LAST_AUTH = "bt_last_auth";


    public Authdata(Context ctx, String userid, String token, String cookies, long stampauth) {
        SharedPreferences mSettings = ctx.getSharedPreferences(PREF_MAIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.putString(PREF_TOKEN_VK, token);
        editor.putString(PREF_USERID, userid);
        editor.putString(PREF_COOKIES, cookies);
        editor.putLong(PREF_LAST_AUTH, stampauth);
        editor.apply();
    }

    public static SharedPreferences getSettings(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_MAIN, Context.MODE_PRIVATE);
        return settings;
    }

    public static String getUserId(Context ctx) {
        SharedPreferences mSettings = getSettings(ctx);
        return mSettings.getString(PREF_USERID, null);
    }

    public static String getTokenVk(Context ctx) {
        SharedPreferences mSettings = getSettings(ctx);
        return mSettings.getString(PREF_TOKEN_VK, null);
    }

    public static long getStamp(Context ctx) {
        SharedPreferences mSettings = getSettings(ctx);
        return mSettings.getLong(PREF_LAST_AUTH, 0);
    }

    public static Map<String, String> getCookies(Context ctx) {
        SharedPreferences mSettings = getSettings(ctx);
        String cookies_str = mSettings.getString(PREF_COOKIES, null);
        Map<String, String> cookies = new HashMap<String, String>();
        if (cookies_str == null) {
            return cookies;
        }

        String[] hmm_parse = cookies_str.split(" ");
        for (String a : hmm_parse) {
            String[] g_p = a.replace(";", "").split("=");
            cookies.put(g_p[0], g_p[1]);
        }

        return cookies;
    }

    public static String getRawCookies(Context ctx) {
        SharedPreferences mSettings = getSettings(ctx);
        return mSettings.getString(PREF_COOKIES, null);
    }

    public static void removeAuth(Context ctx) {
        SharedPreferences mSettings = getSettings(ctx);
        mSettings.edit().clear().apply();
        CookieManager.getInstance().removeAllCookie();
    }
}
