package com.henrytaro.ct.ui;

import android.content.Context;
import android.graphics.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.henrytaro.ct.utils.AbsTouchEventHandle;
import com.henrytaro.ct.utils.TouchUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
    public boolean restoreBitmap(String filePath, boolean isRecycleBitmap) {
        return mCropDraw.restoreBitmap(filePath, isRecycleBitmap);
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
