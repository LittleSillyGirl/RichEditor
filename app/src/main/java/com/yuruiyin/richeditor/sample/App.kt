package com.yuruiyin.richeditor.sample

import android.app.Application
import com.yuruiyin.richeditor.utils.LogUtil

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        LogUtil.setIsLogEnable(BuildConfig.DEBUG)
    }

}