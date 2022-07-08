package com.centit.support.algorithm;

import com.centit.support.file.FileIOOpt;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Date;

@SuppressWarnings("unused")
public abstract class ByteBaseOpt {
    private ByteBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    public static byte[] castObjectToBytes(Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            if (len == 0) {
                return null;
            }
            Object firstObj = Array.get(obj, 0);
            if (firstObj instanceof Byte) {
                byte[] bytes = new byte[len];
                for (int i = 0; i < len; i++) {
                    Object bObj = Array.get(obj, i);
                    bytes[i] = (Byte) bObj;
                }
                return bytes;
            }

            if (firstObj instanceof Character) {
                byte[] bytes = new byte[len];
                for (int i = 0; i < len; i++) {
                    Object bObj = Array.get(obj, i);
                    bytes[i] = (byte) ((Character) bObj).charValue();
                }
                return bytes;
            }
            return StringBaseOpt.objectToString(obj).getBytes();
        }

        if (obj instanceof Long) {
            byte[] buf = new byte[8];
            writeInt64(buf, (Long) obj, 0);
            return buf;
        }

        if (obj instanceof Integer) {
            byte[] buf = new byte[4];
            writeInt32(buf, (Integer) obj, 0);
            return buf;
        }

        if (obj instanceof Date) {
            byte[] buf = new byte[8];
            writeInt64(buf, ((Date) obj).getTime(), 0);
            return buf;
        }

        if (obj instanceof Float) {
            byte[] buf = new byte[4];
            writeFloat(buf, (Float) obj, 0);
            return buf;
        }

        if (obj instanceof Double) {
            byte[] buf = new byte[8];
            writeDouble(buf, (Double) obj, 0);
            return buf;
        }

