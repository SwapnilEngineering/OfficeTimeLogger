package com.example.timelogger;


import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    final double normalWorkingHrs = 8.5;
    public DatabaseHelper databaseOperation = new DatabaseHelper(this);
    TimePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateView();
        Button btnlog = findViewById(R.id.btnLog);
        btnlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDateTime curdttime = LocalDateTime.now();
                LocalTime curtime = LocalTime.now();
                if (validTime(curtime)) {
                    logTime(curdttime);
                } else
                    Toast.makeText(MainActivity.this, "Dont just show off! Get your efficiency right!", Toast.LENGTH_LONG).show();
                updateView();
            }
        });
        final EditText txtMonin = findViewById(R.id.etMonIn);
        txtMonin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                picker = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                txtMonin.setText(sHour + ":" + sMinute);
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });

    }

    public boolean validTime(LocalTime logtime) {
                return (logtime.isAfter(LocalTime.parse("07:45:00")) && logtime.isBefore(LocalTime.parse("19:00:00")));
    }

    public void updateView() {

        TextView weekaccum = findViewById(R.id.txtWeeklyAccumulated);
        long difference = getExcesstime();
        long dhours = TimeUnit.MINUTES.toHours(difference);
        long dmin = TimeUnit.MINUTES.toMinutes(difference) - dhours * 60;
        String excesstime = String.format("%s:%s", dhours, dmin);
        weekaccum.setText(excesstime);
        TextView nout = findViewById(R.id.txtNormalOut);
        LocalTime normalouttime = normalOut();
        if (normalouttime != null) {
            nout.setText("Normal Out Time Today: " + normalouttime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        } else {
            nout.setText("Normal Out Time Today: Not Available");
        }
        TextView eout = findViewById(R.id.txtEarliestOut);
        LocalTime earlieastouttime = earliestOut();
        if (earlieastouttime != null) {
            eout.setText("Ealiest Out Time Today: " + earlieastouttime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        } else {
            eout.setText("Earliest Departure Time Not Available");
        }
        setWeekdayTime();
    }

    public void logTime(LocalDateTime curTime) {
        String strdate = curTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        String strtime = curTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        SQLiteDatabase db = databaseOperation.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("Date", strdate);
        Cursor cursor = db.rawQuery("Select Intime,Outtime from tblTimeLog where Date = ? ", new String[]{strdate});
        if (cursor != null) {
            if (cursor.getCount() == 0) {
                data.put("Intime", strtime);
                db.insert("tblTimeLog", "", data);
            } else {
                data.put("Outtime", strtime);
                db.update("tblTimeLog", data, "Date=?", new String[]{strdate});
            }
        }
        Toast.makeText(MainActivity.this, strdate + strtime, Toast.LENGTH_LONG).show();
    }

    public long getExcesstime() {
        long difference = 0;
        Cursor cursor = getWeekCursor();
        try {
            while (cursor.moveToNext()) {
                String intime = cursor.getString(0);
                String outtime = cursor.getString(1);
                LocalTime it = LocalTime.parse(intime);
                LocalTime ot = LocalTime.parse(outtime);
                long gap = Duration.between(it, ot).toMinutes();
                if (gap > 300) difference += gap - (long) (normalWorkingHrs * 60);
            }
        } finally {
            cursor.close();
            return difference;
        }
    }

    public Cursor getWeekCursor() {
        LocalDate curdt = LocalDate.now();
        String excesstime = "0:00";
        LocalDate start = curdt;
        while (start.getDayOfWeek() != DayOfWeek.MONDAY) {
            start = start.minusDays(1);
        }
        LocalDate end = curdt;
        while (end.getDayOfWeek() != DayOfWeek.SUNDAY) {
            end = end.plusDays(1);
        }
        String stdt = start.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        String enddt = end.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        SQLiteDatabase db = databaseOperation.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select Intime,Outtime from tblTimeLog where Date between ? and ? ", new String[]{stdt, enddt});
        return cursor;
    }

    public LocalTime normalOut() {
        LocalDate curtime = LocalDate.now();
        String strdate = curtime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        SQLiteDatabase db = databaseOperation.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select Intime from tblTimeLog where Date = ? ", new String[]{strdate});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String intime = cursor.getString(0);
            LocalTime dttime = LocalTime.parse(intime);
            dttime = dttime.plusMinutes((long) (normalWorkingHrs * 60));
            return dttime;
        }
        return null;

    }

    public LocalTime earliestOut() {
        LocalTime normalout = normalOut();
        if (normalout != null) {
            long excesstime = getExcesstime();
            LocalTime eout = normalout.minusMinutes(excesstime);
            return eout;
        }
        return null;
    }

    public void setWeekdayTime() {
        Cursor c = getWeekCursor();
        int dayno = 0;
        EditText monin = findViewById(R.id.etMonIn);
        EditText monout = findViewById(R.id.etMonOut);
        EditText tuein = findViewById(R.id.etTueIn);
        EditText tueout = findViewById(R.id.etTueOut);
        EditText wedin = findViewById(R.id.etWedIn);
        EditText wedout = findViewById(R.id.etWedOut);
        EditText thuin = findViewById(R.id.etThuIn);
        EditText thuout = findViewById(R.id.etThuOut);
        EditText friin = findViewById(R.id.etFriIn);
        EditText friout = findViewById(R.id.etFriOut);
        try {
            LocalTime indttime, odttime;
            while (c.moveToNext()) {
                String intime = c.getString(0);
                indttime = LocalTime.parse(intime);
                String fintime = indttime.format(DateTimeFormatter.ofPattern("hh:mm a"));
                String outtime = c.getString(1);
                odttime = LocalTime.parse(outtime);
                String fouttime = odttime.format(DateTimeFormatter.ofPattern("hh:mm a"));
                switch (dayno) {
                    case 0:
                        monin.setText(fintime);
                        monout.setText(fouttime);
                        break;
                    case 1:
                        tuein.setText(fintime);
                        tueout.setText(fouttime);
                        break;
                    case 2:
                        wedin.setText(fintime);
                        wedout.setText(fouttime);
                        break;
                    case 3:
                        thuin.setText(fintime);
                        thuout.setText(fouttime);
                        break;
                    case 4:
                        friin.setText(fintime);
                        friout.setText(fouttime);
                        break;
                }
                dayno++;
            }
        } finally {
            c.close();
        }
    }
}




