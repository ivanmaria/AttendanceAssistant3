package com.github.ivanmaria.attendanceassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button TeacherBtn,StudentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TeacherBtn = (Button)findViewById(R.id.teacher);
        StudentBtn = (Button)findViewById(R.id.student);
    }
    public void TeacherBtn(View v){
    Intent in = new Intent(MainActivity.this, TeacherServer.class);
    startActivity(in);
}
    public void StudentBtn(View v){
        Intent in = new Intent(MainActivity.this, StudentClient.class);
        startActivity(in);
    }
}
