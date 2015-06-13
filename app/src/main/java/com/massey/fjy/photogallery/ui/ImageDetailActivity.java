package com.massey.fjy.photogallery.ui;

//import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;

import java.io.File;
import java.util.Arrays;


public class ImageDetailActivity extends FragmentActivity {
    public static final String EXTRA_IMAGE = "extra_image";
    private int currentIndex = 0;
    private Bitmap mySelectedBitmap;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solo);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            currentIndex = extras.getInt(EXTRA_IMAGE);
            System.out.println("currentIndex = " + currentIndex);
        }

        Toast toast = Toast.makeText(getApplicationContext(), "Loading image...", Toast.LENGTH_SHORT);
        toast.show();
        SystemClock.sleep(100);

        File targetDir = new File(getFilesDir() + "/" + GridFragment.imgDir);
        File[] files = targetDir.listFiles();
        Arrays.sort(files);
        File targetImage = files[currentIndex];
        files = null;

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(), BitmapHelper.IMAGE_DETAIL_ACTIVITY_WINDOW_HEIGHT);
        mySelectedBitmap = BitmapHelper.decodeBitmapFromUri(targetImage.getAbsolutePath(), reqSize, reqSize);

        imageView = (ImageView)findViewById(R.id.image);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //imageView.setImageResource(R.drawable.cat);
        imageView.setImageBitmap(mySelectedBitmap);
        //mySelectedBitmap.recycle();

        TextView tvLocation = (TextView)findViewById(R.id.location);
        TextView tvNote = (TextView)findViewById(R.id.note);

        tvLocation.setText("location 1");
        tvNote.setText("note 1");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_solo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                return true;
            case R.id.action_edit:
                showEditPopup(findViewById(item.getItemId()));
                return true;
            case R.id.action_delete:
                return true;
        }

        return super.onOptionsItemSelected(item);


    }

    private void showEditPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_edit_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.photo_filter:
                        intent = new Intent(ImageDetailActivity.this,ImageEditActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.add_note:
                        break;
                    case R.id.tag_people:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onStop() { // update view mode
        super.onStop();
        System.out.println("ImageDetailActivity onStop.");
        mySelectedBitmap.recycle();
        imageView.setImageDrawable(null);
    }

    @Override
    protected void onResume(){
        super.onResume();
        System.out.println("ImageDetailActivity onResume.");

        // reload image onResume
        File targetDir = new File(getFilesDir() + "/" + GridFragment.imgDir);
        File[] files = targetDir.listFiles();
        Arrays.sort(files);
        File targetImage = files[currentIndex];
        files = null;

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(), BitmapHelper.IMAGE_DETAIL_ACTIVITY_WINDOW_HEIGHT);
        mySelectedBitmap = BitmapHelper.decodeBitmapFromUri(targetImage.getAbsolutePath(), reqSize, reqSize);

        imageView = (ImageView)findViewById(R.id.image);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //imageView.setImageResource(R.drawable.cat);
        imageView.setImageBitmap(mySelectedBitmap);
    }
}
