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
        ByteBaseOpt.writeInt32(buf,1,0);

        //length
        ByteBaseOpt.writeInt32(buf,100,4);
        ByteBaseOpt.writeString(buf,"user1",8);
        ByteBaseOpt.writeInt32(buf,300,48);
        ByteBaseOpt.writeString(buf,"password1",52);
        ByteBaseOpt.writeString(buf,"1.02",68);
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
