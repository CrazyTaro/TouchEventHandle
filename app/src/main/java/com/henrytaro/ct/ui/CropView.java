package com.henrytaro.ct.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by taro on 16/1/22.
 */
public class CropView extends View implements ICropDrawAction {
    private CropDraw mCropDraw = null;

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initial();
    }

    public CropView(Context context) {
        super(context);
        initial();
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }


    /**
     * 初始化
     */
    private void initial() {
        mCropDraw = new CropDraw(this);
    }

    @Override
    public void setImageBitmap(Bitmap src) {
        mCropDraw.setImageBitmap(src);
    }

    @Override
    public boolean setCropWidthAndHeight(int width) {
        return mCropDraw.setCropWidthAndHeight(width);
    }

    @Override
    public boolean restoreBitmap(String fileNameWithPath, Bitmap.CompressFormat bmpFormat, boolean isRecycleBitmap, int bmpQuality) {
        return mCropDraw.restoreBitmap(fileNameWithPath, bmpFormat, isRecycleBitmap, bmpQuality);
    }

    @Override
    public void recycleBitmap() {
        mCropDraw.recycleBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCropDraw.onDraw(canvas);
    }
}
