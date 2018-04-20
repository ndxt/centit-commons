package com.centit.test;

import com.centit.support.algorithm.ByteBaseOpt;

import java.net.Socket;

public class TestSocket {
    public static int generateCheckSum( byte [] buf, int bufLen )
    {
        int idx;
        long cks=0;
        for( idx = 0; idx < bufLen; idx++ ){
            cks += ( buf[ idx ] & 0xFF);
        }
        return (int)(cks & 0xFF);
    }

    public static void main(String[] args) {
        byte[] buf = new byte[2048];
        //MsgType = 1
        ByteBaseOpt.writeInt32(buf,1,0);
        //length = 100
        ByteBaseOpt.writeInt32(buf,100,4);
        //SenderCompID = user1 长度 20
        ByteBaseOpt.writeString(buf,"user1",8);
        //TargetCompID 留空 长度也是 20
        //HeartBtInt 3
        ByteBaseOpt.writeInt32(buf,3,48);
        //Password = password1 长度 16
        ByteBaseOpt.writeString(buf,"password1",52);
        //DefaultAppVerID = 1.02 长度 32
        ByteBaseOpt.writeString(buf,"1.02",68);
        // 总长度刚好100 + 4位校验码
        ByteBaseOpt.writeInt32(buf,generateCheckSum(buf,100),100);
        try  {
            Socket socket = new Socket("192.168.128.83", 5016);
            //socket.setKeepAlive(true);
            //socket.setTcpNoDelay(true);
            socket.getOutputStream().write(buf,0,104);
            socket.getOutputStream().flush();
            //socket.getOutputStream().notify();
            socket.getInputStream().read(buf,0,8);
            int datLen = ByteBaseOpt.readInt(buf,4);
            socket.getInputStream().read(buf,4,datLen<2000?datLen:2000);
            System.out.println(buf);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
