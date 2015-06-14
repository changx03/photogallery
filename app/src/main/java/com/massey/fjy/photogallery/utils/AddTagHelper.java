package com.massey.fjy.photogallery.utils;

import android.content.Context;
import android.graphics.Path;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.massey.fjy.photogallery.R;

/**
 * Created by fangjingyuan on 6/14/15.
 */
public class AddTagHelper extends RelativeLayout implements TextView.OnEditorActionListener {
    public enum Direction{Left, Right};
    private Direction direction = Direction.Left;

    public AddTagHelper(Context context, Direction direction) {
        super(context);
        this.direction = direction;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.tagview_left, this, true);
        TextView tv = (TextView)findViewById(R.id.tag_label);
        EditText et = (EditText)findViewById(R.id.tag_edit);
        View v = findViewById(R.id.tag_background);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return true;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
