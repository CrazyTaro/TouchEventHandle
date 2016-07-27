package com.taro.touchevent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by taro on 16/6/30.
 */
public class TestView extends View {
    private static final int PER_ANIMATION_INTERVAL = 100;
    private int mColumnCount = 5;
    private int mRowCount = 6;

    private int mMaxNumber = mRowCount * mRowCount;

    private Point mViewParams = null;
    private Paint mDrawPaint = null;
    private Paint mTextPaint = null;

    public TestView(Context context) {
        super(context);
        this.initial();
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initial();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        loadViewParams();
        int boxWidth = calculateBoxWidth(mViewParams.x, mColumnCount);
        Point startP = calculateFirstDrawPosition(boxWidth);
        List<String> dataList = calculateRandomNumberPosition(mMaxNumber, mRowCount, mColumnCount);
        drawBox(dataList, canvas, boxWidth, startP.x, startP.y, mColumnCount, mRowCount);
    }

    private void initial() {
        mViewParams = new Point();
        mDrawPaint = new Paint();
        mTextPaint = new Paint();
    }

    private void loadViewParams() {
        if (mViewParams.x <= 0 || mViewParams.y <= 0) {
            int width = this.getWidth();
            int height = this.getHeight();
            if (width <= 0) {
                width = this.getMeasuredWidth();
            }
            if (height <= 0) {
                height = this.getMeasuredHeight();
            }
            mViewParams.set(width, height);
        }
    }

    private int calculateBoxWidth(int viewWidth, int columnCount) {
        int totalSize = columnCount + columnCount / 2 + 1;
        return viewWidth / totalSize;
    }

    private Point calculateFirstDrawPosition(int boxWidth) {
        return new Point(boxWidth / 2, boxWidth / 2);
    }

    private List<String> calculateRandomNumberPosition(int maxNumber, int rowCount, int columnCount) {
        int boxCount = rowCount * columnCount;
        List<String> numberList = new ArrayList<String>(boxCount);
        if (maxNumber < boxCount) {
            for (int i = 0; i < maxNumber; i++) {
                numberList.add(i, String.valueOf(i + 1));
            }
            for (int i = maxNumber; i < boxCount; i++) {
                numberList.add(i, null);
            }
        } else {
            for (int i = 0; i < boxCount; i++) {
                numberList.add(i, String.valueOf(i + 1));
            }
        }
        Collections.shuffle(numberList);
        return numberList;
    }

    private void drawBox(List<String> dataList, Canvas canvas, int boxWidth, int startX, int startY, int columnCount, int rowCount) {
        int drawX = startX;
        int drawY = startY;
        Rect rect = new Rect();
        Box box = new Box();
        box.setEdgeSize(boxWidth);

        mDrawPaint.setStrokeWidth(1);
        mDrawPaint.setColor(Color.BLACK);
        mDrawPaint.setStyle(Paint.Style.STROKE);

        mTextPaint.setStrokeWidth(3);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(box.mTextSize);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                box.setDrawXY(drawX, drawY);
                canvas.drawRect(box.getDrawRect(rect), mDrawPaint);

                box.setText(dataList.get(i * columnCount + j));
                if (box.isNeedToDrawBox()) {
                    box.calculateTextXY();
                    canvas.drawText(box.mText, box.mTextX, box.mTextY, mTextPaint);
                }
                drawX += boxWidth * 1.5;
            }
            drawX = startX;
            drawY += boxWidth * 1.5;
        }
    }

    private class Box {
        public int mDrawX;
        public int mDrawY;
        public int mTextX;
        public int mTextY;
        public int mEdgeSize;
        public int mTextSize;
        public String mText;

        public void setDrawXY(int drawX, int drawY) {
            mDrawX = drawX;
            mDrawY = drawY;
        }

        public void setTextXY(int textX, int textY) {
            mTextX = textX;
            mTextY = textY;
        }

        public void setEdgeSize(int size) {
            mEdgeSize = size;
            mTextSize = mEdgeSize / 3 * 2;
        }

        public void setText(String text) {
            mText = text;
        }

        public boolean isNeedToDrawBox() {
            return mText != null;
        }

        public void calculateTextXY() {
            mTextX = (mEdgeSize - mTextSize) / 2 + mDrawX;
            mTextY = (mEdgeSize + mTextSize - mTextSize / 3) / 2 + mDrawY;
        }

        public Rect getDrawRect(Rect outRect) {
            outRect.set(mDrawX, mDrawY, mDrawX + mEdgeSize, mDrawY + mEdgeSize);
            return outRect;
        }
    }

    private static class DrawUtil {
        public static final Point getDensity(Point storeP, Context context) {
            if (storeP == null) {
                storeP = new Point();
            }
            Resources res = context.getResources();
            storeP.set(res.getDisplayMetrics().widthPixels, res.getDisplayMetrics().heightPixels);
            return storeP;
        }

        public static final float convertDpiToPx(Context context, int dpi) {
            Resources res = context.getResources();
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, res.getDisplayMetrics());
        }
    }
}
