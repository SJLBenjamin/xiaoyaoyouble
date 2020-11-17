package com.edc.xiaoyaoyouble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.service.autofill.FillEventHistory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import bean.*;


import static org.litepal.LitePalApplication.getContext;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context mContext = this;

    DeviceMirror mDeviceMirror;
    String TAG = "MainActivity1";
    boolean IsSendReadCMData = false;//是否发生了读的数据
    boolean IsVersionCMData = false;//是否获取版本号的数据
    byte debugCMD[] = {0x7E, 0x12, (byte) 0xAA, 0x09, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xc3, 0x7e};
    private TextView tvKData;
    private TextView tvBData;
    private TextView tvDeviceStatus;
    private TextView tvReceiveData;
    private Button btTime;
    private Button btStartDebug;
    private Button btStopDebug;
    private Button btOutputHight;
    private Button btOutputLow;
    private Button btInaZOpen;
    private Button btInaZClose;
    private Button btInaFOpen;
    private Button btInaFClose;
    private Button btSetTestCmd;
    private Button btReadTestCmd;
    private Button btAppStartCmd;
    private Button btAppEndCmd;
    private Button btDelay;
    private EditText etKData;
    private EditText etBData;
    private Button btVersion;
    private TextView tvVersion;
    private TextView tvMac;
    private TextView tvDataPackage;
    private boolean isConnect = false;
    private Calendar mCalendar;
    private Button btReconnect;
    private Button mBtDeviceList;
    private List<String> data_list = new ArrayList<String>();
    private List<BluetoothLeDevice> xiaoDeviceList = new ArrayList<BluetoothLeDevice>();
    private ArrayAdapter<String> arr_adapter;
    private ArrayList<Reissue> reissueList = new ArrayList<Reissue>();
    int mPosition = 0;
    private TextView mMTvSendData;
    private boolean mIsErrorUnconnect;
    private Button mMBtTimeSelect;
    private Button mMBtReissue;
    private TextView mMTvNumber;
    private EditText mMEtSerial;
    private ListView mLv_reissue;
    private ReissueAdapter mReissueAdapter;
    private boolean isIssue = false;
    private TextView mMRedLine;
    private boolean isSwitch = false;//是否重新搜索切换过
    private boolean isFirst = true;//是否第一次连接
    private EditText mEtRetransmissionsNumber;
    private TextView mTvNumber;
    private EditText mEtSerialSetId;
    private EditText mEtRestartSendId;
    private EditText mEtAccount;
    private boolean isAutoConnect = true;//是否自动连接
    private String deviceName="";//设备名称
    private EditText etBfCount;//补发的东西

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        initView();


