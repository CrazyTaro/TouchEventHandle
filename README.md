# TouchEventHandle
事件触摸处理类示例

## 详细说明请看博客
http://blog.csdn.net/u011374875/article/details/51074493

---

## 概述
主要是自定义处理常见的单点触摸事件及两点触摸事件;
同时通过触摸事件辅助工具类方便,简单化了常用的 拖动/缩放 功能
这个项目主要是两个类,都是用于处理触摸事件及其细节.

- TouchEventHelper,触摸事件的处理辅助类
- MoveAndScaleTouchHelper,移动和缩放事件的处理辅助类

其中,第一个类是针对触摸事件,完成了单击,双击的检测.同时保留了对不明或者不能处理的触摸事件的传递.
第二个类是针对单点触摸事件进行移动的计算与分配,并处理了两点触摸事件的缩放功能,可以说是对第一个类触摸事件的补充处理.

---

## TouchEventHelper
`TouchEventHelper`原本是一个抽象类(原类名为`AbsTouchEventHandle`),但是由于java的单继承模式,如果做为一个抽象类在某些情况下使用并不方便,所以更新为以helper的形式的一个辅助类,仅需要实现对应的接口即可.

### 处理的事件
- 基于时间的单击事件(在一定时间间隔内的按下抬起事件)
- 基于距离的单击事件(在一定距离内按下抬起事件,与时间无关)
- 基于两种不同单击事件的双击事件
- 普通的单点触摸事件传递
- 普通的多点触摸事件传递

从以上可以知道这个辅助类主要是处理了常用的几个触摸事件.其中单击事件分为两种,区别在于:

基于时间的单击事件就是我们常用的单击事件;
基于距离的单击事件是**与时间无关,只要单击位置按下与抬起前后在一定的范围内即可认为是一次单击事件**,基于距离的单击事件是用于某些情况下的使用的.

双击事件是基于单击事件的,两次单击事件在一定间隔内即认为一次双击事件.(请注意**单击事件分为两种,所以双击事件的触发方式可以有两种,但双击事件只有一种情况,永远是两次单击事件构成一次双击事件**)

---

### 事件的处理规则
1. 优先处理双击事件(当已经触发了一次单击事件当前再触发一次时),当触发了双击事件,直接回调事件,不再处理任何事件;
2. 处理单击事件,处理单击事件时优先处理**基于距离的单击事件**,但可以通过设置设置两种检测方式都处理(不建议这么做,仅使用一种即可)
3. 处理其它触摸事件(传递给接口自行处理)

---

### 使用方式
使用时只需要实现此辅助类定义的接口即可.其它的操作由辅助类完成.

- 实现接口

```JAVA
public interface OnToucheEventListener {
    /**
     * 单点触摸事件处理
     *
     * @param event            单点触摸事件
     * @param extraMotionEvent 建议处理的额外事件,如果不需要进行额外处理则该参数值为{@link #MOTION_EVENT_NOTHING}
     *存在此参数是因为可能用户进行单点触摸并移动之后,会再进行多点触摸(此时并没有松开触摸),在这种情况下是无法分辨需要处理的是单点触摸事件还是多点触摸事件.
     *此时会传递此参数值为单点触摸的{@link MotionEvent#ACTION_UP},建议按抬起事件处理并结束事件
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
     * 所以产生单击事件的同时也会触发up事件{@link #onSingleTouchEventHandle(MotionEvent, int)}
     * 单击事件仅仅只能控制触摸时间少于指定时间的触摸事件,超过时间将不会触摸单击事件
     *
     * @param event 单击触摸事件
     */
    public abstract void onSingleClickByTime(MotionEvent event);

    /**
     * 单击事件处理,触摸点down的坐标与up坐标距离差不大于指定距离像素则认为是一次单击,与时间无关
     *
     * @param event 单击触摸事件
     */
    public abstract void onSingleClickByDistance(MotionEvent event);

    /**
     * 双击事件处理,每次单击判断由时间决定,参考{@link #onSingleClickByTime(MotionEvent)}
     */
    public abstract void onDoubleClickByTime();

}
```

以上接口已经附带了说明,接口方法需要实现的功能也比较明确,前两个是单点触摸及多点触摸事件的传递(此辅助类未处理的所有触摸事件都会传递过去);
后面是此辅助类处理的触摸事件的回调,基于时间的单击事件;基于距离的单击事件;双击事件.

- 设置及使用

