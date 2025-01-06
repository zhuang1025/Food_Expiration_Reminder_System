package com.example.project21;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project21.data.SQLiteDataBaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Notice extends AppCompatActivity {

    private RecyclerView expiredRecyclerView, expiringRecyclerView;
    ExpiredAdapter expiredAdapter;
    ExpiringAdapter expiringAdapter;
    ArrayList<HashMap<String, String>> expiringFoods = new ArrayList<>();
    ArrayList<HashMap<String, String>> expiredFoods = new ArrayList<>();

    private SQLiteDataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        expiredRecyclerView = findViewById(R.id.expiredRecyclerView);
        expiringRecyclerView = findViewById(R.id.expiringRecyclerView);

        LinearLayoutManager expiredLayoutManager = new LinearLayoutManager(this);
        expiredRecyclerView.setLayoutManager(expiredLayoutManager);
        expiredAdapter = new ExpiredAdapter();
        expiredRecyclerView.setAdapter(expiredAdapter);

        LinearLayoutManager expiringLayoutManager = new LinearLayoutManager(this);
        expiringRecyclerView.setLayoutManager(expiringLayoutManager);
        expiringAdapter = new ExpiringAdapter();
        expiringRecyclerView.setAdapter(expiringAdapter);

        dbHelper = new SQLiteDataBaseHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpiredFoods();
        loadExpiringFoods();
    }

    private void loadExpiredFoods() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // 查询过期的食物
        String query = "SELECT foodName, quantity, classify, date FROM food WHERE date < ?";
        Cursor cursor = db.rawQuery(query, new String[]{getCurrentDate()});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                HashMap<String, String> foodItem = new HashMap<>();
                foodItem.put("foodName", cursor.getString(0));
                foodItem.put("quantity", cursor.getString(1));
                foodItem.put("classify", cursor.getString(2));
                foodItem.put("date", cursor.getString(3));
                expiredFoods.add(foodItem);

                // 根據資料是否存在，顯示或隱藏視圖
                if (foodItem.size() > 0) {
                    expiredRecyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.txtExpiredEmpty).setVisibility(View.INVISIBLE);
                } else {
                    expiredRecyclerView.setVisibility(View.GONE);
                    findViewById(R.id.txtExpiredEmpty).setVisibility(View.VISIBLE);
                }
            }
        } else {
            findViewById(R.id.expiredRecyclerView).setVisibility(View.INVISIBLE);
            Toast.makeText(this, "沒有過期商品", Toast.LENGTH_SHORT).show();
            findViewById(R.id.txtExpiredEmpty).setVisibility(View.VISIBLE);

        }
        cursor.close();

        //按照日期排序
        Collections.sort(expiredFoods, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> food1, HashMap<String, String> food2) {
                String date1 = food1.get("date");
                String date2 = food2.get("date");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);

                try {
                    Date dateObj1 = dateFormat.parse(date1);
                    Date dateObj2 = dateFormat.parse(date2);

                    // 升冪排序，越早的日期越靠前
                    return dateObj1.compareTo(dateObj2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // 發生解析錯誤時返回0
                }
            }
        });

        expiredAdapter.setFoodList(expiredFoods);
        expiredAdapter.notifyDataSetChanged();
    }

    private void loadExpiringFoods() {
        // 查询即将过期的食物
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT foodName, quantity, classify, date FROM food WHERE date BETWEEN ? AND ?";
        String[] args = {getCurrentDate(), getExpiringDate()};
        Cursor cursor = db.rawQuery(query, args);
        if (cursor != null && cursor.getCount() > 0) {
        while (cursor.moveToNext()) {
            HashMap<String, String> foodItem = new HashMap<>();
            foodItem.put("foodName", cursor.getString(0));
            foodItem.put("quantity", cursor.getString(1));
            foodItem.put("classify", cursor.getString(2));
            foodItem.put("date", cursor.getString(3));
            expiringFoods.add(foodItem);
            // 根據資料是否存在，顯示或隱藏視圖
           if (foodItem.size() > 0) {
               expiringRecyclerView.setVisibility(View.VISIBLE);
               findViewById(R.id.txtExpiringEmpty).setVisibility(View.INVISIBLE);
           } else {
               expiringRecyclerView.setVisibility(View.GONE);
               findViewById(R.id.txtExpiringEmpty).setVisibility(View.VISIBLE);
           }
        }
        } else {
            findViewById(R.id.expiringRecyclerView).setVisibility(View.INVISIBLE);
            Toast.makeText(this, "沒有過期商品", Toast.LENGTH_SHORT).show();
            findViewById(R.id.txtExpiringEmpty).setVisibility(View.VISIBLE);

        }
        cursor.close();
        // 按日期排序
        Collections.sort(expiringFoods, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> food1, HashMap<String, String> food2) {
                String date1 = food1.get("date");
                String date2 = food2.get("date");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);

                try {
                    Date dateObj1 = dateFormat.parse(date1);
                    Date dateObj2 = dateFormat.parse(date2);

                    // 升冪排序，越早的日期越靠前
                    return dateObj1.compareTo(dateObj2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // 發生解析錯誤時返回0
                }
            }
        });

        expiringAdapter.setFoodList(expiringFoods);
        expiringAdapter.notifyDataSetChanged();
    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        return formatter.format(new Date()) + " 00:00:00";
    }

    private String getExpiringDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 3);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        return formatter.format(c.getTime());
    }

    /*右上角選單=========================================================================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    /*過期刪除=======================================================================*/
    public void btnDelete_Click(View view) {
        RecyclerView.ViewHolder viewHolderExpired = expiredRecyclerView.findContainingViewHolder(view);
        RecyclerView.ViewHolder viewHolderExpiring = expiringRecyclerView.findContainingViewHolder(view);

        if (viewHolderExpired != null) {
            int position = viewHolderExpired.getAdapterPosition();
            String foodNameToDelete = expiredFoods.get(position).get("foodName");
            showExpiredDeleteDialog(foodNameToDelete, position);
        }

        if (viewHolderExpiring != null) {
            int position = viewHolderExpiring.getAdapterPosition();
            String foodNameToDelete = expiringFoods.get(position).get("foodName");
            showExpiringDeleteDialog(foodNameToDelete, position);
        }
    }

    // 顯示過期刪除確認對話框
    private void showExpiredDeleteDialog(final String foodNameToDelete, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認刪除")
                .setMessage("是否確認刪除 " + foodNameToDelete + "？")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFoodFromDatabase(foodNameToDelete);
                        expiredFoods.remove(position);
                        expiredAdapter.notifyItemRemoved(position);

                        // 根據資料是否存在，顯示或隱藏視圖
                        if (expiredFoods.size() > 0) {
                            expiredRecyclerView.setVisibility(View.VISIBLE);
                            findViewById(R.id.txtExpiredEmpty).setVisibility(View.INVISIBLE);
                        } else {
                            expiredRecyclerView.setVisibility(View.INVISIBLE);
                            findViewById(R.id.txtExpiredEmpty).setVisibility(View.VISIBLE);
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

    // 顯示即期刪除確認對話框
    private void showExpiringDeleteDialog(final String foodNameToDelete, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認刪除")
                .setMessage("是否確認刪除 " + foodNameToDelete + "？")
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFoodFromDatabase(foodNameToDelete);
                        expiringFoods.remove(position);
                        expiringAdapter.notifyItemRemoved(position);

                        // 根据数据是否存在，显示或隐藏视图
                        if (expiringFoods.size() > 0) {
                            expiringRecyclerView.setVisibility(View.VISIBLE);
                            findViewById(R.id.txtExpiringEmpty).setVisibility(View.INVISIBLE);
                        } else {
                            expiringRecyclerView.setVisibility(View.INVISIBLE);
                            findViewById(R.id.txtExpiringEmpty).setVisibility(View.VISIBLE);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消删除操作
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
}

// 第一段代码：ExpiredAdapter 类
class ExpiredAdapter extends RecyclerView.Adapter<ExpiredViewHolder> {

    private ArrayList<HashMap<String, String>> foodList;

    public ExpiredAdapter() {
        this.foodList = new ArrayList<>();
    }

    public void setFoodList(ArrayList<HashMap<String, String>> list) {
        this.foodList = list;
    }

    @NonNull
    @Override
    public ExpiredViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建视图持有者并绑定布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item, parent, false);
        return new ExpiredViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpiredViewHolder holder, int position) {
        // 绑定数据到视图持有者
        HashMap<String, String> food = foodList.get(position);
        holder.txtfoodName.setText(food.get("foodName"));
        holder.txtfoodQuantity.setText(food.get("quantity"));
        holder.txtfoodCategory.setText(food.get("classify"));
        holder.txtfoodExpiryDate.setText(food.get("date"));

        // 顯示過期的天數
        String expiredDate = food.get("date");
        int daysUntilExpired = calculateDaysUntilExpired(expiredDate);
        if (holder.txtMes != null) {
            holder.txtMes.setVisibility(View.VISIBLE);
        }
        holder.txtMes.setText("已過期 " + daysUntilExpired + " 天");

        holder.itemView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.btnPlus).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.btnMinus).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.space).setVisibility(View.GONE);
        holder.itemView.setBackgroundColor(Color.parseColor("#FBEDEE"));
    }

    private int calculateDaysUntilExpired(String expirationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        try {
            Date today = new Date();
            Date expiryDate = dateFormat.parse(expirationDate);

            long diffInMillies = Math.abs(today.getTime() - expiryDate.getTime());
            return (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // 解析錯誤時返回0
        }
    }

    @Override
    public int getItemCount() {
        return foodList == null ? 0 : foodList.size();
    }
    // 实现ViewHolder和Adapter的其它方法
}

