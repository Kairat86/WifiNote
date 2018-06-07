package zhet.wi.fi.note.entity

class WifiScanResult(val SSID: String, val caps: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WifiScanResult
        if (caps != other.caps) return false
        return true
    }

    override fun hashCode(): Int {
        return caps.hashCode()
    }
}