package com.workshop.android.btmessenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "BluetoothChat.db";
    private static final String TABLE_NAME_USER = "user";
    private static final String TABLE_NAME_CONVERSATION = "conversation";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_MESSAGE_TYPE = "type";

    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_NAME_USER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME +  " TEXT NOT NULL, " +
                    COLUMN_ADDRESS + " TEXT NOT NULL)";
    private static final String CREATE_TABLE_CONVERSATION =
            "CREATE TABLE " + TABLE_NAME_CONVERSATION + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ADDRESS + " TEXT NOT NULL, " +
                    COLUMN_MESSAGE_TYPE + " INTEGER NOT NULL, " +
                    COLUMN_TIME + " INTEGER NOT NULL, " +
                    COLUMN_MESSAGE + " TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USER);
        sqLiteDatabase.execSQL(CREATE_TABLE_CONVERSATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONVERSATION);
        onCreate(sqLiteDatabase);
    }

    public boolean insertUser(String name, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_ADDRESS, address);
        db.insert(TABLE_NAME_USER, null, contentValues);
        return true;
    }

    public boolean insertConversation(String address, int type, long time, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ADDRESS, address);
        contentValues.put(COLUMN_MESSAGE_TYPE, type);
        contentValues.put(COLUMN_TIME, time);
        contentValues.put(COLUMN_MESSAGE, message);
        db.insert(TABLE_NAME_CONVERSATION, null, contentValues);
        return true;
    }

    public ContactProfile getProfileByAddress(String address) {
        ContactProfile ret;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME_USER + " where "
                + COLUMN_ADDRESS + "=" + address, null);
        if (res.getCount() <= 0) {
            return null;
        }

        String username = res.getString(res.getColumnIndex(COLUMN_NAME));
        ret = new ContactProfile(address, username, username);
        ret.mConversation = getConversationsByAddress(address);
        res.close();
        return ret;
    }

    public ArrayList<Conversation> getConversationsByAddress(String address) {
        ArrayList<Conversation> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + TABLE_NAME_CONVERSATION + " where "
                + COLUMN_ADDRESS + "=" + address, null);

        res.moveToFirst();
        while (!res.isAfterLast()) {
            int type = res.getInt(res.getColumnIndex(COLUMN_MESSAGE_TYPE));
            String msg = res.getString(res.getColumnIndex(COLUMN_MESSAGE));
            long time = res.getLong(res.getColumnIndex(COLUMN_TIME));
            ret.add(new Conversation(type, msg, time));
            res.moveToNext();
        }

        return ret;
    }
}
