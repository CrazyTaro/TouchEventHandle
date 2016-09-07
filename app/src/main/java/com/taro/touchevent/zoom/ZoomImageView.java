package com.taro.touchevent.zoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.taro.touchevent.utils.TouchEventHelper;

/**
 * Created by taro on 16/6/3.
 */
public class ZoomImageView extends View implements ICropDrawAction {
    private ZoomImageDraw mZoom = null;

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ZoomImageView(Context context) {
        super(context);
        mZoom = new ZoomImageDraw(this);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mZoom.onDraw(canvas);
    }

    @Override
    public boolean restoreBitmap(String fileNameWithPath, Bitmap.CompressFormat bmpFormat, boolean isRecycleBmp, int bmpQuality) {
        return false;
    }

    @Override
    public void recycleBitmap() {
        mZoom.recycleBitmap();
    }

    @Override
    public boolean setCropWidthAndHeight(int eachSize) {
        return mZoom.setCropWidthAndHeight(eachSize);
    }

    @Override
    public void setImageBitmap(Bitmap src) {
        mZoom.setImageBitmap(src);
    }

    public TouchEventHelper.OnToucheEventListener getTouchHandle() {
        return mZoom;
    }


    public void setScaleBegin() {
        mZoom.setScaleBegin();
    }

    public boolean isScaleFinished() {
        return mZoom.isScaleFinished();
    }
}