// 第二段代码：ExpiringAdapter 类
class ExpiringAdapter extends RecyclerView.Adapter<ExpiringViewHolder> {

    private ArrayList<HashMap<String, String>> foodList;

    public ExpiringAdapter() {
        this.foodList = new ArrayList<>();
    }

    // 设置 foodList 的方法
    public void setFoodList(ArrayList<HashMap<String, String>> list) {
        foodList = list;
    }

    @NonNull
    @Override
    public ExpiringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建视图持有者并绑定布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item, parent, false);
        return new ExpiringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpiringViewHolder holder, int position) {
        // 绑定数据到视图持有者
        HashMap<String, String> food = foodList.get(position);
        holder.txtfoodName.setText(food.get("foodName"));
        holder.txtfoodQuantity.setText(food.get("quantity"));
        holder.txtfoodCategory.setText(food.get("classify"));
        holder.txtfoodExpiryDate.setText(food.get("date"));

        // 顯示還有多少天過期
        String expiringDate = food.get("date");
        int daysUntilExpiring = calculateDaysUntilExpiring(expiringDate);
        if (holder.txtMes != null) {
            holder.txtMes.setVisibility(View.VISIBLE);
        }
        holder.txtMes.setText("還有 " + daysUntilExpiring + " 天過期");

        holder.itemView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.btnPlus).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.btnMinus).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.space).setVisibility(View.GONE);
        holder.itemView.setBackgroundColor(Color.parseColor("#EDEEFB"));
    }

    private int calculateDaysUntilExpiring(String expiringDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        try {
            Date today = new Date();
            Date expiringDateObj = dateFormat.parse(expiringDate);

            long diffInMillies = Math.abs(expiringDateObj.getTime() - today.getTime());
            return (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // 解析錯誤時返回0
        }
    }


    @Override
    public int getItemCount() {
        return foodList == null ? 0 : foodList.size();
    }
    // 实现ViewHolder和Adapter的其它方法
}

