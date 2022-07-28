package me.commiyou.xposed.nodialog.hook


import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.util.Log
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.YukiHookAPI.Status.executorName
import com.highcapable.yukihookapi.YukiHookAPI.Status.executorVersion
import com.highcapable.yukihookapi.YukiHookAPI.Status.isXposedModuleActive
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.type.android.*
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import me.commiyou.xposed.nodialog.BuildConfig
import me.commiyou.xposed.nodialog.data.DataConst

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() {
        // 配置 YuKiHookAPI
        // 可简写为 configs {}
        YukiHookAPI.configs {
            debugTag = BuildConfig.APPLICATION_ID
           isEnableModulePrefsCache = false

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onHook() {
        YukiHookAPI.encase {
            loadApp{
                    DialogClass.hook {
                    injectMember {
                        method {
                            name = "show"
                            emptyParam()
                            returnType = UnitType
                        }
                        replaceUnit {
                            val pkgName = instance<Dialog>().context.packageName
                            val clsName = instanceClass.name
                            var filterFlag = prefs.getStringSet(pkgName, emptySet())
                                .contains(clsName)
                            loggerD(msg = "prefs? ${prefs.isRunInNewXShareMode} ${prefs.isXSharePrefsReadable} ${executorVersion} ${executorName} $isXposedModuleActive")
                            Log.v(BuildConfig.APPLICATION_ID, "prefs? ${prefs.isRunInNewXShareMode} ${prefs.isXSharePrefsReadable} ${executorVersion} ${executorName} $isXposedModuleActive")
                            loggerD(msg="\n for $pkgName, \n prefs filter ${prefs.getStringSet(pkgName, emptySet())} \n ignore ${prefs.getStringSet(pkgName+"_ignore", emptySet())}")
                            var msg = "filter Dialog ${clsName} for ${pkgName}, $filterFlag"
                            loggerD(msg = msg)
                            if (!filterFlag
                                || instanceClass.name.startsWith("android")
                            ) {
                                method.invokeOriginal<() -> Unit>()
                                return@replaceUnit
                            }
                            if (prefs.get(DataConst.TOAST_FLAG)) {
                                Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    // hook dialog
                    injectMember {
                        method {
                            name = "onStop"
                            emptyParam()
                            returnType = UnitType
                        }
                        afterHook {
                            val pkgName = instance<Dialog>().context.packageName
                            val clsName = instanceClass.name
                            var filterFlag = prefs.getStringSet(pkgName, emptySet())
                                .contains(clsName)
                            var ignoreFlag = prefs.getStringSet(pkgName+"_ignore", emptySet()).contains(clsName)
                            loggerD(
                                msg =
                                "in after hook ${pkgName} ${clsName} ${method.name} filter? ${filterFlag} ignore? $ignoreFlag"
                            )
                            if (filterFlag || ignoreFlag ||  instanceClass.name.startsWith("android")
                            ) {
                                method.invokeOriginal<() -> Unit>()
                                return@afterHook
                            }

                            AlertDialog.Builder(instance<Dialog>().context).setTitle(
                                "Hook Dialog?"
                            ).setMessage("filter $clsName?\n\n")
                                .setPositiveButton("Filter") { _,_ ->
                                    Intent().also { intent ->
                                        intent.action = DataConst.ADD_CLS_TO_FILTER
                                        intent.putExtra(DataConst.EXTRA_PKGNAME, pkgName)
                                        intent.putExtra(DataConst.EXTRA_CLSNAME, clsName)
                                        instance<Dialog>().context.sendBroadcast(intent) ?: loggerE(msg = "send ${intent.action} failed")
                                        loggerD(msg="send broadcast ${intent.action} ${pkgName} ${clsName}")
                                    }
                                }
                                .setNegativeButton("Ignore") { _,_ ->
                                    Intent().also { intent ->
                                        intent.action = DataConst.ADD_CLS_TO_IGNORE
                                        intent.putExtra(DataConst.EXTRA_PKGNAME, pkgName)
                                        intent.putExtra(DataConst.EXTRA_CLSNAME, clsName)
                                        instance<Dialog>().context.sendBroadcast(intent) ?: loggerE(msg = "send ${intent.action} failed")
                                        loggerD(msg="send broadcast ${intent.action}  ${pkgName} ${clsName}")
                                    }
                                }.show()

                            loggerD(
                                msg =
                                "end after hook ${instanceClass.name} ${method.name}"
                            )
                        }
                    }
                }
            }
        }
    }
}