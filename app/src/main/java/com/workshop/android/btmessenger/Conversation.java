package com.workshop.android.btmessenger;

public class Conversation {
    public static final int TYPE_SENT = 0x01;
    public static final int TYPE_RECV = 0x02;

    private int mType;
    private long mTime;
    private String mStr;
    public Conversation(int type, String str, long time) {
        mType = type;
        mStr = str;
        mTime = time;
    }

    public boolean isSentMessage() {
        return TYPE_SENT == mType;
    }

    public boolean isRecvMessage() {
        return TYPE_RECV == mType;
    }

    public String getMessage() {
        return mStr;
    }

    public long getTime() {
        return mTime;
    }
}
