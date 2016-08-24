package com.example.yang.bluetoothtest;


import java.math.BigInteger;

public class BytesUtils {

    /**
     * 二进制转换16进制的字符串形式
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        }
        // return sb.toString().toUpperCase().trim();
        return sb.toString().trim();
    }

    /**
     * 将byte[]转为各种进制的字符串
     * @param bytes byte[]
     * @param radix 基数可以转换进制的范围，从Character.MIN_RADIX到Character.MAX_RADIX，超出范围后变为10进制
     * @return 转换后的字符串
     */
    public static String binary(byte[] bytes, int radix){
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }
    /**
     * 16进制字符串转换byte数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toLowerCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c
     * @return
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

    /**
     * 16进制的ascii码转字符串
     *
     * @param asciiHex
     * @return
     */
    public static String asciiHexToString(String asciiHex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < asciiHex.length(); i += 2) {
            String substring = asciiHex.substring(i, (i + 2));
            int decimal = Integer.parseInt(substring, 16);
            sb.append((char) decimal);
        }
        return sb.toString();
    }


    /**
     * 十六进制转换字符串
     *
     * @param hexStr Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 将字节数组编码成十六进制表示的字符串，每个字节用两个十六进制字符表示，不足两位
     * 时在左边补0
     *
     * @param data      被编码的字节数组
     * @param seperator 字节之间的分隔符
     * @return 转换后的字符串
     */
    public static String toHexString(byte[] data, String seperator) {
        StringBuilder sb = new StringBuilder(data.length * (2 + seperator.length()));

        int pb = 0;
        String pre = "";
        while (pb < data.length) {
            byte b = data[pb++];

            int h = ((0xF0 & b) >> 4);
            int l = (0x0F & b);

            sb.append(pre);
            if (h < 10) {
                sb.append((char) ('0' + h));
            } else {
                sb.append((char) ('a' + h - 10));
            }
            if (l < 10) {
                sb.append((char) ('0' + l));
            } else {
                sb.append((char) ('a' + l - 10));
            }
            pre = seperator;
        }

        return sb.toString();
    }

    /**
     * 将十六进制字符串表示的字节数据还原成字节数组
     *
     * @param text 被还原的字符串
     * @return 还原之后的字节数组
     */
    public static byte[] fromHexString(String text) {
        if (text == null)
            return new byte[0];

        byte[] result = new byte[text.length() / 2];

        text = text.toLowerCase();
        int pb = 0;
        int ps = 0;
        while (pb < result.length && ps < text.length()) {
            char ch = text.charAt(ps++);
            char cl = text.charAt(ps++);

            int a = 0;
            int b = 0;
            if ('0' <= ch && ch <= '9') {
                a = ch - '0';
            } else {
                a = ch - 'a' + 10;
            }
            if ('0' <= cl && cl <= '9') {
                b = cl - '0';
            } else {
                b = cl - 'a' + 10;
            }

            result[pb++] = (byte) ((a << 4) + b);
        }

        return result;
    }
}
