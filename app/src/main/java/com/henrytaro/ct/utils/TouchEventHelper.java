package com.henrytaro.ct.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by CT in 2015-08-14
 * <p>抽像类,处理触摸事件,区分单击及多点触摸事件</p>
 * <p>此类中使用到handler,请确保使用在UI线程或者是自定义looper的线程中(一般也没有人会把触摸事件放在非UI线程吧 =_=)</p>
 */
public class TouchEventHelper implements View.OnTouchListener {
    /**
     * 距离双击事件
     */
    public static final int EVENT_DOUBLE_CLICK_BY_TIME = 0;
    public static final int EVENT_SINGLE_CLICK_BY_TIME = -1;
    public static final int EVENT_SINGLE_CLICK_BY_DISTANCE = -2;


    /**
     * 额外分配的触摸事件,用于建议优先处理的触摸事件
     */
    public static final int MOTION_EVENT_NOTHING = Integer.MIN_VALUE;
    /**
     * 处理时间单击事件
     */
    private static final int CONSUME_LAST_SINGLE_CLICK_EVENT = -1;
    /**
     * 处理单点触摸下的事件
     */
    private static final int HANDLE_SINGLE_DOWN = 2;
    private static String TAG = "touch_event";

    private OnToucheEventListener mTouchListener = null;
    //已经触发单击事件的情况下,是否触发单点触摸事件
    private boolean mIsTriggerSingleTouchEvent = true;
    private boolean mIsShowLog = false;
    private int mMultiTouchCount = 0;
    //是否开始触发本次时间单击事件(整个触摸事件)
    private boolean mIsFireTimeClickEvent = false;
    //是否开始触发本次距离单击事件
    private boolean mIsFireDistanceClickEvent = false;
    //是否完成上一次单击(一次)
    private boolean mIsFireLastClickEvent = false;
    //多点触摸按下
    private boolean mIsMultiDown = false;
    //是否单点触摸按下
    private boolean mIsSingleDown = false;
    //是否进入单击移动事件
    private boolean mIsSingleMove = false;
    //是否进入移动事件
    private boolean mIsInMotionMove = false;
    //单次单击事件中(针对距离单击),触摸点是否产生超过单击的允许范围的移动事件
    private boolean mIsClickDistanceMove = false;
    //时间单击事件是否可用
    private boolean mIsClickTimeEventEnable = true;
    //距离单击事件是否可用
    private boolean mIsClickDistanceEventEnable = true;
    //单击事件的可持续最长时间间隔(down与up事件之间的间隔)
    private int SINGLE_CLICK_INTERVAL = 250;
    //双击事件可可持续最长时间间隔(两次单击事件之间的间隔)
    private int DOUBLE_CLICK_INTERVAL = 350;
    //距离单击事件最大的允许偏移量大小
    private int SINGLE_CLICK_OFFSET_DISTANCE = 10;

    private float mDownX = 0f;
    private float mDownY = 0f;
    private float mUpX = 0f;
    private float mUpY = 0f;


    public TouchEventHelper() {
    }

    /**
     * 触摸事件分析处理类
     *
     * @param listener 触摸事件处理回调事件,包含单击/双击/触摸事件回调
     */
    public TouchEventHelper(OnToucheEventListener listener) {
        mTouchListener = listener;
    }

    /**
     * 设置触摸事件回调处理事件
     *
     * @param listnener
     */
    public void setOnToucheEventListener(OnToucheEventListener listnener) {
        mTouchListener = listnener;
    }