        if (obj instanceof InputStream){
            try {
                return FileIOOpt.readBytesFromInputStream((InputStream)obj);
            } catch (IOException e) {
                return null;
            }
        }
        return StringBaseOpt.objectToString(obj).getBytes();
    }


    public static int writeInt64(byte[] buf, long data, int offset) {
        for (int i = 0; i < 8; i++) {
            buf[offset + 7 - i] = (byte) (data & 0xff);
            data = data >> 8;
        }
        return offset + 8;
    }

    public static int writeLong(byte[] buf, long data, int offset) {
        return writeInt64(buf, data, offset);
    }

    public static int writeInt32(byte[] buf, int data, int offset) {
        for (int i = 0; i < 4; i++) {
            buf[offset + 3 - i] = (byte) (data & 0xff);
            data = data >> 8;
        }
        return offset + 4;
    }

    public static int writeUInt32(byte[] buf, long data, int offset) {
        for (int i = 0; i < 4; i++) {
            buf[offset + 3 - i] = (byte) (data & 0xff);
            data = data >> 8;
        }
        return offset + 4;
    }

    public static int writeInt(byte[] buf, int data, int offset) {
        return writeInt32(buf, data, offset);
    }

    public static int writeInt16(byte[] buf, short data, int offset) {
        for (int i = 0; i < 2; i++) {
            buf[offset + 1 - i] = Integer.valueOf(data & 0xff).byteValue();
            data = (short) (data >> 8);
        }
        return offset + 2;
    }

    public static int writeUInt16(byte[] buf, int data, int offset) {
        for (int i = 0; i < 2; i++) {
            buf[offset + 1 - i] = (byte) (data & 0xff);
            data = (short) (data >> 8);
        }
        return offset + 2;
    }

    public static int writeShort(byte[] buf, short data, int offset) {
        return writeInt16(buf, data, offset);
    }

    public static int writeFloat(byte[] buf, float data, int offset) {
        int intDate = Float.floatToIntBits(data);
        return writeInt32(buf, intDate, offset);
    }

    public static int writeDouble(byte[] buf, double data, int offset) {
        long longDate = Double.doubleToLongBits(data);
        return writeInt64(buf, longDate, offset);
    }

    public static int writeStringAsBytes(byte[] buf, String data, int offset) {
        byte[] strBytes = data.getBytes();
        System.arraycopy(strBytes, 0, buf, offset, strBytes.length);
        /*for(int i=0;i<strBytes.length;i++){
            buf[offset+i] = strBytes[i];
        }*/
        return offset + data.length();
    }

    public static int writeString(byte[] buf, String data, int offset) {
        for (int i = 0; i < data.length(); i++) {
            buf[offset + i] = (byte) data.charAt(i);
        }
        return offset + data.length();
    }

    public static int writeString(byte[] buf, char[] data, int offset) {
        for (int i = 0; i < data.length; i++) {
            buf[offset + i] = (byte) data[i];
        }
        return offset + data.length;
    }

    public static int writeStringApendSpace(byte[] buf, String data, int len, int offset) {
        int i = 0;
        int dataLen = data.length() < len ? data.length() : len;
        for (; i < dataLen; i++) {
            buf[offset + i] = (byte) data.charAt(i);
        }
        for (; i < len; i++) {
            buf[offset + i] = 32;
        }
        return offset + data.length();
    }

    public static int writeStringApendSpace(byte[] buf, char[] data, int len, int offset) {
        int i = 0;
        int dataLen = data.length < len ? data.length : len;
        for (; i < dataLen; i++) {
            buf[offset + i] = (byte) data[i];
        }
        for (; i < len; i++) {
            buf[offset + i] = 32;
        }
        return offset + data.length;
    }

    public static int writeDate(byte[] buf, Date data, int offset) {
        return writeLong(buf, data.getTime(), offset);
    }

    public static int writeDateAsInt32(byte[] buf, Date data, int offset) {
        int intDate =
            DatetimeOpt.getYear(data) * 10000 +
                DatetimeOpt.getMonth(data) * 100 +
                DatetimeOpt.getDay(data);
        return writeInt32(buf, intDate, offset);
    }

    public static int writeDatetimeAsInt64(byte[] buf, Date data, int offset) {
        long longDate =
            DatetimeOpt.getYear(data) * 10000000000L +
                DatetimeOpt.getMonth(data) * 100000000L +
                DatetimeOpt.getDay(data) * 1000000L +
                DatetimeOpt.getHour(data) * 10000L +
                DatetimeOpt.getMinute(data) * 100L +
                DatetimeOpt.getSecond(data);
        return writeInt64(buf, longDate, offset);

    }

    public static int writeTimestampAsInt64(byte[] buf, Date data, int offset) {
        long longDate =
            DatetimeOpt.getYear(data) * 10000000000000L +
                DatetimeOpt.getMonth(data) * 100000000000L +
                DatetimeOpt.getDay(data) * 1000000000L +
                DatetimeOpt.getHour(data) * 10000000L +
                DatetimeOpt.getMinute(data) * 100000L +
                DatetimeOpt.getSecond(data) * 1000L +
                DatetimeOpt.getMilliSecond(data);
        return writeInt64(buf, longDate, offset);
    }


    /*-----------------read------------------------------------------------------------*/
    public static long readInt64(byte[] buf, int offset) {
        long longData = 0;
        for (int i = 0; i < 8; i++) {
            longData = longData << 8;
            longData = longData + (buf[offset + i] & 0xFF);
        }
        return longData;
    }

    public static long readLong(byte[] buf, int offset) {
        return readInt64(buf, offset);
    }

    public static int readInt32(byte[] buf, int offset) {
        int intData = 0;
        for (int i = 0; i < 4; i++) {
            intData = intData << 8;
            intData = intData + (buf[offset + i] & 0xFF);
        }
        return intData;
    }

    public static long readUInt32(byte[] buf, int offset) {
        long intData = 0;
        for (int i = 0; i < 4; i++) {
            intData = intData << 8;
            intData = intData + (buf[offset + i] & 0xFF);
        }
        return intData;
    }

    public static int readInt(byte[] buf, int offset) {
        return readInt32(buf, offset);
    }

    public static short readInt16(byte[] buf, int offset) {
        //Integer.reverseBytes()
        short intData = 0;
        for (int i = 0; i < 2; i++) {
            intData = (short) (intData << 8);
            intData = (short) (intData + (buf[offset + i] & 0xFF));
        }
        return intData;
    }

    public static int readUInt16(byte[] buf, int offset) {
        //Integer.reverseBytes()
        int intData = 0;
        for (int i = 0; i < 2; i++) {
            intData = (intData << 8);
            intData = (intData + (buf[offset + i] & 0xFF));
        }
        return intData;
    }

    public static short readShort(byte[] buf, int offset) {
        return readInt16(buf, offset);
    }

    public static float readFloat(byte[] buf, int offset) {
        int intData = readInt32(buf, offset);
        return Float.intBitsToFloat(intData);
    }

    public static double readDouble(byte[] buf, int offset) {
        long longData = readInt64(buf, offset);
        return Double.longBitsToDouble(longData);
    }

    public static String readString(byte[] buf, int length, int offset) {
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = (char) buf[offset + i];
        }
        return String.valueOf(str);
    }

    public static Date readDate(byte[] buf, int offset) {
        long dateTime = readLong(buf, offset);
        return new Date(dateTime);
    }

    public static Date readDateAsInt32(byte[] buf, int offset) {
        int intDate = readInt32(buf, offset);
        return DatetimeOpt.createUtilDate(intDate / 10000, intDate / 100 % 100, intDate % 100);
    }

    public static Date readDatetimeAsInt64(byte[] buf, int offset) {
        long longDate = readInt64(buf, offset);
        return DatetimeOpt.createUtilDate(
            (int) (longDate / 10000000000L),
            (int) (longDate / 100000000L % 100),
            (int) (longDate / 1000000L % 100),
            (int) (longDate / 10000L % 100),
            (int) (longDate / 100L % 100),
            (int) (longDate % 100));
    }

    public static Date readTimestampAsInt64(byte[] buf, int offset) {
        long longDate = readInt64(buf, offset);
        return DatetimeOpt.createUtilDate(
            (int) (longDate / 10000000000000L),
            (int) (longDate / 100000000000L % 100),
            (int) (longDate / 1000000000L % 100),
            (int) (longDate / 10000000L % 100),
            (int) (longDate / 100000L % 100),
            (int) (longDate / 1000L % 100),
            (int) (longDate % 1000));
    }
}
