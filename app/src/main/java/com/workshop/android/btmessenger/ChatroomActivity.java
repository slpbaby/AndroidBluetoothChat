package com.workshop.android.btmessenger;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class ChatroomActivity extends AppCompatActivity implements MyBluetoothListener {
    private MyBluetoothManager mBtManager;
    private String mAddress, mUsername, mDeviceName;
    private TextView mConversation;
    private Button mSendBtn;
    private EditText mMessageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mBtManager = MyBluetoothManager.getInstance();

        Bundle bundle = getIntent().getExtras();
        mAddress = bundle.getString(MyBluetoothManager.EXTRA_BT_ADDR, "");
        if (mAddress.isEmpty()) {
            finish();
        }

        TextView targetName = (TextView) findViewById(R.id.target_username);
        targetName.setText(mAddress);

        mConversation = (TextView) findViewById(R.id.conversation_textview);
        mConversation.setText("");

        mSendBtn = (Button)findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(mSendMesg);

        mMessageEditText = (EditText)findViewById(R.id.mesg_edit_text);

        mBtManager.addListener(this.getApplicationContext(), this);
    }


    public void onDeviceFound(String device, String address) {
        // Do nothing
    }

    public void onDeviceScanComplete() {
        // Do nothing
    }

    public void onMessageReceived(final String targetAddress, final String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mAddress.equals(targetAddress)) {
                String msg = "Recv: " + message + "\n";
                mConversation.append(msg);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onMessageReceived(targetAddress, message);
                }
            });
        }
    }

    public void onMessageSent(final String targetAddress, final String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mAddress.equals(targetAddress)) {
                String msg = "Sent: " + message + "\n";
                mConversation.append(msg);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onMessageSent(targetAddress, message);
                }
            });
        }
    }

    public void onConnected(String targetAddress) {

    }

    public void onDisconnected(String targetAddress) {

    }

    private View.OnClickListener mSendMesg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String mesg = mMessageEditText.getText().toString();
            Log.d("BTMANAGER", "Trying to send message : " + mesg);
            if (mesg.isEmpty()) {
                return;
            }
            // Clear the edit text
            mMessageEditText.setText("");
            // Send the message
            mBtManager.sendMessage(mAddress, mesg);
        }
    };
}
