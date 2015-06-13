package com.massey.fjy.photogallery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
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

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by fangjingyuan on 6/12/15.
 */

public class ImageEditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_filter);

        LinearLayout ll = (LinearLayout)findViewById(R.id.filter_preview_list);


        // get current image path from sharePreferences
     //   SharedPreferences.Editor editor = getSharedPreferences()

        for (int i = 0; i < 20; i++)
        ll.addView(generateView());
    }

    View generateView() {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(200, 220));
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);

        ImageView iv = new ImageView(getApplicationContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(175, 175));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageResource(R.drawable.cat);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_LONG).show();
            }
        });

        TextView tv = new TextView(getApplicationContext());
        tv.setGravity(Gravity.CENTER);
        tv.setText("Filter Name");

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
            case R.id.action_next:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


}
