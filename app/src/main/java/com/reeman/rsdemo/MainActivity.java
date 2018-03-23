package com.reeman.rsdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.reeman.nerves.RobotActionProvider;
import com.rsc.aidl.OnPrintListener;
import com.rsc.impl.OnROSListener;
import com.rsc.impl.RscServiceConnectionImpl;
import com.rsc.reemanclient.ConnectServer;
import com.synjones.idcard.IDCardInfo;
import com.synjones.idcard.OnIDListener;

public class MainActivity extends Activity {

    private ConnectServer cs;
    private TextView idT;
    private TextView wakeT;
    private TextView head_msg;
    private Button printTxT, threed;
    private Button click_old;
    private ImageView photoV;
    private int idregnum = 0;
    private int argmode = 0;
    private TextView hw1, hw2, hw3, hw4;
    private TextView cb1, cb2, cb3, cb4, cb5, cb6, cb7;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("demo", "action = " + action);
            if ("REEMAN_LAST_MOVTION".equals(action)) {
                int t = intent.getIntExtra("REEMAN_MOVTION_TYPE", 0);
                Log.v("demo", "type：" + t);
                wakeT.setText("收到反馈：" + t);
            } else if ("REEMAN_BROADCAST_SCRAMSTATE".equals(action)) {
                int value = intent.getIntExtra("SCRAM_STATE", -1);
                if (value == 0) {
                    // 按下
                    wakeT.setBackgroundColor(Color.RED);
                } else if (value == 1) {
                    wakeT.setBackgroundColor(Color.GREEN);
                }
            } else if ("REEMAN_BODY_POSITION".equals(action)) {
                int cmd = intent.getIntExtra("CMD", -1);
                int mode = intent.getIntExtra("MODE", -1);
                String data = intent.getStringExtra("DATA");
                Log.v("demo", "cmd: " + cmd + " ,mode: " + mode + " ,data: "
                        + data);
                head_msg.setText(data);
            } else if ("REEMAN_BROADCAST_HIS_STATE".equals(action)) {
                int type = intent.getIntExtra("HIS_STATE", -1);
                Log.v("demo", "HIS_STATE: " + type);
                wakeT.setText("HIS_STATE: " + type);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Log.v("demo", "onCreate ");
        idT = findViewById(R.id.id);
        wakeT = findViewById(R.id.wake);
        head_msg = findViewById(R.id.head_msg);
        printTxT = findViewById(R.id.print_txt);
        click_old = findViewById(R.id.click_old);
        photoV = findViewById(R.id.photo);
        hw1 = findViewById(R.id.hw1);
        hw2 = findViewById(R.id.hw2);
        hw3 = findViewById(R.id.hw3);
        hw4 = findViewById(R.id.hw4);

        cb1 = findViewById(R.id.cb1);
        cb2 = findViewById(R.id.cb2);
        cb3 = findViewById(R.id.cb3);
        cb4 = findViewById(R.id.cb4);
        cb5 = findViewById(R.id.cb5);
        cb6 = findViewById(R.id.cb6);
        cb7 = findViewById(R.id.cb7);

        threed = findViewById(R.id.threed);

        IntentFilter filter = new IntentFilter();
        filter.addAction("REEMAN_LAST_MOVTION");
        filter.addAction("REEMAN_BROADCAST_SCRAMSTATE");
        filter.addAction("REEMAN_BODY_POSITION");
        filter.addAction("REEMAN_BROADCAST_HIS_STATE");
        registerReceiver(receiver, filter);
        argmode = RobotActionProvider.getInstance().getArgMode();
        System.out.println("mode: "
                + RobotActionProvider.getInstance().getBottomMode()
                + ", argmode: " + argmode + ", headmode: "
                + RobotActionProvider.getInstance().getHeadMode());
        System.out.println("Product Model: " + android.os.Build.MODEL + ","
                + android.os.Build.VERSION.SDK_INT + ","
                + android.os.Build.VERSION.RELEASE);
        System.out.println("sdk version: "
                + RobotActionProvider.getInstance().getSDKVersion());

        threed.setText(threed.getText() + " <设备ID: " + RobotActionProvider.getInstance().getRobotID() + ">");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("demo", "onResume ");
        canP = true;
        cs = ConnectServer.getInstance(getApplication(), impl);
        cs.registerROSListener(rosList);
        int stopState = RobotActionProvider.getInstance().getScramState();
        if (stopState == 0) {
            // 按下
            wakeT.setBackgroundColor(Color.RED);
        } else if (stopState == 1) {
            wakeT.setBackgroundColor(Color.GREEN);
        }
        mHandler.sendEmptyMessage(9);
    }

