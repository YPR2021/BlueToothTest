package com.example.yang.bluetoothtest;

/**
 * Created by Yang on 2016/8/8.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    public static final String ACTION_DATA_AVAILABLE = "com.example.yang.bluetoothtest.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "com.example.yang.bluetoothtest.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.yang.bluetoothtest.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.yang.bluetoothtest.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String DEVICE_DOES_NOT_SUPPORT_UART = "com.example.yang.bluetoothtest.DEVICE_DOES_NOT_SUPPORT_UART";
    public static final String EXTRA_DATA = "com.example.yang.bluetoothtest.EXTRA_DATA";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final String TAG = BluetoothLeService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private int mConnectionState = STATE_DISCONNECTED;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onCharacteristicChanged(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, paramBluetoothGattCharacteristic);
            Log.e(TAG, "onCharRead" + paramBluetoothGatt.getDevice().getName() + "read" + paramBluetoothGattCharacteristic.getUuid().toString() + "->" + new String(paramBluetoothGattCharacteristic.getValue()));
            System.out.println("------------------onCharacteristicChanged------------------");
        }

        public void onCharacteristicRead(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int paramInt) {
            if (paramInt == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_AVAILABLE, paramBluetoothGattCharacteristic);
        }

        public void onCharacteristicWrite(BluetoothGatt paramBluetoothGatt, BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int paramInt) {
            Log.e(TAG, "onCharWrite" + paramBluetoothGatt.getDevice().getName() + "write" + paramBluetoothGattCharacteristic.getUuid().toString() + "->" + new String(paramBluetoothGattCharacteristic.getValue()));
        }

        public void onConnectionStateChange(BluetoothGatt paramBluetoothGatt, int paramInt1, int paramInt2) {
            Log.d("Log", "paramInt2:" + paramInt2);
            if (paramInt2 == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Connected to Gatt server");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (paramInt2 == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
//
//            do {
//                mConnectionState = 0;
//                Log.i(BluetoothLeService.TAG, "Disconnected from GATT server");
//                broadcastUpdate("com.android.Wireless-Tag.ACTION_GATT_DISCONNECTED");
//                return;
//            }
//            while (paramInt2 != 0);
//            if (paramInt2 != 0) {
//                mConnectionState = 0;
//                Log.i(TAG, "Disconnected from GATT server");
//                broadcastUpdate("com.example.yang.bluetoothtest.ACTION_GATT_DISCONNECTED");
//            }

        }

        public void onDescriptorWrite(BluetoothGatt paramBluetoothGatt, BluetoothGattDescriptor paramBluetoothGattDescriptor, int paramInt) {
            System.out.println("onDescriptorWriteDescriptorWrite=" + paramInt + ",descriptor=" + paramBluetoothGattDescriptor.getUuid().toString());
        }

        public void onReadRemoteRssi(BluetoothGatt paramBluetoothGatt, int paramInt1, int paramInt2) {
            System.out.println("rssi=" + paramInt1);
        }

        public void onServicesDiscovered(BluetoothGatt paramBluetoothGatt, int paramInt) {
            if (paramInt == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                return;
            }
            Log.w(TAG, "onServicesDiscovered received:" + paramInt);
        }
    };

    private void broadcastUpdate(String paramString) {
        sendBroadcast(new Intent(paramString));
    }

    private void broadcastUpdate(String paramString, BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        Intent localIntent = new Intent(paramString);
        if (SampleGattAttributes.Read.equals(paramBluetoothGattCharacteristic.getUuid()))
            localIntent.putExtra(EXTRA_DATA, paramBluetoothGattCharacteristic.getValue());
        sendBroadcast(localIntent);
//        final byte[] data = paramBluetoothGattCharacteristic.getValue();
//        if (data != null && data.length > 0) {
//            final StringBuilder stringBuilder = new StringBuilder(data.length);
//            for(byte byteChar : data)
//                stringBuilder.append(String.format("%02X ", byteChar));
//            Log.d("Log", data+"~~~~~~"+stringBuilder.toString());
//            localIntent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//        }
//        sendBroadcast(localIntent);
    }



    public void close() {
        if (mBluetoothGatt == null)
            return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean connect(String address) {
        if ((mBluetoothAdapter == null) || (address == null))
            return false;
        if ((mBluetoothDeviceAddress != null) && (address.equals(mBluetoothDeviceAddress)) && (mBluetoothGatt != null)) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            return false;
        }
        BluetoothDevice localBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (localBluetoothDevice == null) {
            Log.v("device of null", "device of null");
            return false;
        }
        mBluetoothGatt = localBluetoothDevice.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null))
            return;
        mBluetoothGatt.disconnect();
    }

    public void enableTXNotification() {
        BluetoothGattCharacteristic localBluetoothGattCharacteristic = mBluetoothGatt.getService(SampleGattAttributes.Service).getCharacteristic(SampleGattAttributes.Read);
        mBluetoothGatt.setCharacteristicNotification(localBluetoothGattCharacteristic, true);
        BluetoothGattDescriptor localBluetoothGattDescriptor = localBluetoothGattCharacteristic.getDescriptor(SampleGattAttributes.Notify);
        localBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(localBluetoothGattDescriptor);
    }

    public List<BluetoothGattService> getServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    public BluetoothGattService getservice(UUID paramUUID) {
        BluetoothGatt localBluetoothGatt = mBluetoothGatt;
        BluetoothGattService localBluetoothGattService = null;
        if (localBluetoothGatt != null)
            localBluetoothGattService = mBluetoothGatt.getService(paramUUID);
        return localBluetoothGattService;
    }

    public boolean initBluetoothParam() {
        if (mBluetoothManager == null) {
            mBluetoothManager = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
            if (mBluetoothManager == null) {
                Toast.makeText(this, "bluetooth初始化失败", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不能获得bluetoothAdapter", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public IBinder onBind(Intent paramIntent) {
        return mBinder;
    }

    public boolean onUnbind(Intent paramIntent) {
        close();
        return super.onUnbind(paramIntent);
    }

    public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
    }

    public void writeCharacteristic(byte[] paramArrayOfByte) {
        BluetoothGattService service = mBluetoothGatt.getService(SampleGattAttributes.Service);
        BluetoothGattCharacteristic localBluetoothGattCharacteristic = service.getCharacteristic(SampleGattAttributes.Write);
        localBluetoothGattCharacteristic.setValue(paramArrayOfByte);
        mBluetoothGatt.writeCharacteristic(localBluetoothGattCharacteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
//        if (SampleGattAttributes.Read.equals(characteristic.getUuid())) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(SampleGattAttributes.Notify);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        }
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
