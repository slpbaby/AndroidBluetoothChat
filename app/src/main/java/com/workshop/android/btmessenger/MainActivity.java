package com.workshop.android.btmessenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MyBluetoothListener {

    private ArrayAdapter<String> mNewDevicesAdapter;
    private MyBluetoothManager mBtManager;

    private Button mStartScanBtn, mStopScanBtn, mAllowDiscoverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNewDevicesAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        ListView newDeviceList = (ListView)findViewById(R.id.new_devices_list);
        newDeviceList.setAdapter(mNewDevicesAdapter);
        newDeviceList.setOnItemClickListener(mDeviceClickListener);

        mBtManager = MyBluetoothManager.getInstance();

        mStartScanBtn = (Button)findViewById(R.id.start_scan_btn);
        if (!mBtManager.checkBluetoothAvailability()) {
            // Disable the start scan button because bluetooth is not available
            mStartScanBtn.setEnabled(false);
        }
        mStopScanBtn = (Button)findViewById(R.id.stop_scan_btn);
        mStopScanBtn.setEnabled(false);

        mAllowDiscoverBtn = (Button)findViewById(R.id.allow_discover_btn);

        mStartScanBtn.setOnClickListener(mStartScan);
        mStopScanBtn.setOnClickListener(mStopScan);
        mAllowDiscoverBtn.setOnClickListener(mAllowDiscover);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBtManager.addListener(this.getApplicationContext(), this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mBtManager.stopDiscover();
        //mBtManager.removeListener(this.getApplicationContext(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBtManager.stopDiscover();
        mBtManager.removeListener(this.getApplicationContext(), this);
        //mBtManager.stopAll();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            try {
                mBtManager.connectDevice(address);
            } catch (IOException e) {
                // Do something
                Log.d("BTMANAGER", "Failed to connect Device", e);
            }
            startChat(address);
        }
    };

    public void onDeviceFound(String device, String address) {
        mNewDevicesAdapter.add(device + "\n" + address);
    }

    public void onDeviceScanComplete() {
        mStopScanBtn.setEnabled(false);
        mStartScanBtn.setEnabled(true);
    }

    public void onMessageReceived(String targetAddress, String message) {
        // Do nothing
    }

    public void onMessageSent(String targetAddress, String message) {
        // Do nothing
    }

    public void onConnected(String targetAddress) {
        Log.d("BTMANAGER", "MainActivity onConnected : " + targetAddress);
        startChat(targetAddress);
    }

    public void onDisconnected(String targetAddress) {

    }

    private void startChat(final String targetAddress) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.d("BTMANAGER", "start Chat in UI Thread");
            Intent intent = new Intent(MainActivity.this, ChatroomActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(MyBluetoothManager.EXTRA_BT_ADDR, targetAddress);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Log.d("BTMANAGER", "Try to run on UI Thread");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startChat(targetAddress);
                }
            });
        }
    }

    private View.OnClickListener mStartScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mNewDevicesAdapter.clear();
            MyBluetoothManager manager = MyBluetoothManager.getInstance();
            manager.startDiscover();
            mStartScanBtn.setEnabled(false);
            mStopScanBtn.setEnabled(true);
        }
    };

    private View.OnClickListener mStopScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MyBluetoothManager manager = MyBluetoothManager.getInstance();
            manager.stopDiscover();
            mStopScanBtn.setEnabled(false);
            mStartScanBtn.setEnabled(true);
        }
    };

    private View.OnClickListener mAllowDiscover = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MyBluetoothManager manager = MyBluetoothManager.getInstance();
            try {
                manager.allowDiscover(MainActivity.this);
            } catch (IOException e) {
                // do something
            }
        }
    };
}
