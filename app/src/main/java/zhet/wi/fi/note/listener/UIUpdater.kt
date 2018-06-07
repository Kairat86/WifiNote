package zhet.wi.fi.note.listener

import zhet.wi.fi.note.entity.WifiScanResult

interface UIUpdater {

    fun onUpdate(openNetworks: ArrayList<WifiScanResult>)
}
