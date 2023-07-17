package com.seaory.seaorydemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;

import com.decard.NDKMethod.BasicOper;
import com.seaory.sdk.SeaorySDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class threadPrinter {
    private boolean bIsStop = true;
    private SeaorySDK PrinterFn;
    private Context mContext;
    private ItemPageCallback mItemPageCallback;
    private String resultStr;

    private threadPrinter(){

    }
    private static class Holder {
        private static final threadPrinter INSTANCE = new threadPrinter();
    }
    public static threadPrinter getInstance() {
        return Holder.INSTANCE;
    }

    public void setParameter(SeaorySDK sdk,Context context,ItemPageCallback itemPageCallback){
        PrinterFn = sdk;
        mContext = context;
        mItemPageCallback = itemPageCallback;
    }

    public void Stop(){
        bIsStop = true;
    }

    public boolean getStatus(){
        return bIsStop;
    }

    @SuppressLint("DefaultLocale")
    public void run(ArrayList<JSONObject> mJsonList, int count) throws JSONException, InterruptedException {
        int extraCount = 0;
        String funName;
        int status = 0;
        JSONObject args;
        if(count == 0)
            count = 999999999;
        bIsStop = false;

        showResultString(0, "Task start...\n");
        while (!bIsStop && extraCount < count){
            extraCount++;
            for(JSONObject jsonObject:mJsonList){
                resultStr = "";
                funName = jsonObject.getString("funName");

                Log.d("begin func",funName);
                if(funName.contains("ConnectPrinter ByIP")){
                    args = jsonObject.getJSONObject("ipAddr");
                    status = PrinterFn.OpenDeviceByIP(args.getString("ipAddr"));
                    resultStr = "OpenDeviceByIP return " + status + ".\n";
                }else if(funName.compareTo("ConnectPrinter") == 0){
                    status = PrinterFn.OpenDevice();
                    resultStr = "ConnectPrinter() return " + status + ".\n";
                }else if(funName.compareTo("DisconnectPrinter") == 0){
                    PrinterFn.CloseDevice();
                    status = 0;
                    resultStr = "DisconnectPrinter() return " + status + ".\n";
                }else if(funName.contains("ExecCommand") || funName.contains("MoveCard To")){
                    args = jsonObject.getJSONObject("ExecCommand");
                    int nCommand = args.getInt("nCommand");
                    status = executeCommand(funName,nCommand);
                }else if(funName.compareTo("PrintOneCard") == 0) {
                    args = jsonObject.getJSONObject("PrintOneCard");
                    String frontColor = null;
                    String frontK = null;
                    String frontO = null;
                    String backColor = null;
                    String backK = null;
                    String backO = null;
                    int nBrightness = args.getInt("nBrightness");
                    int nContrast = args.getInt("nContrast");
                    int nSharpness = args.getInt("nSharpness");
                    int nSaturation = args.getInt("nSaturation");
                    int nHeatFrontK = args.getInt("nHeatFrontK");
                    int nRespFrontK = args.getInt("nRespFrontK");

                    if(args.has("frontColor"))
                        frontColor = args.getString("frontColor");
                    if(args.has("frontK"))
                        frontK = args.getString("frontK");
                    if(args.has("frontO"))
                        frontO = args.getString("frontO");
                    if(args.has("backColor"))
                        backColor = args.getString("backColor");
                    if(args.has("backK"))
                        backK = args.getString("backK");
                    if(args.has("backO"))
                        backO = args.getString("backO");

                    PrinterFn.SetHeatingEnergy(
                            (short) 0,    //Front YMC
                            (short) nHeatFrontK,    //Front K
                            (short) 0,    //Front O
                            (short) 0,    //Front Resin K
                            (short) 0,    //Back  YMC
                            (short) 0,    //Back  K
                            (short) 0,    //Back  O
                            (short) 0,     //Back  Resin K
                            (short) nRespFrontK,    //Front K
                            (short) 0    //Back  K
                    );
                    status = PrinterFn.PrintOneCard(
                            loadBitmapFromAssets(frontColor),
                            loadBitmapFromAssets(frontK),
                            loadBitmapFromAssets(frontO),
                            loadBitmapFromAssets(backColor),
                            loadBitmapFromAssets(backK),
                            loadBitmapFromAssets(backO),
                            //color adjustment-----------
                            (short) nBrightness,    //Brightness			-100 ~ 100
                            (short) nContrast,      //Contrast			    -100 ~ 100
                            (short) nSharpness,     //Sharpness             0 ~ 100
                            (short) 0,              //Red Adjustment		-100 ~ 100
                            (short) 0,              //Green Adjustment	    -100 ~ 100
                            (short) 0,              //Blue Adjustment		-100 ~ 100
                            (short) nSaturation,    //Saturation			-100 ~ 100
                            (short) 100             //Gamma	(x0.01)         10 ~ 999
                    );
                    resultStr = "PrintOneCard() return status = " + status;
                    if (status != 0)
                        resultStr = resultStr + " (" + PrinterFn.errorMap.get((int) status) + ")";
                    resultStr = resultStr + "\n";
                }else if(funName.compareTo("printDemo") == 0) {
                    args = jsonObject.getJSONObject("printDemo");
                    String frontColor = args.getString("frontColor");
                    int nBrightness = args.getInt("nBrightness");
                    int nContrast = args.getInt("nContrast");
                    int nSharpness = args.getInt("nSharpness");
                    int nSaturation = args.getInt("nSaturation");

                    status = PrinterFn.PrintOneCard(
                            loadBitmapFromAssets(frontColor),
                            null,
                            null,
                            null,
                            null,
                            null,
                            //color adjustment-----------
                            (short) nBrightness,    //Brightness			-100 ~ 100
                            (short) nContrast,    //Contrast			    -100 ~ 100
                            (short) nSharpness,    //Sharpness			0 ~ 100
                            (short) 0,    //Red Adjustment		-100 ~ 100
                            (short) 0,    //Green Adjustment	    -100 ~ 100
                            (short) 0,    //Blue Adjustment		-100 ~ 100
                            (short) nSaturation,    //Saturation			-100 ~ 100
                            (short) 100  //Gamma	(x0.01)		10 ~ 999
                    );

                    resultStr = "PrintOneCard() return " + status;

                    if (status != 0)
                        resultStr = resultStr + " (" + PrinterFn.errorMap.get((int) status) + ")";
                    resultStr = resultStr + "\n";
                }else if(funName.compareTo("SimplePrint") == 0) {
                    args = jsonObject.getJSONObject("SimplePrint");
                    int nHeatFrontK = args.getInt("nHeatFrontK");
                    int nRespFrontK = args.getInt("nRespFrontK");
                    status = SimplePrint(nHeatFrontK,nRespFrontK);
                }else if(funName.contains("setCardInOut")) {
                    args = jsonObject.getJSONObject("setCardInOut");
                    int nConfigType = args.getInt("nConfigType");
                    int nConfigValue = args.getInt("nConfigValue");
                    status = PrinterFn.SetPrinterConfiguration(nConfigType, nConfigValue);

                    resultStr = "SetPrinterConfiguration return " + status + ".\n";
                    if (status != 0) {
                        resultStr = resultStr + PrinterFn.errorMap.get((int) status) + ".\n";
                    }
                }else if(funName.contains("setAutoEject")) {
                    args = jsonObject.getJSONObject("setAutoEject");
                    int nConfigType = args.getInt("nConfigType");
                    int nConfigValue = args.getInt("nConfigValue");
                    status = PrinterFn.SetPrinterConfiguration(nConfigType, nConfigValue);

                    resultStr = "SetPrinterConfiguration return " + status + ".\n";
                    if (status != 0) {
                        resultStr = resultStr + PrinterFn.errorMap.get((int) status) + ".\n";
                    }
                }else if(funName.compareTo("ReadTrack") == 0) {
                    char[] t1 = new char[512];
                    char[] t2 = new char[512];
                    char[] t3 = new char[512];

                    Arrays.fill(t1,0,512, (char) 0);
                    Arrays.fill(t2,0,512, (char) 0);
                    Arrays.fill(t3,0,512, (char) 0);

                    args = jsonObject.getJSONObject("ReadTrack");
                    int trackFlag = args.getInt("trackFlag");
                    String track1 = args.getString("track1");
                    String track2 = args.getString("track2");
                    String track3 = args.getString("track3");
                    status = PrinterFn.ReadTrack(trackFlag, t1, t2, t3);
                    if (status == 0) {
                        if(track1.compareTo(new String(t1).trim()) == 0 &&
                                track2.compareTo(new String(t2).trim()) == 0 &&
                                track3.compareTo(new String(t3).trim()) == 0){
                        }else{
                            status = 1;
                        }
                        resultStr = String.format("ReadTrack return %d .\ntrack1:%s\ntrack2:%s\ntrack3:%s\n",
                                status,new String(t1).trim(),new String(t2).trim(),new String(t3).trim() );
                    }else
                        resultStr = String.format("ReadTrack return %d .\n", status );
                }else if(funName.compareTo("EncodeTrack") == 0) {
                    args = jsonObject.getJSONObject("EncodeTrack");
                    int trackFlag = args.getInt("trackFlag");
                    int encMode = args.getInt("encMode");
                    String track1 = args.getString("track1");
                    String track2 = args.getString("track2");
                    String track3 = args.getString("track3");

                    status = PrinterFn.EncodeTrack(trackFlag, encMode, track1, track2, track3, 0);
                    resultStr = "EncodeTrack() return " + status + ".\n";
                }else if(funName.compareTo("CONTACT CPU") == 0) {
                    args = jsonObject.getJSONObject("CONTACT CPU");
                    int nCpuSlot = args.getInt("nCpuSlot");
                    status = Card_Cpu(nCpuSlot);
                }else if(funName.compareTo("dc_open") == 0) {
                    status = connectReader();
                }else if(funName.compareTo("dc_exit") == 0) {
                    status = disconnectReader();
                }else if(funName.compareTo("CPU TYPE A") == 0) {
                    status = cpuTypeA();
                }else if(funName.compareTo("CPU TYPE B") == 0) {
                    status = cpuTypeB();
                }else if(funName.compareTo("M1") == 0) {
                    status = M1();
                }
                showResultString(status,resultStr);
                Thread.sleep(50);
                if(status!=0)
                    break;
            }
        }
        bIsStop = true;
        showResultString(0, "Task stop...\n");
    }

    public int M1() {
        String result = null;
        String[] resultArr = null;
        int nSecNum = 2;
        int nBlockNum = 0;

        //射频复位
        result = BasicOper.dc_reset();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_reset", "success");
            resultStr = resultStr + "dc_reset() return success" + "\n";
        } else {
            Log.d("dc_reset", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_reset() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //配置非接触卡类型
        result = BasicOper.dc_config_card(0x00);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_config_card", "success ");
            resultStr = resultStr + "dc_config_card() return success" + "\n";
        } else {
            Log.d("dc_config_card", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_config_card() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        /*寻卡,防冲撞,选卡三合一(1级)*/
        result = BasicOper.dc_card_hex(1);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_card_hex", "success 卡片序列号：" + resultArr[1]);
            resultStr = resultStr + "dc_card_hex() return card SN = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_card_hex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_card_hex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        for (nSecNum = 0; nSecNum < 16; nSecNum++) {

            //注意：由于多次装载设备密码可能会受硬件存储寿命限制，此接口只用于密码相对固定，装载次数少的场合。
            /*装载设备密码(将密码存入设备flash)*/
            result = BasicOper.dc_load_key_hex(0, nSecNum, "FFFFFFFFFFFF");
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                Log.d("dc_load_key_hex", "success");
                resultStr = resultStr + "dc_load_key_hex(" + nSecNum + ") return success" + "\n";
            } else {
                Log.d("dc_load_key_hex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
                resultStr = resultStr + "dc_load_key_hex(" + nSecNum + ") return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
            }

            /*验证密钥(使用设备内部装载的密码来验证M1卡密码)*/
            result = BasicOper.dc_authentication(0, nSecNum);
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                Log.d("dc_authentication", "success");
                resultStr = resultStr + "dc_authentication(" + nSecNum + ") return success" + "\n";
            } else {
                Log.d("dc_authentication", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
                resultStr = resultStr + "dc_authentication(" + nSecNum + ") return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
            }


            /*读卡片扇区块数据*/
            for (int i = 0; i < 4; i++) {
                nBlockNum = nSecNum * 4 + i;
                result = BasicOper.dc_read_hex(nBlockNum);
                resultArr = result.split("\\|", -1);
                if (resultArr[0].equals("0000")) {
                    Log.d("dc_read_hex", "block [" + nBlockNum + "] data : " + resultArr[1]);
                    resultStr = resultStr + "dc_read_hex(" + nBlockNum + ") return success, data = " + resultArr[1] + "\n";
                } else {
                    Log.d("dc_read_hex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
                    resultStr = resultStr + "dc_read_hex(" + nBlockNum + ") return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
                }
            }
        }
        /*终止卡操作*/
        result = BasicOper.dc_halt();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_halt", "success");
            resultStr = resultStr + "dc_halt() return success" + "\n";
        } else {
            Log.d("dc_halt", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_halt() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        BasicOper.dc_beep(10);

        showResultString(100, "");
        return result.contains("0000")?0:1;
    }

    public int cpuTypeA() {
        String result = null;
        String[] resultArr = null;

        //射频复位
        result = BasicOper.dc_reset();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_reset", "success");
            resultStr = resultStr + "dc_reset() return success" + "\n";
        } else {
            Log.d("dc_reset", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_reset() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //配置非接触卡类型
        result = BasicOper.dc_config_card(0x00);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_config_card", "success ");
            resultStr = resultStr + "dc_config_card() return success" + "\n";
        } else {
            Log.d("dc_config_card", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_config_card() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //寻卡请求、防卡冲突、选卡操作
        result = BasicOper.dc_card_hex(0x01);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_card_hex", "success card sn = " + resultArr[1]);
            resultStr = resultStr + "dc_card_hex() return success, card sn = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_card_hex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_card_hex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //非接触式CPU卡复位
        result = BasicOper.dc_pro_resethex();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_pro_resethex", "success ATR/ATS = " + resultArr[1]);
            resultStr = resultStr + "dc_pro_resethex() return success, ATR/ATS = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_pro_resethex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_pro_resethex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //非接触式CPU卡指令交互
        /** ******由于底层C语言采用无符号类型unsign char ，超过255长度的apdu使用此接口可能会出现返回成功，但是apdu为空的问题，此情况请使用接口 dc_procommandInt_hex ********/
        result = BasicOper.dc_pro_commandhex("0084000008", 7);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_pro_commandhex", "success reponse apdu = " + resultArr[1]);
            resultStr = resultStr + "dc_pro_commandhex() return success, reponse apdu = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_pro_commandhex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_pro_commandhex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        // 终止非接触式CPU卡操作
        result = BasicOper.dc_pro_halt();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_pro_halt", "success");
            resultStr = resultStr + "dc_pro_halt() return success" + "\n";
        } else {
            Log.d("dc_pro_halt", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_pro_halt() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        BasicOper.dc_beep(10);

        return result.contains("0000")?0:1;
    }

    public int cpuTypeB() {
        String result = null;
        String[] resultArr = null;

        //射频复位
        result = BasicOper.dc_reset();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_reset", "success");
            resultStr = resultStr + "dc_reset() return success" + "\n";
        } else {
            Log.d("dc_reset", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_reset() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //配置非接触卡类型
        result = BasicOper.dc_config_card(1);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_config_card", "success ");
            resultStr = resultStr + "dc_config_card() return success" + "\n";
        } else {
            Log.d("dc_config_card", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_config_card() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //寻卡请求、防卡冲突、选卡操作
        result = BasicOper.dc_cardAB();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_card_hex", "success card sn = " + resultArr[1]);
            resultStr = resultStr + "dc_card_hex() return success, card sn = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_card_hex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_card_hex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //非接触式CPU卡复位
        result = BasicOper.dc_card_b_hex();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_pro_resethex", "success ATR/ATS = " + resultArr[1]);
            resultStr = resultStr + "dc_pro_resethex() return success, ATR/ATS = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_pro_resethex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_pro_resethex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //非接触式CPU卡指令交互
        /** ******由于底层C语言采用无符号类型unsign char ，超过255长度的apdu使用此接口可能会出现返回成功，但是apdu为空的问题，此情况请使用接口 dc_procommandInt_hex ********/
        result = BasicOper.dc_procommandInt_hex("0084000008", 7);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_pro_commandhex", "success reponse apdu = " + resultArr[1]);
            resultStr = resultStr + "dc_pro_commandhex() return success, reponse apdu = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_pro_commandhex", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_pro_commandhex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        // 终止非接触式CPU卡操作
        result = BasicOper.dc_pro_halt();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_pro_halt", "success");
            resultStr = resultStr + "dc_pro_halt() return success" + "\n";
        } else {
            Log.d("dc_pro_halt", "error code = " + resultArr[0] + " (" + resultArr[1] + ")");
            resultStr = resultStr + "dc_pro_halt() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        BasicOper.dc_beep(10);
        return result.contains("0000")?0:1;
    }

    public int disconnectReader() {
        int result = 0;
        result = BasicOper.dc_exit();
        resultStr = resultStr + "Disconnect reader return " + result + "\n";
        return result;
    }

    public int connectReader() {
    int status = 1;
        int mScardHandle = BasicOper.dc_open("AUSB", mContext, "", 0);
        if (mScardHandle > 0) {
            Log.d("open", "dc_open() success mScardHandle = " + mScardHandle);
            BasicOper.dc_beep(10);
            status = 0;
        }
        resultStr = resultStr + "Connect reader return " + mScardHandle + "\n";

        return status;
    }


    public int Card_Cpu(int nCpuSlot) {
        String result = null;
        String[] resultArr = null;

        //设置卡座
        result = BasicOper.dc_setcpu(nCpuSlot);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_setcpu", "success");
            resultStr = resultStr + "dc_setcpu() return success" + "\n";
        } else {
            Log.d("dc_setcpu", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            resultStr = resultStr + "dc_setcpu() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //设置接触式CPU卡参数
        result = BasicOper.dc_setcpupara(nCpuSlot, 0x00, 0x5C);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_setcpupara", "接触式CPU卡参数设置成功");
            resultStr = resultStr +  "dc_setcpupara() 接触式CPU卡参数设置成功" + "\n";
        } else {
            Log.d("dc_setcpupara", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            resultStr = resultStr +  "dc_setcpupara() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //检测接触式卡是否在位
        result = BasicOper.dc_card_status();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_card_status", "卡片存在");
            resultStr = resultStr +  "dc_card_status() 卡片存在" + "\n";
        } else {
            Log.d("dc_card_status", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            resultStr = resultStr +  "dc_card_status() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //接触式CPU卡复位
        result = BasicOper.dc_cpureset_hex();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_cpureset_hex", "卡片复位成功，ATR/ATS = " + resultArr[1]);
            resultStr = resultStr + "dc_cpureset_hex() 卡片复位成功，ATR/ATS = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_cpureset_hex", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            resultStr = resultStr + "dc_cpureset_hex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //接触式CPU卡指令交互
        result = BasicOper.dc_cpuapdu_hex("0084000008");
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_cpuapdu_hex", "success response = " + resultArr[1]);
            resultStr = resultStr + "dc_cpuapdu_hex() success response = " + resultArr[1] + "\n";
        } else {
            Log.d("dc_cpuapdu_hex", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            resultStr = resultStr + "dc_cpuapdu_hex() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        //对当前卡座CPU卡进行下电操作
        result = BasicOper.dc_cpudown();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_cpudown", "success");
            resultStr = resultStr + "dc_cpudown() return success" + "\n";
        } else {
            Log.d("dc_cpudown", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            resultStr = resultStr + "dc_cpudown() return error code = " + resultArr[0] + " (" + resultArr[1] + ")" + "\n";
        }

        BasicOper.dc_beep(10);
        return result.contains("0000")?0:1;
    }

    private int SimplePrint(int nHeatFrontK,int nRespFrontK){
        int status = 0;
        int nX = 0, nY = 0, nWidth = 0, nHeight = 0;
        boolean b600x600dpi = false;
        char[] szModel = new char[64];
        status = PrinterFn.GetPrinterInfo(SeaorySDK.INFO_MODEL_NAME, szModel);

        String modelName = new String(szModel).trim();
        if (modelName.compareTo("R600") == 0 || modelName.compareTo("R600M") == 0 || modelName.compareTo("DCE905") == 0 || modelName.compareTo("DR600") == 0 || modelName.compareTo("D600") == 0)
            b600x600dpi = true;

        SeaorySDK.SEAORY_DOC_PROP docProp = new SeaorySDK.SEAORY_DOC_PROP();
        docProp.byResolution = 0;//0=300x300dpi,1=300x600dpi

        PrinterFn.SetHeatingEnergy(
                (short) 0,    //Front YMC
                (short) nHeatFrontK,    //Front K
                (short) 0,    //Front O
                (short) 0,    //Front Resin K
                (short) 0,    //Back  YMC
                (short) 0,    //Back  K
                (short) 0,    //Back  O
                (short) 0,     //Back  Resin K
                (short) nRespFrontK,    //Front K
                (short) 0    //Back  K
        );

        status = PrinterFn.SOY_PR_StartPrinting(docProp);
        resultStr = "SOY_PR_StartPrinting() return " + status + ".\n";
        showResultString(100, resultStr);

        status = PrinterFn.SOY_PR_StartPage();
        resultStr = "SOY_PR_StartPage() return " + status + ".\n";
        showResultString(100, resultStr);

        Bitmap aBmp = loadBitmapFromAssets("img_300x400.png");
        nX = 60;
        nY = 120;
        nWidth = 300;
        nHeight = 400;
        if (b600x600dpi) {
            nX *= 2;
            nY *= 2;
            nWidth *= 2;
            nHeight *= 2;
        }
        status = PrinterFn.SOY_PR_PrintImage(nX, nY, nWidth, nHeight, aBmp);
        resultStr = "SOY_PR_PrintImage() return " + status + ".\n";
        showResultString(100, resultStr);

        String text1 = "姓名：刘德华";
        String text2 = "社会保障号码：430523199909099999";
        String text3 = "卡号：C1234567890";
        String text4 = "发卡日期：2020.09.09";

        String fontName = "simsun.ttc";
        float fFontSize = 8.5f;
        int nTextColor = 0;
        Typeface fontOrg = Typeface.createFromAsset(mContext.getAssets(), fontName);

        //int nFontStyle = Typeface.NORMAL;
        int nFontStyle = Typeface.BOLD;
        //int nFontStyle = Typeface.ITALIC;
        //int nFontStyle = Typeface.BOLD_ITALIC;
        Typeface fontNew = Typeface.create(fontOrg, nFontStyle);

        nX = 363;
        nY = 184;
        if (b600x600dpi) {
            nX *= 2;
            nY *= 2;
        }
        status = PrinterFn.SOY_PR_PrintText(nX, nY, text1, fontNew, fFontSize, nTextColor, true);
        resultStr = "SOY_PR_PrintText(1) return " + status + ".\n";
        showResultString(100, resultStr);

        nFontStyle = Typeface.NORMAL;
        fontNew = Typeface.create(fontOrg, nFontStyle);

        nX = 363;
        nY = 248;
        if (b600x600dpi) {
            nX *= 2;
            nY *= 2;
        }
        status = PrinterFn.SOY_PR_PrintText(nX, nY, text2, fontNew, fFontSize, nTextColor, true);
        resultStr = "SOY_PR_PrintText(2) return " + status + ".\n";
        showResultString(100, resultStr);

        nFontStyle = Typeface.ITALIC;
        fontNew = Typeface.create(fontOrg, nFontStyle);

        nX = 363;
        nY = 312;
        if (b600x600dpi) {
            nX *= 2;
            nY *= 2;
        }
        status = PrinterFn.SOY_PR_PrintText(nX, nY, text3, fontNew, fFontSize, nTextColor, true);
        resultStr = "SOY_PR_PrintText(3) return " + status + ".\n";
        showResultString(100, resultStr);

        nFontStyle = Typeface.BOLD_ITALIC;
        fontNew = Typeface.create(fontOrg, nFontStyle);

        nX = 363;
        nY = 376;
        if (b600x600dpi) {
            nX *= 2;
            nY *= 2;
        }
        status = PrinterFn.SOY_PR_PrintText(nX, nY, text4, fontNew, fFontSize, nTextColor, true);
        resultStr = "SOY_PR_PrintText(4) return " + status + ".\n";
        showResultString(100, resultStr);

        status = PrinterFn.SOY_PR_EndPage();
        resultStr = "SOY_PR_EndPage() return " + status + ".\n";
        showResultString(100, resultStr);

        status = PrinterFn.SOY_PR_EndPrinting(false);
        resultStr = "SOY_PR_EndPrinting() return " + status + ".\n";
        showResultString(100, resultStr);

        resultStr = "";
        if (status == 0)//send print data to printer ok
        {
            //wait printer to print card completely
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            int i = 0;
            while (true) {
                status = PrinterFn.CheckStatus();
                if (status != 170)
                    break;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                i++;
            }

            if (status != 0) {
                resultStr = "Printer status = " + status;
                resultStr += " (" + PrinterFn.errorMap.get((int) status) + ")";
                resultStr += "\n";
            }
        } else {
            resultStr = "";
        }

        return status;
    }

    private Bitmap loadBitmapFromAssets(String name) {

        if (name == null)
            return null;

        InputStream is = null;
        try {
            is = mContext.getAssets().open(name);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return BitmapFactory.decodeStream(is, null , null);
    }

    @SuppressLint({"SuspiciousIndentation", "DefaultLocale"})
    private int executeCommand(String funName,int nCommand) {

        int status = 0;
        try {
            status = PrinterFn.ExecCommand(nCommand);

            resultStr = String.format("%s return %d", funName, status);
            if ( status != 0 )
                resultStr += " (" + PrinterFn.errorMap.get((int) status) + ")";
            resultStr += "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private void showResultString(int what, String msgStr) {
        if(mItemPageCallback!=null) {
            mItemPageCallback.OnItemPageMessage(what,msgStr);
        }
    }
}
