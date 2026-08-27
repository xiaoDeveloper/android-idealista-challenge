package com.xiao.idealistachallenge

import android.app.Application
import com.xiao.idealistachallenge.core.AppContainer

open class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = createContainer()
    }

    protected open fun createContainer(): AppContainer = AppContainer(this)
}