//        Button bTime = (Button) findViewById(R.id.bt_time);
//        bTime.setOnClickListener(this);
//        bTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                byte[] b = {1, 2, 3, 4, 5, 6};
//                writeData(mDeviceMirror, b);
//            }
//        });
        EventBus.getDefault().register(this);
        onInitView();
    }


    private void initView() {
        tvMac = (TextView) findViewById(R.id.tv_mac);
        btReconnect = (Button) findViewById(R.id.bt_reconnect);
        btDelay = (Button) findViewById(R.id.bt_delay);
        tvDataPackage = (TextView) findViewById(R.id.tv_data_package);
        tvVersion = (TextView) findViewById(R.id.tv_version);
        btVersion = (Button) findViewById(R.id.bt_version);
        etKData = (EditText) findViewById(R.id.et_k_data);
        etBData = (EditText) findViewById(R.id.et_b_data);
        tvKData = (TextView) findViewById(R.id.tv_k_data);
        tvBData = (TextView) findViewById(R.id.tv_b_data);
        tvDeviceStatus = (TextView) findViewById(R.id.tv_device_status);
        tvReceiveData = (TextView) findViewById(R.id.tv_receive_data);
        btTime = (Button) findViewById(R.id.bt_time);
        btStartDebug = (Button) findViewById(R.id.bt_start_debug);
        btStopDebug = (Button) findViewById(R.id.bt_stop_debug);
        btOutputHight = (Button) findViewById(R.id.bt_output_hight);
        btOutputLow = (Button) findViewById(R.id.bt_output_low);
        btInaZOpen = (Button) findViewById(R.id.bt_ina_z_open);
        btInaZClose = (Button) findViewById(R.id.bt_ina_z_close);
        btInaFOpen = (Button) findViewById(R.id.bt_ina_f_open);
        btInaFClose = (Button) findViewById(R.id.bt_ina_f_close);
        btSetTestCmd = (Button) findViewById(R.id.bt_set_test_cmd);
        btReadTestCmd = (Button) findViewById(R.id.bt_read_test_cmd);
        btAppStartCmd = (Button) findViewById(R.id.bt_app_start_cmd);
        btAppEndCmd = (Button) findViewById(R.id.bt_app_end_cmd);
        mMTvSendData = (TextView) findViewById(R.id.tv_send_data);
        mMBtTimeSelect = (Button) findViewById(R.id.bt_time_select);
        mMBtReissue = (Button) findViewById(R.id.bt_reissue);
        mMEtSerial = (EditText) findViewById(R.id.et_serial);
        mLv_reissue = (ListView) findViewById(R.id.lv_reissue);
        Button btIssue = (Button) findViewById(R.id.bt_issue_data);
        mMRedLine = (TextView) findViewById(R.id.tv_rea_line);
        findViewById(R.id.tv_fzts).setOnClickListener(this);


        etBfCount = (EditText) findViewById(R.id.et_bf_count);

        findViewById(R.id.bt_set_id).setOnClickListener(this);//设置id按钮
        findViewById(R.id.bt_set_time).setOnClickListener(this);//设置id下的时间选择


        findViewById(R.id.bt_function_8).setOnClickListener(this);
        findViewById(R.id.bt_function_9).setOnClickListener(this);


        findViewById(R.id.bt_restart_send).setOnClickListener(this);
        mEtRestartSendId = (EditText) findViewById(R.id.et_restart_send_id);


        mEtSerialSetId = (EditText) findViewById(R.id.et_serial_set_id);//补发的序号


        mEtRetransmissionsNumber = (EditText) findViewById(R.id.et_retransmissions_number);//重发序号
        findViewById(R.id.bt_retransmissions).setOnClickListener(this);//重发按钮


        findViewById(R.id.bt_read_singer).setOnClickListener(this);//读内存地址


        findViewById(R.id.bt_device_date).setOnClickListener(this);//获取当前设备时间


        findViewById(R.id.bt_for_start).setOnClickListener(this);//循环启动


        mTvNumber = (TextView) findViewById(R.id.tv_number);//物理内存地址


        findViewById(R.id.bt_bind).setOnClickListener(this);//绑定
        mEtAccount = (EditText) findViewById(R.id.et_account);//账号
        findViewById(R.id.bt_connect).setOnClickListener(this);//连接
        findViewById(R.id.bt_unbind).setOnClickListener(this);//解绑
        findViewById(R.id.bt_qz_unbind).setOnClickListener(this);//强制解绑
        findViewById(R.id.bt_change_led).setOnClickListener(this);//清除id

        Switch aSwitch = (Switch) findViewById(R.id.sw_auto_connect);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAutoConnect = isChecked;//是否自动连接
                Log.d("自动连接", "" + isChecked);
                // Ble.options().setAutoConnect(isChecked);
            }
        });

        findViewById(R.id.bt_close).setOnClickListener(this);
        btTime.setOnClickListener(this);
        btStartDebug.setOnClickListener(this);
        btStopDebug.setOnClickListener(this);
        btOutputHight.setOnClickListener(this);
        btOutputLow.setOnClickListener(this);
        btInaZOpen.setOnClickListener(this);
        btInaZClose.setOnClickListener(this);
        btInaFOpen.setOnClickListener(this);
        btInaFClose.setOnClickListener(this);
        btSetTestCmd.setOnClickListener(this);
        btReadTestCmd.setOnClickListener(this);
        btAppStartCmd.setOnClickListener(this);
        btAppEndCmd.setOnClickListener(this);
        btVersion.setOnClickListener(this);
        btDelay.setOnClickListener(this);
        mMBtTimeSelect.setOnClickListener(this);
        mMBtReissue.setOnClickListener(this);

        findViewById(R.id.bt_app_pause_cmd).setOnClickListener(this);
        findViewById(R.id.bt_app_continue_cmd).setOnClickListener(this);
        findViewById(R.id.bt_app_device_status).setOnClickListener(this);//当前设备状态
        findViewById(R.id.bt_start_no_clear).setOnClickListener(this);//恢复不清0
        findViewById(R.id.bt_zl_reissue).setOnClickListener(this);//增量补发
        mBtDeviceList = (Button) findViewById(R.id.bt_device_list);
        findViewById(R.id.bt_news_data).setOnClickListener(this);//最新数据
        mBtDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OptionsPickerView pvSportStyleOptions = new OptionsPickerBuilder(MainActivity.this, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        ViseBle.getInstance().disconnect();
                        ViseBle.getInstance().connect(xiaoDeviceList.get(options1), xiaoConCallback);
                        mPosition = options1;
                        Log.d(TAG, "devicename==" + data_list.get(options1));
                        // Log.d(TAG, "options1==" + options1);
                        //Log.d(TAG, "device_list size==" + xiaoDeviceList.size());
//                        if (mBle.getConnetedDevices().size() == 0) {
//                            mBle.connect(device_list.get(options1), connectCallback);
//                        } else if (mBle.getConnetedDevices().get(0).getBleName().equals(data_list.get(options1))) {
//
//                        } else {
//                            mBle.disconnect(mBle.getConnetedDevices().get(0), new BleConnectCallback<BleDevice>() {
//                                @Override
//                                public void onConnectionChanged(BleDevice device) {
//                                    mPosition = postion;
//                                    mBle.connect(device_list.get(postion), connectCallback);
//                                }
//                            });
//                        }
                        //device_list.get(options1);
                        //tvSportStyle.setText(sportStyleName);
                    }
                }).build();
                pvSportStyleOptions.setPicker(data_list);
                pvSportStyleOptions.show();
            }
        });
        btIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReissueActivity.class);
                if (mDeviceMirror.getConnectState().getCode() == 1) {//如果当前设备时连接的话
                    intent.putExtra("deviceName", mDeviceMirror.getBluetoothLeDevice().getName());
                    intent.putExtra("deviceAddress", mDeviceMirror.getBluetoothLeDevice().getAddress());
                }
                startActivity(intent);
            }
        });
        mReissueAdapter = new ReissueAdapter();
        mLv_reissue.setAdapter(mReissueAdapter);


        //时间初始化
        Calendar cal = Calendar.getInstance();
        sendData16[0] = 7;
        sendData16[1] = (byte) (int) Integer.valueOf(Integer.valueOf(cal.get(Calendar.YEAR)).toString().substring(2));
        sendData16[2] = (byte) (cal.get(Calendar.MONTH) + 1);
        sendData16[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        sendData16[4] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
        sendData16[5] = (byte) (cal.get(Calendar.MINUTE));
        //适配器
        // arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        //arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
//        mMSpDeviceList.setAdapter(arr_adapter);
//        mMSpDeviceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(TAG, "position===" + position);
//
//                mPosition = position;
//                mBle.connect(device_list.get(position), connectCallback);
//                //Toast.makeText(mContext, "" + position, Toast.LENGTH_SHORT).show();
////                if (!isSwitch) {//默认是需要执行这个连接操作的
////                    //Log.d(TAG, "mPosition==" + mPosition);
////                   // Log.d(TAG, " device_list.get(mPosition)==" + device_list.get(mPosition));
////                    ArrayList<BleDevice> connectedDevices = mBle.getConnetedDevices();
////                    for (BleDevice bleDevice : connectedDevices) {
////                        mBle.disconnect(bleDevice);
////                    }
////                    mPosition = position;
////                    mBle.connect(device_list.get(position), connectCallback);
////                }else {//如果是代码里面设置的连接,那么不需要执行断开连接操作
////                    isSwitch = false;
////                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                Log.d(TAG, "onNothingSelected");
//            }
//        });
        btReconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deviceName ="";
                mBtDeviceList.setEnabled(false);//设置设备列表按钮不可点击
                btReconnect.setEnabled(false);//设置重新搜索按钮不可点击
                //mBle.startScan(scanCallback);
                ViseBle.getInstance().startScan(xiaoYaoScanCallback);
                // ArrayList<BleDevice> connectedDevices = mBle.getConnetedDevices();
                //data_list.clear();
                //device_list.clear();
                //tvDeviceStatus.setText("设备已断开");
//                if (connectedDevices.size() == 0) {
//                    mBle.startScan(scanCallback);
//                } else {
//                    for (BleDevice bleDevice : connectedDevices) {
//                        // mBle.disconnect(bleDevice);
//                        mBle.disconnect(bleDevice, new BleConnectCallback<BleDevice>() {
//                            @Override
//                            public void onConnectionChanged(BleDevice device) {
//                                //mMSpDeviceList.setEnabled(false);
//                                mBle.startScan(scanCallback);
//                            }
//                        });
//                    }
//                }

            }
        });
    }

    //开始搜索
    public void startScan(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtDeviceList.setEnabled(false);//设置设备列表按钮不可点击
                btReconnect.setEnabled(false);//设置重新搜索按钮不可点击
            }
        });
        xiaoDeviceList.clear();
        data_list.clear();
        ViseBle.getInstance().startScan(xiaoYaoScanCallback);
    }

    public void setXiaoReadUUid(final DeviceMirror deviceMirror) {
        BluetoothGattChannel bluetoothGattChanne2 = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setServiceUUID(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
                .setCharacteristicUUID(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"))
                .setDescriptorUUID(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"))
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {//绑定通道
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d(TAG, "bindChannelSuccess");

                mDeviceMirror.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
                    @Override
                    public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                        //  Log.d(TAG, "NotifySuccess=="+bytesToHexString(data, data.length));
                        // Log.d(TAG, "receiver: " + Arrays.toString(data));
                        //Log.d(TAG, "receiver: " + bytesToHexString(data, data.length));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (data.length < 3) {
                                    return;
                                }
                                byte receiveData[] = data;
                                if (receiveData[3]==0x0c) {
                                    IsSendReadCMData = false;
                                    byte[] b = new byte[5];
                                    System.arraycopy(receiveData, 4, b, 0, 5);
                                    tvKData.setText("k值" + Arrays.toString(b));
                                    System.arraycopy(receiveData, 9, b, 0, 5);
                                    tvBData.setText("b值" + Arrays.toString(b));
                                }
                                if (receiveData[3]==0x0d) {
                                    IsVersionCMData = false;
                                    byte[] b2 = new byte[10];
                                    System.arraycopy(receiveData, 4, b2, 0, 10);
                                    String versionString = "";
                                    for (int i = 0; i < b2.length; i++) {
                                        versionString += (char) b2[i];
                                    }
                                    tvVersion.setText("版本号" + versionString);
                                }

                                if (receiveData[3] != 0x00) {//如果收到的不是数据包指令,那么就将收发指令设置为false
                                    isIssue = false;
                                }
                                if (receiveData[3] == 0x00) {
                                    byte[] b3 = new byte[5];
                                    System.arraycopy(receiveData, 4, b3, 0, 5);
                                    // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
                                    Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
                                    int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
                                    //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
                                    String showSencondData = "时间" + Arrays.toString(b3);
                                    double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
                                    showSencondData += " i=" + dataL;
                                    double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
                                    DecimalFormat df = new DecimalFormat("#.00");
                                    showSencondData += " bat=" + df.format(batL);
                                    int cL = bytesToInt(receiveData[13], receiveData[14]);
                                    showSencondData += " 序列=" + cL;
                                    tvDataPackage.setText(showSencondData);
                                    new Reissue(mDeviceMirror.getBluetoothLeDevice().getName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
                                    if (isIssue) {
                                        reissueList.add(new Reissue("0", 0, showSencondData, bytesToHexString(receiveData, receiveData.length)));
                                        mReissueAdapter.notifyDataSetChanged();
                                    }
                                }

                                if (receiveData[3] == 0x06) {
                                    byte[] b3 = new byte[5];
                                    System.arraycopy(receiveData, 4, b3, 0, 5);
                                    // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
                                    Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
                                    int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
                                    //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
                                    String showSencondData = "时间为" + Arrays.toString(b3);
                                    double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
                                    showSencondData += "  i==" + dataL;
                                    double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
                                    DecimalFormat df = new DecimalFormat("#.00");
                                    showSencondData += "  bat==" + df.format(batL);
                                    int cL = bytesToInt(receiveData[13], receiveData[14]);
                                    showSencondData += "  序列号=" + cL;
                                    tvDataPackage.setText(showSencondData);
                                    new Reissue(mDeviceMirror.getBluetoothLeDevice().getName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
                                    if (isIssue) {
                                        reissueList.add(new Reissue("0", 0, showSencondData, bytesToHexString(receiveData, receiveData.length)));
                                        mReissueAdapter.notifyDataSetChanged();
                                    }
                                }
                                if(receiveData[3]==0x13){
                                    byte[] b3 = new byte[5];
                                    System.arraycopy(receiveData, 4, b3, 0, 5);
                                    // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
                                    //Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
                                    int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
                                    //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
                                    String showSencondData = "时间为" + Arrays.toString(b3);
                                    double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
                                    showSencondData += "  i==" + dataL;
                                    double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
                                    DecimalFormat df = new DecimalFormat("#.00");
                                    showSencondData += "  bat==" + df.format(batL);
                                    int cL = bytesToInt(receiveData[13], receiveData[14]);
                                    showSencondData += "  序列号=" + cL;
                                    tvDataPackage.setText(showSencondData);
                                }

                                if (receiveData[3] == 0x0a) {//此处用到的数据库是ReissueToRom数据库
                                    byte[] b3 = new byte[5];
                                    System.arraycopy(receiveData, 4, b3, 0, 5);
                                    // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
                                    //Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
                                    int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
                                    //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
                                    String showSencondData = "时间为" + Arrays.toString(b3);
                                    double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
                                    showSencondData += "  i==" + dataL;
                                    double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
                                    DecimalFormat df = new DecimalFormat("#.00");
                                    showSencondData += "  bat==" + df.format(batL);
                                    int cL = bytesToInt(receiveData[13], receiveData[14]);
                                    showSencondData += "  序列号=" + cL;
                                    tvDataPackage.setText(showSencondData);
                                    new ReissueToRom(mDeviceMirror.getBluetoothLeDevice().getName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
                                    if (isSinger) {
                                        isSinger = false;
                                        tvReceiveData.setText(bytesToHexString(receiveData, receiveData.length));
                                        return;
                                    }
                                    if (number2 < 1440) {
                                        number2++;
                                        byte[] sendPauseData19 = {0x0a, (byte) number2, (byte) (number2 >> 8), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                                        writeData(mDeviceMirror, sendPauseData19);
                                        mTvNumber.setText("" + number2);
                                    } else {
                                        number2 = 0;
                                    }


//                    new Reissue(mDeviceMirror.getBleName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
//                    if (isIssue) {
//                        reissueList.add(new Reissue("0", 0, showSencondData, bytesToHexString(receiveData, receiveData.length)));
//                        mReissueAdapter.notifyDataSetChanged();
//                    }
                                }
                                if (receiveData[3] == 0x07||receiveData[3] == 0x17) {//表示当前为补发指令
                                    switch (receiveData[15]) {
                                        case 0://数据匹配
                                            mLv_reissue.setVisibility(View.VISIBLE);
                                            mMRedLine.setVisibility(View.VISIBLE);
                                            reissueList.clear();
                                            mReissueAdapter.notifyDataSetChanged();
                                            Log.d(TAG, bytesToHexString(receiveData, receiveData.length));
                                            reissueList.add(new Reissue("0", 0, bytesToHexString(receiveData, receiveData.length), bytesToHexString(receiveData, receiveData.length)));
                                            isIssue = true;
                                            break;
                                        case 1://数据不存在
                                            Toast.makeText(mContext, "要求补发的序号不存在", Toast.LENGTH_LONG).show();
                                            mMRedLine.setVisibility(View.GONE);
                                            mLv_reissue.setVisibility(View.GONE);
                                            break;
                                        case 2:
                                            Toast.makeText(mContext, "CGM错误", Toast.LENGTH_LONG).show();
                                            mMRedLine.setVisibility(View.GONE);
                                            mLv_reissue.setVisibility(View.GONE);
                                            break;
                                        case 3:
                                            Toast.makeText(mContext, "时间序号不匹配", Toast.LENGTH_LONG).show();
                                            byte buFa[];
                                            if(!etBfCount.getText().toString().isEmpty()) {
                                                buFa = new byte[]{receiveData[3], receiveData[4], receiveData[5], receiveData[6], receiveData[7], receiveData[8], receiveData[13], receiveData[14], 0, 0, 0,0, (byte) (int) Integer.parseInt(etBfCount.getText().toString())};
                                            }else {
                                                buFa = new byte[]{receiveData[3], receiveData[4], receiveData[5], receiveData[6], receiveData[7], receiveData[8], receiveData[13], receiveData[14], 0, 0, 0, 0, 0};
                                            }
                                            writeData(mDeviceMirror, buFa);
                                            break;
                                        case 0x13:
                                            Toast.makeText(mContext, "数据包不足", Toast.LENGTH_LONG).show();
                                            mMRedLine.setVisibility(View.GONE);
                                            mLv_reissue.setVisibility(View.GONE);
                                            break;
                                        case 0x12:
                                            Toast.makeText(mContext, "增量请求错误", Toast.LENGTH_LONG).show();
                                            mMRedLine.setVisibility(View.GONE);
                                            mLv_reissue.setVisibility(View.GONE);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                if (receiveData[3] == 0x20) {//app绑定操作
                                    if(receiveData[10]!=1){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this,"绑定成功",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    //发送解绑
//                                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray("13612874251");
//                                    byte[] unBindData = {0x21, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
//                                    mDeviceMirror.writeData(sendData(unBindData));
                                }
                                if (receiveData[3] == 0x21) {//解绑操作
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this,"解绑成功",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                if (receiveData[3] == 0x22) {//保持连接操作

                                }
                                if(receiveData[3]==0x14){//辅助调试

                                }
                                tvReceiveData.setText(bytesToHexString(receiveData, receiveData.length));
                            }
                        });
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Log.d(TAG, "NotifyonFailure");
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d(TAG, "readOnFailure");
            }
        }, bluetoothGattChanne2);
        deviceMirror.registerNotify(false);


//        BluetoothGattChannel readBluetoothGattChanne = new BluetoothGattChannel.Builder()
//                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
//                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
//                .setServiceUUID(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
//                .setCharacteristicUUID(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"))
//                .setDescriptorUUID(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"))
//                .builder();
//        deviceMirror.bindChannel(new IBleCallback() {
//            @Override
//            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
//                // Log.d(TAG, "receiver: " + Arrays.toString(data));
//                Log.d(TAG, "receiver: " + bytesToHexString(data, data.length));
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (data.length < 3) {
//                            return;
//                        }
//                        byte receiveData[] = data;
//                        if (IsSendReadCMData) {
//                            IsSendReadCMData = false;
//                            byte[] b = new byte[5];
//                            System.arraycopy(receiveData, 4, b, 0, 5);
//                            tvKData.setText("k值" + Arrays.toString(b));
//                            System.arraycopy(receiveData, 9, b, 0, 5);
//                            tvBData.setText("b值" + Arrays.toString(b));
//                        }
//                        if (IsVersionCMData) {
//                            IsVersionCMData = false;
//                            byte[] b2 = new byte[10];
//                            System.arraycopy(receiveData, 4, b2, 0, 10);
//                            String versionString = "";
//                            for (int i = 0; i < b2.length; i++) {
//                                versionString += (char) b2[i];
//                            }
//                            tvVersion.setText("版本号" + versionString);
//                        }
//
//                        if (receiveData[3] != 0x00) {//如果收到的不是数据包指令,那么就将收发指令设置为false
//                            isIssue = false;
//                        }
//                        if (receiveData[3] == 0x00) {
//                            byte[] b3 = new byte[5];
//                            System.arraycopy(receiveData, 4, b3, 0, 5);
//                            // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
//                            Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
//                            int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
//                            //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
//                            String showSencondData = "时间为" + Arrays.toString(b3);
//                            double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
//                            showSencondData += "  i==" + dataL;
//                            double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
//                            DecimalFormat df = new DecimalFormat("#.00");
//                            showSencondData += "  bat==" + df.format(batL);
//                            int cL = bytesToInt(receiveData[13], receiveData[14]);
//                            showSencondData += "  序列号==" + cL;
//                            tvDataPackage.setText(showSencondData);
//                            new Reissue(mDeviceMirror.getBluetoothLeDevice().getName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
//                            if (isIssue) {
//                                reissueList.add(new Reissue("0", 0, showSencondData, bytesToHexString(receiveData, receiveData.length)));
//                                mReissueAdapter.notifyDataSetChanged();
//                            }
//                        }
//                        if (receiveData[3] == 0x06) {
//                            byte[] b3 = new byte[5];
//                            System.arraycopy(receiveData, 4, b3, 0, 5);
//                            // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
//                            Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
//                            int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
//                            //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
//                            String showSencondData = "时间为" + Arrays.toString(b3);
//                            double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
//                            showSencondData += "  i==" + dataL;
//                            double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
//                            DecimalFormat df = new DecimalFormat("#.00");
//                            showSencondData += "  bat==" + df.format(batL);
//                            int cL = bytesToInt(receiveData[13], receiveData[14]);
//                            showSencondData += "  序列号==" + cL;
//                            tvDataPackage.setText(showSencondData);
//                            new Reissue(mDeviceMirror.getBluetoothLeDevice().getName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
//                            if (isIssue) {
//                                reissueList.add(new Reissue("0", 0, showSencondData, bytesToHexString(receiveData, receiveData.length)));
//                                mReissueAdapter.notifyDataSetChanged();
//                            }
//                        }
//
//                        if (receiveData[3] == 0x0a) {//此处用到的数据库是ReissueToRom数据库
//                            byte[] b3 = new byte[5];
//                            System.arraycopy(receiveData, 4, b3, 0, 5);
//                            // tvDataPackage.setText(receiveData[4]+"年"+receiveData[5]+"月"+receiveData[6]+"日"+receiveData[7]+"分");
//                            //Toast.makeText(mContext, Arrays.toString(b3), Toast.LENGTH_SHORT).show();
//                            int ad = (int) ((receiveData[9] & 0x00ff) + ((receiveData[10] << 8) & 0x0000ff00)) / 10;
//                            //tvDataPackage.setText(Arrays.toString(b3)+"i="+(((receiveData[9]&0x00ff)+(((receiveData[10]<<8)&0x0000ff00))/10)+(((receiveData[9]&0x00ff)+((receiveData[10]<<8)&0x0000ff00))%10)*0.1));
//                            String showSencondData = "时间为" + Arrays.toString(b3);
//                            double dataL = (double) bytesToInt(receiveData[9], receiveData[10]) / 10;
//                            showSencondData += "  i==" + dataL;
//                            double batL = receiveData[12] + ((double) (receiveData[11] & 0x00ff)) / 256;
//                            DecimalFormat df = new DecimalFormat("#.00");
//                            showSencondData += "  bat==" + df.format(batL);
//                            int cL = bytesToInt(receiveData[13], receiveData[14]);
//                            showSencondData += "  序列号==" + cL;
//                            tvDataPackage.setText(showSencondData);
//                            new ReissueToRom(mDeviceMirror.getBluetoothLeDevice().getName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
//                            if (isSinger) {
//                                isSinger = false;
//                                return;
//                            }
//                            if (number2 < 1440) {
//                                number2++;
//                                byte[] sendPauseData19 = {0x0a, (byte) number2, (byte) (number2 >> 8), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//                                writeData(mDeviceMirror, sendPauseData19);
//                                mTvNumber.setText("" + number2);
//                            } else {
//                                number2 = 0;
//                            }
//
//
////                    new Reissue(mDeviceMirror.getBleName(), bytesToInt(receiveData[13], receiveData[14]), showSencondData, bytesToHexString(receiveData, receiveData.length)).save();
////                    if (isIssue) {
////                        reissueList.add(new Reissue("0", 0, showSencondData, bytesToHexString(receiveData, receiveData.length)));
////                        mReissueAdapter.notifyDataSetChanged();
////                    }
//                        }
//                        if (receiveData[3] == 0x07) {//表示当前为补发指令
//                            reissueList.add(new Reissue("0", 0, bytesToHexString(receiveData, receiveData.length), bytesToHexString(receiveData, receiveData.length)));
//                            mReissueAdapter.notifyDataSetChanged();
//                            isIssue = true;
//                        }
//                        if (receiveData[3] == 0x20) {//app绑定操作
//
//                        }
//                        if (receiveData[3] == 0x21) {//解绑操作
//
//                        }
//                        if (receiveData[3] == 0x22) {//保持连接操作
//
//                        }
//
//                        tvReceiveData.setText(bytesToHexString(receiveData, receiveData.length));
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(BleException exception) {
//
//            }
//        }, readBluetoothGattChanne);
//        deviceMirror.registerNotify(false);
    }

    public void setXiaoWriteUUid(final DeviceMirror deviceMirror) {
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
                .setCharacteristicUUID(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"))
                .setDescriptorUUID(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"))
                .builder();

        //写成功
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                if (isFirst) {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray("13612874251");
                    byte[] bindData = {0x22, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    mDeviceMirror.writeData(sendData(bindData));
                    isFirst = false;
                }
                Log.d(TAG, "WriteonSuccess==" + bytesToHexString(data, data.length));
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d(TAG, "onFailure");
            }
        }, bluetoothGattChannel);
    }

    IConnectCallback xiaoConCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(final DeviceMirror deviceMirror) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtDeviceList.setEnabled(true);
                    btReconnect.setEnabled(true);
                    isConnect = true;
                    tvDeviceStatus.setText("设备已连接");
                    deviceName = deviceMirror.getBluetoothLeDevice().getName();//设备名称
                    tvMac.setText("mac=" + deviceMirror.getBluetoothLeDevice().getAddress() + ",名字=" + deviceMirror.getBluetoothLeDevice().getName());
                    tvReceiveData.setText("");
                    tvDataPackage.setText("");
                    //mDeviceMirror = device;
                    mDeviceMirror = deviceMirror;
                }
            });
            Log.d(TAG, "onConnectSuccess");
            setXiaoReadUUid(deviceMirror);
            setXiaoWriteUUid(deviceMirror);
            //setNotify(device);
        }

        @Override
        public void onConnectFailure(BleException exception) {
            isFirst = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDeviceStatus.setText("设备连接失败");
                    tvMac.setText("");
                    tvReceiveData.setText("");
                    tvDataPackage.setText("");
                }
            });
            if (isAutoConnect) {
                startScan();
            }
        }

        @Override
        public void onDisconnect(boolean isActive) {
            isFirst = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDeviceStatus.setText("设备已断开");
                    tvMac.setText("");
                    tvReceiveData.setText("");
                    tvDataPackage.setText("");
                }
            });
            if (isAutoConnect) {
                startScan();
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReissueEventBus event) {
        //Toast.makeText(MainActivity.this, "数据已补发", Toast.LENGTH_SHORT).show();
        mLv_reissue.setVisibility(View.VISIBLE);
        mMRedLine.setVisibility(View.VISIBLE);
        reissueList.clear();
        mReissueAdapter.notifyDataSetChanged();
        /* Do something */
        byte[] data = event.getData();
        //Toast.makeText(MainActivity.this,bytesToHexString(data,data.length),Toast.LENGTH_SHORT).show();
        sendData16[0] = 7;
        sendData16[1] = data[4];
        sendData16[2] = data[5];
        sendData16[3] = data[6];
        sendData16[4] = data[7];
        sendData16[5] = data[8];
        sendData16[6] = data[13];
        sendData16[7] = data[14];
        if (mDeviceMirror != null) {
            Toast.makeText(MainActivity.this, "数据已补发", Toast.LENGTH_SHORT).show();
            mMEtSerial.setText(" " + bytesToInt(sendData16[6], sendData16[7]));
            writeData(mDeviceMirror, sendData16);
        }
    }


    class ReissueAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return reissueList.size();
        }

        @Override
        public Object getItem(int position) {
            return reissueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView name;
            TextView parseData;

        }


        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {  //
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_reissue, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.parseData = (TextView) convertView.findViewById(R.id.tv_parse_data);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //设置holder
            holder.name.setText(reissueList.get(position).getDetailData());
            holder.parseData.setText(reissueList.get(position).getData());
            return convertView;
        }
    }

