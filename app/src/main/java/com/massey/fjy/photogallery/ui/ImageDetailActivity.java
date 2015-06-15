package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class ImageDetailActivity extends FragmentActivity implements DialogInterface.OnDismissListener {
    public static final String EXTRA_IMAGE = "extra_image";
    public static final String IMAGE_NAME = "image_name";
    private Bitmap mySelectedBitmap;
    private String imagePath;
    private String myImageName;
    private EditNoteDialog editNoteDialog;
    private EditTagDialog editTagDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        Bundle extras = getIntent().getExtras();
        int currentIndex = 0;
        if(extras != null){
            currentIndex = extras.getInt(EXTRA_IMAGE);
            myImageName = extras.getString(IMAGE_NAME);
            System.out.println("LOG ImageDetailActivity: currentIndex = " + currentIndex);
        }

        Toast toast = Toast.makeText(getApplicationContext(), "Loading image...", Toast.LENGTH_SHORT);
        toast.show();
        SystemClock.sleep(100);

        // get image ful path
        SharedPreferences sharedPref = getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        String photoGalleryPath = sharedPref.getString(DataHelper.PHOTO_GALLERY_FULL_PATH, null);
        DbHelper dbHelper = new DbHelper(this);
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

        editNoteDialog = new EditNoteDialog(this);
        editNoteDialog.setOnDismissListener(this);
        editNoteDialog.setTitle("Edit Note");

        Bundle args = new Bundle();
        args.putString(IMAGE_NAME, myImageName);
        editTagDialog = new EditTagDialog();
        editTagDialog.setArguments(args);
        editNoteDialog.setOnDismissListener(this);

        getActionBar().setIcon(android.R.color.transparent);
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
        // build alert editNoteDialog
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

    private void showEditPopup(final View v) {
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
                    case R.id.edit_tag:
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        editTagDialog.show(ft, "dialog");
                        break;
                    case R.id.add_note:
                        editNoteDialog.show();
                        break;
                    case R.id.tag_people:
                        intent = new Intent(ImageDetailActivity.this, ImageAddTagActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    // update view after dismiss Dialog
    @Override
    public void onDismiss(DialogInterface dialog) {
        // get data from db
        DbHelper dbHelper = new DbHelper(this);
        DataHelper.ImageData imageData = dbHelper.getImageDataByImageName(myImageName);
        System.out.println("LOG ImageDetailActivity: image key = " + imageData.key);

        // update view content
        TextView myTag = (TextView) findViewById(R.id.imageDetail_tag);
        TextView myLocation = (TextView) findViewById(R.id.imageDetail_location);
        TextView myNote = (TextView) findViewById(R.id.imageDetail_note);
        TextView myDate = (TextView) findViewById(R.id.imageDetail_date);
        myTag.setText(imageData.tag);
        myLocation.setText(imageData.location);
        myDate.setText(imageData.date);
        myNote.setText(imageData.note);
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

    public static class EditTagDialog extends DialogFragment {
        private String myImageName;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            myImageName = getArguments().getString(IMAGE_NAME);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.edit_tag_title)
                    .setItems(R.array.tags, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            String updatedTag = getResources().getStringArray(R.array.tags)[which];
                            System.out.println("LOG ImageDetailActivity: new tag = " + updatedTag);
                            DbHelper dbHelper = new DbHelper(getActivity());
                            DataHelper.ImageData imageData = dbHelper.getImageDataByImageName(myImageName);
                            imageData.tag = updatedTag;
                            dbHelper.update(imageData);
                        }
                    });
            return builder.create();
        }

        @Override
        public void onDismiss(final DialogInterface dialog) {
            super.onDismiss(dialog);
            final Activity activity = getActivity();
            if (activity instanceof DialogInterface.OnDismissListener) {
                ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
            }
        }
    }

    private class EditNoteDialog extends Dialog {
        private Context myContext;
        private DbHelper dbHelper;
        private DataHelper.ImageData imageData;

        private EditNoteDialog(Context context) {
            super(context);
            myContext = context;
        }

        @Override
        public void onCreate(Bundle savedInstanState) {
            final View mContentView = getLayoutInflater().inflate(R.layout.dialog_edit_note, null, false);
            final EditText myDialogNote = (EditText) mContentView.findViewById(R.id.dialog_edit_note);
            Button mCancel = (Button) mContentView.findViewById(R.id.cancel);
            Button mSave = (Button) mContentView.findViewById(R.id.save);
            Button mClear = (Button) mContentView.findViewById(R.id.clear);
            dbHelper = new DbHelper(myContext);
            imageData = dbHelper.getImageDataByImageName(myImageName);
            myDialogNote.setText(imageData.note);
            mSave.setClickable(true);
            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("LOG ImageDetailActivity: save the current note");
                    //Save the note to db
                    Toast.makeText(getApplicationContext(), R.string.toast_ImageDetailActivity_noteUpdate,
                            Toast.LENGTH_SHORT).show();

                    imageData.note = myDialogNote.getText().toString();
                    dbHelper.update(imageData);

                    //close editNoteDialog
                    EditNoteDialog.this.cancel();
                }
            });
            mClear.setClickable(true);
            mClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialogNote.setText(null);
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
}
