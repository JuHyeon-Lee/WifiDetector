package com.inha.wifidetector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiListViewAdapter extends BaseAdapter {

    ArrayList<WifiData> wifiData;

    WifiListViewAdapter(ArrayList<WifiData> wifiData){
        this.wifiData = wifiData;
    }

    @Override
    public int getCount() {
        return wifiData.size();
    }

    @Override
    public Object getItem(int i) {
        return wifiData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listviewitem_wifidata, viewGroup, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView tv_room = convertView.findViewById(R.id.textview_room);
        TextView tv_corner = convertView.findViewById(R.id.textview_corner);
        TextView tv_rssi = convertView.findViewById(R.id.textview_rssi);
        TextView tv_time = convertView.findViewById(R.id.textview_time);

        tv_room.setText(wifiData.get(i).room);
        tv_corner.setText(Integer.toString(wifiData.get(i).corner));
        tv_rssi.setText(Integer.toString(wifiData.get(i).RSSI));
        tv_time.setText(wifiData.get(i).time);

        return convertView;
    }
}
