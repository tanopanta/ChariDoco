package com.example.tattata.charidoco;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    EditText editTime;
    EditText editParkingID;
    EditText editMemo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTime = findViewById(R.id.editTime);
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeFragment timeFragment = new TimeFragment();
                timeFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
        editParkingID = findViewById(R.id.editParkingID);
        editMemo = findViewById(R.id.editMemo);
        loadData();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        editTime.setText(String.format(Locale.US, "%02d:%02d", hour,minute));
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }
    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences("ChariDoco", MODE_PRIVATE).edit();
        editor.putString("parkingTime", editTime.getText().toString());
        editor.putString("parkingID", editParkingID.getText().toString());
        editor.putString("memo", editMemo.getText().toString());
        editor.apply();
    }
    private void loadData() {
        SharedPreferences pref = getSharedPreferences("ChariDoco", MODE_PRIVATE);
        editTime.setText(pref.getString("parkingTime", ""));
        editParkingID.setText(pref.getString("parkingID", ""));
        editMemo.setText(pref.getString("memo", ""));
    }
}
