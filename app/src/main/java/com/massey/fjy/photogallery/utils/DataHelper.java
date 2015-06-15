package com.massey.fjy.photogallery.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Luke on 13/06/2015.
 */
public class DataHelper {
    public static final String IMAGE_DIR = "photoGallery";

    // variables saved in sharedPref
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String CURRENT_IMAGE_PATH = "currentImagePath";
    public static final String VIEW_MODE = "viewMode";
    public static final String PHOTO_GALLERY_FULL_PATH = "photoGalleryPath";
    public static final String VIEW_BY = "viewBy";
    public static final int VIEW_BY_ALL = 0, VIEW_BY_TAG = 1, VIEW_BY_SEARCH = 2;
    public static final String OPTION_KEY_WORD = "optionKeyWord";

    public static String getDateTimeToString(){
        long msTime = System.currentTimeMillis();
        Date currentDateTime = new Date(msTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatDate = dateFormat.format(currentDateTime);
        System.out.println("date = " + formatDate);

        return formatDate;
    }

    public static class ImageData{
        public Long key;
        public String tag;
        public String location;
        public Float latitude;
        public Float longitude;
        public String note;
        public String imageName;
        public String date;
        public String tagPeople;
    }
}
