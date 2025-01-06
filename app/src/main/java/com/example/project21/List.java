package com.example.project21;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class List extends AppCompatActivity {
    // 宣告類別變數
    RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    SQLiteDatabase db;

    String[] Name;
    int[] Quantity;
    String[] ExpiryDate;
    String[] Classify;
    int num;
    private String selectedDate = "";
    private View dialogView;
    private RadioGroup edtClassify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 初始化並設定用於分類過濾的下拉式選單
        Spinner spList = findViewById(R.id.spList);
        ArrayAdapter adapterCategory = ArrayAdapter.createFromResource(this,
                R.array.test, android.R.layout.simple_dropdown_item_1line);
        spList.setAdapter(adapterCategory);

        spList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                filterDataByCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // 初始化資料庫並檢索資料
        db = openOrCreateDatabase("foodReminder", MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT foodName,quantity, date, classify FROM food", null);

        // 使用資料庫資料填充陣列
        Name = new String[cursor.getCount()];
        Quantity = new int[cursor.getCount()];
        ExpiryDate = new String[cursor.getCount()];
        Classify = new String[cursor.getCount()];

        num = 0;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Name[num] = cursor.getString(0);
                Quantity[num] = cursor.getInt(1);
                ExpiryDate[num] = cursor.getString(2);
                Classify[num] = cursor.getString(3);
                num++;
            }

            // 使用資料庫資料填充 arrayList 以供 RecyclerView 使用
            for (int i = 0; i < num; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Name", Name[i]);
                hashMap.put("Quantity", String.valueOf(Quantity[i]));
                hashMap.put("Date", ExpiryDate[i]);
                hashMap.put("Classify", Classify[i]);
                arrayList.add(hashMap);
            }

            // 設定 RecyclerView 和適配器
            mRecyclerView = findViewById(R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            myListAdapter = new MyListAdapter();
            mRecyclerView.setAdapter(myListAdapter);

            // 根據資料是否存在，顯示或隱藏視圖
            if (arrayList.size() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                findViewById(R.id.txtDelete).setVisibility(View.VISIBLE);
                findViewById(R.id.emptyDatabaseTextView).setVisibility(View.INVISIBLE);
            } else {
                findViewById(R.id.txtDelete).setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                findViewById(R.id.emptyDatabaseTextView).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.txtDelete).setVisibility(View.INVISIBLE);
            Toast.makeText(this, "資料庫為空", Toast.LENGTH_SHORT).show();
            findViewById(R.id.emptyDatabaseTextView).setVisibility(View.VISIBLE);
        }
    }

    // 內部類別 MyListAdapter 用於 RecyclerView 的適配器
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

        // ViewHolder 類別,用於保留項目的視圖元件
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView txtTitle, txtQuantity, txtDate, txtClassify;
            private ImageView btnMinus, btnPlus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtName);
                txtQuantity = itemView.findViewById(R.id.txtQuantity);
                txtDate = itemView.findViewById(R.id.txtDate);
                txtClassify = itemView.findViewById(R.id.txtClassify);

                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnPlus = itemView.findViewById(R.id.btnPlus);

                btnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decrementQuantity(getAdapterPosition());
                    }
                });

                btnPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        incrementQuantity(getAdapterPosition());
                    }
                });
            }
            @NonNull
            //@Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false);
                return new ViewHolder(view);
            }

            //@Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.txtTitle.setText(arrayList.get(position).get("Name"));
                holder.txtQuantity.setText(arrayList.get(position).get("Quantity"));
                holder.txtDate.setText(arrayList.get(position).get("Date"));
                holder.txtClassify.setText(arrayList.get(position).get("Classify"));

                if (isDateExpired(arrayList.get(position).get("Date"))) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#F1E5E3"));
                } else {
                    holder.itemView.setBackgroundColor(Color.parseColor("#F1F1F1"));
                }
            }

            //@Override
            public int getItemCount() {
                return arrayList.size();
            }
        }

        private void decrementQuantity(int position) {
            int currentQuantity = Integer.parseInt(arrayList.get(position).get("Quantity"));
            if (currentQuantity <= 1) {
                Toast.makeText(List.this, "數量不能小於0，請使用刪除按鈕", Toast.LENGTH_SHORT).show();
                return;
            }

            currentQuantity--;
            updateQuantity(position, currentQuantity);
        }

        private void incrementQuantity(int position) {
            int currentQuantity = Integer.parseInt(arrayList.get(position).get("Quantity"));
            currentQuantity++;
            updateQuantity(position, currentQuantity);
        }

        private void updateQuantity(int position, int newQuantity) {
            //更新資料庫
            SQLiteDatabase db = openOrCreateDatabase("foodReminder", MODE_PRIVATE, null);
            db.execSQL("UPDATE food SET quantity = ? WHERE foodName = ?",
                    new String[]{String.valueOf(newQuantity), arrayList.get(position).get("Name")});
            db.close();

            arrayList.get(position).put("Quantity", String.valueOf(newQuantity));
            notifyItemChanged(position);

            //重新開啟List頁面
            Intent intent = new Intent(List.this, List.class);
            startActivity(intent);
            finish();
        }

        private boolean isDateExpired(String dateString) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date currentDate = new Date();
                Date expiryDate = sdf.parse(dateString);

                // 判斷是否過期，如果當前日期大於過期日期，則表示已過期
                return currentDate.after(expiryDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean isDateExpiring(String dateString) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date currentDate = new Date();
                Date expiryDate = sdf.parse(dateString);

                // 計算過期日期的前三天日期
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(expiryDate);
                calendar.add(Calendar.DAY_OF_MONTH, -3);
                Date threeDaysBeforeExpiry = calendar.getTime();

                // 判斷是否即將過期，如果當前日期在過期日期的前三天之內，則表示即將過期
                return currentDate.after(threeDaysBeforeExpiry) && currentDate.before(expiryDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 創建並返回 ViewHolder 實例
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // 將資料綁定到 ViewHolder 中的元件上
            holder.txtTitle.setText(arrayList.get(position).get("Name"));
            holder.txtQuantity.setText(arrayList.get(position).get("Quantity"));
            holder.txtDate.setText(arrayList.get(position).get("Date"));
            holder.txtClassify.setText(arrayList.get(position).get("Classify"));

            // 判斷日期是否過期，如果過期則更改背景顏色
            if (isDateExpired(arrayList.get(position).get("Date"))) {
                holder.itemView.setBackgroundColor(Color.parseColor("#F1E5E3"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#F1F1F1"));
            }
        }

        @Override
        public int getItemCount() {
            // 返回項目的數量
            return arrayList.size();
        }
    }

    private void filterDataByCategory(String selectedCategory) {

        if (mRecyclerView == null) {
            return;
        }
        arrayList.clear();

        for (int i = 0; i < num; i++) {
            if ("所有食品".equals(selectedCategory) || Classify[i].equals(selectedCategory)) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Name", Name[i]);
                hashMap.put("Quantity", String.valueOf(Quantity[i]));
                hashMap.put("Date", ExpiryDate[i]);
                hashMap.put("Classify", Classify[i]);
                arrayList.add(hashMap);
            }
        }

        if (myListAdapter == null) {
            myListAdapter = new MyListAdapter();
            mRecyclerView.setAdapter(myListAdapter);
        }
        myListAdapter.notifyDataSetChanged();
    }

    // 刪除項目的按鈕點擊事件處理
    public void btnDelete_Click(View view) {
        RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);

        if (viewHolder != null) {
            int position = viewHolder.getAdapterPosition();
            String foodNameToDelete = arrayList.get(position).get("Name");
            showDeleteConfirmationDialog(foodNameToDelete, position);
        }
    }

    // 顯示刪除確認對話框
    private void showDeleteConfirmationDialog(final String foodNameToDelete, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認刪除")
                .setMessage("是否確認刪除 " + foodNameToDelete + "？")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFoodFromDatabase(foodNameToDelete);
                        arrayList.remove(position);
                        myListAdapter.notifyItemRemoved(position);

                        // 根據資料是否存在，顯示或隱藏視圖
                        if (arrayList.size() > 0) {
                            findViewById(R.id.txtDelete).setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            findViewById(R.id.emptyDatabaseTextView).setVisibility(View.INVISIBLE);
                        } else {
                            findViewById(R.id.txtDelete).setVisibility(View.INVISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                            findViewById(R.id.emptyDatabaseTextView).setVisibility(View.VISIBLE);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消刪除操作
                    }
                })
                .show();
    }

    // 從資料庫中刪除食物項目
    private void deleteFoodFromDatabase(String foodName) {
        try {
            SQLiteDatabase db = openOrCreateDatabase("foodReminder", MODE_PRIVATE, null);
            db.delete("food", "foodName=?", new String[]{foodName});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ListActivity", "Error deleting food from database: " + e.getMessage());
        }
    }

    // 編輯項目的按鈕點擊事件處理
    public void btnEdit_Click(View view) {
        RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);

        if (viewHolder != null) {
            int position = viewHolder.getAdapterPosition();
            String foodNameToEdit = arrayList.get(position).get("Name");
            int quantityToEdit = Integer.parseInt(arrayList.get(position).get("Quantity"));
            String dateToEdit = arrayList.get(position).get("Date");
            String classifyToEdit = arrayList.get(position).get("Classify");

            showEditDialog(foodNameToEdit, quantityToEdit, dateToEdit, classifyToEdit, position);
        }
    }

    // 顯示編輯對話框
    private void showEditDialog(final String foodName, int quantity, String date, String classify, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        builder.setView(dialogView);

        final TextView edtName = dialogView.findViewById(R.id.edtName);
        final TextView edtQuantity = dialogView.findViewById(R.id.edtQuantity);
        final EditText edtDate = dialogView.findViewById(R.id.edtDate);
        edtClassify = dialogView.findViewById(R.id.radioGroup);
        edtDate.setText(selectedDate);

        // 找到資料的分類
        String classifyToEdit = arrayList.get(position).get("Classify");

        // 在 RadioGroup 中找到對應的 RadioButton 並設置為選中狀態
        int radioButtonId = -1;

        switch (classifyToEdit) {
            case "肉類":
                radioButtonId = R.id.radioButton;
                break;
            case "蔬菜類":
                radioButtonId = R.id.radioButton2;
                break;
            case "水果類":
                radioButtonId = R.id.radioButton3;
                break;
            case "飲料類":
                radioButtonId = R.id.radioButton4;
                break;
            case "其他類":
                radioButtonId = R.id.radioButton6;
                break;
            // 添加其他分類的 case

            default:
                // 預設選擇第一個 RadioButton
                radioButtonId = R.id.radioButton;
                break;
        }
        edtClassify.setTag(classifyToEdit);
        edtClassify.check(radioButtonId);

        edtName.setText(foodName);
        edtQuantity.setText(String.valueOf(quantity));
        edtDate.setText(date);

        // 監聽 RadioButton 的選擇狀態
        edtClassify.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 取得所選擇的 RadioButton 的文字
                RadioButton selectedRadioButton = group.findViewById(checkedId);
                String selectedText = selectedRadioButton.getText().toString();

                // 更新 txtClassify
                edtClassify.setTag(selectedText);
            }
        });

        builder.setTitle("編輯食品")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 确保 edtClassify 不为 null
                        if (edtClassify != null) {
                            // 确保 edtClassify.getTag() 不为 null
                            String selectedClassify = edtClassify.getTag() != null ? edtClassify.getTag().toString() : "";

                            updateFoodInDatabase(foodName, edtName.getText().toString(),
                                    Integer.parseInt(edtQuantity.getText().toString()),
                                    edtDate.getText().toString(),
                                    selectedClassify);
                        }

                        // 在確認按鈕點擊後啟動新的List Activity
                        Intent intent = new Intent(List.this, List.class);
                        startActivity(intent);
                        finish(); // 結束當前的Activity，以防止使用者按返回鍵回到編輯對話框
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消編輯操作
                    }
                })
                .show();
    }

    // 更新食物項目在資料庫中的資料
    private void updateFoodInDatabase(String originalName, String newName, int newQuantity, String newDate, String newClassify) {
        try {
            SQLiteDatabase db = openOrCreateDatabase("foodReminder", MODE_PRIVATE, null);
            db.execSQL("UPDATE food SET foodName=?, quantity=?, date=?, classify=? WHERE foodName=?",
                    new String[]{newName, String.valueOf(newQuantity), newDate, newClassify, originalName});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ListActivity", "Error updating food in database: " + e.getMessage());
        }
    }

    // 日期選擇按鈕點擊事件處理
    public void btnDate_Click(View view) {
        int year, month, day;
        if (TextUtils.isEmpty(selectedDate)) {
            year = Calendar.getInstance().get(Calendar.YEAR);
            month = Calendar.getInstance().get(Calendar.MONTH);
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date date = sdf.parse(selectedDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            } catch (ParseException e) {
                e.printStackTrace();
                year = Calendar.getInstance().get(Calendar.YEAR);
                month = Calendar.getInstance().get(Calendar.MONTH);
                day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                updateEdtDateField(selectedDate);
            }
        }, year, month, day);

        // 設定最小日期為當前日期
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateEdtDateField(String selectedDate) {
        EditText edtDate = dialogView.findViewById(R.id.edtDate);
        if (edtDate != null) {
            edtDate.setText(selectedDate);
        }
    }

    /*右上角選單=========================================================================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 處理選單項目的點擊事件
        int id = item.getItemId();
        if (id == R.id.Home) {
            Intent intent = new Intent(this, home.class);
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

