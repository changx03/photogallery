package com.massey.fjy.photogallery.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.massey.fjy.photogallery.db.DbTableContract.PhotoGalleryTable;
import com.massey.fjy.photogallery.utils.DataHelper;

import java.util.ArrayList;

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
        db.execSQL(DbTableContract.DB_TABLE_DELETE);
        onCreate(db);
    }

    public Long save(String tag, String location, Float latitude, Float longitude,
                     String note, String imageName, String date, String tagPeople){
        Long rowId = null;
        if(imageName.isEmpty()) {
            return (long) -1;
        }

        ContentValues values = new ContentValues();
        // content can be nullable
        values.put(PhotoGalleryTable.FIELD_TAG, tag);
        values.put(PhotoGalleryTable.FIELD_LOCATION, location);
        values.put(PhotoGalleryTable.FIELD_LATITUDE, latitude);
        values.put(PhotoGalleryTable.FIELD_LONGITUDE, longitude);
        values.put(PhotoGalleryTable.FIELD_NOTE, note);
        values.put(PhotoGalleryTable.FIELD_IMAGE_NAME, imageName);
        values.put(PhotoGalleryTable.FIELD_DATE, date);
        values.put(PhotoGalleryTable.FIELD_TAG_PEOPLE, tagPeople);

        // insert db
        SQLiteDatabase db = this.getWritableDatabase();
        rowId = db.insert(PhotoGalleryTable.DB_TABLE_NAME, null, values);

        return rowId;
    }

    public ArrayList<String> getAllGridView(){
        ArrayList<String> imageList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PhotoGalleryTable.FIELD_IMAGE_NAME +
                " FROM " + PhotoGalleryTable.DB_TABLE_NAME +
                " ORDER BY " + PhotoGalleryTable.FIELD_DATE + " DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            imageList.add(cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_IMAGE_NAME)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return imageList;
    }

    public DataHelper.ImageData getImageDataByImageName(String imageName){
        DataHelper.ImageData imageData = new DataHelper.ImageData();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + PhotoGalleryTable.DB_TABLE_NAME +
                " WHERE " + PhotoGalleryTable.FIELD_IMAGE_NAME +
                "='" + imageName + "'", null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            imageData.key = cursor.getLong(cursor.getColumnIndex(PhotoGalleryTable.FIELD_KEY));
            imageData.imageName = cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_IMAGE_NAME));
            imageData.date = cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_DATE));
            imageData.tag = cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_TAG));
            imageData.location = cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_LOCATION));
            imageData.latitude = cursor.getFloat(cursor.getColumnIndex(PhotoGalleryTable.FIELD_LATITUDE));
            imageData.longitude = cursor.getFloat(cursor.getColumnIndex(PhotoGalleryTable.FIELD_LONGITUDE));
            imageData.note = cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_NOTE));
            imageData.tagPeople = cursor.getString(cursor.getColumnIndex(PhotoGalleryTable.FIELD_TAG_PEOPLE));
        }
        cursor.close();
        db.close();
        System.out.println("DbHelper_key = " + imageData.key);
        return imageData;
    }

    public Integer deleteSingleImage(String imageName) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(PhotoGalleryTable.DB_TABLE_NAME,
                PhotoGalleryTable.FIELD_IMAGE_NAME + " = ?", new String[]{imageName});
    }
}
