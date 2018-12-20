
package com.mediatek.factorymode.sensor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.mediatek.factorymode.AllTest;
import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.BaseTestActivity;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.readboy.nv.NvJniItems;

public class GSensorWithCalibrate extends BaseTestActivity implements OnClickListener {
    private static final String Tag = "GSensorWithCalibrate";
    private ImageView ivimg;
    private TextView tvSensorX;
    private TextView tvSensorY;
    private TextView tvSensorZ;
    private TextView tvSensorResult;
    private Button btnSensorCalicate;
    private Button btnSensorReTest;

    private Button mBtOk;
    private Button mBtFailed;

    SharedPreferences mSp;
    SensorManager mSm = null;

    Sensor mGravitySensor;

    private final static int OFFSET = 2;

    private static final int MSG_GSENSOR_UPDATING = 0x11;
    private static final int MSG_GSENSOR_RESULT = 0x12;
    private static final int MSG_GSENSOR_RETEST = 0x13;
    private static final int MSG_DO_CALIBRATION_20 = 0x14;
    private static final int MSG_DO_CALIBRATING = 0x15;
    private static final int MSG_CALIBRATION_SUCCESS = 0x16;
    private static final int MSG_CALIBRATION_FAIL = 0x17;
    private int count = 0;
    private boolean result = false;
    private boolean fail = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GSENSOR_UPDATING:
                    tvSensorResult.setText(getResources().getString(R.string.GSensor_with_calibrate_result)
                            + (count % 3 == 0 ? "." : (count % 3 == 1 ? ".." : (count % 3 == 2 ? "..." : ""))));

