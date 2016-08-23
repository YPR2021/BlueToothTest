package com.example.yang.bluetoothtest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class BindActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000L;
    Button btn_search;
    ImageView ig;
    ListView lv_show;
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceListAdapter mDevListAdapter;
    private Handler mHandler;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice paramBluetoothDevice, int paramInt, byte[] paramArrayOfByte) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mDevListAdapter.addDevice(paramBluetoothDevice);
                    mDevListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    private boolean mScanning;
    ToggleButton tb_on_off;

    private void initViews() {
        tb_on_off = ((ToggleButton) findViewById(R.id.open));
        btn_search = ((Button) findViewById(R.id.search));
        lv_show = ((ListView) findViewById(R.id.list));
        btn_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                scanLeDevice(true);
            }
        });
        mDevListAdapter = new DeviceListAdapter();
        lv_show.setAdapter(mDevListAdapter);
        if (mBluetoothAdapter.isEnabled())
            tb_on_off.setChecked(true);
        while (true) {
            tb_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean) {
                    if (paramBoolean) {
                        Intent localIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
                        startActivityForResult(localIntent, 1);
                        return;
                    }
                    mBluetoothAdapter.disable();
                }
            });
            lv_show.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
                    if (mDevListAdapter.getCount() > 0) {
                        BluetoothDevice localBluetoothDevice = mDevListAdapter.getItem(paramInt);
                        Intent localIntent = new Intent("BindToMain");
                        Bundle localBundle = new Bundle();
                        localBundle.putString("BLEDevName", localBluetoothDevice.getName());
                        localBundle.putString("BLEDevAddress", localBluetoothDevice.getAddress());
                        localIntent.putExtras(localBundle);
//                        localIntent.setClass(BindActivity.this, MainActivity.class);
//                        startActivity(localIntent);
//                        localIntent.putExtra("BLEDevName", localBluetoothDevice.getName());
//                        localIntent.putExtra("BLEDevAddress", localBluetoothDevice.getAddress());
                        Log.d("Log", localBluetoothDevice.getName() + "+++++++++" + localBluetoothDevice.getAddress());
                        sendBroadcast(localIntent);
                        finish();
                    }
                }
            });
//            tb_on_off.setChecked(false);
            return;
        }
    }

    private void scanLeDevice(boolean paramBoolean) {
        if (paramBoolean) {
            mHandler.postDelayed(new Runnable() {
                                     public void run() {
                                         mScanning = false;
                                         mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                     }
                                 }
                    , 10000L);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            return;
        }
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_bind);
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                111);
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, "only bluetooth4.0 can use", Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "bluetooth4.0 is not supported!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    class DeviceListAdapter extends BaseAdapter {
        private List<BluetoothDevice> mBleArray = new ArrayList();
        private BindActivity.ViewHolder viewHolder;

        public DeviceListAdapter() {
        }

        public void addDevice(BluetoothDevice paramBluetoothDevice) {
            if (!mBleArray.contains(paramBluetoothDevice))
                mBleArray.add(paramBluetoothDevice);
        }

        public int getCount() {
            return mBleArray.size();
        }

        public BluetoothDevice getItem(int paramInt) {
            return (BluetoothDevice) mBleArray.get(paramInt);
        }

        public long getItemId(int paramInt) {
            return paramInt;
        }

        public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
            BluetoothDevice localBluetoothDevice = null;
            String str = null;
            if (paramView == null) {
                paramView = LayoutInflater.from(BindActivity.this).inflate(R.layout.item_list, null);
                viewHolder = new BindActivity.ViewHolder();
                viewHolder.tv_devName = ((TextView) paramView.findViewById(R.id.tv_name));
                viewHolder.tv_devAddress = ((TextView) paramView.findViewById(R.id.tv_address));
                paramView.setTag(viewHolder);

            } else {
                paramView.getTag();
            }
            localBluetoothDevice = (BluetoothDevice) mBleArray.get(paramInt);
            str = localBluetoothDevice.getName();
            if ((str == null) || (str.length() <= 0))
                viewHolder.tv_devName.setText("unknow-device");
            viewHolder.tv_devName.setText(str);
            viewHolder.tv_devAddress.setText(localBluetoothDevice.getAddress());
            return paramView;
        }
    }

    class ViewHolder {
        TextView tv_devAddress;
        TextView tv_devName;

        ViewHolder() {
        }
    }
}