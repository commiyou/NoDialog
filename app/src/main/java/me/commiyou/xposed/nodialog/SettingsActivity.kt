package me.commiyou.xposed.nodialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceFragmentCompat
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.factory.modulePrefs
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import me.commiyou.xposed.nodialog.data.DataConst
import java.util.logging.Logger

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        modulePrefs.putStringSet("com.sankuai.meituan.takeoutnew", setOf("com.sankuai.waimai.business.page.homepage.widget.dialog.UpdateForceInstallDialog"))
        val broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                when (intent?.action) {
                    DataConst.ADD_CLS_TO_FILTER -> {
                        Log.v(BuildConfig.APPLICATION_ID, "get broadcast ${intent.action}")
//                        Toast.makeText(applicationContext, "recviee ${intent.action}", Toast.LENGTH_SHORT)
                        val pkgName = intent.getStringExtra(DataConst.EXTRA_PKGNAME)
                        val clsName = intent.getStringExtra(DataConst.EXTRA_CLSNAME)
                        if (pkgName.isNullOrBlank() || clsName.isNullOrBlank()) return
                        val filterClassSet =
                            modulePrefs.getStringSet(pkgName, mutableSetOf()).toMutableSet()
                        filterClassSet.add(clsName)
                        modulePrefs.putStringSet(pkgName, filterClassSet)
                        val msg = "filter $clsName \n ${modulePrefs.getStringSet(pkgName, emptySet())}"
                        Log.v(BuildConfig.APPLICATION_ID, msg)
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    }
                    DataConst.ADD_CLS_TO_IGNORE -> {
                        Log.v(BuildConfig.APPLICATION_ID, "get broadcast ${intent.action}")
//                        Toast.makeText(applicationContext, "recviee ${intent.action}", Toast.LENGTH_SHORT)
                        val pkgName = intent.getStringExtra(DataConst.EXTRA_PKGNAME)
                        val clsName = intent.getStringExtra(DataConst.EXTRA_CLSNAME)
                        if (pkgName.isNullOrBlank() || clsName.isNullOrBlank()) return
                        val key = pkgName + "_ignore"
                        val filterClassSet =
                            modulePrefs.getStringSet(key, mutableSetOf()).toMutableSet()
                        filterClassSet.add(clsName)
                        modulePrefs.putStringSet(key, filterClassSet)
                        val msg = "ignore $clsName \n${modulePrefs.getStringSet(key, emptySet())}"
                        Log.v(BuildConfig.APPLICATION_ID, msg)
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    }

                }
            }
        }
        this.registerReceiver(broadCastReceiver, IntentFilter().apply {
            addAction(DataConst.ADD_CLS_TO_FILTER)
            addAction(DataConst.ADD_CLS_TO_IGNORE)
        })

    }

    class SettingsFragment : ModulePreferenceFragment() {
        override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}