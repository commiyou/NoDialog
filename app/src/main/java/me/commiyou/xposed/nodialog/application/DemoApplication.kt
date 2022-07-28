package me.commiyou.xposed.nodialog.application

import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication

class DemoApplication : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()
        loggerD(msg = "I am running in module space")
    }
}