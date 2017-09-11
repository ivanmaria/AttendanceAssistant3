package com.github.ivanmaria.attendanceassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedpreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Thread background = new Thread() {
            public void run() {

                try {
                    sleep(1 * 1000);
                    String initial = sharedpreferences.getString("Setup", "");
                    String profession = sharedpreferences.getString("Profession", "");
                    if (initial.equals("true") && profession.equals("Teacher")) {
                        Intent in = new Intent(SplashScreen.this, TeacherServer.class);
                        startActivity(in);
                    } else if (initial.equals("true") && profession.equals("Student")) {
                        Intent in = new Intent(SplashScreen.this, StudentClient.class);
                        startActivity(in);
                    } else {
                        Intent in = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(in);
                    }
                    finish();

                } catch (Exception e) {

                }
            }
        };
        background.start();
    }
}
