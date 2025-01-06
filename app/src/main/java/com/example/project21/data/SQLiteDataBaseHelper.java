package com.example.project21.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "foodReminder";
    private static final int DATABASE_VERSION = 1;

    // User table
    private static final String CREATE_TABLE_USER = "CREATE TABLE user ("
            + "userId INTEGER PRIMARY KEY AUTOINCREMENT"
            + ");";

    // Food table
    private static final String CREATE_TABLE_FOOD = "CREATE TABLE food ("
            + "foodId INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userId INTEGER,"
            + "foodName TEXT,"
            + "quantity INTEGER,"
            + "date DATE,"
            + "classify TEXT,"
            + "FOREIGN KEY (userId) REFERENCES user(userId)"
            + ");";

    public SQLiteDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_FOOD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS food");
        onCreate(db);
    }
}