//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (getIntent().getExtras() != null) {
//            mLv_reissue.setVisibility(View.VISIBLE);
//            mMRedLine.setVisibility(View.VISIBLE);
//            byte[] data = getIntent().getExtras().getByteArray("reissue");
//            //mMEtSerial.setText(""+data[2]);
//            sendData16[0] = 7;
//            sendData16[1] = data[4];
//            sendData16[2] = data[5];
//            sendData16[3] = data[6];
//            sendData16[4] = data[7];
//            sendData16[5] = data[8];
//            sendData16[6] = data[9];
//            sendData16[7] = data[10];
//            if (mDeviceMirror != null) {
//                Toast.makeText(MainActivity.this, "数据已补发", Toast.LENGTH_SHORT).show();
//                writeData(mDeviceMirror, sendData16);
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        if (!isConnect) {
            Toast.makeText(mContext, "设备未连接,请先连接设备", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.bt_time:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
//                mCalendar = Calendar.getInstance();
//                final byte[] sendData = {1, (byte) Integer.parseInt(("" + mCalendar.get(Calendar.YEAR)).substring(2)), (byte) (mCalendar.get(Calendar.MONTH) + 1), (byte) mCalendar.get(Calendar.DATE), (byte) mCalendar.get(Calendar.HOUR_OF_DAY), (byte) mCalendar.get(Calendar.MINUTE), (byte) mCalendar.get(Calendar.SECOND), 0, 0, 0, 0, 0, 0};
//                writeData(mDeviceMirror, sendData);

                //时间选择器
                TimePickerView pvTimetb = new TimePickerBuilder(MainActivity.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        //Toast.makeText(MainActivity.this,date.getDate()+"", Toast.LENGTH_SHORT).show();
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.setTime(date);


//                        sendData16[0]=7;
//                        sendData16[1]=(byte) (int)Integer.valueOf(Integer.valueOf(date.getYear()).toString().substring(1));
//                        sendData16[2]=(byte)(date.getMonth()+1);
//                        sendData16[3]=(byte)date.getDay();
//                        sendData16[4]=(byte) (date.getHours());
//                        sendData16[5]=(byte)(date.getMinutes());

                        byte[] sendData = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                        //Toast.makeText(MainActivity.this, selectedDate.get(Calendar.YEAR) + "", Toast.LENGTH_SHORT).show();
                        sendData[0] = 1;
                        sendData[1] = (byte) (int) Integer.valueOf(Integer.valueOf(selectedDate.get(Calendar.YEAR)).toString().substring(2));
                        Log.d(TAG, "year ==" + sendData[1]);
                        sendData[2] = (byte) (selectedDate.get(Calendar.MONTH) + 1);
                        sendData[3] = (byte) selectedDate.get(Calendar.DAY_OF_MONTH);
                        sendData[4] = (byte) (selectedDate.get(Calendar.HOUR_OF_DAY));
                        sendData[5] = (byte) (selectedDate.get(Calendar.MINUTE));
                        sendData[6] = (byte) (selectedDate.get(Calendar.SECOND));
                        //mCalendar = Calendar.getInstance();
                        writeData(mDeviceMirror, sendData);
                        // byte[] sendData16 = {7, (byte)date.getYear(), (byte)date.getMonth(), (byte)date.getDate(), (byte) date.getHours(), (byte)date.getMinutes(), 0, 0, 0, 0, 0, 0, 0};
                    }
                }).setType(new boolean[]{true, true, true, true, true, true})
                        .
                                build();// 默认全部显示.build();
                pvTimetb.show();
                break;
            case R.id.bt_app_end_cmd:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData1 = {3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData1);
                break;
            case R.id.bt_app_start_cmd:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                mCalendar = Calendar.getInstance();
                byte[] sendData2 = {2, 0, 0, 0, 0, 0, 0, 0, (byte) Integer.parseInt(("" + mCalendar.get(Calendar.YEAR)).substring(2)), (byte) (mCalendar.get(Calendar.MONTH) + 1), (byte) mCalendar.get(Calendar.DATE), (byte) mCalendar.get(Calendar.HOUR_OF_DAY), (byte) mCalendar.get(Calendar.MINUTE)};
                //byte[] sendData2 = {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData2);
                break;
            case R.id.bt_ina_f_close:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData3 = {9, 1, 0, 0x12, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData3);
                break;
            case R.id.bt_ina_f_open:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData4 = {9, 1, 0, 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData4);
                break;
            case R.id.bt_ina_z_close:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData5 = {9, 1, 0, 0x11, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData5);
                break;
            case R.id.bt_ina_z_open:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData6 = {9, 1, 0, 0x01, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData6);
                break;
            case R.id.bt_output_hight:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData7 = {9, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData7);
                break;
            case R.id.bt_output_low:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData8 = {9, 1, 0x11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData8);
                break;
            case R.id.bt_read_test_cmd://读取命令参数
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData9 = {0x0c, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData9);
                break;
            case R.id.bt_set_test_cmd://设置命令参数
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                String bdata = etBData.getText().toString();
                String kdata = etKData.getText().toString();
                Log.i(TAG, "bdata length=" + bdata.length());
                // byte[] sendData10 = {0x0b, (byte) Integer.parseInt(kdata.charAt(0) + ""), (byte) Integer.parseInt(kdata.charAt(1) + ""), (byte) Integer.parseInt(kdata.charAt(2) + ""), (byte) Integer.parseInt(kdata.charAt(3) + ""), (byte) Integer.parseInt(kdata.charAt(4) + ""),(byte) bdata.charAt(0), (byte) Integer.parseInt(bdata.charAt(1) + ""), (byte) Integer.parseInt(bdata.charAt(2) + ""), (byte) Integer.parseInt(bdata.charAt(3) + ""), (byte) Integer.parseInt(bdata.charAt(4) + ""), 0, 0};
                //bdata.length()>0?(byte) Integer.parseInt(kdata.charAt(0) + ""):0;
                byte[] sendData10 = {0x0b, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                //byte c ='+';
                for (int i = 0; i < bdata.length() && i < 5; i++) {
                    sendData10[1 + i] = (byte) Integer.parseInt(kdata.charAt(i) + "");
                }
                for (int j = 0; j < kdata.length() && j < 5; j++) {
                    if (j == 0) {
                        sendData10[6 + j] = (byte) bdata.charAt(0);
                    } else {
                        sendData10[6 + j] = (byte) Integer.parseInt(bdata.charAt(j) + "");
                    }
                }
                writeData(mDeviceMirror, sendData10);
                break;
            case R.id.bt_start_debug:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData11 = {9, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData11);
                break;
            case R.id.bt_stop_debug:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData12 = {9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData12);
                break;
            case R.id.bt_version:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData13 = {0x0d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData13);
                break;
            case R.id.bt_delay:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendData14 = {2, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendData14);
                break;
            case R.id.bt_app_continue_cmd:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                mCalendar = Calendar.getInstance();
                byte[] sendContinueData = {5, 0, 0, 0, 0, 0, 0, 0, (byte) Integer.parseInt(("" + mCalendar.get(Calendar.YEAR)).substring(2)), (byte) (mCalendar.get(Calendar.MONTH) + 1), (byte) mCalendar.get(Calendar.DATE), (byte) mCalendar.get(Calendar.HOUR_OF_DAY), (byte) mCalendar.get(Calendar.MINUTE)};
                // byte[] sendContinueData = {5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendContinueData);
                break;
            case R.id.bt_app_pause_cmd:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendPauseData = {4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendPauseData);
                break;
            case R.id.bt_time_select:
                //时间选择器
                TimePickerView pvTime = new TimePickerBuilder(MainActivity.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        //Toast.makeText(MainActivity.this,date.getDate()+"", Toast.LENGTH_SHORT).show();
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.setTime(date);

//                        sendData16[0]=7;
//                        sendData16[1]=(byte) (int)Integer.valueOf(Integer.valueOf(date.getYear()).toString().substring(1));
//                        sendData16[2]=(byte)(date.getMonth()+1);
//                        sendData16[3]=(byte)date.getDay();
//                        sendData16[4]=(byte) (date.getHours());
//                        sendData16[5]=(byte)(date.getMinutes());


                        //Toast.makeText(MainActivity.this, selectedDate.get(Calendar.YEAR) + "", Toast.LENGTH_SHORT).show();

                        sendData16[1] = (byte) (int) Integer.valueOf(Integer.valueOf(selectedDate.get(Calendar.YEAR)).toString().substring(2));
                        sendData16[2] = (byte) (selectedDate.get(Calendar.MONTH) + 1);
                        sendData16[3] = (byte) selectedDate.get(Calendar.DAY_OF_MONTH);
                        sendData16[4] = (byte) (selectedDate.get(Calendar.HOUR_OF_DAY));
                        sendData16[5] = (byte) (selectedDate.get(Calendar.MINUTE));
                        // byte[] sendData16 = {7, (byte)date.getYear(), (byte)date.getMonth(), (byte)date.getDate(), (byte) date.getHours(), (byte)date.getMinutes(), 0, 0, 0, 0, 0, 0, 0};
                    }
                }).setType(new boolean[]{true, true, true, true, true, false})
                        .build();// 默认全部显示.build();
                pvTime.show();
                break;
            case R.id.bt_reissue:
                sendData16[0] = 7;
                if(!etBfCount.getText().toString().isEmpty()){
                    sendData16[12] =(byte) (int) Integer.parseInt(etBfCount.getText().toString());
                }
                if (!mMEtSerial.getText().toString().isEmpty()) {
                    sendData16[6] = (byte) (int) Integer.parseInt(mMEtSerial.getText().toString());
                    sendData16[7] = (byte) ((int) Integer.parseInt(mMEtSerial.getText().toString()) >> 8);
                    writeData(mDeviceMirror, sendData16);
                } else {
                    Toast.makeText(MainActivity.this, "序号未输入", Toast.LENGTH_SHORT).show();
                    //Reissue last = LitePal.findLast(Reissue.class);
                    //last.getData
                    writeData(mDeviceMirror, sendData16);
                }
                break;

            case R.id.bt_zl_reissue:
                sendData16[0] = 0x17;
                if(!etBfCount.getText().toString().isEmpty()){
                    sendData16[12] =(byte) (int) Integer.parseInt(etBfCount.getText().toString());
                }
                if (!mMEtSerial.getText().toString().isEmpty()) {
                    sendData16[6] = (byte) (int) Integer.parseInt(mMEtSerial.getText().toString());
                    sendData16[7] = (byte) ((int) Integer.parseInt(mMEtSerial.getText().toString()) >> 8);
                    writeData(mDeviceMirror, sendData16);
                } else {
                    Toast.makeText(MainActivity.this, "序号未输入", Toast.LENGTH_SHORT).show();
                    //Reissue last = LitePal.findLast(Reissue.class);
                    //last.getData
                    writeData(mDeviceMirror, sendData16);
                }
                break;
            case R.id.bt_close:
                mMRedLine.setVisibility(View.GONE);
                mLv_reissue.setVisibility(View.GONE);
                break;
            case R.id.bt_app_device_status:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendPauseData17 = {0x0e, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendPauseData17);
                break;

            case R.id.bt_device_date:
                tvReceiveData.setText("");
                tvDataPackage.setText("");
                byte[] sendPauseData18 = {0x10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendPauseData18);
                break;
            case R.id.bt_retransmissions:
                if (!TextUtils.isEmpty(mEtRetransmissionsNumber.getText().toString())) {
                    number2 = Short.valueOf(mEtRetransmissionsNumber.getText().toString());
                }

                //sendData19[6] = (byte) (int) Integer.parseInt(mMEtSerial.getText().toString());
                //sendData16[7] = (byte) ((int) Integer.parseInt(mMEtSerial.getText().toString()) >> 8);
                byte[] sendPauseData19 = {0x0a, (byte) number2, (byte) (number2 >> 8), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendPauseData19);
                break;
            case R.id.bt_read_singer:
                short number4 = 0;
                if (!TextUtils.isEmpty(mEtRetransmissionsNumber.getText().toString())) {
                    number4 = Short.valueOf(mEtRetransmissionsNumber.getText().toString());
                    isSinger = true;
                } else {
                    Toast.makeText(mContext, "请先输入序号", Toast.LENGTH_SHORT);
                    break;
                }

                //sendData19[6] = (byte) (int) Integer.parseInt(mMEtSerial.getText().toString());
                //sendData16[7] = (byte) ((int) Integer.parseInt(mMEtSerial.getText().toString()) >> 8);
                byte[] sendPauseData20 = {0x0a, (byte) number4, (byte) (number4 >> 8), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, sendPauseData20);
                break;
            case R.id.bt_for_start:
                findViewById(R.id.bt_for_start).setClickable(false);
                //每9.5分钟开始一次
                TimerTask starTask = new TimerTask() {
                    @Override
                    public void run() {
                        mCalendar = Calendar.getInstance();
                        Log.d(TAG, "startCmd" + new SimpleDateFormat("yyyy-MM--dd HH:mm:ss").format(mCalendar.getTime()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvReceiveData.setText("");
                                tvDataPackage.setText("");
                            }
                        });
                        byte[] sendStartData = {2, 0, 0, 0, 0, 0, 0, 0, (byte) Integer.parseInt(("" + mCalendar.get(Calendar.YEAR)).substring(2)), (byte) (mCalendar.get(Calendar.MONTH) + 1), (byte) mCalendar.get(Calendar.DATE), (byte) mCalendar.get(Calendar.HOUR_OF_DAY), (byte) mCalendar.get(Calendar.MINUTE)};
                        //byte[] sendData2 = {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                        writeData(mDeviceMirror, sendStartData);
                    }
                };
                new Timer().schedule(starTask, 0, 1000 * 60 * 12);
                //每10分钟结束一次
                TimerTask endTask = new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(TAG, "endCmd" + new SimpleDateFormat("yyyy-MM--dd HH:mm:ss").format(mCalendar.getTime()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvReceiveData.setText("");
                                tvDataPackage.setText("");
                            }
                        });
                        byte[] sendEndData = {3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                        //byte[] sendData2 = {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                        writeData(mDeviceMirror, sendEndData);
                    }
                };
                new Timer().schedule(endTask, 1000 * 60 * 11 + 1000 * 30, 1000 * 60 * 11 + 1000 * 30);
                break;
            case R.id.bt_set_time:
                //时间选择器
                TimePickerView pvIdTime = new TimePickerBuilder(MainActivity.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.setTime(date);
                        setIdSendData[0] = 0x0F;
                        setIdSendData[1] = (byte) ((selectedDate.get(Calendar.YEAR) + "").charAt(2));
                        setIdSendData[2] = (byte) ((selectedDate.get(Calendar.YEAR) + "").charAt(3));
                        if (selectedDate.get(Calendar.MONTH) + 1 >= 10) {
                            Log.d(TAG, "month==" + (selectedDate.get(Calendar.MONTH) + 1) + "");
                            setIdSendData[3] = (byte) (((selectedDate.get(Calendar.MONTH) + 1) + "").charAt(0));
                            setIdSendData[4] = (byte) ((selectedDate.get(Calendar.MONTH) + 1) + "").charAt(1);
                        } else {
                            setIdSendData[3] = '0';
                            setIdSendData[4] = (byte) ((selectedDate.get(Calendar.MONTH) + 1) + "").charAt(0);
                        }

                        if (selectedDate.get(Calendar.DAY_OF_MONTH) >= 10) {
                            setIdSendData[5] = (byte) (selectedDate.get(Calendar.DAY_OF_MONTH) + "").charAt(0);
                            setIdSendData[6] = (byte) (selectedDate.get(Calendar.DAY_OF_MONTH) + "").charAt(1);
                        } else {
                            setIdSendData[5] = '0';
                            setIdSendData[6] = (byte) (selectedDate.get(Calendar.DAY_OF_MONTH) + "").charAt(0);
                        }
                    }
                }).setType(new boolean[]{true, true, true, true, true, false}).build();// 默认全部显示.build();
                pvIdTime.show();
                break;
            case R.id.bt_set_id:
                String setIdNumber = mEtSerialSetId.getText().toString().trim();
                if (setIdNumber.toCharArray().length < 2) {
                    Toast.makeText(mContext, "序号长度小于2", Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    setIdSendData[7] = (byte) setIdNumber.charAt(0);
                    setIdSendData[8] = (byte) setIdNumber.charAt(1);
                }
                writeData(mDeviceMirror, setIdSendData);
                break;
            case R.id.bt_function_8:
                byte[] function8SendData = {9, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, function8SendData);
                break;
            case R.id.bt_function_9:
                byte[] function9SendData = {9, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, function9SendData);
                break;
            case R.id.bt_restart_send:
                String restartId = mEtRestartSendId.getText().toString().trim();
                if (restartId.isEmpty()) {
                    Toast.makeText(mContext, "未输入序号", Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    byte[] restartSend = {6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    restartSend[10] = (byte) Integer.parseInt(restartId);
                    restartSend[11] = (byte) (Integer.parseInt(restartId) >> 8);
                    writeData(mDeviceMirror, restartSend);
                }
                break;
            case R.id.bt_start_no_clear:
                byte[] startNoClear = {2, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, startNoClear);
                break;
            case R.id.bt_bind://绑定操作
                if (mEtAccount.getText().toString().trim().isEmpty()) {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray("13612874251");
                    byte[] bindData = {0x20, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                } else {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray(mEtAccount.getText().toString().trim());
                    byte[] bindData = {0x20, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                }
                break;
            case R.id.bt_unbind://解绑操作
                if (mEtAccount.getText().toString().trim().isEmpty()) {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray("13612874251");
                    byte[] bindData = {0x21, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                } else {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray(mEtAccount.getText().toString().trim());
                    byte[] bindData = {0x21, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                }
                break;
            case R.id.bt_connect://保持连接操作
                if (mEtAccount.getText().toString().trim().isEmpty()) {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray("13612874251");
                    byte[] bindData = {0x22, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                } else {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray(mEtAccount.getText().toString().trim());
                    byte[] bindData = {0x22, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 0, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                }
                break;
            case R.id.bt_qz_unbind://解绑操作
                if (mEtAccount.getText().toString().trim().isEmpty()) {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray("13612874251");
                    byte[] bindData = {0x21, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 1, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                } else {
                    byte[] accountBytes = BitFieldUtils.biteFieldToByteArray(mEtAccount.getText().toString().trim());
                    byte[] bindData = {0x21, accountBytes[0], accountBytes[1], accountBytes[2], accountBytes[3], accountBytes[4], accountBytes[5], 1, 0, 0, 0, 0, 0};
                    writeData(mDeviceMirror, bindData);
                }
                break;
            case R.id.bt_news_data:
                byte[] newsData = {0x13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, newsData);
                break;
            case R.id.tv_fzts:
                byte[] fzts = {0x14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                writeData(mDeviceMirror, fzts);
                break;
            case R.id.bt_change_led:
                //0x7E 0x12 0xAA 0x09 0x01 0x00 0x00 0x00 0x00 0x00 0xFF 0x00 0x00 0x00 0x00 0x00
                byte[] changeLed = {0x09 ,0x01, 0x00, 0x00, 0x00, 0x00 ,0x00, (byte) 0xFF, 0x00 ,0x00, 0x00, 0x00, 0x00};
                writeData(mDeviceMirror, changeLed);
                break;
            default:
                break;
        }
    }

    boolean isSinger = false;//是否读单条

    public byte[] sendData16 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public byte[] setIdSendData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    protected void onInitView() {
        requestPermission(new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                "请求蓝牙相关权限", new GrantedResult() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            //初始化蓝牙
                            LitePal.getDatabase();
//                            Vibrator vibrator = (Vibrator)MainActivity.this.getSystemService(MainActivity.this.VIBRATOR_SERVICE);
//                            vibrator.vibrate(10000);
                            //LitePal.deleteAll(Reissue.class);
                            //initBle();
                            initXiaoYaoBle();
                        } else {
                            finish();
                        }
                    }
                });
        //initView();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == RESULT_OK & resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else if (requestCode == RESULT_OK & resultCode == Activity.RESULT_OK) {
            //6、若打开，则进行扫描
            // mBle.startScan(scanCallback);
            ViseBle.getInstance().startScan(xiaoYaoScanCallback);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void xiaoYaoCheckBle() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        // 初始化蓝牙适配器. API版本必须是18以上, 通过 BluetoothManager 获取到BLE的适配器.

        // 检查当前的蓝牙设别是否支持.
        if (adapter == null) {
            Toast.makeText(this, "不支持Ble蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //  if (!mBle.isBleEnable()) {

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RESULT_OK);
        } else {
            data_list.clear();
            xiaoDeviceList.clear();
            mBtDeviceList.setEnabled(false);
            btReconnect.setEnabled(false);
            ViseBle.getInstance().startScan(xiaoYaoScanCallback);
        }
    }


    /* 将byte数组转换为字符串,用16进制表示 */
    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || len <= 0) {
            System.out.println("src == null");
            return null;
        }
        for (int i = 0; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + ",");
        }
        return stringBuilder.toString();
    }


    ScanCallback xiaoYaoScanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {

            //Log.d(TAG, "onDeviceFound");
            if (bluetoothLeDevice.getName() != null && bluetoothLeDevice.getName().contains("Endoc")) {
                if (!data_list.contains(bluetoothLeDevice.getName())) {//如果搜索到没有存在设备列表的数据,那么直接添加
                    if(bluetoothLeDevice.getName().equals(deviceName)){
                        ViseBle.getInstance().connect(bluetoothLeDevice,xiaoConCallback);
                    }
                    data_list.add(bluetoothLeDevice.getName());
                    //device_list.add(device);
                    xiaoDeviceList.add(bluetoothLeDevice);
                    Log.d(TAG, "DeviceName==" + bluetoothLeDevice.getName());
                }
            }
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            mBtDeviceList.setEnabled(true);
            btReconnect.setEnabled(true);
        }

        @Override
        public void onScanTimeout() {
            mBtDeviceList.setEnabled(true);
            btReconnect.setEnabled(true);
        }
    });

    private void initXiaoYaoBle() {
        ViseBle.config()
                .setScanTimeout(10 * 1000)//扫描超时时间，这里设置为永久扫描
                .setConnectTimeout(10 * 1000)//连接超时时间
                .setOperateTimeout(5 * 1000)//设置数据操作超时时间
                .setConnectRetryCount(3)//设置连接失败重试次数
                .setConnectRetryInterval(1000)//设置连接失败重试间隔时间
                .setOperateRetryCount(3)//设置数据操作失败重试次数
                .setOperateRetryInterval(1000)//设置数据操作失败重试间隔时间
                .setMaxConnectCount(3);//设置最大连接设备数量
        //蓝牙信息初始化，全局唯一，必须在应用初始化时调用
        ViseBle.getInstance().init(MainActivity.this);
        xiaoYaoCheckBle();//检查是否支持蓝牙
    }

//    private void initBle() {
//        mBle = Ble.options()
//                .setLogBleExceptions(true)//设置是否输出打印蓝牙日志（非正式打包请设置为true，以便于调试）
//                .setThrowBleException(true)//设置是否抛出蓝牙异常
//                .setAutoConnect(true)//设置是否自动连接
//                .setConnectFailedRetryCount(3)
//                .setConnectTimeout(6 * 1000)//设置连接超时时长（默认10*1000 ms）
//                .setScanPeriod(10 * 1000)//设置扫描时长（默认10*1000 ms）
//                .setUuid_service(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))//主服务的uuid
//                .setUuid_write_cha(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"))//可写特征的uuid
//                .create(getApplicationContext());
//        checkBle();//检查是否支持蓝牙
//
////        new CountDownTimer(60 * 1000, 1000) {
////            @Override
////            public void onTick(final long millisUntilFinished) {
////                Log.d(TAG, millisUntilFinished / 1000 + "秒");
////                // TODO Auto-generated method stub
////                //textView_time_shengxu.setText("还剩" + millisUntilFinished / 1000 + "秒");
//////                mActivity.runOnUiThread(new Runnable() {
//////                    @Override
//////                    public void run() {
//////                        Log.d(TAG, millisUntilFinished / 1000 + "秒");
//////                        ToastUtils.showToast(mContext, "CountDownTimer");
//////                        mNormalDialog.setMessage(formatTime(millisUntilFinished));
//////                    }
//////                });
////
////            }
////
////            @Override
////            public void onFinish() {
////                // Log.d(TAG,"时间到");
////                //textView_time_shengxu.setText("时间已到,请重新获取验证码!!!");
////            }
////        }.start();
//
//    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        if (mBle != null) {
//            mBle.destory(getApplicationContext());
//        }
    }


    public static int bytesToInt(byte src, byte src2) {
        int value;
        value = (int) ((src & 0xFF)
                | ((src2 & 0xFF) << 8)) & 0x0000ffff;
        return value;
    }


    short number2 = 0;//读物理内存中的数据,从0到1439


    public byte[] sendData(byte data[]) {
        byte sendData[] = {0x7E, 0x12, (byte) 0xAA, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7e};
        for (int i = 0; i < 13; i++) {
            sendData[3 + i] = data[i];
        }
        for (int j = 0; j < sendData.length - 2; j++) {
            sendData[16] += sendData[j];
        }
        sendData[16] += sendData[17];
        // Log.d(TAG,"sendData=="+bytesToHexString(sendData,sendData.length));
        mMTvSendData.setText(bytesToHexString(sendData, sendData.length));
        return sendData;
    }

    public void writeData(DeviceMirror device, byte data[]) {
        device.writeData(sendData(data));

    }

