package com.example.tattata.charidoco;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView textDate;
    TextView textTime;
    TextView textElapsedTime;
    EditText editParkingID;
    EditText editMemo;

    Calendar parkingCalendar;
    Handler timerHandler;
    Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        textElapsedTime = findViewById(R.id.textElapsedTime);
        editParkingID = findViewById(R.id.editParkingID);
        editMemo = findViewById(R.id.editMemo);

        final FloatingActionButton fab = findViewById(R.id.floatingActionButton);
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

        //経過時間を表示するためのハンドラ
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long diffTime = (System.currentTimeMillis() - parkingCalendar.getTimeInMillis()) / 60000;
                textElapsedTime.setText(String.format(Locale.US, "%02d:%02d", diffTime / 60, diffTime % 60));
                timerHandler.postDelayed(this, 30000);
            }
        };

        findViewById(R.id.textTime)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                MainActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                        textTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
                                        parkingCalendar.set(Calendar.HOUR_OF_DAY, hour);
                                        parkingCalendar.set(Calendar.MINUTE, minute);
                                        resetElapsedTime();
                                    }
                                },
                                parkingCalendar.get(Calendar.HOUR_OF_DAY),
                                parkingCalendar.get(Calendar.MINUTE),
                                true
                        );
                        timePickerDialog.show();
                    }
                });
        findViewById(R.id.textDate)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //DatePickerDialogインスタンスを取得
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                MainActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        parkingCalendar.set(Calendar.YEAR, year);
                                        parkingCalendar.set(Calendar.MONTH, month);
                                        parkingCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                        textDate.setText(String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth));
                                        resetElapsedTime();
                                    }
                                },
                                parkingCalendar.get(Calendar.YEAR),
                                parkingCalendar.get(Calendar.MONTH),
                                parkingCalendar.get(Calendar.DATE)
                        );

                        //dialogを表示
                        datePickerDialog.show();
                    }
                });
        findViewById(R.id.buttonOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationCompat.Builder mNotification = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("ChariDoco")
                        .setContentText(String.format(Locale.US, "入庫時刻:%02d:%02d 駐輪番号:%s",
                                parkingCalendar.get(Calendar.HOUR_OF_DAY),
                                parkingCalendar.get(Calendar.MINUTE),
                                editParkingID.getText().toString()));
                NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
                manager.notify(749812, mNotification.build());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
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
        if (unixTime != 0) {
            longToCalendar(unixTime, parkingCalendar);
        }
        //else(初回起動時):parkingCalendar=現在時刻　に初期化済み。
        Date date = parkingCalendar.getTime();
        textDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date));
        textTime.setText(new SimpleDateFormat("HH:mm", Locale.US).format(date));

        editParkingID.setText(pref.getString("parkingID", ""));
        editMemo.setText(pref.getString("memo", ""));
    }

    private void reset() {
        parkingCalendar = Calendar.getInstance();
        Date date = parkingCalendar.getTime();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
        String now = new SimpleDateFormat("HH:mm", Locale.US).format(date);
        textDate.setText(today);
        textTime.setText(now);
        editParkingID.setText("");
        editMemo.setText("");

        resetElapsedTime();
    }

    private void resetElapsedTime() {
        //経過時間をリセット
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    private long calendarToLong(Calendar from) {
        return from.getTime().getTime();
    }

    private void longToCalendar(long from, Calendar to) {
        Date date = new Date(from);
        to.setTime(date);
    }
}
