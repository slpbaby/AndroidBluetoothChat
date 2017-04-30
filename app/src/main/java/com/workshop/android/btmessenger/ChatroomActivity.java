package com.workshop.android.btmessenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChatroomActivity extends AppCompatActivity implements MyBluetoothListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void onDeviceFound(String device, String address) {
        // Do nothing
    }

    public void onDeviceScanComplete() {
        // Do nothing
    }

    public void onMessageReceived(final String targetAddress, final String message) {

    }

    public void onMessageSent(final String targetAddress, final String message) {

    }

    public void onConnected(String targetAddress) {

    }

    public void onDisconnected(String targetAddress) {

    }
}