    private RscServiceConnectionImpl impl = new RscServiceConnectionImpl() {
        @Override
        public void onServiceConnected(int name) {
            if (cs == null)
                return;
            if (name == ConnectServer.Connect_Pr_Id) {
                Log.v("demo", "Connect_Pr_Id ");
                cs.registerIDListener(Ilistener);
            }
        }

        @Override
        public void onServiceDisconnected(int name) {
            System.out.println("onServiceDisconnected......");
        }
    };

    private OnROSListener rosList = new OnROSListener() {
        @Override
        public void onResult(String ttys3) {
            System.out.println("ttys3: " + ttys3);
        }
    };

    private OnIDListener Ilistener = new OnIDListener.Stub() {

        @Override
        public void onResult(IDCardInfo info, byte[] photo)
                throws RemoteException {
            Log.e("ID",
                    "name: " + info.getName() + ",nation: " + info.getNation()
                            + ",birthday: " + info.getBirthday() + ",sex: "
                            + info.getSex() + ",address: " + info.getAddress()
                            + ",append: " + info.getAppendAddress()
                            + ",fpname: " + info.getFpName() + ",grantdept: "
                            + info.getGrantdept() + ",idcardno: "
                            + info.getIdcardno() + ",lifebegin: "
                            + info.getUserlifebegin() + ",lifeend: "
                            + info.getUserlifeend());
            if (photo != null)
                Log.e("ID", "photo: " + photo.length);
            else
                Log.e("ID", "photo=null");
            Bundle bundle = new Bundle();
            bundle.putByteArray("photo", photo);
            bundle.putParcelable("info", info);
            Message msg = mHandler.obtainMessage();
            msg.setData(bundle);
            msg.what = 1;
            mHandler.sendMessage(msg);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    Bundle bundle0 = msg.getData();
                    if (bundle0 == null)
                        return;
                    idregnum = idregnum + 1;
                    IDCardInfo info = (IDCardInfo) bundle0.get("info");
                    byte[] photo = bundle0.getByteArray("photo");
                    if (info == null) return;
                    if (photo == null) return;
                    String account =
                            "name: " + info.getName() + ", nation: "
                                    + info.getNation() + "   " + idregnum;
                    idT.setText(account);
                    if (photo.length > 0) {
                        photoV.setImageBitmap(BitmapFactory.decodeByteArray(photo,
                                0, photo.length));
                    }
                    break;
                case 2:
                    String an = String.valueOf(msg.obj);
                    String anFormat = getResources().getString(R.string.ros);
                    wakeT.setText(anFormat + an);
                    break;
                case 3:
                    break;
                case 6:
                    String txt = printTxT.getText().toString();
                    if (txt.length() < 1) {
                        cs.onPrint("F888", 0, null);
                    } else {
                        cs.onPrint(txt, 0, null);
                    }
                    break;
                case 7:
                    canP = true;
                    break;
                case 8:
                    click_old.setText("状态码：" + msg.arg1);
                    break;
                case 9:
                    int aa1 = RobotActionProvider.getInstance().getRevTtys3(21);
                    int aa2 = RobotActionProvider.getInstance().getRevTtys3(22);
                    int aa3 = RobotActionProvider.getInstance().getRevTtys3(23);
                    int aa4 = RobotActionProvider.getInstance().getRevTtys3(24);
                    hw1.setText("右红外:" + aa1);
                    hw2.setText("左红外:" + aa2);
                    hw3.setText("Fcc 红外:" + aa3);
                    hw4.setText("Top 红外:" + aa4);
                    int bb1 = RobotActionProvider.getInstance().getRevTtys3(25);
                    int bb2 = RobotActionProvider.getInstance().getRevTtys3(26);
                    int bb3 = RobotActionProvider.getInstance().getRevTtys3(27);
                    int bb4 = RobotActionProvider.getInstance().getRevTtys3(28);
                    int bb5 = RobotActionProvider.getInstance().getRevTtys3(29);
                    int bb6 = RobotActionProvider.getInstance().getRevTtys3(30);
                    int bb7 = RobotActionProvider.getInstance().getRevTtys3(31);
                    cb1.setText("后超声波:" + bb1);
                    cb2.setText("前超声波:" + bb2);
                    cb3.setText("左侧超声波:" + bb3);
                    cb4.setText("左中超声波:" + bb4);
                    cb5.setText("中间超声波:" + bb5);
                    cb6.setText("右中超声波:" + bb6);
                    cb7.setText("右侧超声波:" + bb7);
                    mHandler.sendEmptyMessageDelayed(9, 100);
                    break;
                default:
                    break;
            }
        }
    };

    private boolean canP = true;

    public void test(View v) {
        if (cs == null)
            return;
        if (v.getId() == R.id.turn_left) {
            RobotActionProvider.getInstance().moveLeft(90, 0);
        } else if (v.getId() == R.id.turn_right) {
            RobotActionProvider.getInstance().moveRight(90, 0);
        } else if (v.getId() == R.id.turn_upt) {
            RobotActionProvider.getInstance().moveFront(100);
        } else if (v.getId() == R.id.turn_backt) {
            RobotActionProvider.getInstance().moveBack(100, 0);
        } else if (v.getId() == R.id.up) {
            RobotActionProvider.getInstance().combinedActionTtyS4(20);
        } else if (v.getId() == R.id.stop) {
            RobotActionProvider.getInstance().stopMove();
        } else if (v.getId() == R.id.head_center) {
            RobotActionProvider.getInstance().combinedActionTtyS4(14);
        } else if (v.getId() == R.id.arm) {
            RobotActionProvider.getInstance().combinedActionTtyS4(2);
        } else if (v.getId() == R.id.print_txt) {
            Log.v("demo", "canP: " + canP);
            if (!canP)
                return;
            new Thread() {
                @Override
                public void run() {
                    canP = false;
                    mHandler.sendEmptyMessageDelayed(7, 4000);
                    cs.onPrint("/sdcard/logo1.bmp;", 2, oListener);
                }
            }.start();
        } else if (v.getId() == R.id.click) {
            Log.v("demo", "canP: " + canP);
            if (!canP)
                return;
            new Thread() {
                @Override
                public void run() {
                    canP = false;
                    mHandler.sendEmptyMessageDelayed(7, 4000);
                    // 普通示例打印
                    // cs.onPrint("F888", 0, oListener);
                    // 带bmp图片打印json
                    // cs.onPrint(
                    // "{\"data\":[{\"iNums\":\"1\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"【XXXXX支行】\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"欢迎您光临\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"Welcome to CCB\",\"alignmen\":\"1\",\"feedline\":\"3\",\"changerow\":\"0\"},{\"text\":\"【F888】\",\"alignmen\":\"1\",\"feedline\":\"2\",\"changerow\":\"0\",\"bold\":\"1\",\"sizetext\":\"1\"},{\"text\":\"前面有：【XX】人，请稍候：【XX】clients ahead of you\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"尊敬的：【XX】客户\",\"alignmen\":\"0\",\"changerow\":\"0\"},{\"text\":\"您将要办理：【XX】\",\"alignmen\":\"0\",\"changerow\":\"0\"},{\"text\":\"【打印日期，精确到秒】\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"不向陌生人汇款、转账，谨防上当！\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"温馨提示：您是我行优质客户，诚邀你办理我行信用卡。\",\"alignmen\":\"0\",\"feedline\":\"5\",\"changerow\":\"0\",\"cutpaper\":\"0\"}]}",
                    // 1, oListener);
                    // 带二维码打印json（设置qr=1为二维码显示，默认为0不需要输入）
                    cs.onPrint(
                            "{\"data\":[{\"iNums\":\"1\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"【XXXXX支行】\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"欢迎您光临\",\"alignmen\":\"1\",\"changerow\":\"0\"},{\"text\":\"Welcome to CCB\",\"alignmen\":\"1\",\"feedline\":\"3\",\"changerow\":\"0\"},{\"text\":\"【F888】\",\"alignmen\":\"1\",\"feedline\":\"2\",\"changerow\":\"0\",\"bold\":\"1\",\"sizetext\":\"1\"},{\"text\":\"前面有：【XX】人，请稍候：【XX】clients ahead of you\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"尊敬的：【XX】客户\",\"alignmen\":\"0\",\"changerow\":\"0\"},{\"text\":\"您将要办理：【XX】\",\"alignmen\":\"0\",\"changerow\":\"0\"},{\"text\":\"【打印日期，精确到秒】\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"不向陌生人汇款、转账，谨防上当！\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"text\":\"温馨提示：您是我行优质客户，诚邀你办理我行信用卡。\",\"alignmen\":\"0\",\"feedline\":\"2\",\"changerow\":\"0\"},{\"qr\":\"1\",\"text\":\"aBcd1234\",\"leftmargin\":\"30\",\"sizetext\":\"6\",\"feedline\":\"5\",\"changerow\":\"0\",\"cutpaper\":\"0\"}]}",
                            1, oListener);
                }
            }.start();

        } else if (v.getId() == R.id.click_old) {
        } else if (v.getId() == R.id.head_l) {
            RobotActionProvider.getInstance().headControlTtyS4(2, 2, -60, 50);
        } else if (v.getId() == R.id.head_r) {
            RobotActionProvider.getInstance().headControlTtyS4(2, 2, 60, 50);
        } else if (v.getId() == R.id.head_u) {
            RobotActionProvider.getInstance().headControlTtyS4(1, 2, -5, 30);
        } else if (v.getId() == R.id.head_d) {
            RobotActionProvider.getInstance().headControlTtyS4(1, 2, 15, 30);
        } else if (v.getId() == R.id.head_h) {
            RobotActionProvider.getInstance().earControlTtyS4(1);
        } else if (v.getId() == R.id.head_v) {
            RobotActionProvider.getInstance().earControlTtyS4(255);
        } else if (v.getId() == R.id.head_h1) {
            RobotActionProvider.getInstance().eyeControlTtyS4(1);
        } else if (v.getId() == R.id.head_v1) {
            RobotActionProvider.getInstance().eyeControlTtyS4(255);
        } else if (v.getId() == R.id.head_msg) {
            byte[] a = new byte[]{0x02, (byte) 0x8d, 0x08};
            byte[] b = new byte[]{0x02, (byte) 0x8e, 0x08};
            RobotActionProvider.getInstance().sendTtyS4(a);
            RobotActionProvider.getInstance().sendTtyS4(b);
        } else if (v.getId() == R.id.threed) {
            RobotActionProvider.getInstance().shutDown();
        }
    }

    private OnPrintListener oListener = new OnPrintListener.Stub() {

        @Override
        public void onResult(int arg0) throws RemoteException {
            Message pcode = mHandler.obtainMessage();
            pcode.what = 8;
            pcode.arg1 = arg0;
            mHandler.sendMessage(pcode);
        }
    };

    @Override
    protected void onPause() {
        Log.v("demo", "onPause ");
        mHandler.removeCallbacks(null);
        ConnectServer.getInstance(getApplication()).release();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
