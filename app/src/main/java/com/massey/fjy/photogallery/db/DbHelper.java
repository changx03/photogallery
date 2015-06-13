package com.massey.fjy.photogallery.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Luke on 12/06/2015.
 */
public class DbHelper extends SQLiteOpenHelper{
    public DbHelper(Context context) {
        super(context, DbTableContract.DB_NAME, null, DbTableContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbTableContract.DB_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
