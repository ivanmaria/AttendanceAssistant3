package com.github.ivanmaria.attendanceassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button TeacherBtn,StudentBtn;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TeacherBtn = (Button)findViewById(R.id.teacher);
        StudentBtn = (Button)findViewById(R.id.student);
        sharedpreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
    }
    public void TeacherBtn(View v){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("Profession", "Teacher");
        editor.commit();
        Intent in = new Intent(MainActivity.this, Teacher_Info.class);
    startActivity(in);
}
    public void StudentBtn(View v){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("Profession", "Student");
        editor.commit();
        Intent in = new Intent(MainActivity.this, Student_Info.class);
        startActivity(in);
        finish();
    }
}
