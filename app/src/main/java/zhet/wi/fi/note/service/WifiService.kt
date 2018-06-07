package zhet.wi.fi.note.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.EXTRA_RESULTS_UPDATED
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import zhet.wi.fi.note.R
import zhet.wi.fi.note.binder.WifiBinder
import zhet.wi.fi.note.entity.WifiScanResult

class WifiService : Service() {
    companion object {
        private val TAG: String = WifiService::class.java.simpleName

    }

    private lateinit var wifiReceiver: WifiReceiver
    private var binder = WifiBinder()
    private lateinit var wifiManager: WifiManager
    private val openNetworks = ArrayList<WifiScanResult>()
    private lateinit var mp: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mp = MediaPlayer.create(applicationContext, R.raw.note)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
        val filter = IntentFilter()
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        wifiReceiver = WifiReceiver()
        registerReceiver(wifiReceiver, filter)
        wifiManager.startScan()
        val notification = NotificationCompat.Builder(this, TAG)
                .build()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiReceiver)
    }

    inner class WifiReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "${intent.action}, ${intent.getBooleanExtra(EXTRA_RESULTS_UPDATED, false)} ")
            val initialLen = openNetworks.size
            val scanResults = wifiManager.scanResults
            Log.i(TAG, "scan rez=>${scanResults.size}")
            openNetworks.clear()
            scanResults.forEach({
                val wifiScanResult = WifiScanResult(it.SSID, it.capabilities)
                if (it.capabilities == "[ESS]") {
                    openNetworks.add(wifiScanResult)
                }
            })

            if (openNetworks.size - initialLen > 0) {
                mp.start()
                Log.i(TAG, "should play ")
            }
            binder.upDateUI(openNetworks)


            Handler().postDelayed({
                wifiManager.startScan()
                Log.i(TAG, "scanning")
            }, 5000)
        }
    }


}