                    if (count < 10) {
                        mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_UPDATING, 200);
                        count++;
                    } else {
                        count = 0;
                        mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_RESULT, 200);
                    }
                    break;
                case MSG_GSENSOR_RESULT:
                    if(fail){
                        tvSensorResult.setText(R.string.Failed);
                        mBtOk.setEnabled(false);
                        new AlertDialog.Builder(GSensorWithCalibrate.this).setTitle(
                                R.string.alert_gsensor_title).setMessage(
                                R.string.alert_gsensor_content).setPositiveButton(
                                R.string.alert_dialog_gsensor_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).create().show();
                    }else if (result) {
                        tvSensorResult.setText(R.string.Success);
                        btnSensorCalicate.setVisibility(View.GONE);
                        mBtOk.setEnabled(true);
                        if (AllTest.begin_auto_test) {
                            Utils.SetPreferences(GSensorWithCalibrate.this, mSp, R.string.gsensor_with_calibrate_name, AppDefine.FT_SUCCESS);
                            finish();
                        }
                    } else {
                        tvSensorResult.setText(R.string.Failed);
                        mBtOk.setEnabled(false);
                        btnSensorCalicate.setVisibility(View.VISIBLE);
                    }
                    count = 0;
                    break;
                case MSG_GSENSOR_RETEST:
                    mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_UPDATING, 20);
                    btnSensorReTest.setVisibility(View.GONE);
                    count = 0;
                    break;
                case MSG_DO_CALIBRATION_20:
                    btnSensorCalicate.setEnabled(false);
                    mHandler.sendEmptyMessageDelayed(MSG_DO_CALIBRATING, 20);
                    count = 0;
                    break;
                case MSG_DO_CALIBRATING:
                    tvSensorResult.setText(getResources().getString(R.string.GSensor_with_calibrate_do_calibrate)
                            + (count % 3 == 0 ? "." : (count % 3 == 1 ? ".." : (count % 3 == 2 ? "..." : ""))));

                    if (count < 50) {
                        mHandler.sendEmptyMessageDelayed(MSG_DO_CALIBRATING, 200);
                        count++;
                    } else {
                        count = 0;
                        mHandler.sendEmptyMessageDelayed(MSG_CALIBRATION_FAIL, 200);
                    }
                    break;
                case MSG_CALIBRATION_SUCCESS:
                    tvSensorResult.setText(R.string.proximity_success);
                    mHandler.removeMessages(MSG_DO_CALIBRATING);
                    count = 0;
                    btnSensorCalicate.setEnabled(true);
                    btnSensorCalicate.setVisibility(View.GONE);
                    btnSensorReTest.setVisibility(View.VISIBLE);

                    byte[] nvdata = NvJniItems.getInstance().getNv2499();//read 128 byte
                    String data = read_file("/sys/class/sensors/accelerometer/custom_driver/cali_data");
                    byte[] data_dyte = data.getBytes();
                    int l = data_dyte.length;
                    nvdata[79] = (byte) l;
                    Log.e("FactoryMode","GSensor l = " + l);
                    for (int i = 0; i < l; i++) {
                        nvdata[80 + i] = data_dyte[i];
                    }
                    NvJniItems.getInstance().writeNv2499(nvdata); //write 128 byte

                    break;
                case MSG_CALIBRATION_FAIL:
                    tvSensorResult.setText(R.string.proximity_fail);
                    mHandler.removeMessages(MSG_DO_CALIBRATING);
                    count = 0;
                    btnSensorCalicate.setEnabled(true);
                    btnSensorCalicate.setVisibility(View.VISIBLE);
                    btnSensorReTest.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.mediatek.factorymode.action_calibration_result".equals(action)) {
                boolean result = intent.getBooleanExtra("result", false);
                if (result) {
                    // success
                    mHandler.sendEmptyMessage(MSG_CALIBRATION_SUCCESS);
                } else {
                    // failed
                    mHandler.sendEmptyMessage(MSG_CALIBRATION_FAIL);
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        View mView = LayoutInflater.from(this).inflate(R.layout.title, new LinearLayout(this), false);
        TextView mTextView = (TextView) mView.findViewById(R.id.action_bar_title);
        getActionBar().setCustomView(mView, lp);

        mTextView.setText(getTitle());

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        setContentView(R.layout.gsensor_with_calibrate);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        ivimg = (ImageView) findViewById(R.id.gsensor_iv_img);
        tvSensorX = (TextView) findViewById(R.id.gsensor_tv_x);
        tvSensorY = (TextView) findViewById(R.id.gsensor_tv_y);
        tvSensorZ = (TextView) findViewById(R.id.gsensor_tv_z);
        tvSensorResult = (TextView) findViewById(R.id.gsensor_tv_result);
        mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_UPDATING, 200);
        btnSensorCalicate = (Button) findViewById(R.id.gsensor_btn_calicate);
        btnSensorCalicate.setOnClickListener(this);
        btnSensorReTest = (Button) findViewById(R.id.gsensor_btn_retest);
        btnSensorReTest.setOnClickListener(this);
        btnSensorReTest.setVisibility(View.GONE);
        mBtOk = (Button) findViewById(R.id.gsensor_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtOk.setEnabled(false);
        mBtFailed = (Button) findViewById(R.id.gsensor_bt_failed);
        mBtFailed.setOnClickListener(this);
        mSm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGravitySensor = mSm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER); //TYPE_GRAVITY  //ellery modify
        boolean suc = mSm.registerListener(lsn, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
        Log.d("cwj", "registerListener result:" + suc);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.mediatek.factorymode.action_calibration_result");
        registerReceiver(mReceiver, intentFilter);
    }

    protected void onDestroy() {
        mSm.unregisterListener(lsn);
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    SensorEventListener lsn = new SensorEventListener() {
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent e) {
            DecimalFormat decimalFormat = new DecimalFormat("0.000");
            float xx = e.values[SensorManager.DATA_X];
            float yy = e.values[SensorManager.DATA_Y];
            float zz = e.values[SensorManager.DATA_Z];
            Log.d("cwj", "xx:" + xx + " yy:" + yy + " zz:" + zz);
            tvSensorX.setText("X: " + ((xx >= 0.0) ? "+" : "") + decimalFormat.format(xx));
            tvSensorY.setText("Y: " + ((yy >= 0.0) ? "+" : "") + decimalFormat.format(yy));
            tvSensorZ.setText("Z: " + ((zz >= 0.0) ? "+" : "") + decimalFormat.format(zz));

            if (e.sensor == mGravitySensor) {
                float x = (float) e.values[SensorManager.DATA_X];
                float y = (float) e.values[SensorManager.DATA_Y];
                float z = (float) e.values[SensorManager.DATA_Z];

                float absx = Math.abs(x);
                float absy = Math.abs(y);
                float absz = Math.abs(z);

                /*boolean zzz = (absz >= 7.0 && absz <= 13.0);
                if(SystemProperties.get("ro.cenon_factorymode_feature").equals("1")) {
                    zzz = (absz >= 5.0 && absz <= 15.0);
                }
                if(absx <= 5.0 && absy <= 5.0 && zzz) {
                    result = true;
                } else {
                    result = false;
                }*/
                boolean zzz = (absz >= 8.5 && absz <= 11.5);
                if (absx <= 1.5 && absy <= 1.5 && zzz) {
                    result = true;
                } else {
                    result = false;
                }
                zzz = (absz >= 7.0 && absz <= 14.0);
                if (absx <= 5 && absy <= 5 && zzz) {
                    fail = false;
                }else{
                    fail = true;
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gsensor_bt_ok:
                Utils.SetPreferences(this, mSp, R.string.gsensor_with_calibrate_name, AppDefine.FT_SUCCESS);
                finish();
                break;
            case R.id.gsensor_bt_failed:
                Utils.SetPreferences(this, mSp, R.string.gsensor_with_calibrate_name, AppDefine.FT_FAILED);
                finish();
                break;
            case R.id.gsensor_btn_calicate:
                /*mHandler.sendEmptyMessage(MSG_DO_CALIBRATION_20);
                Intent intent = new Intent();
                intent.setAction("com.mediatek.engineermode.action_sensor_emsensor");
                sendBroadcast(intent);*/
                try {
                    write_file("/sys/class/sensors/accelerometer/custom_driver/enable_cali", "1");
                    mHandler.sendEmptyMessageDelayed(MSG_CALIBRATION_SUCCESS, 1 * 1000);
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessageDelayed(MSG_CALIBRATION_SUCCESS, 0);
                }
                break;
            case R.id.gsensor_btn_retest:
                mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_RETEST, 200);
                break;
            default:
                break;
        }
    }



    public static void write_file(String file_path, String msg) throws IOException {
        try {
            FileWriter fWriter = new FileWriter(file_path);
            //为了提高字符写入流的效率，加入了缓冲技术
            //只要将需要被提高效率的流对象作为参数传递给缓冲区的构造函数即可
            BufferedWriter bfWriter = new BufferedWriter(fWriter);
            Log.e(Tag, "write file:" + file_path + ", msg:" + msg);
            bfWriter.write(msg);
            bfWriter.newLine();
            //记住，只要用到缓冲区，就要记得刷新。
            fWriter.flush();
            //其实关闭缓存区，就是在关闭缓存区中流对象，所有不再需要关闭fWriter对象
            bfWriter.close();
        } catch (IOException e) {
            Log.e(Tag, "write file: " + file_path + "fail");
            e.printStackTrace();
            throw e;
        }
    }

    public static String read_file(String file_path) {
        try {
            FileReader fReader = new FileReader(file_path);
            BufferedReader bfReader = new BufferedReader(fReader);
            Log.e(Tag, "read file:" + file_path);
            String str = "";
            do {
                str += bfReader.readLine();
            } while (bfReader.read() != -1);
            Log.e(Tag, "read file:" + file_path + " str:" + str);
            bfReader.close();
            fReader.close();
            return str;
        } catch (IOException e) {
            Log.e(Tag, "read file:" + file_path + "fail");
            e.printStackTrace();
            return "";
        }
    }
}
