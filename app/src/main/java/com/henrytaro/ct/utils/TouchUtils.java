package com.henrytaro.ct.utils;

import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by CT on 16/3/24.
 * 触摸事件的辅助工具类,用于处理基本的拖动及缩放事件,提供简单的回调接口
 */
public class TouchUtils {

    private IScaleEvent mScaleEvent = null;
    private IMoveEvent mMoveEvent = null;
    //多点触控缩放按下坐标
    private float mScaleFirstDownX = 0f;
    private float mScaleFirstDownY = 0f;
    private float mScaleSecondDownX = 0f;
    private float mScaleSecondDownY = 0f;
    private float mScaleFirstUpX = 0f;
    private float mScaleFirstUpY = 0f;
    private float mScaleSecondUpX = 0f;
    private float mScaleSecondUpY = 0f;
    //上一次的缩放比例
    private float mLastScaleRate = 1f;

    //任何时候绘制需要的偏移量
    protected float mDrawOffsetY = 0f;
    protected float mDrawOffsetX = 0f;
    //上一次移动后保存的偏移量
    protected float mLastDrawOffsetX = 0f;
    protected float mLastDrawOffsetY = 0f;
    //移动过程中临时保存的移动前的偏移量
    protected float mTempDrawOffsetX = 0f;
    protected float mTempDrawOffsetY = 0f;
    //按下事件的坐标
    private float mDownX = 0f;
    private float mDownY = 0f;
    //抬起事件的坐标
    private float mUpX = 0f;
    private float mUpY = 0f;
    //是否打印消息
    private boolean mIsShowLog = true;

    public TouchUtils() {
    }

    public TouchUtils(IScaleEvent scaleEvent, IMoveEvent moveEvent) {
        this.mScaleEvent = scaleEvent;
        this.mMoveEvent = moveEvent;
    }

    /**
     * 获取上一次移动后的X轴偏移量,此值只会保存移动的上一次偏移量,若回滚过一次偏移量,此值与当前偏移量值相同
     *
     * @return
     */
    public float getLastOffsetX() {
        return this.mLastDrawOffsetX;
    }

    /**
     * 获取上一次移动后的Y轴偏移量,此值只会保存移动的上一次偏移量,若回滚过一次偏移量,此值与当前偏移量值相同
     *
     * @return
     */
    public float getLastOffset() {
        return this.mLastDrawOffsetY;
    }

