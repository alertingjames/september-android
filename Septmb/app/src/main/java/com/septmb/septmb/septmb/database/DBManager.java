package com.septmb.septmb.septmb.database;

/**
 * Created by a on 5/14/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.septmb.septmb.septmb.models.ChatEntity;
import com.septmb.septmb.septmb.models.UserEntity;

import java.util.ArrayList;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String email, String photo, String phone, String messages) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.NAME, name);
        contentValue.put(DatabaseHelper.EMAIL, email);
        contentValue.put(DatabaseHelper.PHOTO, photo);
        contentValue.put(DatabaseHelper.PHONE, phone);
        contentValue.put(DatabaseHelper.MESSAGES, messages);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    //TABLE2
    public void insert2(String image, String istyping, String lat, String lon, String message, String online, String time, String user, String video) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.IMAGE, image);
        contentValue.put(DatabaseHelper.ISTYPING, istyping);
        contentValue.put(DatabaseHelper.LAT, lat);
        contentValue.put(DatabaseHelper.LON, lon);
        contentValue.put(DatabaseHelper.MESSAGE, message);
        contentValue.put(DatabaseHelper.ONLINE, online);
        contentValue.put(DatabaseHelper.TIME, time);
        contentValue.put(DatabaseHelper.USER, user);
        contentValue.put(DatabaseHelper.VIDEO, video);
        database.insert(DatabaseHelper.TABLE_NAME2, null, contentValue);
    }

    //TABLE3
    public void insert3(String name, String email, String photo, String messages, String date) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.MNAME, name);
        contentValue.put(DatabaseHelper.MEMAIL, email);
        contentValue.put(DatabaseHelper.MPHOTO, photo);
        contentValue.put(DatabaseHelper.MSG, messages);
        contentValue.put(DatabaseHelper.DATE, date);
        database.insert(DatabaseHelper.TABLE_NAME3, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.EMAIL, DatabaseHelper.PHOTO };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String email, String photo, String messages) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NAME, name);
        contentValues.put(DatabaseHelper.EMAIL, email);
        contentValues.put(DatabaseHelper.PHOTO, photo);
        contentValues.put(DatabaseHelper.MESSAGES, messages);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }

    //TABLE2
    public void delete2(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME2, DatabaseHelper._CHID + "=" + _id, null);
    }
    //TABLE3
    public void delete3(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME3, DatabaseHelper.M_ID + "=" + _id, null);
    }

    public ArrayList<UserEntity> getAllMembers() {

        ArrayList<UserEntity> userEntities = new ArrayList<>();

        String[] columns = new String[] {
                DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.EMAIL, DatabaseHelper.PHOTO, DatabaseHelper.PHONE, DatabaseHelper.MESSAGES};

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null,null);

        if (cursor.moveToFirst()) {

            do {
                UserEntity user = new UserEntity();
                user.setIdx(cursor.getString(0));
                user.setName(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPhotoUrl(cursor.getString(3));
                user.setPhone(cursor.getString(4));
                user.setMessages(cursor.getString(5));
                userEntities.add(0,user);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return userEntities;
    }

    //TABLE2
    public ArrayList<ChatEntity> getChatEntities() {

        ArrayList<ChatEntity> chatEntities = new ArrayList<>();

        String[] columns = new String[] {
                DatabaseHelper._CHID, DatabaseHelper.IMAGE, DatabaseHelper.ISTYPING,DatabaseHelper.LAT, DatabaseHelper.LON,
                DatabaseHelper.MESSAGE, DatabaseHelper.ONLINE,DatabaseHelper.TIME, DatabaseHelper.USER, DatabaseHelper.VIDEO};

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME2, columns, null, null, null, null, null,null);

        if (cursor.moveToFirst()) {

            do {
                ChatEntity chatEntity = new ChatEntity();
                chatEntity.set_chid(Integer.parseInt(cursor.getString(0)));
                chatEntity.setImage(cursor.getString(1));
                chatEntity.setIstyping(cursor.getString(2));
                chatEntity.setLat(cursor.getString(3));
                chatEntity.setLon(cursor.getString(4));
                chatEntity.setMessage(cursor.getString(5));
                chatEntity.setOnline(cursor.getString(6));
                chatEntity.setTime(cursor.getString(7));
                chatEntity.setUser(cursor.getString(8));
                chatEntity.setVideo(cursor.getString(9));

                chatEntities.add(0,chatEntity);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return chatEntities;
    }

    //TABLE2
    public ArrayList<UserEntity> getNotiUsers() {

        ArrayList<UserEntity> userEntities = new ArrayList<>();

        String[] columns = new String[] {
                DatabaseHelper.M_ID, DatabaseHelper.MNAME, DatabaseHelper.MEMAIL,DatabaseHelper.MPHOTO, DatabaseHelper.MSG, DatabaseHelper.DATE};

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME3, columns, null, null, null, null, null,null);

        if (cursor.moveToFirst()) {

            do {
                UserEntity user = new UserEntity();
                user.setIdx(cursor.getString(0));
                user.setName(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPhotoUrl(cursor.getString(3));
                user.setMessages(cursor.getString(4));
                user.setDate(cursor.getString(5));
                userEntities.add(0,user);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return userEntities;
    }

}

