package com.github.ivanmaria.attendanceassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Teacher_Info extends AppCompatActivity {
    EditText fname, lname, subcode;
    TextView subnum, textview, subjects, status;
    Button addsub, clearsub, submit;
    int TotalSubject = 0;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_info);

        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        subcode = (EditText) findViewById(R.id.subcode);

        subnum = (TextView) findViewById(R.id.subnum);
        textview = (TextView) findViewById(R.id.textview);
        subjects = (TextView) findViewById(R.id.subjects);
        status = (TextView) findViewById(R.id.status);

        addsub = (Button) findViewById(R.id.addsub);
        clearsub = (Button) findViewById(R.id.clearsub);
        submit = (Button) findViewById(R.id.submit);
        sharedpreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void AddSubject(View v) {
        String Sub = subcode.getText().toString();
        if (Sub.equals("")) {
            status.setText("SUBJECT CODE cannot be blank!");
        } else {
            TotalSubject++;
            subnum.setText("No. of Subject: " + TotalSubject);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("Subject" + TotalSubject, Sub);
            editor.putInt("TotalSubject", TotalSubject);
            editor.commit();
            String str = subjects.getText().toString();
            subjects.setText(str + TotalSubject + ": " + Sub + "\n");
        }
    }

    public void ClearSubject(View v) {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        for (int i = 1; i <= TotalSubject; i++) {
            preferences.edit().remove("Subject" + TotalSubject).apply();
        }
        subjects.setText("");
        TotalSubject = 0;
        subnum.setText("No. of Subject: " + TotalSubject);
        editor.putInt("TotalSubject", TotalSubject);
        editor.commit();
    }

    public void Submit(View v) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Intent in = new Intent(Teacher_Info.this, TeacherServer.class);
        if (fname.getText().toString().equals("") || lname.getText().equals("")) {
            status.setText("Enter Name & Surname");
        } else if (TotalSubject == 0) {
            status.setText("Add ateast 1 subject");
        } else {
            editor.putString("First Name", fname.getText().toString());
            editor.putString("Last  Name", lname.getText().toString());
            editor.putString("Setup", "true");
            editor.commit();
            startActivity(in);
            finish();
        }
    }
}
