package com.taro.touchevent.ui;/**
 * Created by xuhaolin on 15/9/25.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by CT on 15/9/25.
 * 此View演示了AbsTouchEventHandle与TouchUtils怎么用
 */
public class TestView extends View {
    public static int RECTANGLE_DRAW = 0;
    public static int CIRCLE_DRAW = 1;
    private int mWhickDraw = RECTANGLE_DRAW;

    //创建绘制圆形示例界面专用的绘制类
    TestCircleDraw mTestCircleDraw = new TestCircleDraw(this, getContext());
    //创建绘制方形示例界面专用的绘制类
    TestRectangleDraw mTestRectfDraw = new TestRectangleDraw(this);

    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void rollback() {
        if(mWhickDraw==RECTANGLE_DRAW) {
            mTestRectfDraw.rollback();
        }else if(mWhickDraw==CIRCLE_DRAW){
            mTestCircleDraw.rollback();
        }
    }

    public void setWhickDraw(int which) {
        mWhickDraw = which;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //实际上的绘制工作全部都交给了绘制专用类
        //在绘制很复杂的界面时,这样可以很清楚地分开
        //绘制与视图,因为视图本身可能还要处理其它的事件(比如来自绘制事件中回调的事件等)
        //而且View本身的方法就够多了,还加一很多绘制方法,看起来也不容易理解
        if (mWhickDraw == RECTANGLE_DRAW) {
            mTestRectfDraw.onDraw(canvas);
        } else if (mWhickDraw == CIRCLE_DRAW) {
            mTestCircleDraw.onDraw(canvas);
        }
    }
}
