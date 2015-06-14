package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;
import com.massey.fjy.photogallery.utils.FilterHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by fangjingyuan on 6/12/15.
 */

public class ImageEditActivity extends Activity {
    private Bitmap resourceImage;
    private boolean clickFilter[];
    private int filterNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_filter);

        LinearLayout ll = (LinearLayout)findViewById(R.id.filter_preview_list);


        // get current image path from sharePreferences
        SharedPreferences prefs = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        String imagePath = prefs.getString(DataHelper.CURRENT_IMAGE_PATH, null);

        System.out.println("LOG: I want to edit this image: " + imagePath);

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(), BitmapHelper.IMAGE_DETAIL_ACTIVITY_WINDOW_HEIGHT);
        Bitmap mySelectedBitmap = BitmapHelper.decodeBitmapFromUri(imagePath, reqSize, reqSize);
        resourceImage = getResizedBitmap(mySelectedBitmap, 640);

        System.out.println("LOG: bitmap size = " + byteSizeOf(mySelectedBitmap));

        ImageView imageView = (ImageView)findViewById(R.id.bigImage);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        imageView.setImageBitmap(resourceImage);


        ArrayList<Bitmap> filterBitmaps = generateFilters(resourceImage);
        filterNum = filterBitmaps.size();
        clickFilter = new boolean[filterNum];
        for (int i = 0; i < filterBitmaps.size(); i++) {
            ll.addView(generateView(filterBitmaps.get(i), i));
        }
    }

   String filterNames[] = {"Brighter", "Red", "Green", "Blue", "Depth", "Constract", "Hue", "Invert" };

    private Bitmap useFilter(Bitmap src, int filterIndex) {
        FilterHelper filterhelper;
        filterhelper = new FilterHelper();

        Bitmap output = src;

        switch (filterIndex) {
            case 0:
                output = filterhelper.applyBrightnessEffect(src, 100);
                break;
            case 1:
                output = filterhelper.applyColorFilterEffect(src, 255, 0, 0);
                break;
            case 2:
                output = filterhelper.applyColorFilterEffect(src, 0, 255, 0);
                break;
            case 3:
                output = filterhelper.applyColorFilterEffect(src, 0, 0, 255);
                break;
            case 4:
                output = filterhelper.applyDecreaseColorDepthEffect(src, 64);
                break;
            case 5:
                output = filterhelper.applyContrastEffect(src, 70);
                break;
            case 6:
                output = filterhelper.applyHueFilter(src, 2);
                break;
            case 7:
                output = filterhelper.applyInvertEffect(src);
                break;
            default:
                break;

        }
        return output;
    }

    private ArrayList<Bitmap> generateFilters(Bitmap resource) {
        FilterHelper filterhelper;
        filterhelper = new FilterHelper();

        Bitmap src = getResizedBitmap(resource, 160);

        ArrayList<Bitmap> filterBitmaps = new ArrayList<>();

        for (int i = 0; i < 8; i++)
            filterBitmaps.add(useFilter(src, i));

        return filterBitmaps;
    }

    public static int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) { // resize bitmap
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    private View generateView(Bitmap bitmap, final int index) {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(200, 220));
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);

        ImageView iv = new ImageView(getApplicationContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(175, 175));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageBitmap(bitmap);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) findViewById(R.id.bigImage);
                imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                if (!clickFilter[index]) {
                    imageView.setImageBitmap(useFilter(resourceImage, index));
                    clickFilter = new boolean[filterNum];
                    clickFilter[index] = true;
                } else {
                    imageView.setImageBitmap(resourceImage);
                    clickFilter[index] = false;
                }

            }
        });

        TextView tv = new TextView(getApplicationContext());
        tv.setGravity(Gravity.CENTER);
        tv.setText(filterNames[index]);

        layout.addView(iv);
        layout.addView(tv);
        return layout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:

                return true;

        }

        return super.onOptionsItemSelected(item);
    }


}
