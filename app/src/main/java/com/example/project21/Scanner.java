package com.example.project21;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    ZXingScannerView zXingScannerView;
    String nameResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        //找到介面
        zXingScannerView = findViewById(R.id.ZXingScannerView_QRCode);
        //判斷有沒有給CAMERA權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this
                        , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    100);
        }
        else{
            //若先前已取得權限，則直接開啟
            openQRCamera();
        }
    }
    //開啟QRCode相機
    private void openQRCamera(){
        if (zXingScannerView != null) {
            zXingScannerView.setResultHandler(this);
            zXingScannerView.startCamera();
        }
    }
    //取得權限回傳
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults[0] ==0){
            openQRCamera();
        }else{
            Toast.makeText(this, "權限勒？", Toast.LENGTH_SHORT).show();
        }
    }
    /*關閉QRCode相機*/
    @Override
    protected void onStop() {
        zXingScannerView.stopCamera();
        super.onStop();
    }

    /**取得QRCode掃描到的物件回傳*/
    @Override
    public void handleResult(Result rawResult) {
        TextView tvResult = findViewById(R.id.textView_Result);
        tvResult.setText(rawResult.getText());
        nameResult=rawResult.getText();


        //ZXing相機預設掃描到物件後就會停止，以此這邊再次呼叫開啟，使相機可以為連續掃描之狀態
        //openQRCamera();
        Intent intent=new Intent(this,home.class);
        Bundle bundle=new Bundle();
        bundle.putString("name",nameResult);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /*跳轉回首頁*/
    public void btnBack_Click(View view){
        Intent intent=new Intent(this,home.class);
        startActivity(intent);
    }

}
