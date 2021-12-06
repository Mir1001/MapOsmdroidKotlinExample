package com.um.feri.cs.pora.mapkotlinexample
//adb shell am broadcast -a android.intent.action.ACTION_BOOT_COMPLETED com.um.feri.cs.pora.mapkotlinexample
 import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.um.feri.cs.pora.mapkotlinexample.location.MyEventLocationSettingsChange
import timber.log.Timber

class MyBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.action?.let { act ->
            Timber.d("MyBootReceiver $act ")
        }
    }
}
