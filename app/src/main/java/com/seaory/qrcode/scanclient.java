package com.seaory.qrcode;

public class scanclient {
    SerialPort mSerialPort = new SerialPort();


    public void OpenPort(){
        mSerialPort.Open("/dev/ttyUSB0",9600,1,0);
    }
    public void run(){
        byte[] startBuff = new byte[]{0x16 ,0x56 ,0x0D ,0x21};
        try {
            mSerialPort.Write(startBuff,4);
            Thread.sleep(100);
            startBuff[1]=0x54;
            mSerialPort.Write(startBuff,4);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
