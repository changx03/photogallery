package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;
import com.massey.fjy.photogallery.utils.TagImageLayout;

public class ImageAddTagActivity extends Activity {
    String imagePath;
    Bitmap resourceImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_add_tag);

        // get current image path from sharePreferences
        SharedPreferences prefs = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        imagePath = prefs.getString(DataHelper.CURRENT_IMAGE_PATH, null);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        // scale down the image
        resourceImage = BitmapHelper.decodeBitmapFromUri(imagePath, height, height);
        System.out.println("LOG: resize size = " + height);

        System.out.println("LOG: bitmap size = " + BitmapHelper.getByteSizeOf(resourceImage));

        ImageView tagImageView = (ImageView)findViewById(R.id.tag_image);
       // TagImageLayout tagImageView = (TagImageLayout)findViewById(R.id.tag_image);
        tagImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
      //  tagImageView.setBackground(new BitmapDrawable(getResources(), resourceImage));
        tagImageView.setImageBitmap(resourceImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_add_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_undo) {

            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.save) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}