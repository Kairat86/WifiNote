package zhet.wi.fi.note.binder

import android.os.Binder
import zhet.wi.fi.note.entity.WifiScanResult
import zhet.wi.fi.note.listener.UIUpdater

class WifiBinder : Binder() {

    private var listener: UIUpdater? = null

    fun setUIUpdater(listener: UIUpdater) {
        this.listener = listener
    }

    fun upDateUI(openNetworks: ArrayList<WifiScanResult>) {
        listener?.onUpdate(openNetworks)
    }
}