    /**
     * 是否可回滚到上一次移动的偏移量
     *
     * @return
     */
    public boolean isCanRollBack() {
        if (mDrawOffsetX == mLastDrawOffsetX && mDrawOffsetY == mLastDrawOffsetY) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 回滚到上一次移动的偏移量,若回滚成功返回true,否则返回False
     *
     * @return
     */
    public boolean rollbackToLastOffset() {
        boolean isRollbackSuccess = isCanRollBack();
        if (isRollbackSuccess) {
            //将当前的移动偏移值替换为上一次的偏移量
            this.mDrawOffsetX = mLastDrawOffsetX;
            this.mDrawOffsetY = mLastDrawOffsetY;
            this.mTempDrawOffsetX = mLastDrawOffsetX;
            this.mTempDrawOffsetY = mLastDrawOffsetY;
            //通过移动事件进行移动
            if (mMoveEvent != null) {
                mMoveEvent.onMove(Integer.MIN_VALUE);
            }
        }
        return isRollbackSuccess;
    }

    /**
     * 获取X轴偏移量
     *
     * @return
     */
    public float getDrawOffsetX() {
        return this.mDrawOffsetX;
    }

    /**
     * 获取Y轴偏移量
     *
     * @return
     */
    public float getDrawOffsetY() {
        return this.mDrawOffsetY;
    }

    /**
     * 通过此方法可以设置初始值
     *
     * @param offsetX
     */
    public void setOffsetX(float offsetX) {
        this.mDrawOffsetX = offsetX;
    }

    /**
     * 通过此方法可以设置初始值
     *
     * @param offsetY
     */
    public void setOffsetY(float offsetY) {
        this.mDrawOffsetY = offsetY;
    }

    /**
     * 设置缩放处理事件
     *
     * @param event
     */
    public void setScaleEvent(IScaleEvent event) {
        this.mScaleEvent = event;
    }

    /**
     * 设置移动处理事件
     *
     * @param event
     */
    public void setMoveEvent(IMoveEvent event) {
        this.mMoveEvent = event;
    }

    /**
     * 根据坐标值计算需要缩放的比例,<font color="#ff9900"><b>返回值为移动距离与按下距离的商,move/down</b></font>
     *
     * @param firstDownX  多点触摸按下的pointer_1_x
     * @param firstDownY  多点触摸按下的pointer_1_y
     * @param secondDownX 多点触摸按下的pointer_2_x
     * @param secondDownY 多点触摸按下的pointer_2_y
     * @param firstUpX    多点触摸抬起或移动的pointer_1_x
     * @param firstUpY    多点触摸抬起或移动的pointer_1_y
     * @param secondUpX   多点触摸抬起或移动的pointer_2_x
     * @param secondUpY   多点触摸抬起或移动的pointer_2_y
     * @return
     */
    public static float getScaleRate(float firstDownX, float firstDownY, float secondDownX, float secondDownY,
                                     float firstUpX, float firstUpY, float secondUpX, float secondUpY) {
        //计算平方和
        double downDistance = Math.pow(Math.abs((firstDownX - secondDownX)), 2) + Math.pow(Math.abs(firstDownY - secondDownY), 2);
        double upDistance = Math.pow(Math.abs((firstUpX - secondUpX)), 2) + Math.pow(Math.abs(firstUpY - secondUpY), 2);
        //计算比例
        double newRate = Math.sqrt(upDistance) / Math.sqrt(downDistance);
        //计算与上一个比例的差
        //差值超过阀值则使用该比例,否则返回原比例
        if (newRate > 0.02 && newRate < 10) {
            //保存当前的缩放比为上一次的缩放比
            return (float) newRate;
        }
        return 1;
    }

    /**
     * 单点触摸事件处理
     *
     * @param event            单点触摸事件
     * @param extraMotionEvent 建议处理的额外事件,一般值为{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP},{@link MotionEvent#ACTION_CANCEL}
     *                         <p>存在此参数是因为可能用户进行单点触摸并移动之后,会再进行多点触摸(此时并没有松开触摸),在这种情况下是无法分辨需要处理的是单点触摸事件还是多点触摸事件.
     *                         <font color="#ff9900"><b>此时会传递此参数值为单点触摸的{@link MotionEvent#ACTION_UP},建议按抬起事件处理并结束事件</b></font></p>
     */
    public void singleTouchEvent(MotionEvent event, int extraMotionEvent) {
        //单点触控
        int action = event.getAction();
        //用于记录此处事件中新界面与旧界面之间的相对移动距离
        float moveDistanceX = 0f;
        float moveDistanceY = 0f;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //特别处理额外的事件
                //此处处理的事件是在单击事件并移动中用户进行了多点触摸
                //此时尝试结束进行移动界面,并将当前移动的结果固定下来作为新的界面(效果与直接抬起结束触摸相同)
                //并不作任何操作(因为在单击后再进行多点触摸无法分辨需要进行处理的事件是什么)
                if (extraMotionEvent == MotionEvent.ACTION_UP) {
                    showMsg("move 处理为 up 事件");
                    //已经移动过且建议处理为up事件时
                    //处理为up事件
                    event.setAction(MotionEvent.ACTION_UP);
                    singleTouchEvent(event, Integer.MIN_VALUE);
                    return;
                }

                showMsg("move 拖动重绘界面");
                mUpX = event.getX();
                mUpY = event.getY();
                moveDistanceX = mUpX - mDownX;
                moveDistanceY = mUpY - mDownY;
                //此次移动加数据量达到足够的距离触发移动事件
                invalidateInSinglePoint(moveDistanceX, moveDistanceY, MotionEvent.ACTION_MOVE);
                mUpX = 0f;
                mUpY = 0f;
                break;
            case MotionEvent.ACTION_UP:

                mUpX = event.getX();
                mUpY = event.getY();
                moveDistanceX = mUpX - mDownX;
                moveDistanceY = mUpY - mDownY;

                invalidateInSinglePoint(moveDistanceX, moveDistanceY, MotionEvent.ACTION_UP);
                //移动操作完把数据还原初始状态,以防出现不必要的错误
                mDownX = 0f;
                mDownY = 0f;
                mUpX = 0f;
                mUpY = 0f;
                break;
        }
    }


