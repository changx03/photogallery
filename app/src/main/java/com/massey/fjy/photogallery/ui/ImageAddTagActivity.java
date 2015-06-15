package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;
import com.massey.fjy.photogallery.utils.TagImageLayout;
import com.massey.fjy.photogallery.utils.TagLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ImageAddTagActivity extends Activity {
    String imagePath;
    Bitmap resourceImage;
    ProgressDialog progressDiag;

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

    private class SaveTagImageToFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            SaveTagImageToFile();
            finish();
            return null;
        }
        @Override
        protected void onPreExecute() {
            ShowProgressDialog();
        }

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
            undoTagView();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveAndClearTags();
            SaveTagImageToFileTask s = new SaveTagImageToFileTask();
            s.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAndClearTags() {
        TagImageLayout tagImage = (TagImageLayout)findViewById(R.id.tag_image_layout);
        ArrayList<String> tagsContent = getTagsContent(tagImage); // Get All content
        System.out.println("LOG: tags content" + tagsContent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDiag != null)
            progressDiag.dismiss();
    }

    private void undoTagView() {
        TagImageLayout tagImage = (TagImageLayout)findViewById(R.id.tag_image_layout);
        int childNum = tagImage.getChildCount();
        if (childNum > 1) tagImage.removeViewAt(childNum - 1);
    }

    private void SaveTagImageToFile() {
        TagImageLayout tagImage = (TagImageLayout)findViewById(R.id.tag_image_layout);
        tagImage.setDrawingCacheEnabled(true);
        Bitmap outImage = tagImage.getDrawingCache();
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

    private ArrayList<String> getTagsContent(TagImageLayout tagImage) {
        ArrayList<String> tagsContent = new ArrayList<>();
        int childNum = tagImage.getChildCount();
        for (int i = 1; i < childNum; i++) {
            TagLayout tl = (TagLayout)tagImage.getChildAt(i);
            ViewGroup vg = (ViewGroup)tl.getChildAt(0);
            RelativeLayout rl = (RelativeLayout)vg.getChildAt(0);
            TextView tv = (TextView)rl.getChildAt(0);
            EditText ev = (EditText)rl.getChildAt(1);
            ev.setEnabled(false);
            if (tv.getText().length() != 0) {
                tagsContent.add(tv.getText().toString());
            }
        }
        System.out.println("LOG tags content = " + tagsContent);
        return tagsContent;
    }

    private void ShowProgressDialog() {
        progressDiag = new ProgressDialog(this);
        progressDiag.setMessage("Saving...");
        progressDiag.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDiag.setIndeterminate(true);
        progressDiag.setProgress(0);
        progressDiag.show();

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while(jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progressDiag.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }
}