
package com.mediatek.factorymode;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.SystemProperties;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
//import android.util.TpdFWNative;
import android.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class DeviceInfo extends BaseTestActivity implements OnClickListener {
    public static final String TAG = "DeviceInfo";

    private TextView mStatus;
    private TextView mLevel;
    private TextView mMeid;
    private TextView mImei;
    private TextView mSn;
    private TextView mWifiMac;
    private TextView mTpFWVersion;
    private TextView mRfInfo;
    private TextView mBaInfo;
    private TextView mTPName;
    private TextView mTPVer;
    private TextView mLCDName;
    private TextView mLCDType;
    private TextView mGsensorInfo;
    private LinearLayout mMeidLayout;
    private LinearLayout mTpFWLayout;

    private TextView mToEM;

    private Button mBtOK;
    private Button mBtFailed;

    private TelephonyManager telephony;
    private WifiManager mWifi;
    private WifiInfo mWifiInfo;

    private SharedPreferences mSp;
    private String meid;
    private String imei1, imei2, snCheck, MotherBoader;
    private String snStr;
    private String meidStr = null;
    private String imeiStr = null;
    private String wifiMacStr = null;

    private final static String CUSTOM_VERSION="ro.custom.build.version";
    private final static String CUSTOM_VERSION_DATE="ro.build.date";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar.LayoutParams lp =new  ActionBar.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        View mView =  LayoutInflater.from(this).inflate(R.layout.title, new LinearLayout(this), false);
        TextView mTextView = (TextView) mView.findViewById(R.id.action_bar_title);
        getActionBar().setCustomView(mView, lp); 

        mTextView.setText(getTitle());

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        setContentView(R.layout.device_info);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        telephony = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        imei1 = telephony.getDeviceId(0);
        imei2 = telephony.getDeviceId(1);
        snStr = SystemProperties.get("ro.boot.serialno");
        snCheck = SystemProperties.get("gsm.serial");
        System.out.println("snCheck=["+snCheck.trim()+"]");
        String s1=null, s2=null;
        try {
            s1 = snCheck.substring(60, 61);
            s2 = snCheck.substring(61, 62);
            int i1 = Integer.parseInt(s1);
            int i2 = Integer.parseInt(s2);
            if(i1 * 10 + i2 == 10) {
                snCheck = getString(R.string.Success);//"success 10P";
            }else{
                snCheck = getString(R.string.Failed);//"fail";
            }
        } catch (Exception e) {
            snCheck = getString(R.string.Failed);//"fail";
        }

//    	boolean mFlag1 = false, mFlag2 = false, mFlag3 = false;
//    	try {
//			TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
//			String flag = telephonyManager.getDeviceCalibrationFlag();
//			Log.v("testResult", "testResult__flag = " + flag);
//			String flag1 = flag.substring(0, 1);
//			mFlag1 = flag1.equalsIgnoreCase("P");
//			String flag2 = flag.substring(1, 2);
//			mFlag2 = flag2.equalsIgnoreCase("P");
//			String flag3 = flag.substring(2, 3);
//			mFlag3 = flag3.equalsIgnoreCase("P");
//		} catch (Exception e) {
//			// TODO: handle exception
//			Log.e("testResult", "testResult__error");
//			e.printStackTrace();
//		}
//		String testResult = "";
//		if(mFlag1)
//		{
//			testResult = testResult + "校准：" + "PASS" + "\n";
//		}
//		else 
//		{
//			testResult = testResult + "校准：" + "FAIL" + "\n";
//		}
//		
//		if(mFlag2)
//		{
//			testResult = testResult + "综测：" + "PASS" + "\n";
//		}
//		else 
//		{
//			testResult = testResult + "综测：" + "FAIL" + "\n";
//		}
//		
//		if(mFlag3)
//		{
//			testResult = testResult + "耦合：" + "PASS";
//		}
//		else 
//		{
//			testResult = testResult + "耦合：" + "FAIL";
//		}
//		snCheck = testResult;

//        MotherBoader = "hct6737m_35g_n";
//        if(SystemProperties.getBoolean("ro.project.a863_w7", false)) {
//            MotherBoader = "A863_W7";
//        } else if(SystemProperties.getBoolean("ro.project.a863_kb", false)) {
//            MotherBoader = "A863_KB";
//        }
        
        MotherBoader = "MSM8909";

        String ddrType = "DDR";
        File file = new File("/sys/bus/platform/drivers/ddr_type/ddr_type");
        if(file.exists()) {
            ddrType = readFile(file);
        }
        MotherBoader += "\n" + ddrType;
    }

    @Override
    public void onResume() {
        super.onResume();

        mStatus = (TextView) findViewById(R.id.status);
        mLevel = (TextView) findViewById(R.id.level);
        mMeid = (TextView) findViewById(R.id.meid);
        mImei = (TextView) findViewById(R.id.imei);
        mSn = (TextView) findViewById(R.id.sn);
        mWifiMac = (TextView) findViewById(R.id.wifi_mac);
        mRfInfo = (TextView) findViewById(R.id.rf_info);
        mBaInfo = (TextView) findViewById(R.id.battery_info);
        mTPName = (TextView) findViewById(R.id.tp_name);
        mTPVer = (TextView) findViewById(R.id.tp_ver);
        mLCDName = (TextView) findViewById(R.id.sceen_name);
        mLCDType = (TextView) findViewById(R.id.sceen_type);
        mGsensorInfo = (TextView) findViewById(R.id.device_gsensor_info);
        mMeidLayout = (LinearLayout) findViewById(R.id.device_meid_layout);
        if(!SystemProperties.getBoolean("ro.mtk_c2k_support", false)) {
            mMeidLayout.setVisibility(View.GONE);
        }
        mTpFWLayout = (LinearLayout) findViewById(R.id.tp_version_layout);
        mTpFWVersion = (TextView) findViewById(R.id.tp_version);
        if(FactoryModeFeatureOption.CENON_TP_FW_VERSION_SUPPORT) {
            mTpFWLayout.setVisibility(View.VISIBLE);
            mTpFWVersion.setText(getTpFWVersion());
        }
        mToEM = (TextView) findViewById(R.id.to_engineer_mode);
        if("1".equals(SystemProperties.get("ro.mtk_gemini_support"))) {
            imei1 = SystemProperties.get("gsm.mtk.imei1");
            imei2 = SystemProperties.get("gsm.mtk.imei2");
        } else {
            imei1 = SystemProperties.get("gsm.mtk.imei1");
        }
        if(SystemProperties.getBoolean("ro.mtk_c2k_support", false)) {
            meid = SystemProperties.get("gsm.mtk.meid");
        }
        meidStr = meid;
//        imeiStr = (FactoryModeFeatureOption.MTK_GEMINI_SUPPORT) ? (imei1+"\n"+imei2): (imei1);
        imeiStr = telephony.getImei();
        mStatus.setText(MotherBoader);
//        mLevel.setText(setTextValue(CUSTOM_VERSION));
        mLevel.setText(getSoftwareVersion()+"");
        mMeid.setText(meidStr);
        mImei.setText(imeiStr);
        mSn.setText(snStr);
        updateWifiAddress();
        mRfInfo.setText(snCheck);
        mBaInfo.setText(readbatinfo());
        mTPName.setText(readInfo("/sys/readboy/tp_name"));
        mTPVer.setText(readInfo("/sys/readboy/tp_ver"));
        mLCDName.setText(readInfo("/sys/readboy/lcd_name"));
        mLCDType.setText(readInfo("/sys/readboy/lcd_type"));

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor motionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGsensorInfo.setText(motionSensor.getVendor()+"\n"+motionSensor.getName());

        mBtOK = (Button) findViewById(R.id.deviceinfo_bt_ok);
        mBtOK.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.deviceinfo_bt_failed);
        mBtFailed.setOnClickListener(this);

        mToEM.setOnClickListener(this);

    }


    public void onClick(View v) {
        if(v.getId() == mToEM.getId()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.mediatek.engineermode", "com.mediatek.engineermode.EngineerMode");
            startActivity(intent);
        } else {
            Utils.SetPreferences(this, mSp, R.string.device_info,
                    (v.getId() == mBtOK.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
            finish();
        }
    }

    private String setTextValue(String string){
        String buildver = "unknow";
        try {
            buildver = SystemProperties.get(string,"");
            return buildver;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildver;
    }
    private String getSoftwareVersion() {
        return android.os.Build.DISPLAY;
    }

    private static String readFile(File fn) {
        FileReader f;
        int len;

        f = null;
        try {
            f = new FileReader(fn);
            String s = "";
            char[] cbuf = new char[200];
            while ((len = f.read(cbuf, 0, cbuf.length)) >= 0) {
                s += String.valueOf(cbuf, 0, len);
            }
            s = s.substring(2, s.length() - 1);  //ellery add
            return s;
        } catch (IOException ex) {
            return "0";
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ex) {
                    return "0";
                }
            }
        }
    }

    private void updateWifiAddress() {
        mWifiInfo = mWifi.getConnectionInfo();
        wifiMacStr = mWifiInfo.getMacAddress();
        if(!mWifi.isWifiEnabled() && wifiMacStr.startsWith("02")) {
            mWifi.setWifiEnabled(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWifiInfo = mWifi.getConnectionInfo();
                    wifiMacStr = mWifiInfo.getMacAddress();
                    mWifiMac.setText(wifiMacStr);
//                    mWifi.setWifiEnabled(false);
                }
            }, 5000);
        } else {
            mWifiMac.setText(wifiMacStr);
        }
    }

    private String getTpFWVersion() {
//        TpdFWNative.openDev();
//        byte[] buff = new byte[2];
//        TpdFWNative.SetTpdFWVersion(buff);
//        TpdFWNative.closeDev();
//
//        Log.v(TAG, "TpVersion: " + buff[0] + ", " + buff[1]);
//        return buff[0] + "." + buff[1];
        return "aaaaaa";
    }
    
    public String readbatinfo() {
        String str1 = "/sys/readboy/battery_vendor";
    	File file = new File(str1);
        if(!file.exists()) {
        	return "未知(文件不存在)";
        }


        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 300);
            String str2 = localBufferedReader.readLine();
            Log.e("##sunshine##", "read = " + str2);
            if(null != str2) {
                if (str2.equals("Chinachip625_ls")) {
                    return "力神("+str2+")";
                } else if (str2.equals("Idea670_sp")) {
                    return "曙鹏("+str2+")";
                } else if (str2.equals("Idea660_zwd")) {
                    return "众旺德("+str2+")";
                } else {
                    return str2;
                }
            }
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "错误";
        }
        return "未知";
    }
    
    public String readInfo(String path) {
    	File file = new File(path);
        if(!file.exists()) {
        	return "未知(文件不存在)";
        }

        try {
            FileReader localFileReader = new FileReader(path);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 300);
            String str2 = localBufferedReader.readLine();
            Log.e("##sunshine##", "read = " + str2);
            if(null != str2) {
            	return str2;
            }
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "错误";
        }
        return "未知";
    }
}
