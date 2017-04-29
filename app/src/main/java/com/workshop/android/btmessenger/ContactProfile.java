package com.workshop.android.btmessenger;

import java.util.ArrayList;

public class ContactProfile {
    public String mAddress, mDeviceName, mUserName;
    public ArrayList<Conversation> mConversation;

    public ContactProfile(String address, String deviceName, String userName) {
        mAddress = address;
        mDeviceName = deviceName;
        mUserName = userName;
        mConversation = new ArrayList<>();
    }

    public void addSentMessage(int time, String msg) {
        addMessage(Conversation.TYPE_SENT, time, msg);
    }

    public void addRecvMessage(int time, String msg) {
        addMessage(Conversation.TYPE_RECV, time, msg);
    }

    private void addMessage(int type, long time, String msg) {
        mConversation.add(new Conversation(type, msg, time));
    }
}

