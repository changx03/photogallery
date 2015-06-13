package com.massey.fjy.photogallery.db;

import android.provider.BaseColumns;

/**
 * Created by Luke on 12/06/2015.
 */
public class DbTableContract {
    private DbTableContract(){} // constructor

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "AndroidDB";

    public static final String DB_TABLE_CREATE = "CREATE TABLE " + PhotoGalleryTable.DB_TABLE_NAME +
            " (" + PhotoGalleryTable.FIELD_KEY + " INTEGER PRIMARY KEY, " +
            PhotoGalleryTable.FIELD_TAG + " TEXT, " +
            PhotoGalleryTable.FIELD_LOCATION + " TEXT, " +
            PhotoGalleryTable.FIELD_LATITUDE + " REAL, " +
            PhotoGalleryTable.FIELD_LONGITUDE + " REAL, " +
            PhotoGalleryTable.FIELD_NOTE + " TEXT, " +
            PhotoGalleryTable.FIELD_IMAGE_NAME + " TEXT, " +
            PhotoGalleryTable.FIELD_DATE + " TEXT, " +
            PhotoGalleryTable.FIELD_TAG_PEOPLE + " TEXT);";

    public static final String DB_TABLE_DELETE = "DROP TABLE IF EXISTS " + PhotoGalleryTable.DB_TABLE_NAME;

    public static abstract class PhotoGalleryTable implements BaseColumns{
        public static final String DB_TABLE_NAME = "PhotoGalleryTable";
        public static final String FIELD_KEY = "Key";   // integer
        public static final String FIELD_TAG = "Tag";   // text
        public static final String FIELD_LOCATION = "Location";  // text
        public static final String FIELD_LONGITUDE = "Longitude";   // real
        public static final String FIELD_LATITUDE = "Latitude"; // real
        public static final String FIELD_NOTE = "Note"; // text
        public static final String FIELD_IMAGE_NAME = "Name"; // text
        public static final String FIELD_DATE = "Date"; // text ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
        public static final String FIELD_TAG_PEOPLE = "TagPeople";  // text (2;luke;5;5;jean;6;6) numTags = 2, luke(5,5), jean(6,6)
    }
}
