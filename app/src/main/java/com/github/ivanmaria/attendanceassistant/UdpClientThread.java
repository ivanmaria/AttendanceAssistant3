package com.github.ivanmaria.attendanceassistant;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpClientThread extends Thread{

    String dstAddress,Num;
    int dstPort;
    StudentClient.UdpClientHandler handler;
    DatagramSocket socket;
    InetAddress address;
    private boolean running;

    public UdpClientThread(String addr, int port, StudentClient.UdpClientHandler handler, String Num) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.handler = handler;
        this.Num = Num;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state){
        handler.sendMessage(
                Message.obtain(handler,
                        StudentClient.UdpClientHandler.UPDATE_STATE, state));
    }

    @Override
    public void run() {
        sendState("connecting...");

        running = true;

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(dstAddress);

            // send request
            byte[] buf = new byte[256];


            String dString = "Roll No: "+Num+" is present!";
            buf = dString.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, dstPort);
            socket.send(packet);

            sendState("connected");

            // get response
            packet = new DatagramPacket(buf, buf.length);


            socket.receive(packet);
            String line = new String(packet.getData(), 0, packet.getLength());

            handler.sendMessage(
                    Message.obtain(handler, StudentClient.UdpClientHandler.UPDATE_MSG, line));
            handler.sendEmptyMessage(StudentClient.UdpClientHandler.UPDATE_END);


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                //handler.sendEmptyMessage(StudentClient.UdpClientHandler.UPDATE_END);
            }
        }

    }
}