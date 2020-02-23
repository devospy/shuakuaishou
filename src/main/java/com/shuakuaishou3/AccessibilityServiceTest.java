package com.shuakuaishou3;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

import java.util.List;


public class AccessibilityServiceTest extends AccessibilityService {
    private static final String TAG = "ABSTest";
    private boolean isFirst = true;
    private boolean isScroll = true;
    private Integer[] arr = {5000, 6000,7000, 8000, 9000, 10000};

    private int X=0, Y=0;
    private int count = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        X = intent.getIntExtra("width", 0);
        Y = intent.getIntExtra("height", 0);

        String val = "X: " + String.valueOf(X) + "   Y: " + String.valueOf(Y);
        Log.d(TAG, val);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent jinru success.");
        if (event == null || event.getPackageName() == null || event.getPackageName().equals("")){
            Log.d(TAG, "get PackageName null. ");
            return;
        }

        if (event.getPackageName().equals("com.kuaishou.nebula")){
            Log.d(TAG, "current view in  kuaishou interface.");
            final AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo != null) {
                Log.d(TAG, "nodeinfo:" + nodeInfo.getClassName());
            }

            AccessibilityNodeInfo rootInfo =this.getRootInActiveWindow();
            List<AccessibilityNodeInfo> listInfo = rootInfo.findAccessibilityNodeInfosByText("关闭");
            if (listInfo.size() ==1){
                Log.d(TAG, "find the 关闭 button");
                for (int i=0; i<listInfo.size();i++){
                    Log.d(TAG, listInfo.get(i).toString()+"==size:"+listInfo.size());
                    listInfo.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }

            if (!isFirst){
                return;
            }

            if (isFirst){
                isFirst = false;
            }

           // if (nodeInfo !=null){
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        try{
                            while(true) {
                                int index = (int) (Math.random() * arr.length);
                                Thread.sleep(arr[index]);
                                Log.d(TAG, "sleep" + String.valueOf(arr[index]) + " ms");
                                mockScroll();
                                //nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                AccessibilityNodeInfo rootInfo =AccessibilityServiceTest.this.getRootInActiveWindow();
                                List<AccessibilityNodeInfo> listInfo = rootInfo.findAccessibilityNodeInfosByText("点击重播");
                                if (listInfo.size() ==1){
                                    Log.d(TAG, "find the 点击重播 button");
                                    for (int i=0; i<listInfo.size();i++){
                                        listInfo.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }
                                }
                                count += 1;
                                Log.d(TAG, "count:"+String.valueOf(count));
                                if (count % 20 == 0){
                                    Log.d(TAG, "开始双击点赞了: " + "count=" +String.valueOf(count));
                                    mockDoubleClick();
                                }
                            }
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
           // }else{
           //     isFirst = true;
           //     Log.d(TAG, "nodeinfo is null. ");
           // }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void mockScroll(){
        final Path path = new Path();
        path.moveTo((int)(X/2), (int)(Y*2/3));
        path.lineTo((int)(X/2), 0);

        GestureDescription.Builder builder = new GestureDescription.Builder();

        GestureDescription gestureDescription = builder.addStroke(
                new GestureDescription.StrokeDescription(path, 200, 400)
        ).build();

        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "scroll finish.");
                path.close();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "scroll cancell.");
            }
        }, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void mockDoubleClick(){
        final Path path = new Path();
        path.moveTo((int)(X/2), (int)(Y/2));

        GestureDescription.Builder builder = new GestureDescription.Builder();

        GestureDescription gestureDescription = builder.addStroke(
                new GestureDescription.StrokeDescription(path, 0, 100)
        ).build();

        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Path path2 = new Path();
                path2.moveTo((int)(X/2), (int)(Y/2));

                GestureDescription.Builder builder2 = new GestureDescription.Builder();

                GestureDescription gestureDescription2 = builder2.addStroke(
                        new GestureDescription.StrokeDescription(path2, 0, 100)
                ).build();

                AccessibilityServiceTest.this.dispatchGesture(gestureDescription2, null, null);

                Log.d(TAG, "double click finish.");
                path.close();
                path2.close();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "scroll cancell.");
            }
        }, null);

    }
    @Override
    public void onInterrupt() {
        Log.d(TAG, "AccessibilityEvent end.");
    }

    public static boolean isAccessibilityServiceSettingOn(Context mContext){
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + AccessibilityServiceTest.class.getCanonicalName();
        Log.d(TAG, service);
        try{
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
            Log.d(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        }catch (Settings.SettingNotFoundException e){
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1){
            Log.d(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null){
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.d(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }

            }
        }else {
            Log.d(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return  false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this.getApplicationContext(), "onServiceConnected: success.", Toast.LENGTH_LONG);
        Log.d(TAG,"onServiceConnected: success.");
    }


}
