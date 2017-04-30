package com.workshop.android.btmessenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements MyBluetoothListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onDeviceFound(String device, String address) {

    }

    public void onDeviceScanComplete() {

    }

    public void onMessageReceived(String targetAddress, String message) {
        // Do nothing
    }

    public void onMessageSent(String targetAddress, String message) {
        // Do nothing
    }

    public void onConnected(String targetAddress) {

    }

    public void onDisconnected(String targetAddress) {

    }
}
