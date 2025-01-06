package com.example.project21;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project21.data.SQLiteDataBaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    // 在 MainActivity 中声明数据库帮助器
    private SQLiteDataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据库帮助器
        dbHelper = new SQLiteDataBaseHelper(this);
    }


    private Handler expiryCheckHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            checkExpiryDates();
            expiryCheckHandler.sendEmptyMessageDelayed(0, 24 * 60 * 60 * 1000); // 每隔一天检查一次,可以根据需要调整间隔时间
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // 開始定期檢查過期日期
        expiryCheckHandler.sendEmptyMessageDelayed(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除定期檢查過期日期的消息
        expiryCheckHandler.removeCallbacksAndMessages(null);
    }

    // 檢查過期日期的方法
    private void checkExpiryDates() {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT foodName, date FROM food", null);

            // 获取结果集的长度
            int databaseLength = cursor.getCount();

            ArrayList<String> expiredFoods = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    String foodName = cursor.getString(cursor.getColumnIndexOrThrow("foodName"));
                    String expiryDateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                    // 将日期字符串转换为日期对象，这里使用了简单的DateFormat，请根据实际情况选择合适的方式
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
                    try {
                        Date expiryDate = dateFormat.parse(expiryDateStr);
                        Date currentDate = new Date();

                        if (currentDate.after(expiryDate)) {
                            // 过期，加入集合
                            expiredFoods.add(foodName);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("MainActivity", "Error parsing date: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();

            // 如果有过期的食物，显示提醒对话框
            if (!expiredFoods.isEmpty()) {
                showExpiryListAlertDialog(expiredFoods);
            } else {
                // 没有过期的食物，直接跳转到 home.class 页面
                Intent intent = new Intent(MainActivity.this, home.class);
                startActivity(intent);
                finish(); // 如果需要结束当前页面
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error in checkExpiryDates: " + e.getMessage());
        }
    }


    // 顯示過期提醒列表的對話框
    private void showExpiryListAlertDialog(ArrayList<String> expiredFoods) {
        // 使用AlertDialog.Builder構建對話框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("食品過期提醒")
                .setMessage("以下食物已經過期,請注意檢查:\n\n" + TextUtils.join("\n", expiredFoods) + "\n\n是否前往過期/即期區確認")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 啟動 Notice Activity
                        Intent intent = new Intent(MainActivity.this, Notice.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("回首頁", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 啟動 Notice Activity
                        Intent intent = new Intent(MainActivity.this, home.class);
                        startActivity(intent);
                    }
                })
                .show();
    }
}
