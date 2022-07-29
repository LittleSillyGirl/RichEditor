package com.yuruiyin.richeditor.utils;

import android.util.Log;

/**
 * Title: 自定义的日志类
 *
 * @author yuruiyin
 * @version 2018/12/23
 */
public class LogUtil {

    public static final int VERBOSE = 1;

    public static final int DEBUG = 2;

    public static final int INFO = 3;

    public static final int WARN = 4;

    public static final int ERROR = 5;

    public static final int NOTHING = 6;

    /**
     * 是否开启日志
     */
    private static boolean mIsLogEnable;

    public static void setIsLogEnable(boolean isLogEnable) {
        mIsLogEnable = isLogEnable;
    }

    public static boolean isLogEnable() {
        return mIsLogEnable;
    }


    public static int level = mIsLogEnable ? VERBOSE : NOTHING;

    public static void v(String tag, String msg) {
        if (level <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (level <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (level <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (level <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (level <= ERROR) {
            Log.e(tag, msg);
        }
    }
}
