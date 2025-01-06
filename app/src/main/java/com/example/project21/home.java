package com.example.project21;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.project21.data.SQLiteDataBaseHelper;
import com.facebook.stetho.Stetho;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class home extends AppCompatActivity {

    String Name = "";

    private EditText txtName, txtDate, txtQuantity;
    private DatePickerDialog.OnDateSetListener datePicker;
    private Calendar calendar = Calendar.getInstance();

    // 在 MainActivity 中声明数据库帮助器
    private SQLiteDataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化数据库帮助器
        dbHelper = new SQLiteDataBaseHelper(this);

        txtName = findViewById(R.id.txtName);
        txtDate = findViewById(R.id.txtClassify);
        txtQuantity = findViewById(R.id.txtQuantity);

        Spinner spCategory = findViewById(R.id.spCategory);

        Stetho.initializeWithDefaults(this);//設置資料庫監視

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Name = bundle.getString("name");
            if (Name != null) {
                txtName.setText(Name);
            }
        }

        //手動選擇日期
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this,
                R.array.test, android.R.layout.simple_dropdown_item_1line);

        // 将ArrayAdapter转换为可变列表
        ArrayList<CharSequence> categoryList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.category_options)));

        // 将修改后的列表转换回数组,并将其设置到适配器上
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryList.toArray(new CharSequence[0]));
        spCategory.setAdapter(adapter1);

        // 設定手動選擇日期按鈕的點擊事件
        datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // 获取当前日期
                Calendar todayCalendar = Calendar.getInstance();

                // 将用户选择的日期转换为 Date 对象
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, day);
                Date selectedDate = selectedCalendar.getTime();

                // 检查选择的日期是否在当天之前
                if (selectedDate.before(todayCalendar.getTime())) {
                    // 提示用户重新选择
                    Toast.makeText(home.this, "不能选择当天或之前的日期", Toast.LENGTH_LONG).show();
                    return;
                }

                // 更新选择的日期到界面上的 EditText
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                String myFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.TAIWAN);
                txtDate.setText(sdf.format(calendar.getTime()));
            }
        };
    }

    // 插入資料到資料庫
    public void insertData(String name, String date, int Quantity, String category) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase(); // 使用 dbHelper 获取数据库对象
            if (db != null) {
                ContentValues values = new ContentValues();
                values.put("foodName", name);
                values.put("date", date);
                values.put("quantity", Quantity);
                values.put("classify", category);

                long result = db.insert("food", null, values);

                if (result == -1) {
                    Log.e("DatabaseInsertError", "Error inserting data into database");
                } else {
                    Log.d("DatabaseInsertSuccess", "Data inserted successfully");
                }
                db.close();
            } else {
                Log.e("DatabaseError", "Unable to get writable database");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DatabaseError", "Error in insertData: " + e.getMessage());
        }
    }

    public void btnDate_Click(View view) {
        // 获取明天的日期
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowTimeInMillis = tomorrowCalendar.getTimeInMillis();

        DatePickerDialog dialog = new DatePickerDialog(home.this,
                datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // 设置最小日期为明天
        dialog.getDatePicker().setMinDate(tomorrowTimeInMillis);

        dialog.show();
    }

    public void btnCreate_Click(View view) {
        try {
            // 获取用户输入的数据
            String name = txtName.getText().toString().trim();
            String date = txtDate.getText().toString().trim();
            int quantity = Integer.parseInt(txtQuantity.getText().toString().trim());
            String category = ((Spinner) findViewById(R.id.spCategory)).getSelectedItem().toString().trim();

            // 插入数据到数据库
            insertData(name, date, quantity, category);

            // 启动 List 页面并传递数据
            Intent intent = new Intent(this, List.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ButtonClickError", "Error in btnCreate_Click: " + e.getMessage());
        }
    }

    public void btnScan_Click(View view) {
        Intent intent = new Intent(this, Scanner.class);
        startActivity(intent);
    }

    public void btnClear_Click(View view) {
        // 清除EditText内容
        txtName.setText("");
        txtDate.setText("");
        txtQuantity.setText("");

        // 清除Spinner选中项
        Spinner spCategory = findViewById(R.id.spCategory);
        spCategory.setSelection(0); // 如果有默认项的话,可以设置为默认项的位置
    }

    //右上角選單
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.FoodList) {
            Intent intent = new Intent(this, List.class);
            startActivity(intent);
        } else if (id == R.id.notice) {
            Intent intent = new Intent(this, Notice.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}