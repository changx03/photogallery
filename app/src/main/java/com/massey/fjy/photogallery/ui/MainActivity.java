package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.db.DbHelper;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {
    private static final int REQUEST_CAMERA = 0, SELECT_FILE = 1; // intent request code
    private static final int VIEW_MODE_GRID = 0, VIEW_MODE_LIST = 1;
    private int mViewMode;

    private Integer[] mImgIds = {
            R.drawable.img_0,
            R.drawable.img_1,
            R.drawable.img_2,
            R.drawable.img_3,
            R.drawable.img_4,
            R.drawable.img_5,
            R.drawable.img_6,
            R.drawable.img_7,
            R.drawable.img_8,
            R.drawable.img_9,
    };

    // navigation drawer
    private String[] mImageTags;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    private int mViewBy;
    private String mOptionKeyWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("LOG MainActivity onCreate");
        // provide some initial testing images
        DbHelper dbHelper = new DbHelper(this);
        String photoGalleryPath = getFilesDir() + "/" + DataHelper.IMAGE_DIR;
        File photoGalleryDir = new File(photoGalleryPath);
        if (!photoGalleryDir.exists()) {
            photoGalleryDir.mkdir();
            for (int i = 0; i < mImgIds.length; i++) {
                // loading smaller image into private gallery folder. Nexus 5 emulator seems doesn't have enough memory for full size
                Bitmap mImg = BitmapHelper.decodeBitmapFromResource(getResources(), mImgIds[i], 640, 480);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                mImg.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(photoGalleryDir, getResources().getStringArray(R.array.thumbnailImages)[i]);
                mImg.recycle();

                FileOutputStream fos;
                try {
                    destination.createNewFile();
                    fos = new FileOutputStream(destination);
                    fos.write(bytes.toByteArray());
                    fos.close();

                    //get date
                    String date = DataHelper.getDateTimeToString();
                    // add to database
                    //save(tag,location,latitude,longitude, note, imageName, date, tagPeople)
                    Long save = dbHelper.save(getResources().getStringArray(R.array.tags)[i],
                            "Auckland",
                            (float) 999.999,
                            (float) 666.666,
                            ("Note: " + getResources().getStringArray(R.array.thumbnailImages)[i]),
                            getResources().getStringArray(R.array.thumbnailImages)[i], date, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // save gallery path
        SharedPreferences.Editor editor = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(DataHelper.PHOTO_GALLERY_FULL_PATH, photoGalleryPath);
        editor.apply();

        // navigation drawer
        mTitle = mDrawerTitle = getTitle();
        mImageTags = getResources().getStringArray(R.array.tags);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        //System.out.println("LOG MainActivity mDrawerLayout = " + mDrawerLayout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        //System.out.println("LOG MainActivity mDrawerList = " + mDrawerList);
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mImageTags));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_action_navigation_menu,
                R.string.drawer_open,
                R.string.drawer_close) {
            // Called when a drawer has settled in a completely closed state
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely open state
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setIcon(android.R.color.transparent);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // get view mode
        SharedPreferences settings = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        mViewMode = settings.getInt(DataHelper.VIEW_MODE, VIEW_MODE_GRID);   // use fragment_grid view as default
        // Create view fragment
        mViewBy = DataHelper.VIEW_BY_ALL;
        mOptionKeyWord = mImageTags[0];
        showViewFragment(mViewMode, mViewBy, mOptionKeyWord); // read view mode from settings in sharedpreferences

        handleIntent(getIntent()); //for search
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_add).setVisible(!drawerOpen);
        menu.findItem(R.id.action_choose).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void showViewFragment(int mode, int viewBy, String optionKeyWord) {
        Bundle args = new Bundle();
        args.putInt(DataHelper.VIEW_BY, viewBy);
        args.putString(DataHelper.OPTION_KEY_WORD, optionKeyWord);
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (mode == 0) {
            GridFragment gf = new GridFragment();
            gf.setArguments(args);
            ft.replace(R.id.container, gf, "grid_view");
            ft.commit();
        } else if (mode == 1){
            ListFragment lf = new ListFragment();
            lf.setArguments(args);
            ft.replace(R.id.container, lf, "list_view");
            ft.commit();
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        // Insert the fragment by replacing any existing fragment
        showViewFragment(mViewMode, DataHelper.VIEW_BY_TAG, mImageTags[position]);

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mImageTags[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String myQueryStr = intent.getStringExtra(SearchManager.QUERY);
            System.out.println("query word = " + myQueryStr);
            //use the query to search data somehow
            mDrawerList.setVisibility(View.INVISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            Thread.sleep(1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mDrawerList.setVisibility(View.VISIBLE);
                                    mViewBy = DataHelper.VIEW_BY_SEARCH;
                                    showViewFragment(mViewMode, mViewBy, myQueryStr);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            }).start();
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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
                        //File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
                       // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
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

        // reset tag option
        mOptionKeyWord = mImageTags[0];
        mViewBy = DataHelper.VIEW_BY_ALL;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
        showViewFragment(mViewMode, mViewBy, mOptionKeyWord);
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
      //  File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
       // Bitmap bitmap = BitmapHelper.decodeBitmapFromUri(file.getAbsolutePath(), 1000, 700);

        if(!saveBitmapToPrivateGallery(bitmap)){
            System.out.println("Photo Capture: Save image failed ");
        }
        bitmap.recycle();
    }

    private boolean saveBitmapToPrivateGallery(Bitmap bitmap){
        boolean success = false;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        // get galleryPath from SharedPref
        SharedPreferences sharedPref = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        String galleryPath = sharedPref.getString(DataHelper.PHOTO_GALLERY_FULL_PATH, null);
        //System.out.println("MainActivity_galleryPath = " + galleryPath);

        if(galleryPath != null) {
            File photoGalleryDir = new File(galleryPath);
            if (!photoGalleryDir.exists()) {
                photoGalleryDir.mkdir();
            }
            String imageName = "myPhoto" + System.currentTimeMillis() + ".jpg";
            File imgFile = new File(photoGalleryDir,
                    imageName);
            System.out.println("MainActivity_imageName = " + imageName);

            FileOutputStream fos;
            try {
                imgFile.createNewFile();
                fos = new FileOutputStream(imgFile);
                fos.write(bytes.toByteArray());
                fos.close();
                System.out.println("Image saved");
                success = true;



                // add image to database
                DbHelper dbHelper = new DbHelper(getApplication());
                String date = DataHelper.getDateTimeToString();
                dbHelper.save(null, null, null, null, null, imageName, date, null);
                System.out.println("MainActivity_date = " + date);

            } catch (IOException e) {
                success = false;
                e.printStackTrace();
            }
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
                switch (item.getItemId()) {
                    case R.id.grid_view:
                        mViewMode = VIEW_MODE_GRID;
                        showViewFragment(mViewMode, mViewBy, mOptionKeyWord);
                        break;
                    case R.id.list_view:
                        mViewMode = VIEW_MODE_LIST;
                        showViewFragment(mViewMode, mViewBy, mOptionKeyWord);
                        break;
                    case R.id.multiple_select:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onStop() { // update view mode
        System.out.println("LOG MainActivity onStop");
        System.out.println("mViewMode = " + mViewMode + " mViewBy = " + mViewBy + " mOptionKeyWord = " + mOptionKeyWord);

        SharedPreferences.Editor editor = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(DataHelper.VIEW_MODE, mViewMode);
        editor.putInt(DataHelper.VIEW_BY, mViewBy);
        editor.putString(DataHelper.OPTION_KEY_WORD, mOptionKeyWord);
        editor.apply();

        super.onStop();
    }

    // Don't have foreground activity yet
//    @Override
//    public void onPause(){
//        System.out.println("LOG MainActivity onPause");
//        System.out.println("mViewMode = " + mViewMode + " mViewBy = " + mViewBy + " mOptionKeyWord = " + mOptionKeyWord);
//
//        SharedPreferences.Editor editor = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE).edit();
//        editor.putInt(DataHelper.VIEW_MODE, mViewMode);
//        editor.putInt(DataHelper.VIEW_BY, mViewBy);
//        editor.putString(DataHelper.OPTION_KEY_WORD, mOptionKeyWord);
//        editor.apply();
//
//        super.onPause();
//    }

    @Override
    public void onResume(){
        System.out.println("LOG MainActivity onResume");

        SharedPreferences sharedPref = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        mViewMode = sharedPref.getInt(DataHelper.VIEW_MODE, VIEW_MODE_GRID);
        mViewBy = sharedPref.getInt(DataHelper.VIEW_BY, DataHelper.VIEW_BY_ALL);
        mOptionKeyWord = sharedPref.getString(DataHelper.OPTION_KEY_WORD, getResources().getStringArray(R.array.tags)[0]);
        System.out.println("mViewMode = " + mViewMode + " mViewBy = " + mViewBy + " mOptionKeyWord = " + mOptionKeyWord);

        //showViewFragment(mViewMode, mViewBy, mOptionKeyWord);
        super.onResume();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
