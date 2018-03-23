package com.example.tattata.charidoco;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    EditText editDate;
    EditText editTime;
    EditText editParkingID;
    EditText editMemo;

    Calendar parkingCalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        editParkingID = findViewById(R.id.editParkingID);
        editMemo = findViewById(R.id.editMemo);

        findViewById(R.id.buttonTimeEdit)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TimeFragment timeFragment = new TimeFragment();
                        timeFragment.show(getSupportFragmentManager(), "timePicker");
                    }
                });

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("確認")
                        .setMessage("リセットしますか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                reset();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        parkingCalendar = Calendar.getInstance();

        loadData();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        editTime.setText(String.format(Locale.US, "%02d:%02d", hour,minute));
        parkingCalendar.set(Calendar.HOUR_OF_DAY, hour);
        parkingCalendar.set(Calendar.MINUTE, minute);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }
    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences("ChariDoco", MODE_PRIVATE).edit();

        editor.putString("parkingID", editParkingID.getText().toString());
        editor.putString("memo", editMemo.getText().toString());
        editor.putLong("parkingUnixTime", calendarToLong(parkingCalendar));
        editor.apply();
    }
    private void loadData() {
        SharedPreferences pref = getSharedPreferences("ChariDoco", MODE_PRIVATE);

        long unixTime = pref.getLong("parkingUnixTime", 0);
        if(unixTime != 0) {
            longToCalendar(unixTime, parkingCalendar);
        }
        //else(初回起動時):parkingCalendar=現在時刻　に初期化済み。
        Date date = parkingCalendar.getTime();
        editDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date));
        editTime.setText(new SimpleDateFormat("HH:mm", Locale.US).format(date));

        editParkingID.setText(pref.getString("parkingID", ""));
        editMemo.setText(pref.getString("memo", ""));
    }
    private void reset() {
        parkingCalendar = Calendar.getInstance();
        Date date = parkingCalendar.getTime();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
        String now = new SimpleDateFormat("HH:mm", Locale.US).format(date);
        editDate.setText(today);
        editTime.setText(now);
        editParkingID.setText("");
        editMemo.setText("");
    }

    private long calendarToLong(Calendar from) {
        return from.getTime().getTime();
    }
    private void longToCalendar(long from, Calendar to) {
        Date date = new Date(from);
        to.setTime(date);
    }
}
