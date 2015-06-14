package com.massey.fjy.photogallery.utils;

import android.content.Context;
import android.graphics.Rect;
import android.net.wifi.WifiConfiguration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.massey.fjy.photogallery.R;

/**
 * Created by fangjingyuan on 6/15/15.
 */

public class TagImageLayout extends RelativeLayout implements View.OnTouchListener {

    private View touchView, clickView;

    private int startX = 0;
    private int startY = 0;

    private int MoveViewLeft = 0;
    private int MoveViewTop = 0;

    public TagImageLayout(Context context) {
        super(context, null);
    }

    public TagImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int CLICKMOVERANGE = 5;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // start touch
                touchView = null;
                if (clickView != null) {
                    ((TagLayout)clickView).setInputStatus(TagLayout.InputStatus.Normal);
                    clickView = null;
                }
                startX = (int)event.getX();
                startY = (int)event.getY();
                if (hasView(startX, startY)) {
                    MoveViewLeft = touchView.getLeft();
                    MoveViewTop = touchView.getTop();
                } else {
                    addTagView(startX, startY);
                }
                break;
            case MotionEvent.ACTION_MOVE: // move fingers
                moveView((int)event.getX(), (int)event.getY());
                break;
            case MotionEvent.ACTION_UP: // fingers up
                int endX = (int)event.getX();
                int endY = (int)event.getY();

                if (touchView != null &&
                        Math.abs(endX - startX) < CLICKMOVERANGE &&
                        Math.abs(endY - startY) < CLICKMOVERANGE) {
                    ((TagLayout)touchView).setInputStatus(TagLayout.InputStatus.Edit);
                    clickView = touchView;
                }
                touchView = null;
                break;
        }
        return true;
    }

    private void moveView(int x, int y) {
        if (touchView == null) return;
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.leftMargin = x - startX + MoveViewLeft;
        params.topMargin = y - startY + MoveViewTop;

        if(params.leftMargin < 0 ||
                (params.leftMargin + touchView.getWidth()) > getWidth())
            params.leftMargin = touchView.getLeft();
        if(params.topMargin < 0 ||
                (params.topMargin + touchView.getHeight()) > getHeight())
            params.topMargin = touchView.getTop();

        touchView.setLayoutParams(params);
    }

    private boolean hasView(int x, int y) {

        for (int i = 0; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            if (view instanceof ImageView) continue;
            int left = (int)view.getX();
            int top = (int)view.getY();
            int right = (int)view.getRight();
            int bottom = (int)view.getBottom();
            Rect rect = new Rect(left, top, right, bottom);
            if(rect.contains(x, y)) {
                touchView = view;
                touchView.bringToFront();
                return true;
            }
        }
        touchView = null;
        return false;
    }

    private void addTagView(int x, int y) {
        if (this.getChildCount() > 3) {
            Toast.makeText(getContext(), "No more than 3 tags!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        View view = null;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        if (x <= getWidth() * 0.5) {
            params.leftMargin = x;
            view = new TagLayout(getContext(), TagLayout.Direction.Left);
        } else {
            params.leftMargin = x - TagLayout.ViewWidth;
            view = new TagLayout(getContext(), TagLayout.Direction.Right);
        }
        params.topMargin = y;
        if (params.topMargin < 0)
            params.topMargin = 0;
        else if (params.topMargin + TagLayout.ViewHeight > getHeight())
            params.topMargin = getHeight() - TagLayout.ViewHeight;
        this.addView(view, params);
    }
}
