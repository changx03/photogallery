package com.massey.fjy.photogallery.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Created by Luke on 12/06/2015.
 */
public class BitmapHelper {
    public static final int IMAGE_DETAIL_ACTIVITY_WINDOW_HEIGHT = 300;  //R.dimen.image_detail_activity_window_height
    public static final int IMAGE_THUMBNAIL_SIZE = 100;//R.dimen.image_thumbnail_size

    public static Bitmap decodeBitmapFromUri(String path, int reqWidth, int reqHeight){
        Bitmap bitmap;
        // check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        System.out.println("inSampleSize = " + options.inSampleSize);
        // decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    public static Bitmap decodeBitmapFromResource(Resources resources,int id, int reqWidth, int reqHeight){
        Bitmap bitmap;
        // check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, id, options);

        // calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        System.out.println("inSampleSize = " + options.inSampleSize);
        // decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeResource(resources, id, options);

        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int getPixelValueFromDps(Context context,int dps){
        final float scale = context.getResources().getDisplayMetrics().density;
//        System.out.println("dps = " + dps);
//        System.out.println("scale = " + scale);
        int pixels = (int) (dps * scale + 0.5f);
//        System.out.println("pixels = " + pixels);
        return pixels;
    }
}