//    public void writeData(BleDevice device, byte data[]) {
//        boolean result = mBle.write(device, sendData(data), new BleWriteCallback<BleDevice>() {
//            @Override
//            public void onWriteSuccess(BluetoothGattCharacteristic characteristic) {
//                byte sendData[] = characteristic.getValue();
//                mMTvSendData.setText(bytesToHexString(sendData, sendData.length));
//                if (sendData[3] == 0x0c) {
//                    IsSendReadCMData = true;//如果发生数据是读取参数命令
//                } else {
//                    IsSendReadCMData = false;
//                }
//                if (sendData[3] == 0x0d) {
//                    IsVersionCMData = true;//如果发生数据是读取参数命令
//                } else {
//                    IsVersionCMData = false;
//                }
//
//                Log.e(TAG, "onWriteSuccess: " + bytesToHexString(characteristic.getValue(), 18));
//                //Toast.makeText(mContext, "发送数据成功", Toast.LENGTH_SHORT).show();
//            }
//        });
//        if (!result) {
//            Log.e(TAG, "changeLevelInner: " + "发送数据失败!");
//        }
//    }


    /*---------------------------------------------------------------------------以下是android6.0动态授权的封装十分好用---------------------------------------------------------------------------*/
    private int mPermissionIdx = 0x10;//请求权限索引
    private SparseArray<GrantedResult> mPermissions = new SparseArray<>();//请求权限运行列表

    @SuppressLint("Override")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        GrantedResult runnable = mPermissions.get(requestCode);
        if (runnable == null) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            runnable.mGranted = true;
        }
        runOnUiThread(runnable);
    }

    public void requestPermission(String[] permissions, String reason, GrantedResult runnable) {
        if (runnable == null) {
            return;
        }
        runnable.mGranted = false;
        if (Build.VERSION.SDK_INT < 23 || permissions == null || permissions.length == 0) {
            runnable.mGranted = true;//新添加
            runOnUiThread(runnable);
            return;
        }
        final int requestCode = mPermissionIdx++;
        mPermissions.put(requestCode, runnable);

		/*
            是否需要请求权限
		 */
        boolean granted = true;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                granted = granted && checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            }
        }

        if (granted) {
            runnable.mGranted = true;
            runOnUiThread(runnable);
            return;
        }

		/*
			是否需要请求弹出窗
		 */
        boolean request = true;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request = request && !shouldShowRequestPermissionRationale(permission);
            }
        }

        if (!request) {
            final String[] permissionTemp = permissions;
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(reason)
                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(permissionTemp, requestCode);
                            }
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            GrantedResult runnable = mPermissions.get(requestCode);
                            if (runnable == null) {
                                return;
                            }
                            runnable.mGranted = false;
                            runOnUiThread(runnable);
                        }
                    }).create();
            dialog.show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, requestCode);
            }
        }
    }


    public static abstract class GrantedResult implements Runnable {
        private boolean mGranted;

        public abstract void onResult(boolean granted);

        @Override
        public void run() {
            onResult(mGranted);
        }
    }

}
