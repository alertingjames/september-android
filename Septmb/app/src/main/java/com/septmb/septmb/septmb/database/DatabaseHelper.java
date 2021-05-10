package com.septmb.septmb.septmb.database;

/**
 * Created by a on 5/14/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "USERS";

    // Table columns
    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PHOTO = "photo";
    public static final String PHONE = "phone";
    public static final String MESSAGES = "messages";

    // Database Information
    static final String DB_NAME = "CHAT_USERS";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT NOT NULL, " + EMAIL + " TEXT NOT NULL, " + PHOTO + " TEXT NOT NULL, " + PHONE + " TEXT NOT NULL, " + MESSAGES  + " TEXT NOT NULL);";

    // Chat History Table Name
    public static final String TABLE_NAME2 = "CHAT";

    // Table columns
    public static final String _CHID = "_chid";
    public static final String IMAGE = "image";
    public static final String ISTYPING = "istyping";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String MESSAGE = "message";
    public static final String ONLINE = "online";
    public static final String TIME = "time";
    public static final String USER = "user";
    public static final String VIDEO = "video";

    // Creating table2 query
    private static final String CREATE_TABLE2 = "create table " + TABLE_NAME2 + "(" + _CHID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + IMAGE + " TEXT NOT NULL, " + ISTYPING + " TEXT NOT NULL, " + LAT + " TEXT NOT NULL, "+
            LON + " TEXT NOT NULL, "+ MESSAGE + " TEXT NOT NULL, "+ ONLINE + " TEXT NOT NULL, "+ TIME + " TEXT NOT NULL, "+ USER + " TEXT NOT NULL, " + VIDEO  + " TEXT NOT NULL);";


    // Chat Secret Table Name
    public static final String TABLE_NAME3 = "MSG_USERS";

    // Table columns
    public static final String M_ID = "_id";
    public static final String MNAME = "name";
    public static final String MEMAIL = "email";
    public static final String MPHOTO = "photo";
    public static final String MSG = "msg";
    public static final String DATE = "date";

    // Creating table2 query
    private static final String CREATE_TABLE3 = "create table " + TABLE_NAME3+ "(" + M_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MNAME + " TEXT NOT NULL, " + MEMAIL + " TEXT NOT NULL, " + MPHOTO + " TEXT NOT NULL, "+ MSG  + " TEXT NOT NULL, " + DATE + " TEXT NOT NULL);";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE2);
        db.execSQL(CREATE_TABLE3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        onCreate(db);
    }
}