// 第三段代码：ExpiredViewHolder 类
class ExpiredViewHolder extends RecyclerView.ViewHolder {
    // 实现ViewHolder
    TextView txtfoodName, txtfoodQuantity, txtfoodCategory, txtfoodExpiryDate, txtMes;

    public ExpiredViewHolder(@NonNull View itemView) {
        super(itemView);
        txtfoodName = itemView.findViewById(R.id.txtName);
        txtfoodQuantity = itemView.findViewById(R.id.txtQuantity);
        txtfoodCategory = itemView.findViewById(R.id.txtClassify);
        txtfoodExpiryDate = itemView.findViewById(R.id.txtDate);
        txtMes = itemView.findViewById(R.id.txtMes);
    }
}

// 第四段代码：ExpiringViewHolder 类
class ExpiringViewHolder extends RecyclerView.ViewHolder {
    // 实现ViewHolder
    TextView txtfoodName, txtfoodQuantity, txtfoodCategory, txtfoodExpiryDate, txtMes;

    public ExpiringViewHolder(@NonNull View itemView) {
        super(itemView);
        txtfoodName = itemView.findViewById(R.id.txtName);
        txtfoodQuantity = itemView.findViewById(R.id.txtQuantity);
        txtfoodCategory = itemView.findViewById(R.id.txtClassify);
        txtfoodExpiryDate = itemView.findViewById(R.id.txtDate);
        txtMes = itemView.findViewById(R.id.txtMes);
    }
}

