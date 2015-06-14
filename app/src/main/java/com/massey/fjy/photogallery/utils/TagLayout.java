package com.massey.fjy.photogallery.utils;

import android.content.Context;
import android.graphics.Path;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.massey.fjy.photogallery.R;

/**
 * Created by fangjingyuan on 6/14/15.
 */
public class TagLayout extends RelativeLayout implements TextView.OnEditorActionListener {
    // input status
    public enum InputStatus{Normal, Edit};
    // tag's direction
    public enum Direction{Left, Right};
    private Direction direction = Direction.Left;
    // UI
    private TextView tv;
    private EditText et;
    private View tl; // The layout of one tag
    // input keyboard
    private InputMethodManager imm;
    // width height
    public static final int ViewWidth = 200;
    public static final int ViewHeight = 50;
    public TagLayout(Context context, Direction direction) {
        super(context);
        this.direction = direction;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.tagview_left, this, true);
        tv = (TextView)findViewById(R.id.tag_left_textview);
        et = (EditText)findViewById(R.id.tag_left_edittext);
        tl = findViewById(R.id.tag_left_layout);
        imm = (InputMethodManager)context.getSystemService(context.INPUT_METHOD_SERVICE);
        et.setOnEditorActionListener(this);
    }

    public void setInputStatus(InputStatus status) {
        switch(status) {
            case Normal:
                tv.setVisibility(View.VISIBLE);
                et.clearFocus();
                tv.setText(et.getText());
                et.setVisibility(View.GONE);
                imm.hideSoftInputFromInputMethod(et.getWindowToken(), 0);
                break;
            case Edit:
                tv.setVisibility(View.GONE);
                et.setVisibility(View.VISIBLE);
                et.requestFocus();
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                break;
        }
    }

    private void ChangeDirection() {
        switch(direction) {
            case Left:
                tl.setBackgroundResource(R.drawable.tagview_left);
                break;
            case Right:
                tl.setBackgroundResource(R.drawable.tagview_right);
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        setInputStatus(InputStatus.Normal);
        return true;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View parent = (View) getParent();
        int halfParentWidth = (int)(parent.getWidth() * 0.5);
        int center = (int)(l + this.getWidth()* 0.5);
        if (center <= halfParentWidth) direction = Direction.Left;
        else direction = Direction.Right;
        ChangeDirection();
    }
}
