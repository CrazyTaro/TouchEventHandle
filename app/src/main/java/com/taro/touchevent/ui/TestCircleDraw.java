package com.taro.touchevent.ui;/**
 * Created by xuhaolin on 15/9/25.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.taro.touchevent.utils.MoveAndScaleTouchHelper;
import com.taro.touchevent.utils.TouchEventHelper;


/**
 * Created by taro on 15/9/25.
 * 绘制工具类
 */
public class TestCircleDraw implements TouchEventHelper.OnToucheEventListener, MoveAndScaleTouchHelper.IScaleEvent, MoveAndScaleTouchHelper.IMoveEvent {
    //创建工具类
    MoveAndScaleTouchHelper mTouch = new MoveAndScaleTouchHelper();
    View mDrawView = null;
    Context mContext = null;
    Paint mPaint = new Paint();
    //用于绘制的半径(相当于实际绘制界面所需要的数据)
    float mRadius = 200;
    //暂时性保存的半径(同理在绘制时也需要一个暂时性存放的数据)
    float mTempRadius = mRadius;

    private TouchEventHelper mTouchHelper = null;

    //针对构造函数,可有不同的需求,在此例中,其实并不需要context
    //此参数是可有可无的,有时自定义绘制界面需要加载一些资源什么的需要用到context,
    //这个时候就有用了,这个看需要
    public TestCircleDraw(View drawView, Context context) {
        this.mDrawView = drawView;
        this.mContext = context;
        //设置工具类的监听事件
        mTouch.setMoveEvent(this);
        mTouch.setScaleEvent(this);
        //绑定view与触摸事件
        mTouchHelper = new TouchEventHelper(this);
        this.mDrawView.setOnTouchListener(mTouchHelper);

        mTouch.setIsShowLog(false);
        mTouchHelper.setIsShowLog(false, null);
    }

    public void onDraw(Canvas canvas) {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        //模拟绘制的界面
        canvas.drawCircle(mTouch.getDrawOffsetX() + 300, mTouch.getDrawOffsetY() + 300, mRadius, mPaint);
    }

    public void rollback() {
        mTouch.rollbackToLastOffset();
    }

    @Override
    public void onSingleTouchEventHandle(MotionEvent event, int extraMotionEvent) {
        //工具类默认处理的单点触摸事件
        mTouch.singleTouchEvent(event, extraMotionEvent);
    }

    @Override
    public void onMultiTouchEventHandle(MotionEvent event, int extraMotionEvent) {
        //工具类默认处理的多点(实际只处理了两点事件)触摸事件
        mTouch.multiTouchEvent(event, extraMotionEvent);
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
        //更新当前的数据
        //newScaleRate缩放比例一直是相对于按下时的界面的相对比例,所以在移动过程中
        //每一次都是要与按下时的界面进行比例缩放,而不是针对上一次的结果
        //使用这种方式一方面在缩放时的思路处理是比较清晰的
        //另一方面是缩放的比例不会数据很小(若相对于上一次,每一次move移动几个像素,
        //这种情况下缩放的比例相对上一次肯定是0.0XXXX,数据量一小很容易出现一些不必要的问题)
        mRadius = mTempRadius * newScaleRate;
        //当返回的标志为true时,提醒为已经到了up事件
        //此时应该把最后一次缩放的比例当做最终的数据保存下来
        if (isNeedStoreValue) {
            mTempRadius = mRadius;
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
