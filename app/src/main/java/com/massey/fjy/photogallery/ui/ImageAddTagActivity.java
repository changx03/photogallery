package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;

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

        System.out.println("LOG: I want to edit this image: " + imagePath);

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(), BitmapHelper.IMAGE_EDIT_ACTIVITY_WINDOW_HEIGHT);
        resourceImage = BitmapHelper.decodeBitmapFromUri(imagePath, reqSize, reqSize);
        System.out.println("LOG: resize size = " + reqSize);

        System.out.println("LOG: bitmap size = " + BitmapHelper.getByteSizeOf(resourceImage));

        ImageView imageView = (ImageView)findViewById(R.id.bigImage);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        imageView.setImageBitmap(resourceImage);


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

        //noinspection SimplifiableIfStatement
        if (id == R.id.save) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
