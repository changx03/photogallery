package com.massey.fjy.photogallery.ui;

//import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.db.DbHelper;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;

import java.io.File;
import java.util.ArrayList;


public class ImageDetailActivity extends FragmentActivity {
    public static final String EXTRA_IMAGE = "extra_image";

    private Bitmap mySelectedBitmap;
    private String imagePath;
    private String myImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        Bundle extras = getIntent().getExtras();
        int currentIndex = 0;
        if(extras != null){
            currentIndex = extras.getInt(EXTRA_IMAGE);
            System.out.println("LOG ImageDetailActivity: currentIndex = " + currentIndex);
        }

        Toast toast = Toast.makeText(getApplicationContext(), "Loading image...", Toast.LENGTH_SHORT);
        toast.show();
        SystemClock.sleep(100);

        // get image name from database
        DbHelper dbHelper = new DbHelper(this);
        ArrayList<String> imagesNames = dbHelper.getAllGridView();
        myImageName = imagesNames.get(currentIndex);
        SharedPreferences sharedPref = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        String photoGalleryPath = sharedPref.getString(DataHelper.PHOTO_GALLERY_FULL_PATH, null);
        imagePath = photoGalleryPath + "/" + myImageName;
        System.out.println("LOG ImageDetailActivity: imagePath = " + imagePath);

        // update sharedPref
        SharedPreferences.Editor editor = getSharedPreferences(DataHelper.PREFS_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putString(DataHelper.CURRENT_IMAGE_PATH, imagePath);
        editor.apply();

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(),
                BitmapHelper.IMAGE_DETAIL_ACTIVITY_WINDOW_HEIGHT);
        mySelectedBitmap = BitmapHelper.decodeBitmapFromUri(imagePath, reqSize, reqSize);

        ImageView imageView = (ImageView) findViewById(R.id.imageDetail_image);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        imageView.setImageBitmap(mySelectedBitmap);
        //mySelectedBitmap.recycle();

        // get data from db
        DataHelper.ImageData imageData = dbHelper.getImageDataByImageName(myImageName);
        System.out.println("LOG ImageDetailActivity: image key = " + imageData.key);

        // update view content
        TextView myTag = (TextView) findViewById(R.id.imageDetail_tag);
        TextView mylocation = (TextView) findViewById(R.id.imageDetail_location);
        TextView myNote = (TextView) findViewById(R.id.imageDetail_note);
        TextView myDate = (TextView) findViewById(R.id.imageDetail_date);
        myTag.setText(imageData.tag);
        mylocation.setText(imageData.location);
        myDate.setText(imageData.date);
        myNote.setText(imageData.note);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
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
                deleteImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteImage() {
        // build alert dialog
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Delete image");
        alertBuilder.setMessage("Are you sure you want to delete this image?");
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DbHelper dbHelper = new DbHelper(getApplicationContext());
                Toast.makeText(getApplicationContext(), R.string.toast_ImageDetailActivity_delete,
                        Toast.LENGTH_SHORT).show();
                // delete from db
                dbHelper.deleteSingleImage(myImageName);
                // delete from storage
                File file = new File(imagePath);
                if (file.exists()) {
                    file.delete();
                }
                // return to perilous activity
                finish();
            }
        });
        alertBuilder.show();
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
                        intent = new Intent(ImageDetailActivity.this, ImageEditActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.add_note:
                        showEditNote();
                        break;
                    case R.id.tag_people:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private class EditNoteDialog extends Dialog {
        private View mContentView;
        private EditText mNote;
        private TextView mSave;
        private TextView mCancel;

        private EditNoteDialog(Context context) {
            super(context);
            initViews();
        }

        private void initViews() {
            mContentView = getLayoutInflater().inflate(R.layout.dialog_edit_note, null, false);
            mNote = (EditText) mContentView.findViewById(R.id.myNote);
            mCancel = (TextView) mContentView.findViewById(R.id.cancel);
            mSave = (TextView) mContentView.findViewById(R.id.save);
            mNote.setText("replace these with real note query from db!!!", TextView.BufferType.EDITABLE);

            mSave.setClickable(true);
            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("LOG ImageDetailActivity: save the current note");
                    //Save the note to db
                }
            });

            mCancel.setClickable(true);
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditNoteDialog.this.cancel();
                }
            });
            super.setContentView(mContentView);
        }

    }

    private void showEditNote() {

        EditNoteDialog dialog = new EditNoteDialog(ImageDetailActivity.this);
        dialog.setTitle("Edit Note");
        dialog.show();
    }

    @Override
    protected void onStop() { // update view mode
        super.onStop();
        System.out.println("LOG ImageDetailActivity: onStop.");
        mySelectedBitmap.recycle();
        ImageView imageView = (ImageView) findViewById(R.id.imageDetail_image);
        imageView.setImageDrawable(null);

        SharedPreferences.Editor editor = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(DataHelper.CURRENT_IMAGE_PATH, imagePath);

        editor.apply();
    }

    @Override
    protected void onResume(){
        super.onResume();
        System.out.println("LOG ImageDetailActivity: onResume.");

        SharedPreferences sharedPref = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        imagePath = sharedPref.getString(DataHelper.CURRENT_IMAGE_PATH, null);

        // scale down the image
        int reqSize = BitmapHelper.getPixelValueFromDps(getApplicationContext(),
                BitmapHelper.IMAGE_DETAIL_ACTIVITY_WINDOW_HEIGHT);
        mySelectedBitmap = BitmapHelper.decodeBitmapFromUri(imagePath, reqSize, reqSize);

        ImageView imageView = (ImageView) findViewById(R.id.imageDetail_image);
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        imageView.setImageBitmap(mySelectedBitmap);
    }
}