    /**
     * 多点触摸事件处理(两点触摸,暂没有做其它任何多点触摸)
     *
     * @param event            多点触摸事件
     * @param extraMotionEvent 建议处理的额外事件,一般值为{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP},{@link MotionEvent#ACTION_CANCEL}
     */
    public void multiTouchEvent(MotionEvent event, int extraMotionEvent) {
        //使用try是为了防止获取系统的触摸点坐标失败
        //该部分可能为系统的问题
        try {
            int action = event.getAction();
            float newScaleRate = 0f;
            switch (action & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_POINTER_DOWN:
                    mScaleFirstDownX = event.getX(0);
                    mScaleFirstDownY = event.getY(0);
                    mScaleSecondDownX = event.getX(1);
                    mScaleSecondDownY = event.getY(1);

                    break;
                case MotionEvent.ACTION_MOVE:
                    mScaleFirstUpX = event.getX(0);
                    mScaleFirstUpY = event.getY(0);
                    mScaleSecondUpX = event.getX(1);
                    mScaleSecondUpY = event.getY(1);

                    newScaleRate = TouchUtils.getScaleRate(mScaleFirstDownX, mScaleFirstDownY, mScaleSecondDownX, mScaleSecondDownY,
                            mScaleFirstUpX, mScaleFirstUpY, mScaleSecondUpX, mScaleSecondUpY);
                    invalidateInMultiPoint(newScaleRate, MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mScaleFirstUpX = event.getX(0);
                    mScaleFirstUpY = event.getY(0);
                    mScaleSecondUpX = event.getX(1);
                    mScaleSecondUpY = event.getY(1);

                    newScaleRate = TouchUtils.getScaleRate(mScaleFirstDownX, mScaleFirstDownY, mScaleSecondDownX, mScaleSecondDownY,
                            mScaleFirstUpX, mScaleFirstUpY, mScaleSecondUpX, mScaleSecondUpY);
                    invalidateInMultiPoint(newScaleRate, MotionEvent.ACTION_POINTER_UP);

                    mScaleFirstDownX = 0;
                    mScaleFirstDownY = 0;
                    mScaleSecondDownX = 0;
                    mScaleSecondDownY = 0;
                    mScaleFirstUpX = 0;
                    mScaleFirstUpY = 0;
                    mScaleSecondUpX = 0;
                    mScaleSecondUpY = 0;
                    break;
            }
        } catch (IllegalArgumentException e) {
        }
    }


    /**
     * 多点触摸的重绘,是否重绘由实际缩放的比例决定
     *
     * @param newScaleRate     新的缩放比例,该比例可能为1(通常情况下比例为1不缩放,没有意义)
     * @param invalidateAction 重绘的动作标志
     */
    private void invalidateInMultiPoint(float newScaleRate, int invalidateAction) {
        if (mScaleEvent == null) {
            return;
        }
        //若缩放比为1且不为缩放的最终事件时,不进行重绘,防止反复多次地重绘..
        //如果是最后一次(up事件),必定重绘并记录缩放比
        boolean isCanScale = false;
        boolean isTrueSetValue = invalidateAction == MotionEvent.ACTION_POINTER_UP;
        if (newScaleRate == 1 && !isTrueSetValue) {
            return;
        }

        //回调缩放事件接口,是否允许缩放
        if (mScaleEvent.isCanScale(newScaleRate)) {
            //进行缩放,更新最后一次缩放比例为当前值
            mLastScaleRate = newScaleRate;
            isCanScale = true;
        } else if (isTrueSetValue) {
            //若缩放比不合法且当前缩放为最后一次缩放(up事件),则将上一次的缩放比作为此次的缩放比,用于记录数据
            //此处不作此操作会导致在缩放的时候达到最大值后放手,再次缩放会在最开始的时候复用上一次缩放的结果(因为没有保存当前缩放值,有闪屏的效果...)
            newScaleRate = mLastScaleRate;
            //将最后一次的缩放比设为1(缩放事件已经终止,所以比例使用1)
            mLastScaleRate = 1;
            //最后一次必须缩放并保存值
            isCanScale = true;
        }
        //更新缩放数据
        mScaleEvent.setScaleRate(newScaleRate, isTrueSetValue);
        if (!isCanScale) {
            //正常情况下UP事件必定会完成缩放保存工作,此处为保险措施
            //若为抬起缩放事件,则不管是否已经通知过,必定再通知一次
            if (invalidateAction == MotionEvent.ACTION_UP) {
                mScaleEvent.onScaleFail(invalidateAction);
            }
            return;
        }
        //缩放回调
        mScaleEvent.onScale(invalidateAction);
    }


    /**
     * 根据移动的距离计算是否重新绘制
     *
     * @param moveDistanceX    X轴方向的移动距离(可负值)
     * @param moveDistanceY    Y轴方向的移动距离(可负值)
     * @param invalidateAction 重绘的行为标志
     */
    private boolean invalidateInSinglePoint(float moveDistanceX, float moveDistanceY, int invalidateAction) {
        if (mMoveEvent == null) {
            return false;
        }
        //此处做大于5的判断是为了避免在检测单击事件时
        //有可能会有很微小的变动,避免这种情况下依然会造成移动的效果
        if (Math.abs(moveDistanceX) > 5 || Math.abs(moveDistanceY) > 5 || invalidateAction == MotionEvent.ACTION_UP) {
            //新的偏移量
            float newDrawOffsetX = mTempDrawOffsetX + moveDistanceX;
            float newDrawOffsetY = mTempDrawOffsetY + moveDistanceY;

            //当前绘制的最左边边界坐标大于0(即边界已经显示在屏幕上时),且移动方向为向右移动
            if (!mMoveEvent.isCanMovedOnX(moveDistanceX, newDrawOffsetX)) {
                //保持原来的偏移量不变
                newDrawOffsetX = mDrawOffsetX;
            }
            //当前绘制的顶端坐标大于0且移动方向为向下移动
            if (!mMoveEvent.isCanMovedOnY(moveDistanceY, newDrawOffsetY)) {
                //保持原来的Y轴偏移量
                newDrawOffsetY = mDrawOffsetY;
            }

            //其它情况正常移动重绘
            //当距离确实有效地改变了再进行重绘制,否则原界面不变,减少重绘的次数
            if (newDrawOffsetX != mDrawOffsetX || newDrawOffsetY != mDrawOffsetY || invalidateAction == MotionEvent.ACTION_UP) {
                mDrawOffsetX = newDrawOffsetX;
                mDrawOffsetY = newDrawOffsetY;
                //抬起事件时
                if (invalidateAction == MotionEvent.ACTION_UP) {
                    //保存上一次的偏移量
                    mLastDrawOffsetX = mTempDrawOffsetX;
                    mLastDrawOffsetY = mTempDrawOffsetY;
                    //将此次的新偏移量保存为临时数据后续拖动时可使用
                    mTempDrawOffsetX = mDrawOffsetX;
                    mTempDrawOffsetY = mDrawOffsetY;
                }
                mMoveEvent.onMove(invalidateAction);
                return true;
            } else {
                mMoveEvent.onMoveFail(invalidateAction);
            }
        }
        return false;
    }

    /**
     * 设置是否打印消息
     *
     * @param isShowLog
     */
    public void setIsShowLog(boolean isShowLog) {
        mIsShowLog = isShowLog;
    }

    /**
     * 打印消息
     *
     * @param msg
     */
    public void showMsg(String msg) {
        if (mIsShowLog) {
            Log.i("touchUtils", msg + "");
        }
    }

    /**
     * 缩放事件处理接口
     */
    public interface IScaleEvent {

        /**
         * 是否允许进行缩放
         *
         * @param newScaleRate 新的缩放比例值,请注意该值为当前值与缩放前的值的比例,即<font color="#ff9900"><b>在move期间,
         *                     此值都是相对于move事件之前的down的坐标计算出来的,并不是上一次move结果的比例</b></font>,建议
         *                     修改缩放值或存储缩放值在move事件中不要处理,在up事件中处理会比较好,防止每一次都重新存储数据,可能
         *                     造成数据的大量读写而失去准确性
         * @return
         */
        public abstract boolean isCanScale(float newScaleRate);

        /**
         * 设置缩放的比例(存储值),<font color="#ff9900"><b>当up事件中,且当前不允许缩放,此值将会返回最后一次在move中允许缩放的比例值,
         * 此方式保证了在处理up事件中,可以将最后一次缩放的比例显示出来,而不至于结束up事件不存储数据导致界面回到缩放前或者之前某个状态</b></font>
         *
         * @param newScaleRate     新的缩放比例
         * @param isNeedStoreValue 是否需要存储比例,此值仅为建议值;当move事件中,此值为false,当true事件中,此值为true;不管当前
         *                         up事件中是否允许缩放,此值都为true;
         */
        public abstract void setScaleRate(float newScaleRate, boolean isNeedStoreValue);

        /**
         * 缩放事件
         *
         * @param suggestEventAction 建议处理的事件,值可能为{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP}
         */
        public abstract void onScale(int suggestEventAction);

        /**
         * 无法进行缩放事件,可能某些条件不满足,如不允许缩放等
         *
         * @param suggetEventAction 建议处理的事件,值可能为{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP}
         */
        public abstract void onScaleFail(int suggetEventAction);
    }


    /**
     * 移动事件处理接口
     */
    public interface IMoveEvent {

        /**
         * 是否可以实现X轴的移动
         *
         * @param moveDistanceX 当次X轴的移动距离(可正可负)
         * @param newOffsetX    新的X轴偏移量(若允许移动的情况下,此值实际上即为上一次偏移量加上当次的移动距离)
         * @return
         */
        public abstract boolean isCanMovedOnX(float moveDistanceX, float newOffsetX);

        /**
         * 是否可以实现Y轴的移动
         *
         * @param moveDistacneY 当次Y轴的移动距离(可正可负)
         * @param newOffsetY    新的Y轴偏移量(若允许移动的情况下,此值实际上即为上一次偏移量加上当次的移动距离)
         * @return
         */
        public abstract boolean isCanMovedOnY(float moveDistacneY, float newOffsetY);

        /**
         * 移动事件
         *
         * @param suggestEventAction 建议处理的事件,值可能为{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP}
         * @return
         */
        public abstract void onMove(int suggestEventAction);

        /**
         * 无法进行移动事件
         *
         * @param suggetEventAction 建议处理的事件,值可能为{@link MotionEvent#ACTION_MOVE},{@link MotionEvent#ACTION_UP}
         */
        public abstract void onMoveFail(int suggetEventAction);
    }
}
