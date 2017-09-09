package com.github.ivanmaria.attendanceassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class TeacherServer extends AppCompatActivity {

    static final int UdpServerPORT = 4445;
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_WRITE_SETTINGS = 1;
    private static String SSID = "Test";
    private static String PASS = "12345678";
    TextView infoIp;
    TextView textViewState, textViewPrompt;
    Button btn;
    Context context;
    UdpServerThread udpServerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_server);
        infoIp = (TextView) findViewById(R.id.infoip);
        textViewState = (TextView)findViewById(R.id.state);
        textViewPrompt = (TextView)findViewById(R.id.prompt);
        btn = (Button) findViewById(R.id.button);
        infoIp.setText(getIpAddress());
    }

    @Override
    protected void onStart() {
        super.onStart();
        setWifiTetheringEnabled(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                setWifiTetheringEnabled(true);
            }
        }, 5000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            new AlertDialog.Builder(this)
                    .setMessage("Allow reading/writing the system settings? Necessary to set up access points.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
                        }
                    }).show();
            return;
        }
    }

    public void AttendanceBtn(View v) {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (btn.getText().toString().equals("Take Attendance") || btn.getText().toString().equals("Try Again!")) {
            btn.setText("Stop");
            udpServerThread = new UdpServerThread(UdpServerPORT);
            udpServerThread.start();
        } else if (btn.getText().toString().equals("Stop")) {
            setWifiTetheringEnabled(false);
            btn.setText("Take Attendance");
            btn.setEnabled(false);
        } else {
            btn.setText("Try Again!");
        }
    }

    @Override
    protected void onStop() {
        if(udpServerThread != null){
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }

        super.onStop();
    }

    private void updateState(final String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewState.setText(state);
            }
        });
    }

    private void updatePrompt(final String prompt){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewPrompt.append(prompt);
            }
        });
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public void setWifiTetheringEnabled(boolean enable) {
        //Log.d(TAG,"setWifiTetheringEnabled: "+enable);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (enable) {
            wifiManager.setWifiEnabled(!enable);    // Disable all existing WiFi Network
        } else {
            if (!wifiManager.isWifiEnabled())
                wifiManager.setWifiEnabled(!enable);
        }
        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("setWifiApEnabled")) {
                WifiConfiguration netConfig = new WifiConfiguration();
                if (!SSID.isEmpty() || !PASS.isEmpty()) {
                    netConfig.SSID = SSID;
                    netConfig.preSharedKey = PASS;
                    netConfig.status = WifiConfiguration.Status.ENABLED;
                    netConfig.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.SHARED);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(4);
                }
                try {
                    method.invoke(wifiManager, netConfig, enable);
                    Log.e(TAG, "set hotspot enable method");
                } catch (Exception ex) {
                }
                break;
            }
        }
    }

    private class UdpServerThread extends Thread{

        int serverPort;
        DatagramSocket socket;

        boolean running;

        public UdpServerThread(int serverPort) {
            super();
            this.serverPort = serverPort;
        }

        public void setRunning(boolean running){
            this.running = running;
        }

        @Override
        public void run() {

            running = true;

            try {
                updateState("Initializing Attendance. Please Wait!");
                socket = new DatagramSocket(serverPort);

                updateState("Taking Attendance...");
                Log.e(TAG, "UDP Server is running");

                while(running){
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);     //this code block the program flow

                    // send the response to the client at "address" and "port"
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    String line = new String(packet.getData(), 0, packet.getLength());
                    updatePrompt(line+"\n");

                    String dString = "You are marked present!";
                    buf = dString.getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);

                }

                Log.e(TAG, "UDP Server ended");

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket != null){
                    socket.close();
                    Log.e(TAG, "socket.close()");
                }
            }
        }
    }
}