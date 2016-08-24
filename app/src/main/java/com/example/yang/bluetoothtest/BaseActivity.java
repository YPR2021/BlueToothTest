package com.example.yang.bluetoothtest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ypr on 2016-08-23  11:02.
 * Description:
 * TODO:
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = DeviceControlActivity.class.getSimpleName();
    private String bLEDevAddress;
    private String bleDevName;
    public ArrayList<BluetoothGattCharacteristic> characteristics;
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder) {
            mBluetoothLEService = ((BluetoothLeService.LocalBinder) paramIBinder).getService();
            if (!mBluetoothLEService.initBluetoothParam()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            if (mBluetoothLEService == null)
                finish();
        }

        public void onServiceDisconnected(ComponentName paramComponentName) {
            mBluetoothLEService = null;
        }
    };
    public boolean isGetAciton = false;
    public boolean connect;
    private Bundle data;
    public BluetoothLeService mBluetoothLEService;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent paramIntent) {
            Log.d("Log", "onReceive");
            String str = paramIntent.getAction();
            Log.d("Log", "action = " + str);
            if ("BindToMain".equals(str)) {
                Log.d("Log", "收到广播了");
                isGetAciton = true;
                data = paramIntent.getExtras();
            }
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(str)) {
                invalidateOptionsMenu();
                Log.d("Log", "action = " + str);
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(str))
                mBluetoothLEService.close();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(str))
                mBluetoothLEService.enableTXNotification();
            if (BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART.equals(str))
                mBluetoothLEService.disconnect();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(str)) {
                final byte[] arrayOfByte = paramIntent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                final String stringExtra = paramIntent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String str = new String(arrayOfByte, "UTF-8");
                            String s = BytesUtils.str2HexStr(str);
                            InputStream inputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/APP1.bin"));
                            Log.d("Log", "" + Environment.getExternalStorageDirectory());
                            readInputStream(inputStream);
                            Toast.makeText(BaseActivity.this, "收到:" + s, Toast.LENGTH_SHORT).show();
                            return;
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });
            }
        }
    };

//    public interface OnOnceReadListener {
//        void onOnceRead();
//    }
//
//    private  OnOnceReadListener mOnceReadListener;
//
//    public  void setOnOnceReadListener(OnOnceReadListener listener) {
//        mOnceReadListener = listener;
//    }

    public void readInputStream(final InputStream inputStream) {
        // 1.建立通道对象
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 2.定义存储空间
        byte[] buffer = new byte[20];
        // 3.开始读文件
        int len = -1;
        try {
            if (inputStream != null) {
                while ((len = inputStream.read(buffer)) != -1) {
                    // 将Buffer中的数据写到outputStream对象中
                    outputStream.write(buffer, 0, len);
                    Log.d("Log", "" + BytesUtils.binary(buffer, 16));
                }
            }
            // 4.关闭流
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        return outputStream.toByteArray();
    }

    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private ProgressDialog progressDialog;

    public static String bytesToHexString(byte[] paramArrayOfByte) {
        String str1 = "";
        for (int i = 0; ; i++) {
            if (i >= paramArrayOfByte.length)
                return str1;
            String str2 = Integer.toHexString(0xFF & paramArrayOfByte[i]);
            if (str2.length() == 1)
                str2 = '0' + str2;
            str1 = str1 + str2.toUpperCase();
        }
    }

    private void getIntentData() {
        bleDevName = data.getString("BLEDevName");
        bLEDevAddress = data.getString("BLEDevAddress");
        Log.d("Log", bleDevName + "=======" + bLEDevAddress);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        localIntentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        localIntentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        localIntentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        localIntentFilter.addAction("BindToMain");
        return localIntentFilter;
    }

    public ArrayList<BluetoothGattCharacteristic> getCharacteristic() {
        ArrayList localArrayList = new ArrayList();
        List localList = mBluetoothLEService.getServices();
        int i = 0;
        if (i >= localList.size())
            return localArrayList;
        Iterator localIterator = ((BluetoothGattService) localList.get(i)).getCharacteristics().iterator();
        while (true) {
            if (!localIterator.hasNext()) {
                i++;
                break;
            }
            localArrayList.add((BluetoothGattCharacteristic) localIterator.next());
        }
        return localArrayList;
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d("Log", "onCreate");
    }

    protected void onDestroy() {
        Log.d("Log", "onDestroy");
        super.onDestroy();
        if (isGetAciton) {
            mBluetoothLEService.disconnect();
            mBluetoothLEService.close();
            unbindService(conn);
            mBluetoothLEService = null;
        }
    }

    protected void onPause() {
        Log.d("Log", "onPause");
        super.onPause();
        if (isGetAciton)
            unregisterReceiver(this.mGattUpdateReceiver);
    }

    protected void onResume() {
        Log.d("Log", "onResume");
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (isGetAciton) {
            getIntentData();
            bindService(new Intent(this, BluetoothLeService.class), conn, Context.BIND_AUTO_CREATE);
            progressDialog = ProgressDialog.show(this, "Connect-bluetooth4.0", "Connecting devivce...");
            new Handler().postDelayed(new Runnable() {
                                          public void run() {
                                              Log.d("Log", "111111");
                                              if (mBluetoothLEService != null) {
                                                  connect = mBluetoothLEService.connect(bLEDevAddress);
                                                  Log.d("Log", "2222222");
                                                  if (connect) {
                                                      new NotifyThread().execute(new String[0]);
                                                      Log.d("Log", "333333333");
                                                  } else {
                                                      progressDialog.dismiss();
                                                      Log.d("Log", "失败");
                                                  }
                                              }
                                          }
                                      }
                    , 1000L);
        }
    }

    class NotifyThread extends AsyncTask<String, String, String> {
        NotifyThread() {
        }

        protected String doInBackground(String[] paramArrayOfString) {
            try {
                Thread.sleep(1000L);
                characteristics = getCharacteristic();
                System.out.print(characteristics);
                return null;
            } catch (InterruptedException localInterruptedException) {
                while (true)
                    localInterruptedException.printStackTrace();
            }
        }

        protected void onPostExecute(String paramString) {
            if (connect) {
                Log.e("successful", "connectting success");
                progressDialog.dismiss();
            }
            super.onPostExecute(paramString);
        }
    }
}
