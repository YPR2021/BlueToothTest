package com.example.yang.bluetoothtest;

/**
 * Created by Yang on 2016/8/8.
 */
import java.util.HashMap;
import java.util.UUID;

public class SampleGattAttributes
{
    public static final UUID Notify;
    public static final UUID Read;
    public static final UUID Service;
    public static final UUID Write;
    private static HashMap<String, String> attributes = new HashMap();

    static
    {
        Service = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb");
        Write = UUID.fromString("00001235-0000-1000-8000-00805f9b34fb");
        Read = UUID.fromString("00001236-0000-1000-8000-00805f9b34fb");
        Notify = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static String lookup(String paramString1, String paramString2)
    {
        String str = (String)attributes.get(paramString1);
        if (str == null)
            return paramString2;
        return str;
    }
}
