package com.taro.touchevent.zoom;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.taro.touchevent.R;
import com.taro.touchevent.utils.TouchEventHelper;

/**
 * Created by taro on 16/6/3.
 */
public class ZoomBrowserActivity extends Activity implements TouchEventHelper.OnToucheEventListener {
    private ZoomImageView[] mCacheView = null;
    private int mCurrentItem = 0;
    private LinearLayout mViewGroup = null;
    private TouchEventHelper mHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_browser);
        mViewGroup = (LinearLayout) findViewById(R.id.ll_container);

        mCacheView = new ZoomImageView[3];
        for (int i = 0; i < 3; i++) {
            mCacheView[i] = new ZoomImageView(this);
        }
        mCacheView[0].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg));
        mCacheView[1].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pkq));
        mCacheView[2].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shot));

        addView(0);
        mHelper = new TouchEventHelper(this);
        mHelper.setIsShowLog(true, null);
    }

    private void removeView(int index) {
        View view = mCacheView[index];
        mViewGroup.removeView(view);
    }

    private void addView(int index) {
        View view = mCacheView[index];
        mViewGroup.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int eventType = ev.getAction() & MotionEvent.ACTION_MASK;
        if (eventType == MotionEvent.ACTION_POINTER_DOWN) {
            ZoomImageView view = mCacheView[mCurrentItem];
            if (view.isScaleFinished()) {
                view.setScaleBegin();
            }
        }
        ZoomImageView view = mCacheView[mCurrentItem];
        if (view.isScaleFinished()) {
            mHelper.onTouch(null, ev);
            return true;
        }
        mHelper.onTouch(null, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSingleTouchEventHandle(MotionEvent event, int extraMotionEvent) {
        if (mHelper.getTouchMotionEventAction(event) == MotionEvent.ACTION_UP) {
            int newItemIndex = mCurrentItem;
            newItemIndex++;
            newItemIndex %= 3;
            removeView(mCurrentItem);
            addView(newItemIndex);
            mCurrentItem = newItemIndex;
        }
    }

    @Override
    public void onMultiTouchEventHandle(MotionEvent event, int extraMotionEvent) {
    }

    @Override
    public void onSingleClickByTime(MotionEvent event) {

    }

    @Override
    public void onSingleClickByDistance(MotionEvent event) {

    }

    @Override
    public void onDoubleClickByTime() {

    }
}
