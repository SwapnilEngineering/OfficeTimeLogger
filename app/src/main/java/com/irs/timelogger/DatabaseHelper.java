package com.irs.timelogger;
import android.content.Context;
import  android.database.sqlite.SQLiteDatabase;
import  android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String dbName = "OfficeTime";
    public static final int dbVersion = 1;

    public DatabaseHelper(Context context) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUpdateTable(db, 0, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUpdateTable(db, oldVersion, newVersion);
    }

    public void createUpdateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("Create table tblTimeLog (_id integer primary key autoincrement," +
                    "Date text,Intime text,Outtime text); ");
        }
    }

}
