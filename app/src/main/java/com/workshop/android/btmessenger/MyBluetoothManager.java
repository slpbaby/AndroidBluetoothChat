package com.workshop.android.btmessenger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MyBluetoothManager extends BroadcastReceiver {
    private static final String TAG = "BTMANAGER";
    public static final String EXTRA_BT_ADDR = "EXT_BLUETOOTH_ADDR";

    private static final UUID MY_UUID = UUID.fromString("ba87c0d3-afac-11de-8a39-0800200c9a77");
    private static final int MAX_CONNECTION = 5;
    private static final int DISCOVER_DURATION = 60; // one minute

    // Singleton
    private static MyBluetoothManager sInstance;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<MyBluetoothListener> mListeners;
    private boolean mReceiverRegistered;

    private BtSocketAddress mBluetoothSocket;
    private ArrayList<BtSocketAddress> mBluetoothSockets;

    private ListenThread mListenThread;
    private Context mContext;

    public synchronized static MyBluetoothManager getInstance() {
        if (sInstance == null) {
            sInstance = new MyBluetoothManager();
        }

        return sInstance;
    }

    private MyBluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mListeners = new ArrayList<>();
        mReceiverRegistered = false;
        mListenThread = null;
        mContext = null;
    }

    public void enableBluetooth(Context context) {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            // Enable Bluetooth
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public boolean checkBluetoothAvailability(){
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return true;
        }
    }

    public void startDiscover() {
        if (mBluetoothAdapter == null) {
            return;
        } else {
            if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.isDiscovering()) {
                return;
            } else {
                mBluetoothAdapter.startDiscovery();
            }
        }
    }

    public void stopDiscover() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void allowDiscover(Activity activity) throws IOException {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
            activity.startActivity(discoverableIntent);
        }

        if (mListenThread == null) {
            BluetoothServerSocket serverSocket = null;

            // Create a new listening server socket
            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Service", MY_UUID);
            mListenThread = new ListenThread(serverSocket);
            mListenThread.start();
        }
    }

    public void addListener(Context context, MyBluetoothListener listener) {
        mContext = context.getApplicationContext();
        mListeners.add(listener);
        synchronized (this) {
            // register broadcast receiver if not register yet
            if (!mReceiverRegistered) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.getApplicationContext().registerReceiver(this, filter);
                filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                context.getApplicationContext().registerReceiver(this, filter);
                mReceiverRegistered = true;
            }
        }
    }

    public void removeListener(Context context, MyBluetoothListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
            synchronized (this) {
                if (mListeners.isEmpty() && mReceiverRegistered) {
                    context.getApplicationContext().unregisterReceiver(this);
                    mReceiverRegistered = false;
                }
            }
        }
    }

    public void connectDevice(String address) throws IOException {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        socket.connect();
        startSocketThread(address, socket);
    }

    public void startSocketThread(String address, BluetoothSocket socket) {
        // TODO: multi device connections

        // Start a thread to handle incoming and outgoing messages
        SocketThread socketThread = new SocketThread(address, socket);
        mBluetoothSocket = new BtSocketAddress(address, socket, socketThread);

        socketThread.start();
    }

    public void onMessageRecv(String address, String message) {
        if (mContext != null) {
            // Store conversation into db
            DBHelper dbHelper = new DBHelper(mContext);
            dbHelper.insertConversation(address, Conversation.TYPE_RECV, SystemClock.currentThreadTimeMillis(), message);
        }
        for (MyBluetoothListener listener : mListeners) {
            listener.onMessageReceived(address, message);
        }
    }

    public void onMessageSent(String address, String message) {
        if (mContext != null) {
            // Store conversation into db
            DBHelper dbHelper = new DBHelper(mContext);
            dbHelper.insertConversation(address, Conversation.TYPE_SENT, SystemClock.currentThreadTimeMillis(), message);
        }
        for (MyBluetoothListener listener : mListeners) {
            listener.onMessageSent(address, message);
        }
    }

    public void sendMessage(String address, String message) {
        if (mBluetoothSocket != null && mBluetoothSocket.mmAddress.equals(address)) {
            Log.d(TAG, "sending Message : " + message);
            mBluetoothSocket.mmThread.write(message.getBytes());
        }
    }

    public void onClientConnected(String targetAddress, BluetoothSocket socket) {
        Log.d(TAG, "on Client Connected " + targetAddress);
        startSocketThread(targetAddress, socket);
        for (MyBluetoothListener listener : mListeners) {
            listener.onConnected(targetAddress);
        }
    }

    public void disconnectClient(String address) {
        if (mBluetoothSocket != null && mBluetoothSocket.mmAddress.equals(address)) {
            mBluetoothSocket.mmThread.cancel();
        }
    }

    public void stopAll() {
        if (mBluetoothSocket != null) {
            mBluetoothSocket.mmThread.cancel();
        }

        if (mListenThread != null) {
            mListenThread.cancel();
        }
    }

    public ArrayList<String>  getPairedDevicesAddress() {
        ArrayList<String> ret = new ArrayList<>();
        if (mBluetoothAdapter == null) {
            return ret;
        }
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            ret.add(device.getName() + "\n" + device.getAddress());
        }
        return ret;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            for (MyBluetoothListener listener : mListeners) {
                listener.onDeviceFound(device.getName(), device.getAddress());
            }
        }  if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            for (MyBluetoothListener listener : mListeners) {
                listener.onDeviceScanComplete();
            }
        }
    }

    /*
    This class is used to store the bluetooth socket and address
     */
    private class BtSocketAddress {
        BluetoothSocket mmSocket;
        String mmAddress;
        SocketThread mmThread;

        BtSocketAddress(String address, BluetoothSocket socket,SocketThread socketThread) {
            mmAddress = address;
            mmSocket = socket;
            mmThread = socketThread;
        }
    }

    /*
    This thread handles all incoming and outgoing messages.
     */
    private class SocketThread extends Thread {
        private final String mmAddress;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mExit;

        public SocketThread(String address, BluetoothSocket socket) {
            mmAddress = address;
            mmSocket = socket;
            mExit = false;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                // Do something
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "Socket Thread Started : " + mmAddress + " , isConnected " + mmSocket.isConnected());
            setName("Socket Thread : " + mmAddress);
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (!mExit) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String msg = new String(buffer, 0, bytes);
                    Log.d(TAG, "Read Message : " + msg);

                    // Inform the listener
                    onMessageRecv(mmAddress, msg);
                } catch (IOException e) {
                    Log.e(TAG, "Read Failed", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                // Do something
                Log.e(TAG, "Write to target Failed", e);
            }
            String msg = new String(buffer);
            onMessageSent(mmAddress, msg);
        }

        public void cancel() {
            mExit = true;
            Log.d(TAG, "Socket Thread cancel");
            try {
                mmSocket.close();
            } catch (IOException e) {
                // Do something
            }
        }
    }

    /*
     * This thread listen for incoming connections
     */
    private class ListenThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private boolean mExit;

        public ListenThread(BluetoothServerSocket socket) {
            mmServerSocket = socket;
            mExit = false;
        }

        public void run() {
            Log.d(TAG, "Listen Thread started");
            setName("Listen Thread");

            BluetoothSocket socket = null;

            while (!mExit) {
                try {
                    Log.d(TAG, "Start Accepting");
                    socket = mmServerSocket.accept();
                    Log.d(TAG, "Accepted");
                } catch (IOException e) {
                    // Do something
                    Log.e(TAG, "FAILED to accept", e);
                }
                if (socket != null) {
                    Log.d(TAG, "Get Connected");
                    onClientConnected(socket.getRemoteDevice().getAddress(), socket);
                }
            }
        }

        public void cancel() {
            Log.d(TAG, "Listen Thread cancel");
            mExit = true;
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                // Do something
            }
        }
    }
}
