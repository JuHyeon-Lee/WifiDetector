package com.inha.wifidetector;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LocationManager lm;
    WifiData wifiData;
    ArrayList<WifiData> list_wifiData;
    WifiListViewAdapter wifiListViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiData = new WifiData();

        // 리스트뷰 생성
        list_wifiData = new ArrayList<>();
        final ListView listview_wifidata = findViewById(R.id.listview_wifidata);
        wifiListViewAdapter = new WifiListViewAdapter(list_wifiData);
        listview_wifidata.setAdapter(wifiListViewAdapter);
        listview_wifidata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.dialog_wifidata, null);

                TextView tv_room = v.findViewById(R.id.tv_room);
                TextView tv_time = v.findViewById(R.id.tv_time);
                TextView tv_rssi = v.findViewById(R.id.tv_wifi);
                TextView tv_lat = v.findViewById(R.id.tv_lat);
                TextView tv_lon = v.findViewById(R.id.tv_lon);
                TextView tv_MAC = v.findViewById(R.id.tv_mac);
                TextView tv_APMAC = v.findViewById(R.id.tv_apmac);

                final WifiData tmp = list_wifiData.get(i);
                tv_room.setText("ROOM: "+tmp.room+" / "+Integer.toString(tmp.corner));
                tv_time.setText("TIME: "+tmp.time);
                tv_rssi.setText("RSSI: "+Integer.toString(tmp.RSSI));
                tv_lat.setText("LAT: "+Double.toString(tmp.latitude));
                tv_lon.setText("LON: "+Double.toString(tmp.longitude));
                tv_MAC.setText("MAC: "+tmp.MAC);
                tv_APMAC.setText("AP MAC: "+tmp.AP_MAC);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(v);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("삭제하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        list_wifiData.remove(i);
                        wifiListViewAdapter.notifyDataSetChanged();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        // BroadcastReceiver 등록
        registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        // MAC 주소 가져오기
        String MAC = getMACAddress("wlan0");
        wifiData.MAC = MAC;
        TextView tvMAC = findViewById(R.id.tv_mac);
        tvMAC.setText("MAC: " + MAC);

        // 위치 권한 받아오기
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }

        // 위치 정보 수신
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);

        // 버튼 클릭 시 리스트뷰 추가 (하단 onClickListener)
        Button button1 = findViewById(R.id.btn_corner1);
        Button button2 = findViewById(R.id.btn_corner2);
        Button button3 = findViewById(R.id.btn_corner3);
        Button button4 = findViewById(R.id.btn_corner4);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                // 권한 허용하면 LocationManager 업데이트
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // BroadcastReceiver 해제
        unregisterReceiver(rssiReceiver);
    }

    private BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            WifiManager wman = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wman.getConnectionInfo();

            // Wifi세기(RSSI) 가져오기
            int RSSI = info.getRssi();
            wifiData.RSSI = RSSI;
            TextView tvRSSI = findViewById(R.id.tv_wifi);
            tvRSSI.setText("RSSI: "+ RSSI + " dBm");

            // AP MAC 주소 가져오기
            String macADD = info.getBSSID();
            wifiData.AP_MAC = macADD;
            TextView tvAPMAC = findViewById(R.id.tv_apmac);
            tvAPMAC.setText("AP MAC: "+macADD);

        }
    };

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    private final LocationListener mLocationListener = new LocationListener() {
        // 위치 정보가 바뀔 때마다 업데이트
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            wifiData.latitude = latitude;
            wifiData.longitude = longitude;

            TextView lat = findViewById(R.id.tv_lat);
            TextView lon = findViewById(R.id.tv_lon);
            lat.setText("LAT: "+Double.toString(latitude));
            lon.setText("LON: "+Double.toString(longitude));
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btn_corner1 ||
                view.getId() == R.id.btn_corner2 ||
                view.getId() == R.id.btn_corner3 ||
                view.getId() == R.id.btn_corner4) {

            // wifiData 객체 복사
            WifiData tmp = wifiData.clone();

            // 시간 설정
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            tmp.time = sdf.format(date);

            // 강의실 설정
            EditText room = findViewById(R.id.editText_room);
            tmp.room = room.getText().toString();

            // 코너 설정
            if (view.getId() == R.id.btn_corner1)
                tmp.corner = 1;
            if (view.getId() == R.id.btn_corner2)
                tmp.corner = 2;
            if (view.getId() == R.id.btn_corner3)
                tmp.corner = 3;
            if (view.getId() == R.id.btn_corner4)
                tmp.corner = 4;

            // 리스트뷰에 추가 후 새로고침
            list_wifiData.add(tmp);
            wifiListViewAdapter.notifyDataSetChanged();

        }

    }
}
