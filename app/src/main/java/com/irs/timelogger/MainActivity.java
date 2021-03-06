package com.irs.timelogger;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import java.util.HashMap;
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
                LogTime();
            }
        });
        final EditText txtMonin = findViewById(R.id.etMonIn);
        txtMonin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtMonin);
            }
        });
        final EditText txtMonout= findViewById(R.id.etMonOut);
        txtMonout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtMonout);
            }
        });
        final EditText txtTuein= findViewById(R.id.etTueIn);
        txtTuein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtTuein);
            }
        });
        final EditText txtTueout= findViewById(R.id.etTueOut);
        txtTueout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtTueout);
            }
        });
        final EditText txtWedin= findViewById(R.id.etWedIn);
        txtWedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtWedin);
            }
        });
        final EditText txtWedout= findViewById(R.id.etWedOut);
        txtWedout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtWedout);
            }
        });
        final EditText txtThuin= findViewById(R.id.etThuIn);
        txtThuin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtThuin);
            }
        });
        final EditText txtThuout= findViewById(R.id.etThuOut);
        txtThuout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtThuout);
            }
        });
        final EditText txtFriin= findViewById(R.id.etFriIn);
        txtFriin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtFriin);
            }
        });
        final EditText txtFriout= findViewById(R.id.etFriOut);
        txtFriout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimepicker(txtFriout);
            }
        });
    }

    public void showTimepicker(EditText et) {
        final EditText etext = et;
        final Calendar cldr = Calendar.getInstance();
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minutes = cldr.get(Calendar.MINUTE);
        // time picker dialog
        picker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                boolean isPM = (sHour >= 12);
                updateDb(etext.getId(),sHour,sMinute);
                etext.setText(String.format("%02d:%02d %s", (sHour == 12 || sHour == 0) ? 12 : sHour % 12, sMinute, isPM ? "PM" : "AM"));
            }
        }, hour, minutes,false);
        picker.show();
    }

    public void updateDb(int etid,int hr, int min){
        DayOfWeek day= getDayfromEt(etid);
        String timeType=getTimeType(etid);
        LocalDate curdt = LocalDate.now();
        LocalDate editDate=getWeekDateFromDay(getWeekStartDate(curdt),day);
        String strDate=editDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String editTime = String.format("%02d:%02d",hr,min);
        SQLiteDatabase db = databaseOperation.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put("Date", strDate);
        data.put(timeType, editTime);
        db.update("tblTimeLog", data, "Date=?", new String[]{strDate});
        updateView();
    }
    public DayOfWeek getDayfromEt(int etid){
        if (etid==R.id.etMonIn || etid==R.id.etMonOut) return  DayOfWeek.MONDAY;
        if (etid==R.id.etTueIn || etid==R.id.etTueOut) return  DayOfWeek.TUESDAY;
        if (etid==R.id.etWedIn || etid==R.id.etWedOut) return  DayOfWeek.WEDNESDAY;
        if (etid==R.id.etWedIn || etid==R.id.etThuOut) return  DayOfWeek.THURSDAY;
        if (etid==R.id.etFriIn || etid==R.id.etFriOut) return  DayOfWeek.FRIDAY;
        return  DayOfWeek.SUNDAY;
    }

    public String getTimeType(int etid){
        if (etid==R.id.etMonIn || etid==R.id.etTueIn ||etid==R.id.etWedIn ||etid==R.id.etWedIn ||etid==R.id.etFriIn)return "Intime";
        return "Outtime";
    }


    public void LogTime() {
        LocalDateTime curDtTime = LocalDateTime.now();
        if (validDay(curDtTime)) {
            LocalTime curTime = curDtTime.toLocalTime();
            LocalTime okTime = validTime(curTime);
            curDtTime = curDtTime.with(okTime);
            if (saveTime(curDtTime)) Toast.makeText(MainActivity.this,
                    curDtTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a")),
                    Toast.LENGTH_LONG).show();
            if (okTime != curTime) Toast.makeText(MainActivity.this,
                    "Dont just show off by coming early or sitting late! Get your efficiency right!",
                    Toast.LENGTH_LONG).show();
            updateView();
        } else {
            Toast.makeText(MainActivity.this, "Go home, get life", Toast.LENGTH_LONG);
        }
    }

    public LocalTime validTime(LocalTime logtime) {
        Settings defaultSettings = new Settings();
        if (logtime.isBefore(defaultSettings.earliestIn)) return defaultSettings.earliestIn;
        if (logtime.isAfter(defaultSettings.latestOut)) return defaultSettings.latestOut;
        return logtime;
    }

    public boolean validDay(LocalDateTime dtTime) {
        DayOfWeek day = dtTime.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;
        return true;
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

    public boolean saveTime(LocalDateTime curTime) {
        try {
            String strdate = curTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
            return true;
        } catch (SQLiteException e) {
            return false;
        }
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

    public LocalDate getWeekStartDate(LocalDate dt){
        LocalDate dtStart=dt;
        while (dtStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            dtStart = dtStart.minusDays(1);
        }
        return  dtStart;
    }

    public LocalDate getWeekEndDate(LocalDate dt){
        LocalDate dtEnd=dt;
        while (dtEnd.getDayOfWeek() != DayOfWeek.SUNDAY) {
            dtEnd = dtEnd.plusDays(1);
        }
        return dtEnd;
    }

    public LocalDate getWeekDateFromDay(LocalDate weekStartDate,DayOfWeek day){
        LocalDate weekDate=weekStartDate;
        while(weekDate.getDayOfWeek() !=day){
            weekDate=weekDate.plusDays(1);
        }
        return weekDate;
    }

    public Cursor getWeekCursor() {
        LocalDate curdt = LocalDate.now();
        String excesstime = "0:00";
        LocalDate startDate=getWeekStartDate(curdt);
        String stdt = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate=getWeekEndDate(curdt);
        String enddt = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SQLiteDatabase db = databaseOperation.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select Intime,Outtime from tblTimeLog where Date between ? and ? ", new String[]{stdt, enddt});
        return cursor;
    }

    public LocalTime normalOut() {
        Settings defaultsettings = new Settings();
        LocalDate curtime = LocalDate.now();
        String strdate = curtime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SQLiteDatabase db = databaseOperation.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select Intime from tblTimeLog where Date = ? ", new String[]{strdate});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String intime = cursor.getString(0);
            LocalTime dttime = LocalTime.parse(intime);
            dttime = dttime.plusMinutes((long) (normalWorkingHrs * 60));
            if (defaultsettings.latestOut.isBefore(dttime)) dttime = defaultsettings.latestOut;
            return dttime;
        }
        return null;

    }

    public LocalTime earliestOut() {
        Settings defaultSettings = new Settings();
        LocalTime normalout = normalOut();
        if (normalout != null) {
            long excesstime = getExcesstime();
            LocalTime eout = normalout.minusMinutes(excesstime);
            if (defaultSettings.latestOut.isBefore(eout)) eout = defaultSettings.latestOut;
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
                if (intime != null) {
                    indttime = LocalTime.parse(intime);
                    intime = indttime.format(DateTimeFormatter.ofPattern("hh:mm a"));
                }
                String outtime = c.getString(1);
                if (outtime != null) {
                    odttime = LocalTime.parse(outtime);
                    outtime = odttime.format(DateTimeFormatter.ofPattern("hh:mm a"));
                }

                switch (dayno) {
                    case 0:
                        monin.setText(intime);
                        monout.setText(outtime);
                        break;
                    case 1:
                        tuein.setText(intime);
                        tueout.setText(outtime);
                        break;
                    case 2:
                        wedin.setText(intime);
                        wedout.setText(outtime);
                        break;
                    case 3:
                        thuin.setText(intime);
                        thuout.setText(outtime);
                        break;
                    case 4:
                        friin.setText(intime);
                        friout.setText(outtime);
                        break;
                }
                dayno++;
            }
        } finally {
            c.close();
        }
    }
}




