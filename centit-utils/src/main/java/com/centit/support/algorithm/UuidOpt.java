package com.centit.support.algorithm;

import java.util.UUID;
@SuppressWarnings("unused")
public abstract class UuidOpt {

    public static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
    
    public static String uuidToString32(UUID uuid){
        long leastSigBits =uuid.getLeastSignificantBits() ;
        long mostSigBits = uuid.getMostSignificantBits();
        return Long.toHexString(mostSigBits)+ Long.toHexString(leastSigBits);
    }
    
    public static String uuidToString36(UUID uuid){
        long leastSigBits =uuid.getLeastSignificantBits() ;
        long mostSigBits = uuid.getMostSignificantBits();
        return (digits(mostSigBits >> 32, 8) + "-" +
                digits(mostSigBits >> 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits >> 48, 4) + "-" +
                digits(leastSigBits, 12));
    }
    
    public static String getUuidAsString36(){
    	return uuidToString36(UUID.randomUUID());
    }
    
    public static String getUuidAsString32(){
    	return uuidToString32(UUID.randomUUID());
    }
    
    public static String getUuidAsString(){
    	return uuidToString32(UUID.randomUUID());
    }
}
