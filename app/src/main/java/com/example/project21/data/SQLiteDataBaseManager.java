package com.example.project21.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SQLiteDataBaseManager {
    private SQLiteDataBaseHelper dbHelper;
    private SQLiteDatabase database;

    public SQLiteDataBaseManager(Context context) {
        dbHelper = new SQLiteDataBaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // 插入用户数据
    public long insertUser() {
        ContentValues values = new ContentValues();
        // 不需要为userId赋值，因为它是自增的
        return database.insert("user", null, values);
    }

    // 插入食物数据
    public long insertFood(int userId, String foodName, int quantity, String date, String classify) {
        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("foodName", foodName);
        values.put("quantity", quantity);
        values.put("date", date);
        values.put("classify", classify);


        // 插入数据
        return database.insert("food", null, values);
    }

    // 查詢過期的食物
    public Cursor getExpiredFoods() {
        String currentDate = getCurrentDate(); // 實作 getCurrentDate() 方法
        String query = "SELECT * FROM food WHERE date < ?;";
        return database.rawQuery(query, new String[]{currentDate});
    }

    // 查詢即將在未來3天內到期的食物
    public Cursor getNearExpiryFoods() {
        String currentDate = getCurrentDate();
        String nearExpiryDate = calculateNearExpiryDate(currentDate); // 實作 calculateNearExpiryDate() 方法
        String query = "SELECT * FROM food WHERE date < ?;";
        return database.rawQuery(query, new String[]{nearExpiryDate});
    }
    // 輔助方法以獲取當前日期
    private String getCurrentDate() {
        // 使用 SimpleDateFormat 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date()); // 返回當前日期
    }

    // 輔助方法以計算即將到期的日期
    private String calculateNearExpiryDate(String currentDate) {
        try {
            // 使用 SimpleDateFormat 解析當前日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(currentDate);

            // 使用 Calendar 添加三天
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, 3);

            // 返回計算後的日期
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return currentDate; // 發生異常時返回原日期
        }
    }
    // 其他数据库操作方法可以根据需求添加
}
