package zhet.wi.fi.note.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.provider.Settings.ACTION_WIFI_SETTINGS
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ListView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import zhet.wi.fi.note.R
import zhet.wi.fi.note.adapter.WifiAdapter
import zhet.wi.fi.note.binder.WifiBinder
import zhet.wi.fi.note.entity.WifiScanResult
import zhet.wi.fi.note.listener.UIUpdater
import zhet.wi.fi.note.service.WifiService


class MainActivity : AppCompatActivity(), ServiceConnection, UIUpdater {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val PERMISSION_REQUEST = 1
        private const val ACTIVITY_REQUEST_CODE_LOCATION_SETTINGS = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this, getString(R.string.app_id))
        setTitle(R.string.open_networks)
        val permissions = arrayOf(ACCESS_FINE_LOCATION)
        if (!checkPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
        } else {
            init()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    init()
                } else {
                    finish()
                }
                return
            }
        }
    }

    private fun isLocationServiceEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return gpsEnabled || networkEnabled

    }

    private fun showDialog() {
        // notify user
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(getString(R.string.gps_network_not_enabled))
        dialog.setPositiveButton(getString(R.string.open_location_settings), { _, _ ->
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(myIntent, ACTIVITY_REQUEST_CODE_LOCATION_SETTINGS)
        })
        dialog.setNegativeButton(getString(android.R.string.cancel), { _, _ ->
            finish()
        })
        dialog.show()
    }

    private fun init() {
        if (!isLocationServiceEnabled()) {
            showDialog()
        }
        setContentView(R.layout.activity_main)
        val intent = Intent(this, WifiService::class.java)
        startService(intent)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        adView.loadAd(AdRequest.Builder().build())
    }


    private fun checkPermissions(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    override fun onServiceDisconnected(name: ComponentName?) {}

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as WifiBinder
        binder.setUIUpdater(this)
    }

    override fun onUpdate(openNetworks: ArrayList<WifiScanResult>) {
        Log.i(TAG, "networks len=>${openNetworks.size}")
        if (!openNetworks.isEmpty()) {
            tvNoNets.visibility = GONE
            val wifiAdapter = WifiAdapter(openNetworks)
            list.adapter = wifiAdapter
        } else {
            tvNoNets.visibility = VISIBLE
        }
        progressBar.visibility = GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun connect(menuItem: MenuItem) {
        Log.i(TAG, "connect")
        startActivity(Intent(ACTION_WIFI_SETTINGS))
    }

    fun stop(menuItem: MenuItem) {
        Log.i(TAG, "stop")
        stopService(Intent(this, WifiService::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        unbindService(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            init()
        } else {
            Log.i(TAG, "result cancelled")
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}
