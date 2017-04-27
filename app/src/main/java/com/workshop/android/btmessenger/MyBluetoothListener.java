package com.workshop.android.btmessenger;

public interface MyBluetoothListener {
    void onDeviceFound(String device, String address);
    void onDeviceScanComplete();
    void onMessageReceived(String targetAddress, String message);
    void onMessageSent(String targetAddress, String message);
    void onConnected(String targetAddress);
    void onDisconnected(String targetAddress);
}
