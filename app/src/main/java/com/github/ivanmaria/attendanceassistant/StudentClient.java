package com.github.ivanmaria.attendanceassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class StudentClient extends AppCompatActivity {

    private static String SSID;
    private static String PASS;
    private static String Num;
    //EditText editTextAddress;
    Button buttonConnect, selsub;
    EditText ip1, ip2, ip3, ip4;
    TextView textViewState, textViewRx;
    Spinner spinner2;
    int netId;
    UdpClientHandler udpClientHandler;
    UdpClientThread udpClientThread;
    String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_client);
        ip1 = (EditText) findViewById(R.id.ip1);
        ip2 = (EditText) findViewById(R.id.ip2);
        ip3 = (EditText) findViewById(R.id.ip3);
        ip4 = (EditText) findViewById(R.id.ip4);
        selsub = (Button) findViewById(R.id.selsub);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        buttonConnect = (Button) findViewById(R.id.connect);
        textViewState = (TextView)findViewById(R.id.state);
        textViewRx = (TextView)findViewById(R.id.received);
        udpClientHandler = new UdpClientHandler(this);
        SharedPreferences sharedpreferences = getSharedPreferences("settings", MODE_PRIVATE);
        int n = sharedpreferences.getInt("TotalSubject", 0);
        Num = sharedpreferences.getString("Roll No", "");

        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= n; i++) {
            list.add(sharedpreferences.getString("Subject" + i, ""));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void buttonConnectOnClickListener(View v) {
        if (ip1.getText().toString().equals("") || ip2.getText().toString().equals("") || ip3.getText().toString().equals("") || ip4.getText().toString().equals("")) {
            textViewState.setText("Enter Code Properly!");
        } else {
            ipAddress = ip1.getText().toString() + "." + ip2.getText().toString() + "." + ip3.getText().toString() + "." + ip4.getText().toString();
            WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr.isWifiEnabled()) {
                buttonConnect.setText("Give Attendance");
                udpClientThread = new UdpClientThread(ipAddress, 4445, udpClientHandler, Num);
                udpClientThread.start();
                buttonConnect.setEnabled(false);
            } else {
                buttonConnect.setText("Error! Try Again");
                WifiConnect();
            }
        }
    }

    public void updateState(String state){
        textViewState.setText(state);
    }

    private void updateRxMsg(String rxmsg){
        textViewRx.append(rxmsg + "\n");
    }

    private void clientEnd(){
        udpClientThread = null;
        textViewState.setText("Disconnected!");
        buttonConnect.setEnabled(true);
        buttonConnect.setVisibility(View.GONE);

    }

    public void WifiConnect() {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", SSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", PASS);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    public void SelectSubject(View v) {
        SSID = String.valueOf(spinner2.getSelectedItem());
        PASS = "password_" + SSID;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        WifiConnect();
        ip1.setVisibility(View.VISIBLE);
        ip2.setVisibility(View.VISIBLE);
        ip3.setVisibility(View.VISIBLE);
        ip4.setVisibility(View.VISIBLE);
        buttonConnect.setVisibility(View.VISIBLE);
        textViewState.setVisibility(View.VISIBLE);
        textViewRx.setVisibility(View.VISIBLE);
    }

    public static class UdpClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private StudentClient parent;

        public UdpClientHandler(StudentClient parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }
}