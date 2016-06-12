package com.henrytaro.ct.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.henrytaro.ct.utils.MoveAndScaleTouchHelper;
import com.henrytaro.ct.utils.TouchEventHelper;

/**
 * Created by taro on 16/3/24.
 */
public class TestRectangleDraw implements TouchEventHelper.OnToucheEventListener, MoveAndScaleTouchHelper.IMoveEvent, MoveAndScaleTouchHelper.IScaleEvent {
    //创建工具
    private MoveAndScaleTouchHelper mMoveAndScaleTouchHelper = null;
    //保存显示的View
    private View mDrawView = null;
    //画笔
    private Paint mPaint = null;
    //绘制时使用的数据
    private RectF mDrawRectf = null;
    //缩放时保存的缩放数据
    //此数据保存的是每一次缩放后的数据(屏幕不存在触摸时,才算缩放后,缩放时为滑动屏幕期间)
    private RectF mTempRectf = null;

    private TouchEventHelper mTouchHelper = null;

    public TestRectangleDraw(View drawView) {
        mMoveAndScaleTouchHelper = new MoveAndScaleTouchHelper();
        mMoveAndScaleTouchHelper.setMoveEvent(this);
        mMoveAndScaleTouchHelper.setScaleEvent(this);
        mDrawView = drawView;
        mTouchHelper = new TouchEventHelper(this);
        mDrawView.setOnTouchListener(mTouchHelper);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        //起始位置为 300,300
        //宽为200,长为300
        mDrawRectf = new RectF();
        mDrawRectf.left = 300;
        mDrawRectf.right = 500;
        mDrawRectf.top = 300;
        mDrawRectf.bottom = 600;

        //必须暂存初始化时使用的数据
        mTempRectf = new RectF(mDrawRectf);

        mMoveAndScaleTouchHelper.setIsShowLog(false);
        mTouchHelper.setIsShowLog(false, null);
    }

    public void rollback() {
        mMoveAndScaleTouchHelper.rollbackToLastOffset();
    }

    public void onDraw(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        //此处是实际的绘制界面+偏移量,偏移量切记不能保存到实际绘制的数据中!!!!
        //不可以使用 mDrawRectf.offset(x,y)
        canvas.drawRect(mDrawRectf.left + mMoveAndScaleTouchHelper.getDrawOffsetX(), mDrawRectf.top + mMoveAndScaleTouchHelper.getDrawOffsetY(),
                mDrawRectf.right + mMoveAndScaleTouchHelper.getDrawOffsetX(), mDrawRectf.bottom + mMoveAndScaleTouchHelper.getDrawOffsetY(),
                mPaint);
    }

    @Override
    public void onSingleTouchEventHandle(MotionEvent event, int extraMotionEvent) {
        //工具类默认处理的单点触摸事件
        mMoveAndScaleTouchHelper.singleTouchEvent(event, extraMotionEvent);
    }

    @Override
    public void onMultiTouchEventHandle(MotionEvent event, int extraMotionEvent) {
        //工具类默认处理的多点(实际只处理了两点事件)触摸事件
        mMoveAndScaleTouchHelper.multiTouchEvent(event, extraMotionEvent);
    }

    @Override
    public void onSingleClickByTime(MotionEvent event) {
        //基于时间的单击事件
        //按下与抬起时间不超过500ms
    }

    @Override
    public void onSingleClickByDistance(MotionEvent event) {
        //基于距离的单击事件
        //按下与抬起的距离不超过20像素(与时间无关，若按下不动几小时后再放开只要距离在范围内都可以触发)
    }

    @Override
    public void onDoubleClickByTime() {
        //基于时间的双击事件
        //单击事件基于clickByTime的两次单击
        //两次单击之间的时间不超过250ms
    }

    @Override
    public boolean isCanMovedOnX(float moveDistanceX, float newOffsetX) {
        return true;
    }

    @Override
    public boolean isCanMovedOnY(float moveDistacneY, float newOffsetY) {
        return true;
    }

    @Override
    public void onMove(int suggestEventAction) {
        mDrawView.postInvalidate();
    }

    @Override
    public void onMoveFail(int suggetEventAction) {

    }

    @Override
    public boolean isCanScale(float newScaleRate) {
        return true;
    }

    @Override
    public void setScaleRate(float newScaleRate, boolean isNeedStoreValue) {
        float newWidth = mTempRectf.width() * newScaleRate;
        float newHeight = mTempRectf.height() * newScaleRate;
        //计算中心位置
        float centerX = mTempRectf.centerX();
        float centerY = mTempRectf.centerY();
        //根据中心位置调整大小
        //此处确保了缩放时是按绘制物体中心为标准
        mDrawRectf.left = centerX - newWidth / 2;
        mDrawRectf.top = centerY - newHeight / 2;
        mDrawRectf.right = mDrawRectf.left + newWidth;
        mDrawRectf.bottom = mDrawRectf.top + newHeight;
        //此方式缩放中心为左上角
//        mDrawRectf.right=mDrawRectf.left+newWidth;
//        mDrawRectf.bottom=mDrawRectf.top+newHeight;
        if (isNeedStoreValue) {
            mTempRectf = new RectF(mDrawRectf);
        }
    }

    @Override
    public void onScale(int suggestEventAction) {
        mDrawView.postInvalidate();
    }

    @Override
    public void onScaleFail(int suggetEventAction) {

    }
}
