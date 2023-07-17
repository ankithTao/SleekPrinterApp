package com.seaory.qrcode;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialPort {

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private boolean mopenflag=false;
    public SerialPort(){}

    public int Open(String devname, int baudrate, int Databit ,int Stopbit) {
        int RET = 0;
        File device = new File(devname);
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()|| !device.canWrite()) {
                    RET=1;
                }
            } catch (Exception e) {
                RET=2;
                return RET;
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate,Databit,Stopbit );
        if (mFd == null) {
            RET=3;
            return RET;
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        mopenflag=true;
        return RET;
    }

    public int Write(byte[] Data, int WrtLen)
    {
        int RET = 0;
        try {
            if(mopenflag)
                mFileOutputStream.write(Data, 0, WrtLen);
            else
            {
                RET=4;
            }
        } catch (IOException e) {
            RET = 1;
        }
        return RET;
    }

    public int Read(byte[] Buf, int[] ReadLen)
    {
        int RET = 0;
        int len;
        try {
            if(mopenflag){
                len =  mFileInputStream.read(Buf,0,ReadLen[0]);
                ReadLen[0]=len;
            }
            else			{
                RET=4;
            }
        } catch (IOException e) {
            RET = 1;
        }
        return RET;
    }

    public int Close(int ret)
    {
        if(mopenflag)close();
        mopenflag=false;
        return ret;
    }

    /**
     * @param path
     * @param baudrate
     * @param Databit
     * @param Stopbit
     * @return
     */
    // JNI
    private native static FileDescriptor open(String path, int baudrate, int Databit ,int Stopbit);
    private native void close();
    static {
        System.loadLibrary("serial_port");
    }
}
