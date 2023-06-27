package com.centit.support.compiler;

public class OptStack {
    /**
     * @param args 数值越小优先级越低
     */
    final static private int optsPri[] =
        {5, 5, 6, 6, 4, 4, 4, 4, 4, 4, 2, 3, 9, 8, 5, 5, 4,   7, 2, 3,  6, 6,   4, 4, 4, 4};
        //+ -  *  /  == >  < <=  >= != |  &  !  ^  >> << like in or and % dbmod xor
    //5 is normal
    private int sourceLen;
    private int optsStack[];

    public OptStack() {
        sourceLen = 0;
        optsStack = new int[10];
    }

    public void empty() {
        sourceLen = 0;
    }

    public int pushOpt(int optID) {
        if (sourceLen == 0 || optsPri[optID - ConstDefine.OP_BASE] > optsPri[optsStack[sourceLen - 1] - ConstDefine.OP_BASE]) {
            optsStack[sourceLen] = optID;
            sourceLen++;
            return 0;
        } else
            return popOpt();
    }

    public int popOpt() {
        if (sourceLen > 0)
            return optsStack[--sourceLen];
        return 0;
    }

}
