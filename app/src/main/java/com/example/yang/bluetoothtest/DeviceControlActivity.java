package com.example.yang.bluetoothtest;

/**
 * Created by Yang on 2016/8/8.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeviceControlActivity extends Activity implements View.OnClickListener {
    private static final String TAG = DeviceControlActivity.class.getSimpleName();
    private String bLEDevAddress;
    private String bleDevName;
    Button btn_send;
    Button btn_send_clear;
    Button btn_show_char_clear;
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
    public boolean connect;
    private Bundle data;
    TextView input_char;
    public BluetoothLeService mBluetoothLEService;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent paramIntent) {
            String str = paramIntent.getAction();
           Log.d("Log","action = " + str);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(str)) {
                invalidateOptionsMenu();
                Log.d("Log","action = " + str);
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(str))
                mBluetoothLEService.close();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(str))
                mBluetoothLEService.enableTXNotification();
            if (BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART.equals(str))
                mBluetoothLEService.disconnect();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(str)) {
                final byte[] arrayOfByte = paramIntent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String str = new String(arrayOfByte, "UTF-8");
                            show_char.setText(show_char.getText() + str);
                            return;
                        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
                            localUnsupportedEncodingException.printStackTrace();
                        }
                    }
                });
            }
        }
    };
    private Handler mHandler;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private ProgressDialog progressDialog;
    TextView show_char;
    TextView show_device;

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
        data = getIntent().getExtras();
        bleDevName = data.getString("BLEDevName");
        bLEDevAddress = data.getString("BLEDevAddress");
    }

    private void init() {
        mHandler = new Handler();
        String str = getIntent().getStringExtra("BLEDevAddress");
        show_device = ((TextView) findViewById(R.id.tv_dev_address));
        show_device.setText(str);
        input_char = ((TextView) findViewById(R.id.et_data));
        show_char = ((TextView) findViewById(R.id.tv_data));
        btn_send = ((Button) findViewById(R.id.btn_send));
        btn_send_clear = ((Button) findViewById(R.id.btn_send_clear));
        btn_show_char_clear = ((Button) findViewById(R.id.btn_show_clear));
        btn_send.setOnClickListener(this);
        btn_send_clear.setOnClickListener(this);
        btn_show_char_clear.setOnClickListener(this);
        bindService(new Intent(this, BluetoothLeService.class), conn, Context.BIND_AUTO_CREATE);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        localIntentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        localIntentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        localIntentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
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

    public void onClick(View paramView) {
        switch (paramView.getId()) {
            case R.id.et_data:
            default:
                return;
            case R.id.btn_send:
                byte[] arrayOfByte = input_char.getText().toString().getBytes();
                Log.d("Log",arrayOfByte.toString());
                mBluetoothLEService.writeCharacteristic(arrayOfByte);
                return;
            case R.id.btn_send_clear:
                input_char.setText("");
                return;
            case R.id.btn_show_clear:
        }
        show_char.setText("");
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_device);
        getIntentData();
        init();
    }

    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLEService.disconnect();
        mBluetoothLEService.close();
        unbindService(conn);
        mBluetoothLEService = null;
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.mGattUpdateReceiver);
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        progressDialog = ProgressDialog.show(this, "Connect-bluetooth4.0", "Connecting devivce...");
        mHandler.postDelayed(new Runnable() {
                                 public void run() {
                                     if (mBluetoothLEService != null) {
                                         connect = mBluetoothLEService.connect(bLEDevAddress);
                                         if (connect) {
                                             new DeviceControlActivity.NotifyThread().execute(new String[0]);
                                         }
                                     }
                                 }
                             }
                , 1000L);
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
