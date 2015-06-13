package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.massey.fjy.photogallery.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String VIEW_MODE = "viewMode";
    private final int REQUEST_CAMERA = 0, SELECT_FILE = 1; // intent request code
    private final int VIEW_MODE_GRID = 0, VIEW_MODE_LIST = 1;
    private int mViewMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mViewMode = settings.getInt(VIEW_MODE, VIEW_MODE_GRID);   // use grid view as default
        showViewViaMode(mViewMode); // read view mode from settings in sharedpreferences

        handleIntent(getIntent()); //for search
    }

    private void showViewViaMode(int mode) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (mode == 0) {
            GridFragment gf = new GridFragment();
            ft.replace(R.id.container, gf, "grid_view");
            ft.commit();
        } else if (mode == 1){
            ListFragment lf = new ListFragment();
            ft.replace(R.id.container, lf, "list_view");
            ft.commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            System.out.println("query word" + query);
            //use the query to search data somehow
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddPopup(findViewById(item.getItemId()));
                return true;
            case R.id.action_choose:
                showChoosePopup(findViewById(item.getItemId()));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_add_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.take_photo:
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                        break;
                    case R.id.choose_from_library:
                        intent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data){
        // doing same thing as onCaptureImageResult right now
        // copy image from library to private directory
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        Bitmap myImg = BitmapFactory.decodeFile(filePath);

        if(!saveBitmapToPrivateGallery(myImg)){
            System.out.println("Selected Gallery: Save image failed ");
        }
        myImg.recycle();
    }

    private void onCaptureImageResult(Intent data){
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");

        if(!saveBitmapToPrivateGallery(bitmap)){
            System.out.println("Photo Capture: Save image failed ");
        }
        bitmap.recycle();
    }

    private boolean saveBitmapToPrivateGallery(Bitmap bitmap){
        boolean success;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File photoGalleryDir = new File(getFilesDir() + "/" + GridFragment.imgDir);
        if(!photoGalleryDir.exists()){
            photoGalleryDir.mkdir();
        }
        File imgFile = new File(photoGalleryDir,
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fos;
        try{
            imgFile.createNewFile();
            fos = new FileOutputStream(imgFile);
            fos.write(bytes.toByteArray());
            fos.close();
            System.out.println("Image saved");
            success = true;

        }catch (IOException e){
            success =false;
            e.printStackTrace();
        }

        bitmap.recycle();
        return success;
    }

    private void showChoosePopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_choose_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
//                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

                switch (item.getItemId()) {
                    case R.id.grid_view:
                        mViewMode = VIEW_MODE_GRID;
                        showViewViaMode(mViewMode);
                        break;
                    case R.id.list_view:
                        mViewMode = VIEW_MODE_LIST;
                        showViewViaMode(mViewMode);
                        break;
                    case R.id.multiple_select:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    // Don't have foreground activity yet
//    @Override
//    public void onPause(){
//        super.onPause();
//        System.out.println("MainActivity onPause.");
//        System.out.println("mViewMode = " + mViewMode);
//
//        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
//        editor.putInt(VIEW_MODE, mViewMode);
//        editor.apply();
//    }

    @Override
    protected void onStop() { // update view mode
        super.onStop();
        System.out.println("MainActivity onStop.");
        System.out.println("mViewMode = " + mViewMode);

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(VIEW_MODE, mViewMode);
        editor.apply();
    }

    @Override
    public void onResume(){
        super.onResume();
        System.out.println("MainActivity onResume.");

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mViewMode = sharedPref.getInt(VIEW_MODE, VIEW_MODE_GRID);
        showViewViaMode(mViewMode);
    }
}
