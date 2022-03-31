package com.centit.support.algorithm;

import org.apache.commons.codec.binary.Base64;

import java.util.UUID;

@SuppressWarnings("unused")
public abstract class UuidOpt {

    private UuidOpt() {
        throw new IllegalAccessError("Utility class");
    }

    public static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static String uuidToString32(UUID uuid) {
        long leastSigBits = uuid.getLeastSignificantBits();
        long mostSigBits = uuid.getMostSignificantBits();
        return digits(mostSigBits >> 32, 8) +
            digits(mostSigBits, 8) +
            digits(leastSigBits >> 32, 8) +
            digits(leastSigBits, 8);
        //Long.toHexString(mostSigBits) + Long.toHexString(leastSigBits);
    }

    public static String uuidToString36(UUID uuid) {
        long leastSigBits = uuid.getLeastSignificantBits();
        long mostSigBits = uuid.getMostSignificantBits();
        return digits(mostSigBits >> 32, 8) + "-" +
            digits(mostSigBits >> 16, 4) + "-" +
            digits(mostSigBits, 4) + "-" +
            digits(leastSigBits >> 48, 4) + "-" +
            digits(leastSigBits, 12);
    }

    public static String uuidToBase64String(UUID uuid) {
        byte[] buf = new byte[16];
        ByteBaseOpt.writeInt64(buf, uuid.getMostSignificantBits(), 0);
        ByteBaseOpt.writeInt64(buf, uuid.getLeastSignificantBits(), 8);
        return new String(Base64.encodeBase64URLSafe(buf), 0, 22);
    }

    public static String getUuidAsString36() {
        return uuidToString36(UUID.randomUUID());
    }

    public static String getUuidAsString32() {
        return uuidToString32(UUID.randomUUID());
    }

    public static String getUuidAsBase64String() {
        return uuidToBase64String(UUID.randomUUID());
    }

    public static String getUuidAsString22() {
        return uuidToBase64String(UUID.randomUUID());
    }

    public static String getUuidAsString() {
        return uuidToString32(UUID.randomUUID());
    }

    public static String randomString(int length){
        if (length < 1) {
            length = 8;// defalut length
        }
        StringBuilder sbBuilder = new StringBuilder(length + 2);
        String md5Hex = "";
        int nLen = 0;
        while (nLen < length) {
            md5Hex = getUuidAsString22();
            int md5Len = md5Hex.length();
            int copylen = md5Len < length - nLen ? md5Len : length - nLen;
            sbBuilder.append(md5Hex, 0, copylen);
            nLen += copylen;
            if (nLen == length) {
                break;
            }
        }
        return sbBuilder.toString();
    }
}