    /**
     * 获取触摸事件的action类型,单点触摸类型为{@code MotionEvent.ACTION_XXX},多点触摸类型为{@code Motion.ACTION_POINTER_XXX}
     *
     * @param event
     * @return
     */
    public static int getTouchMotionEventAction(MotionEvent event) {
        return event.getAction() & MotionEvent.ACTION_MASK;
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //取消完成一次单击事件的标识(用于识别双击事件)
                case CONSUME_LAST_SINGLE_CLICK_EVENT:
                    mIsFireLastClickEvent = false;
                    break;
                //取消单点触摸的有效时间(用于识别基于时间的单击事件)
                case HANDLE_SINGLE_DOWN:
                    mIsSingleDown = false;
                    break;
            }
        }
    };

    /**
     * 检测单击事件的触发,触发单击事件返回 true,否则返回 false
     *
     * @param event     触摸事件
     * @param fireEvent 需要检测的事件,{@link #EVENT_SINGLE_CLICK_BY_TIME}基于时间的单击事件;
     *                  {@link #EVENT_SINGLE_CLICK_BY_DISTANCE}基于距离的单击事件
     * @return
     */
    private boolean isFireSingleClickEvent(MotionEvent event, int fireEvent) {
        //不触发多点触摸事件的情况下才进行单击事件的判断处理
        if (!mIsMultiDown) {
            if (fireEvent == EVENT_SINGLE_CLICK_BY_TIME) {
                //根据时间处理单击事件
                //触摸点down之后500ms内触摸点抬起则认为是一次单击事件
                //两次单击事件之间的时间间隔在允许间隔内即为一次双击事件
                //是否在单点触摸按下时间间隔内(该变量在按下后指定间隔时间内重置,此处是基于时间的单击事件)
                if (mIsSingleDown) {
                    //触发单击事件
                    return true;
                }
            } else if (fireEvent == EVENT_SINGLE_CLICK_BY_DISTANCE) {
                //移动距离单击处理事件
                //触摸点down事件的坐标与up事件的坐标距离不超过10像素时,认为一次单击事件(与时间无关)
                //两次单击事件之间的时间间隔在500ms内则认为是一次双击事件
                mUpX = event.getX();
                mUpY = event.getY();
                float moveDistanceX = mUpX - mDownX;
                float moveDistanceY = mUpY - mDownY;
                //根据触摸点up与down事件的坐标差判断是否为单击事件(不由时间决定)
                if (Math.abs(moveDistanceX) < SINGLE_CLICK_OFFSET_DISTANCE
                        && Math.abs(moveDistanceY) < SINGLE_CLICK_OFFSET_DISTANCE) {
                    //触摸单击事件
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 处理一次单击事件,此过程不负责单击事件的检测,只负责执行
     *
     * @param event
     * @param handleEvent
     */
    private void handleSingleClickEvent(MotionEvent event, int handleEvent) {
        if (handleEvent == EVENT_SINGLE_CLICK_BY_TIME) {
            //完成一次时间单击事件
            showMsg("单击事件 single");
            mTouchListener.onSingleClickByTime(event);
            //记录本次触摸了单击事件
            mIsFireTimeClickEvent = true;
            //处理事件为距离单击事件且在移动过程中不可超过允许范围
        } else if (handleEvent == EVENT_SINGLE_CLICK_BY_DISTANCE && !mIsClickDistanceMove) {
            //完成一次距离单击事件
            showMsg("单击事件(距离) single");
            mTouchListener.onSingleClickByDistance(event);
            //记录本次触摸了单击事件
            mIsFireDistanceClickEvent = true;
        }
    }

    /**
     * 处理一次点击事件,完整的一个过程,包括检测/执行/反馈;处理事件包括单击事件/双击事件</br>
     * 若触发双击事件,返回true,其它情况返回false
     *
     * @param isDistanceEnable 基于距离的单击事件是否可用
     * @param isTimeEnable     基于时间的单击事件是否可用
     * @param fireEvent        需要触发的事件,一次只能处理一个事件;{@link #EVENT_SINGLE_CLICK_BY_TIME}基于时间的单击事件
     *                         {@link #EVENT_SINGLE_CLICK_BY_DISTANCE}基于距离的单击事件
     * @param event            触摸事件
     * @return 若触发双击事件, 返回true, 其它情况返回false
     */
    private boolean handleClickEvent(boolean isDistanceEnable, boolean isTimeEnable, int fireEvent, MotionEvent event) {
        //检测指定事件是否触发
        if (this.isFireSingleClickEvent(event, fireEvent)) {
            //若事件触发,检测是否已经执行过一次单击事件且在有效时间间隔内
            //并且必须不在是本次触摸事件中触发的单击事件
            //mIsFireTimeClickEvent与mIsFireDistanceCLickEvent用于检测本次是否触发单击事件
            //mIsFireLastClickEvent用于检测上一次单击事件是否在有效时间范围内
            if ((mIsFireTimeClickEvent || mIsFireDistanceClickEvent) && mIsFireLastClickEvent) {
                //触摸双击事件
                showMsg("双击事件(时间) double");
                mTouchListener.onDoubleClickByTime();
                //取消双击事件的标识
                this.cancelDoubleClickEvent(EVENT_DOUBLE_CLICK_BY_TIME);
                //返回已触发双击事件
                return true;
            } else {
                //未执行过一次单击事件
                //触发对应的单击事件
                if (fireEvent == EVENT_SINGLE_CLICK_BY_TIME && isTimeEnable) {
                    this.handleSingleClickEvent(event, fireEvent);
                } else if (fireEvent == EVENT_SINGLE_CLICK_BY_DISTANCE && isDistanceEnable) {
                    this.handleSingleClickEvent(event, fireEvent);
                }
            }
        }
        return false;
    }

    /**
     * 结束触摸事件,重置所有应该重置的变量
     */
    private void finishTouchEvent() {
        //取消移动状态的记录
        mIsInMotionMove = false;
        //多点单击的标志必须在此处才可以被重置
        //因为多点单击的抬起事件优先处理于单击的抬起事件
        //如果在多点单击的抬起事件时重置该变量则会导致上面的判断百分百是成立的
        mIsMultiDown = false;
        mIsSingleDown = false;
        mIsSingleMove = false;
        mIsFireTimeClickEvent = false;
        mIsClickDistanceMove = false;
        mMultiTouchCount = 0;
        //记录此触摸事件中是否产生了单击事件(用于后续的双击事件判断)
        mIsFireLastClickEvent = mIsFireTimeClickEvent || mIsFireDistanceClickEvent;
        //重置本次单击事件的标识
        mIsFireTimeClickEvent = false;
        mIsFireDistanceClickEvent = false;
        //发送延迟消费记录的单击事件标识
        mHandle.sendEmptyMessageDelayed(CONSUME_LAST_SINGLE_CLICK_EVENT, DOUBLE_CLICK_INTERVAL);
        //mIsFireLastClickEvent 此变量不可以重置,这是保存已经完成一次单击事件的标识,用于后续识别双击事件
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //进入单点单击处理
                showMsg("单点触摸 down ");
                mIsSingleDown = true;
                mHandle.sendEmptyMessageDelayed(HANDLE_SINGLE_DOWN, SINGLE_CLICK_INTERVAL);

                mDownX = event.getX();
                mDownY = event.getY();
                mTouchListener.onSingleTouchEventHandle(event, MOTION_EVENT_NOTHING);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //开始多点单击事件
                showMsg("多点触控 down");
                mIsMultiDown = true;
                mMultiTouchCount += 1;
                mTouchListener.onMultiTouchEventHandle(event, MOTION_EVENT_NOTHING);
                break;
            case MotionEvent.ACTION_UP:
                showMsg("单点触摸 up");
                //任何一种事件中,只要触发了双击事件,则结束事件
                //优先检测基于距离的单击事件
                if (this.handleClickEvent(mIsClickDistanceEventEnable, mIsClickTimeEventEnable, EVENT_SINGLE_CLICK_BY_DISTANCE, event)) {
                    this.finishTouchEvent();
                    break;
                }
                //检测基于时间的单击事件
                if (this.handleClickEvent(mIsClickDistanceEventEnable, mIsClickTimeEventEnable, EVENT_SINGLE_CLICK_BY_TIME, event)) {
                    this.finishTouchEvent();
                    break;
                }

                //允许触发单点触摸事件
                if (mIsTriggerSingleTouchEvent) {
                    //在处理单击事件up中,任何时候只要在结束up之前产生任何的多点触控,都不将此次的事件处理为单击up
                    //因为这时候单点触摸事件已经不完整了,混合了其它的事件
                    //而且多点触摸可能导致原本的单点触摸事件的坐标数据等获取不正常,所以不再处理单点触摸事件
                    if (!mIsMultiDown && mMultiTouchCount <= 0) {
                        //此处分为两种情况
                        //一种是未进行任何多点触摸状态的,那么必定为单点触摸,事件必须响应
                        //在事件响应处两个判断条件是:1.用户快速单击,不产生move事件;此时 isInMotionMove=false
                        if (!mIsInMotionMove
                                //2. 用户慢速单击, 产生了move事件但仍没有造成多点触摸事件;
                                //此时 isInMotionMove=true 且 isSingleMove=true
                                || (mIsInMotionMove && mIsSingleMove)) {
                            showMsg("单击 up");
                            mTouchListener.onSingleTouchEventHandle(event, MOTION_EVENT_NOTHING);
                        } else {
                            //一种是进行了多点触摸,且在多点触摸之后保持着单点触摸的状态,此时以多点触摸按下的时刻处理掉单点触摸事件(即在move中已经按up处理掉事件了)
                            //则在完成所有事件之后的up中将不再处理该事件,即下面的"不处理"
                            showMsg("单击 up 不处理");
                        }
                    }
                }
                //处理触摸结束事件,重置变量
                this.finishTouchEvent();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //当确认进入多点单击状态,则执行多点单击抬起事件
                if (mMultiTouchCount > 0) {
                    showMsg("多点触控 up");
                    mTouchListener.onMultiTouchEventHandle(event, MOTION_EVENT_NOTHING);
                }
                mMultiTouchCount -= 1;
                //此处不重置mIsMultiDown变量是因为后面检测单击事件的up与多点触控的up需要
                //而且此处不重置并不会对其它的部分造成影响
                break;
            case MotionEvent.ACTION_MOVE:
                //进入移动状态
                mIsInMotionMove = true;
                //当前不是多点单击状态,则进行移动操作
                //若触发了多点触摸事件,则结束单点移动事件,进入多点触摸移动事件

                //结束单点移动操作后在触摸事件结束之前都不会再执行单点移动操作
                //这种情况是为了避免有可能有用户单击移动之后再进行多点触控,这种情况无法处理为用户需要移动还是需要缩放
                //而且引起的坐标变化可能导致一些错乱
                if (!mIsMultiDown && mMultiTouchCount <= 0) {
                    showMsg("单点触摸 move");
                    mTouchListener.onSingleTouchEventHandle(event, MOTION_EVENT_NOTHING);
                    mIsSingleMove = true;
                    //多点触摸事件触发了,进入多点触摸移动事件
                } else if (mIsMultiDown && mMultiTouchCount > 0) {
                    //若此前是单点触摸的移动状态时
                    if (mIsSingleMove) {
                        //按单点触摸的结束状态处理并不再响应单点触摸移动状态
                        showMsg("单点触摸 move 结束");
                        mTouchListener.onSingleTouchEventHandle(event, MotionEvent.ACTION_UP);
                        mIsSingleMove = false;
                    }
                    //正常直接多点移动操作
                    showMsg("多点触控 move");
                    mTouchListener.onMultiTouchEventHandle(event, MOTION_EVENT_NOTHING);
                }


                //当可能发生一次距离单击事件时,需要检测是否产生了超过偏移量的移动距离
                //此处不在up事件中判断是因为:
                //存在一种可能是(由于距离单击事件有足够长的时间),在move的时候移动距离超过偏移量
                //但之后又移动到单击位置的坐标,在up事件中move所引起的坐标变化是不可见的
                //所以于对up事件,down的坐标与up的坐标偏移量是允许范围内,会处理为一次距离单击事件
                //但实际上这是一次移动事件
                if (!mIsClickDistanceMove) {
                    float moveDistanceX = event.getX() - mUpX;
                    float moveDistanceY = event.getY() - mUpY;
                    int offsetDistance = SINGLE_CLICK_OFFSET_DISTANCE + 20;
                    if (Math.abs(moveDistanceX) > offsetDistance
                            || Math.abs(moveDistanceY) > offsetDistance) {
                        //一旦取消了距离单击事件有效性的标识,则不需要再次检测了
                        mIsClickDistanceMove = true;
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置单击有效的时间间隔(down与up事件的最长允许时间间隔),默认250ms
     *
     * @param interval
     */
    public void setSingleClickInterval(int interval) {
        this.SINGLE_CLICK_INTERVAL = interval;
    }

    /**
     * 设置双击有效时间间隔(两次单击事件的最长允许时间间隔),默认350ms
     *
     * @param interval
     */
    public void setDoubleClickInterval(int interval) {
        this.DOUBLE_CLICK_INTERVAL = interval;
    }

    /**
     * 设置单击允许的最大偏移量范围(坐标偏移像素值),默认10像素
     *
     * @param offsetDistance
     */
    public void setSingleClickOffsetDistance(int offsetDistance) {
        this.SINGLE_CLICK_OFFSET_DISTANCE = offsetDistance;
    }

    /**
     * 基于时间的单击事件是否可响应,若为true则事件触发时回调响应;若为false事件触发时不回调
     *
     * @param isEnabled
     */
    public void setIsEnableSingleClickByTime(boolean isEnabled) {
        this.mIsClickTimeEventEnable = isEnabled;
    }

    /**
     * 基于距离的单击事件是否可响应,若为true则事件触发时回调响应;若为false事件触发时不回调
     *
     * @param isEnabled
     */
    public void setIsEnableSingleClickByDistance(boolean isEnabled) {
        this.mIsClickDistanceEventEnable = isEnabled;
    }


    /**
     * 设置在触发单击事件时是否同时触发单点触摸事件;默认触发
     * <p>单击事件本身属于单点触摸事件之中的一种,只是触摸时间在500ms以内则认为是单击事件,但同时是满足触发单点触摸事件的(此处仅指up事件)</p>
     * 在up事件中,事件优先处理级如下: 双击 > 单击 > 普通的UP事件
     *
     * @param isTrigger true为同时触发,false为忽略单点触摸事件
     */
    public void setIsTriggerSingleTouchEvent(boolean isTrigger) {
        this.mIsTriggerSingleTouchEvent = isTrigger;
    }

    /**
     * 设置是否显示log
     *
     * @param isShowLog
     * @param tag       tag为显示log的标志,可为null,tag为null时使用默认标志"touch_event"
     */
    public void setIsShowLog(boolean isShowLog, String tag) {
        if (tag != null) {
            TAG = tag;
        } else {
            TAG = "touch_event";
        }
        this.mIsShowLog = isShowLog;
    }

    /**
     * 打印默认的log,默认标志为:touch_event
     *
     * @param msg 打印消息
     */
    public void showMsg(String msg) {
        if (mIsShowLog) {
            Log.i(TAG, msg);
        }
    }

    /**
     * 打印log
     *
     * @param tag 标志tag
     * @param msg 打印信息
     */
    public void showMsg(String tag, String msg) {
        if (mIsShowLog) {
            Log.i(tag, msg);
        }
    }

    /**
     * <font color="#ff9900">取消事件有效性,目前仅对双击事件有效{@link OnToucheEventListener#onDoubleClickByTime()}</font><br/>
     * 每一次单击事件之后会有一个暂存的延迟标识,在允许时间内再次触发单击事件时,此时不会响应单击事件,而是转化成双击事件
     *
     * @param event 需要取消的事件
     */
    public void cancelDoubleClickEvent(int event) {
        switch (event) {
            case EVENT_DOUBLE_CLICK_BY_TIME:
                //一旦取消双击事件,所有有关的变量都重置
                this.mIsFireLastClickEvent = false;
                this.mIsFireTimeClickEvent = false;
                this.mIsFireDistanceClickEvent = false;
                break;
        }
    }


    public interface OnToucheEventListener {
        /**
         * 单点触摸事件处理
         *
         * @param event            单点触摸事件
         * @param extraMotionEvent 建议处理的额外事件,如果不需要进行额外处理则该参数值为{@link #MOTION_EVENT_NOTHING}
         *                         <p>存在此参数是因为可能用户进行单点触摸并移动之后,会再进行多点触摸(此时并没有松开触摸),在这种情况下是无法分辨需要处理的是单点触摸事件还是多点触摸事件.
         *                         <font color="#ff9900"><b>此时会传递此参数值为单点触摸的{@link MotionEvent#ACTION_UP},建议按抬起事件处理并结束事件</b></font></p>
         */
        public abstract void onSingleTouchEventHandle(MotionEvent event, int extraMotionEvent);

        /**
         * 多点触摸事件处理(两点触摸,暂没有做其它任何多点触摸)
         *
         * @param event            多点触摸事件
         * @param extraMotionEvent 建议处理的额外事件,如果不需要进行额外处理则该参数值为{@link #MOTION_EVENT_NOTHING}
         */
        public abstract void onMultiTouchEventHandle(MotionEvent event, int extraMotionEvent);

        /**
         * 单击事件处理,由于只要触摸到屏幕且时间足够长,就可以产生move事件,并不一定需要移动触摸才能产生move事件,
         * <font color="#ff9900"><b>所以产生单击事件的同时也会触发up事件{@link #onSingleTouchEventHandle(MotionEvent, int)}</b></font>,
         * <p>单击事件仅仅只能控制触摸时间<font color="#ff9900"><b>少于500ms</b></font>的触摸事件,超过500ms将不会触摸单击事件</p>
         *
         * @param event 单击触摸事件
         */
        public abstract void onSingleClickByTime(MotionEvent event);

        /**
         * 单击事件处理,触摸点down的坐标与up坐标距离差不大于10像素则认为是一次单击,<font color="#ff9900"><b>与时间无关</b></font>
         *
         * @param event 单击触摸事件
         */
        public abstract void onSingleClickByDistance(MotionEvent event);

        /**
         * 双击事件处理,每次单击判断由时间决定,参考{@link #onSingleClickByTime(MotionEvent)}
         */
        public abstract void onDoubleClickByTime();

    }

}
