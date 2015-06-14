package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;
import com.massey.fjy.photogallery.utils.FilterHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by fangjingyuan on 6/12/15.
 */

public class ImageEditActivity extends Activity {
    private Bitmap resourceImage;
    private boolean clickFilter[];
    private int filterNum;
    private String imagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        LinearLayout ll = (LinearLayout)findViewById(R.id.filter_preview_list);

        // get current image path from sharePreferences
        SharedPreferences prefs = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        imagePath = prefs.getString(DataHelper.CURRENT_IMAGE_PATH, null);

        System.out.println("LOG: I want to edit this image: " + imagePath);

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(), BitmapHelper.IMAGE_EDIT_ACTIVITY_WINDOW_HEIGHT);
        resourceImage = BitmapHelper.decodeBitmapFromUri(imagePath, reqSize, reqSize);
        System.out.println("LOG: resize size = " + reqSize);

        System.out.println("LOG: bitmap size = " + BitmapHelper.getByteSizeOf(resourceImage));

        ImageView imageView = (ImageView)findViewById(R.id.bigImage);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        imageView.setImageBitmap(resourceImage);

        ArrayList<Bitmap> filterBitmaps = generateFilters(resourceImage);
        filterNum = filterBitmaps.size();
        clickFilter = new boolean[filterNum];
        for (int i = 0; i < filterBitmaps.size(); i++) {
            ll.addView(generateView(filterBitmaps.get(i), i));
        }

        getActionBar().setIcon(android.R.color.transparent);
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

        Bitmap src = BitmapHelper.getResizedBitmap(resource, 160);

        ArrayList<Bitmap> filterBitmaps = new ArrayList<>();

        for (int i = 0; i < 8; i++)
            filterBitmaps.add(useFilter(src, i));

        return filterBitmaps;
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
        getMenuInflater().inflate(R.menu.menu_image_edit, menu);
        return true;
    }

    private void SaveFilteredImageToFile() {

        for (int i = 0; i < filterNum; i++) {
            if (clickFilter[i]) {
                // get a bigger size image
                int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(), BitmapHelper.IMAGE_SAVE_SIZE);
                Bitmap srcImage = BitmapHelper.decodeBitmapFromUri(imagePath, reqSize, reqSize);
                System.out.println("LOG: filePath = " + imagePath);
                Bitmap outImage = useFilter(srcImage, i);
                System.out.println("LOG: save a new image size = " + BitmapHelper.getByteSizeOf(outImage));

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(imagePath);
                    outImage.compress(Bitmap.CompressFormat.JPEG, 85, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                final ProgressDialog progress = new ProgressDialog(this);
                progress.setMessage("Saving...");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setIndeterminate(true);
                progress.setProgress(0);
                progress.show();

                final int totalProgressTime = 100;
                final Thread t = new Thread() {
                    @Override
                    public void run() {
                        int jumpTime = 0;

                        while (jumpTime < totalProgressTime) {
                            try {
                                sleep(200);
                                jumpTime += 5;
                                progress.setProgress(jumpTime);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                };
                t.start();
            /*    System.out.println("LOG: show toast");
                Toast toast = Toast.makeText(getApplicationContext(), "Saving image...", Toast.LENGTH_LONG);
                toast.show();
                System.out.println("LOG: end show toast");*/

                //update real image in directory
                SaveFilteredImageToFile();

                //end this activity
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


}
