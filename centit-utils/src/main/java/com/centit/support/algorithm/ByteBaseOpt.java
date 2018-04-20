package com.centit.support.algorithm;

import java.sql.Timestamp;
import java.util.Date;

@SuppressWarnings("unused")
public abstract class ByteBaseOpt {
    private ByteBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    public static int writeInt64(byte [] buf, long data, int offset){
        for(int i=0;i<8;i++){
            buf[offset+7-i] = (byte)(data & 0xff);
            data = data>>8;
        }
        return offset + 8;
    }

    public static int writeLong(byte [] buf, long data, int offset){
        return writeInt64(buf, data, offset);
    }

    public static int writeInt32(byte [] buf, int data, int offset){
        //Integer.highestOneBit()
        for(int i=0;i<4;i++){
            buf[offset+3-i] = (byte)(data & 0xff);
            //System.out.println(buf[offset+3-i]);
            data = data>>8;
            //System.out.println(data);
        }
        return offset + 4;
    }

    public static int writeInt(byte [] buf, int data, int offset){
        return writeInt32(buf, data, offset);
    }

    public static int writeInt16(byte [] buf, short data, int offset){
        //Integer.reverseBytes()
        for(int i=0;i<2;i++){
            buf[offset+1-i] = Integer.valueOf(data & 0xff).byteValue();
            data = (short) (data >> 8);
        }
        return offset + 2;
    }

    public static int writeShort(byte [] buf, short data, int offset){
        return writeInt16(buf, data, offset);
    }

    public static int writeFloat(byte [] buf, float data, int offset){
        int intDate = Float.floatToIntBits(data);
        return writeInt32(buf,intDate,offset);
    }

    public static int writeDouble(byte [] buf, double data, int offset){
        long longDate = Double.doubleToLongBits(data);
        return writeInt64(buf,longDate,offset);
    }

    public static int writeString(byte [] buf, String data, int offset){
        for(int i=0;i<data.length();i++){
            buf[offset+i] = (byte) data.charAt(i);
        }
        return offset + data.length();
    }

    public static int writeString(byte [] buf, char[] data, int offset){
        for(int i=0;i<data.length;i++){
            buf[offset+i] = (byte) data[i];
        }
        return offset + data.length;
    }

    public static int writeDateAsInt32(byte [] buf, Date data, int offset){
        int intDate =
            DatetimeOpt.getYear(data) * 10000 +
            DatetimeOpt.getMonth(data) * 100 +
            DatetimeOpt.getDay(data);
        return writeInt32(buf, intDate, offset);
    }

    public static int writeDatetimeAsInt64(byte [] buf, Date data, int offset){
        long longDate =
                DatetimeOpt.getYear(data)  * 10000000000L +
                DatetimeOpt.getMonth(data) * 100000000L +
                DatetimeOpt.getDay(data)   * 1000000L +
                DatetimeOpt.getHour(data)  * 10000L +
                DatetimeOpt.getMinute(data)* 100L +
                DatetimeOpt.getSecond(data);
        return writeInt64(buf, longDate, offset);

    }

    public static int writeTimestampAsInt64(byte [] buf, Date data, int offset){
        long longDate =
                DatetimeOpt.getYear(data)  * 10000000000000L +
                DatetimeOpt.getMonth(data) * 100000000000L +
                DatetimeOpt.getDay(data)   * 1000000000L +
                DatetimeOpt.getHour(data)  * 10000000L +
                DatetimeOpt.getMinute(data)* 100000L +
                DatetimeOpt.getSecond(data)* 1000L +
                DatetimeOpt.getMilliSecond(data);
        return writeInt64(buf, longDate, offset);
    }


    /*-----------------read------------------------------------------------------------*/
    public static long readInt64(byte [] buf, int offset){
        long longData = 0;
        for(int i=0;i<8;i++){
            longData = longData << 8;
            longData = longData + (buf[offset+i] & 0x0FF);
        }
        return longData;
    }

    public static long readLong(byte [] buf, int offset){
        return readInt64(buf, offset);
    }

    public static int readInt32(byte [] buf, int offset){
        int intData = 0;
        for(int i=0;i<4;i++){
            intData = intData << 8;
            //System.out.println(buf[offset+i]);
            intData = intData + (buf[offset+i] & 0x0FF);
            //System.out.println(intData);
        }
        return intData;
    }

    public static int readInt(byte [] buf, int offset){
        return readInt32(buf, offset);
    }

    public static short readInt16(byte [] buf,  int offset){
        //Integer.reverseBytes()
        short intData = 0;
        for(int i=0;i<2;i++){
            intData = (short) (intData << 8);
            intData = (short) (intData + (buf[offset+i] & 0x0FF));
        }
        return intData;
    }

    public static short readShort(byte [] buf, int offset){
        return readInt16(buf, offset);
    }

    public static float readFloat(byte [] buf,  int offset){
        int intData = readInt32(buf,offset);
        return Float.intBitsToFloat(intData);
    }

    public static double readDouble(byte [] buf, int offset){
        long longData = readInt64(buf,offset);
        return Double.longBitsToDouble(longData);
    }

    public static String readString(byte [] buf, int length, int offset){
        char [] str = new char[length];
        for(int i=0;i<length;i++){
            str[i] = (char)buf[offset+i];
        }
        return String.valueOf(str);
    }

    public static Date readDateAsInt32(byte [] buf, int offset){
        int intDate = readInt32(buf, offset);
        return DatetimeOpt.createUtilDate(intDate / 10000, intDate / 100 % 100, intDate % 100);
    }

    public static Date readDatetimeAsInt64(byte [] buf, int offset){
        long longDate = readInt64(buf, offset);
        return DatetimeOpt.createUtilDate(
                (int)(longDate / 10000000000L),
                (int)(longDate / 100000000L % 100),
                (int)(longDate  / 1000000L % 100),
                (int)(longDate / 10000L% 100),
                (int)(longDate / 100L % 100),
                (int)(longDate % 100));
    }

    public static Date readTimestampAsInt64(byte [] buf, int offset){
        long longDate = readInt64(buf, offset);
        return DatetimeOpt.createUtilDate(
                (int)(longDate / 10000000000000L),
                (int)(longDate / 100000000000L % 100),
                (int)(longDate  / 1000000000L % 100),
                (int)(longDate / 10000000L% 100),
                (int)(longDate / 100000L % 100),
                (int)(longDate  / 1000L % 100),
                (int)(longDate % 1000));
    }
}