```JAVA
//创建辅助类对象,参数为以上接口类型
mTouchHelper = new TouchEventHelper(this);
//设置基于距离的单击事件检测是否可用
mTouchHelper.setIsEnableSingleClickByDistance(true);
//设置基于时间的单击事件检测是否可用
mTouchHelper.setIsEnableSingleClickByTime(false);
//设置单击事件检测的间隔时间(仅用于基于时间的单击事件)
mTouchHelper.setSingleClickInterval(500);
//设置双击事件检测的间隔时间,两次单击构成一次双击
mTouchHelper.setDoubleClickInterval(500);
//这个地方比较不好理解...
//设置在触发了单击事件的情况下,是否回调单点触摸事件的接口,传递事件给接口自行处理.
//默认情况下为false,暂时也没有发现需要特别处理的情况
mTouchHelper.setIsTriggerSingleTouchEvent(false);
//helper辅助类是通过实现View.onTouchListener处理事件的
//当需要使用到某个view中时,需要将helper对象作为触摸接口传递给view
view.setOnTouchListener(mTouchHelper);
```

使用时不需要对所有设置重新设置,建议设置的仅有

```JAVA
//设置基于距离的单击事件检测是否可用
mTouchHelper.setIsEnableSingleClickByDistance(true);
//设置基于时间的单击事件检测是否可用
mTouchHelper.setIsEnableSingleClickByTime(false);
```

其它的设置一般使用默认即可.默认使用`基于时间的单击检测方式/单击间隔为250ms/双击间隔为350ms/不同时触发单击事件及单击触摸事件回调`

---

## MoveAndScaleTouchHelper
这是一个专门处理单点触摸移动事件及两点触摸缩放事件的辅助类,使用方式类似上面的类,都是实现对应的接口并作为参数传递给此类即可.
此类有一定程度上依赖于上一个类,本来两个类是为了处理同一个需求,但内容较多,同时各自的功能也可以独立分开,所以拆分成两个辅助类.

### 处理的事件
- 单点触摸时的移动事件及计算
- 两点触摸时的移动事件及计算

此类主要是处理了以上两个事件的通用部分的计算和逻辑处理,同时提供了接口用于交互及确认相关的逻辑处理事件.

---

### 使用方式
使用方式为实现对应的接口并传递给此辅助类,同时在对应的地方调用此辅助类的方法即可正确使用.

- 实现的接口

由于这个辅助类处理了两种不同的事件,所以接口分为两个,一个是单点触摸移动的接口回调,一个是两点触摸的缩放事件回调.

```JAVA
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
```

单点触摸移动事件的接口功能是比较明显的,分为能否移动的判断回调及移动(包括无法移动)回调.
能否移动的处理是由实现类自行决定的,这是由于每个不同的需求可能针对移动的条件不同;

```JAVA
/**
 * 缩放事件处理接口
 */
public interface IScaleEvent {

    /**
     * 是否允许进行缩放
     *
     * @param newScaleRate 新的缩放比例值,请注意该值为当前值与缩放前的值的比例,即在move期间,
     *此值都是相对于move事件之前的down的坐标计算出来的,并不是上一次move结果的比例,建议
     *修改缩放值或存储缩放值在move事件中不要处理,在up事件中处理会比较好,防止每一次都重新存储数据,可能造成数据的大量读写而失去准确性
     * @return
     */
    public abstract boolean isCanScale(float newScaleRate);

    /**
     * 设置缩放的比例(存储值),当up事件中,且当前不允许缩放,此值将会返回最后一次在move中允许缩放的比例值,
     * 此方式保证了在处理up事件中,可以将最后一次缩放的比例显示出来,而不至于结束up事件不存储数据导致界面回到缩放前或者之前某个状态
     *
     * @param newScaleRate     新的缩放比例
     * @param isNeedStoreValue 是否需要存储比例,此值仅为建议值;当move事件中,此值为false,当up事件中,此值为true;不管当前up事件中是否允许缩放,此值都为true;
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
```

多点触摸事件的接口也是与单点触摸移动类似的,都是确定是否可以进行缩放,缩放状态保存,缩放(及无法缩放)回调.

- 设置及使用

使用此辅助类时基本上只需要设置对应的接口即可,如果需要实现移动事件,设置移动接口;如果需要实现缩放事件,设置缩放接口;然后最重要的一个,需要调用其事件处理方法,否则无法触发事件的处理.

```JAVA
//创建处理辅助类对象
mMoveAndScaleTouchHelper = new MoveAndScaleTouchHelper();
//设置移动事件回调接口
mMoveAndScaleTouchHelper.setMoveEvent(this);
//设置缩放事件回调接口
mMoveAndScaleTouchHelper.setScaleEvent(this);


//此处的方法来自 TouchEventHelper 的接口
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
```

- 使用注意

