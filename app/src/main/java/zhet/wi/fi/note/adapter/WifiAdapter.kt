package zhet.wi.fi.note.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import zhet.wi.fi.note.R
import zhet.wi.fi.note.entity.WifiScanResult

class WifiAdapter(val list: List<WifiScanResult>) : BaseAdapter() {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val wifiScanResult = list[position]
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.activity_list_item, parent, false) as TextView
        view.text = wifiScanResult.SSID
        return view
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return list.size
    }
}