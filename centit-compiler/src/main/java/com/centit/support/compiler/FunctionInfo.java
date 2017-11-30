package com.centit.support.compiler;

public class FunctionInfo {

    public String  sName;
    public int     nPrmSum;
    public int     nFuncID;
    public int     nRetType;

    public FunctionInfo(String sN,int nP,int nF,int nR)
    {
        sName = sN;
        nPrmSum = nP;
        nFuncID =nF;
        nRetType = nR;
    }
    /*static public FunctionInfo newFunctionInfo(String sN,int nP,int nF,int nR)
    {
        return new FunctionInfo(sN, nP, nF, nR);

    }
    */
}
