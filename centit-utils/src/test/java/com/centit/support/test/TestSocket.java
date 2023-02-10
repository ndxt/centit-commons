package com.centit.support.test;

import com.centit.support.algorithm.ByteBaseOpt;

import java.net.Socket;

public class TestSocket {
    public static int generateCheckSum(byte[] buf, int bufLen) {
        int idx;
        long cks = 0;
        for (idx = 0; idx < bufLen; idx++) {
            cks += (buf[idx] & 0xFF);
        }
        return (int) (cks % 256);
    }

    public static synchronized int sendDataBySocket(Socket socket, byte[] buf, int offest, int length) {
        try {
            socket.getOutputStream().write(buf, offest, length);
            socket.getOutputStream().flush();
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        byte[] buf = new byte[120];
        //MsgType = 1
        ByteBaseOpt.writeInt32(buf, 1, 0);
        //length = 100
        ByteBaseOpt.writeInt32(buf, 92, 4);
        //SenderCompID = user1 长度 20
        ByteBaseOpt.writeStringApendSpace(buf, "user1", 20, 8);
        //TargetCompID 留空 长度也是 20
        //SenderCompID = user1 长度 20
        ByteBaseOpt.writeStringApendSpace(buf, "   ", 20, 28);
        //HeartBtInt 3
        ByteBaseOpt.writeInt32(buf, 3, 48);
        //Password = password1 长度 16
        ByteBaseOpt.writeStringApendSpace(buf, "password1", 16, 52);
        //DefaultAppVerID = 1.02 长度 32
        ByteBaseOpt.writeStringApendSpace(buf, "1.02", 32, 68);
        // 总长度刚好100 + 4位校验码
        ByteBaseOpt.writeInt32(buf, generateCheckSum(buf, 100), 100);
        try (Socket socket = new Socket("192.168.128.83", 5016)) {
            //socket.setKeepAlive(true);
            //socket.setTcpNoDelay(true);
            socket.getOutputStream().write(buf, 0, 104);
            socket.getOutputStream().flush();
            byte[] readbuf = new byte[2010];
            //socket.getOutputStream().notify();
            socket.getInputStream().read(readbuf, 0, 8);
            int datLen = ByteBaseOpt.readInt(readbuf, 4);
            socket.getInputStream().read(readbuf, 8, datLen < 2000 ? datLen : 2000);
            System.out.println(readbuf);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
