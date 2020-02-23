package com.shuakuaishou3;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private int width = 0;
    private int height = 0;
    Intent intent2;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDis();

        //TextView start_button = (TextView)findViewById(R.id.start);

       // if (AccessibilityServiceTest.isAccessibilityServiceSettingOn(MainActivity.this)){
      //      start_button.setEnabled(false);
        //}else{
        //    start_button.setEnabled(true);
       // }

        if (!AccessibilityServiceTest.isAccessibilityServiceSettingOn(MainActivity.this)){
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }

        intent2 = new Intent(MainActivity.this, AccessibilityServiceTest.class);
        intent2.putExtra("width", width);
        intent2.putExtra("height", height);
        startService(intent2);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent2);
    }

    public void getDis(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        float density = dm.density;

        width = (int)(dm.widthPixels);
        height = (int)(dm.heightPixels);

        String val = "width: " + String.valueOf(width) + "         height: " + String.valueOf(height);
        Log.d(TAG, val);
    }


}
