package me.commiyou.xposed.nodialog.data

import com.highcapable.yukihookapi.hook.xposed.channel.data.ChannelData
import com.highcapable.yukihookapi.hook.xposed.prefs.data.PrefsData
import me.commiyou.xposed.nodialog.BuildConfig

object DataConst {
    val TOAST_FLAG = PrefsData("toast_flag", true)
    val ADD_CLS_TO_FILTER = "${BuildConfig.APPLICATION_ID}.ADD_CLS_TO_FILTER"
    val ADD_CLS_TO_IGNORE = "${BuildConfig.APPLICATION_ID}.ADD_CLS_TO_IGNORE"
    val EXTRA_PKGNAME = "pkgName"
    val EXTRA_CLSNAME = "clsName"
}