移动接口的实现和使用并没有什么特别之处,缩放的回调接口需要注意一个方法`setScaleRate(float newScaleRate, boolean isNeedStoreValue)`.
辅助类已经处理了缩放相关的计算,会得到一个缩放的比例值,但是如何处理界面的缩放辅助类并不能完成,所以会通过这个方法回调给实现类去处理缩放的相关数据保存及调整工作.

这里的参数需要特别注意一下:
**newScaleRate**,参数名已经描述了这个参数的作用,新的缩放比例,这里的"新"是指两点触摸按下时的界面与当前界面(移动触摸点之后)的比例;当放大时,此参数值>1;当缩小时,此参数值< 1
**isNeedStoreValue**,是否需要储存相关数据值,这是因为当两点触摸抬起时,缩放结束,肯定需要储存相关缩放的数据值;还有其它情况,如两点触摸移动过程中抬起了一只手指,则变为单点触摸,此时也是缩放结束的一个标志.

- 矩形的参考实例

鉴于部分功能或者方法无法通过文字完全说明清楚,所以下面附上一个矩形的缩放与移动的接口实现.

```JAVA
//能否在X轴方向上移动
public boolean isCanMovedOnX(float moveDistanceX, float newOffsetX) {
    return true;
}

//能否在Y轴方向上移动
public boolean isCanMovedOnY(float moveDistacneY, float newOffsetY) {
    return true;
}

//移动回调,一般都是重绘界面
public void onMove(int suggestEventAction) {
    mDrawView.postInvalidate();
}

@Override
public void onMoveFail(int suggetEventAction) {

}

//能否缩放,此处为示例,缩放条件没有任何要求,都是允许的
public boolean isCanScale(float newScaleRate) {
    return true;
}

//保存缩放数据的工作,重点
public void setScaleRate(float newScaleRate, boolean isNeedStoreValue) {
    //mTempRectf为缩放前的矩形
    //计算新的缩放后的矩形宽高(注意newScaleRate是相对于缩放前的矩形的比例)
    float newWidth = mTempRectf.width() * newScaleRate;
    float newHeight = mTempRectf.height() * newScaleRate;
    //计算中心位置
    float centerX = mTempRectf.centerX();
    float centerY = mTempRectf.centerY();
    //根据中心位置调整大小
    //此处确保了缩放时是以物体中心为标准
    mDrawRectf.left = centerX - newWidth / 2;
    mDrawRectf.top = centerY - newHeight / 2;
    mDrawRectf.right = mDrawRectf.left + newWidth;
    mDrawRectf.bottom = mDrawRectf.top + newHeight;
    //若直接在原矩形的左上角上更新宽高,则此方式缩放中心为左上角
    //mDrawRectf.right=mDrawRectf.left+newWidth;
    //mDrawRectf.bottom=mDrawRectf.top+newHeight;
    if (isNeedStoreValue) {
        //当需要保存缩放数据时,说明一次缩放已经结束
        //将计算的mDrawRectf保存为暂存的矩形数据mTempRectf
        //mTempRectf也是下一次矩形缩放前的标准
        mTempRectf = new RectF(mDrawRectf);
    }
}

//缩放更新时也是重绘界面
public void onScale(int suggestEventAction) {
    mDrawView.postInvalidate();
}

@Override
public void onScaleFail(int suggetEventAction) {

}
```

以上部分的是一个矩形的移动与缩放的接口实现示例,具体的实现最后会附上整个类文件,使用时需要注意以上提到的一些细节问题.

- 新的功能

对于缩放的辅助类,后来添加了一个新的功能,只是很简单的功能,一般也不需要用到吧.
在移动的情况下,可以在一次移动之后回滚到未移动前的位置.

```JAVA
//判断是否可以进行回滚
mMoveAndScaleTouchHelper.isCanRollBack();
//回滚到未移动前的位置,对一个移动阶段,只能回滚一次
//回滚只会记录最近的一次移动位置及状态,无法连续回滚
//此方法已经内部判断了是否可以进行回滚,不需要判断回滚再调用此方法,可以直接使用
mMoveAndScaleTouchHelper.rollbackToLastOffset();
```

---

## 完整的辅助类使用(矩形的缩放与移动)
以下是对一个矩形的缩放与移动事件处理,这里使用到了上面两个辅助类.

```JAVA
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
    //此数据保存的是每一次缩放后的数据
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
        //两次单击之间的时间不超过350ms
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
        //mDrawRectf.right=mDrawRectf.left+newWidth;
        //mDrawRectf.bottom=mDrawRectf.top+newHeight;
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
```

---

## 示例图片
![](https://github.com/CrazyTaro/TouchEventHandle/raw/master/screenshot/touchEventDemo.gif)
