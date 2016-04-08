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
 * Created by xuhaolin on 16/1/22.
 */
public class CropView extends View {
    private CropDrawUtils mCropDraw = null;

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
        mCropDraw = new CropDrawUtils(this);
    }

    /**
     * 设置裁剪的图片
     *
     * @param src
     */
    public void setImageBitmap(Bitmap src) {
        mCropDraw.setImageBitmap(src);
    }

    /**
     * 设置边长
     *
     * @param width
     */
    public void setCropWidthAndHeight(int width) {
        mCropDraw.setCropWidthAndHeight(width);
    }

    /**
     * 保存图片
     *
     * @param filePath
     * @return
     */
    public boolean restoreBitmap(String filePath) {
        return mCropDraw.restoreBitmap(filePath);
    }

    /**
     * 回收图片
     */
    public void recycleBitmap() {
        mCropDraw.recycleBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCropDraw.onDraw(canvas);
    }

    /**
     * 裁剪类
     */
    private class CropDrawUtils extends AbsTouchEventHandle implements TouchUtils.IMoveEvent, TouchUtils.IScaleEvent {
        //创建工具类
        private TouchUtils mTouch = new TouchUtils();
        private View mDrawView = null;
        public static final float DEFAULT_CROP_WIDTH = 0;
        public static final float DEFAULT_CROP_HEIGHT = 0;

        private Paint mPaint = null;
        private Bitmap mBitmap = null;
        private RectF mCropRecf = null;
        private RectF mBitmapRecf = null;
        private PointF mViewParams = null;

        private float mCropWidth = DEFAULT_CROP_WIDTH;
        private float mCropHeight = DEFAULT_CROP_HEIGHT;

        //暂时性保存的半径(同理在绘制时也需要一个暂时性存放的数据)
        private RectF mTempBmpRectF = null;

        private boolean mIsFirstGetViewParams = true;

        //针对构造函数,可有不同的需求,在此例中,其实并不需要context
        //此参数是可有可无的,有时自定义绘制界面需要加载一些资源什么的需要用到context,
        //这个时候就有用了,这个看需要
        public CropDrawUtils(View drawView) {
            this.mDrawView = drawView;
            //设置工具类的监听事件
            mTouch.setMoveEvent(this);
            mTouch.setScaleEvent(this);
            //绑定view与触摸事件
            this.mDrawView.setOnTouchListener(this);

            initial();
        }

        public void onDraw(Canvas canvas) {
            getViewParams();
            drawBitmap(canvas);
            drawCropRecf(canvas);

            mPaint.setAlpha(255);
        }

        /**
         * 设置imageBitmap
         *
         * @param src
         */
        public void setImageBitmap(Bitmap src) {
            if (src == null) {
                return;
            } else {
                mBitmap = src;
                mBitmapRecf.setEmpty();
                mDrawView.postInvalidate();
            }
        }

        /**
         * 设置裁剪区域的宽高大小,此方法应该在界面绘制之前调用有效
         *
         * @param eachSize 边长
         * @return
         */
        public boolean setCropWidthAndHeight(int eachSize) {
            mCropWidth = eachSize;
            mCropHeight = eachSize;

            //调整裁剪区域宽高大小
            //若调整了返回true,否则返回false
            //此处返回相反的值是因为:使用指定参数设置成功的话返回true,若调整过了则使用不到参数,返回False
            return !isAdjustCropWidthAndHeight();
        }

        /**
         * 初始化数据及对象
         */
        private void initial() {
            mPaint = new Paint();
            mBitmapRecf = new RectF();
            mViewParams = new PointF();

            this.setIsShowLog(false, null);
        }

        /**
         * 调整裁剪区域的宽高大小
         *
         * @return
         */
        private boolean isAdjustCropWidthAndHeight() {
            boolean isChanged = false;
            //默认裁剪区域的大小与图片最大边大小相同,都是90%
            float largeCropSizeX = mViewParams.x * 0.9f;
            float largeCropSizeY = mViewParams.y * 0.9f;
            float largeCropSize = largeCropSizeX < largeCropSizeY ? largeCropSizeX : largeCropSizeY;
            if (mCropWidth > largeCropSize || mCropWidth <= 0) {
                mCropWidth = largeCropSize;
                isChanged = true;
            }
            if (mCropHeight > largeCropSize || mCropHeight <= 0) {
                mCropHeight = largeCropSize;
                isChanged = true;
            }
            return isChanged;
        }

        /**
         * 获取当前view的宽高等参数
         */
        private void getViewParams() {
            if (mIsFirstGetViewParams) {
                mViewParams.x = mDrawView.getWidth();
                mViewParams.y = mDrawView.getHeight();
            }
        }

        /**
         * 绘制图片
         *
         * @param canvas
         * @return
         */
        private boolean drawBitmap(Canvas canvas) {
            if (mBitmap != null && canvas != null) {
                //第一次绘制先计算图片的绘制大小
                if (mBitmapRecf == null || mBitmapRecf.isEmpty()) {
                    //计算图片缩放显示的绘制区域
                    int width = mBitmap.getWidth();
                    int height = mBitmap.getHeight();
                    //计算最大绘制区域
                    //此处不使用屏幕大小是为了四边留空白
                    float largeDrawWidth = mViewParams.x * 0.9f;
                    float largeDrawHeight = mViewParams.y * 0.9f;

                    //以图片高填充屏幕,计算填充的宽度
                    int tempDrawWidth = (int) ((largeDrawHeight / height) * width);
                    //以图片宽填充屏幕,计算填充的高度
                    int tempDrawHeight = (int) ((largeDrawWidth / width) * height);
                    //若填充宽度小于最大绘制宽度时
                    if (tempDrawWidth < largeDrawWidth) {
                        //以高填充(填充后的宽不超过屏幕宽,图片可以完全显示)
                        width = tempDrawWidth;
                        height = (int) largeDrawHeight;
                    } else {
                        //否则,以宽填充,填充后的高不超过屏幕宽,图片可以完全显示
                        height = tempDrawHeight;
                        width = (int) largeDrawWidth;
                    }
                    //不存在以下的情况:
                    //不管以高填充还是宽填充,填充后的另一边都超过屏幕的长度,数学证明略

                    //创建绘制区域
                    mBitmapRecf.left = (mViewParams.x - width) / 2;
                    mBitmapRecf.right = mBitmapRecf.left + width;
                    mBitmapRecf.top = (mViewParams.y - height) / 2;
                    mBitmapRecf.bottom = mBitmapRecf.top + height;

                    //计算绘制区域的最小边,与裁剪区域的大小进行比较
                    float smallDrawSize = 0;
                    if (mBitmapRecf.width() > mBitmapRecf.height()) {
                        smallDrawSize = mBitmapRecf.height();
                    } else {
                        smallDrawSize = mBitmapRecf.width();
                    }
                    //创建裁剪区域对象
                    if (mCropRecf == null) {
                        mCropRecf = new RectF();
                    }
                    float centerX = mViewParams.x / 2;
                    float centerY = mViewParams.y / 2;
                    mCropRecf.left = centerX - smallDrawSize / 2;
                    mCropRecf.top = centerY - smallDrawSize / 2;
                    mCropRecf.right = mCropRecf.left + smallDrawSize;
                    mCropRecf.bottom = mCropRecf.top + smallDrawSize;
                    //更新裁剪区域的边大小
                    mCropWidth = mCropRecf.width();
                    mCropHeight = mCropRecf.height();
                }

                canvas.drawBitmap(mBitmap, null, getRectfAfterMove(mBitmapRecf), mPaint);
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取移动后的实际绘制区域
         *
         * @param src 默认的绘制区域
         * @return 返回新的绘制区域对象
         */
        private RectF getRectfAfterMove(RectF src) {
            if (src != null) {
                RectF moveRectf = new RectF(src);

                //X/Y添加上偏移量
                moveRectf.left += mTouch.getDrawOffsetX();
                moveRectf.top += mTouch.getDrawOffsetY();
                moveRectf.right += mTouch.getDrawOffsetX();
                moveRectf.bottom += mTouch.getDrawOffsetY();

                return moveRectf;
            } else {
                return null;
            }
        }

        /**
         * 获取当前裁剪图片区域在实际图片中的裁剪区域
         *
         * @param bitmap    原始图片对象
         * @param cropRectF 当前裁剪区域
         * @param bmpRectF  图片绘制区域
         * @return 返回实际图片中的裁剪区域大小
         */
        private Rect getBitmapScaleRect(Bitmap bitmap, RectF cropRectF, RectF bmpRectF) {
            //图片存在且相应的区域都有效的情况下才能进行计算
            if (bitmap != null && cropRectF != null && bmpRectF != null) {
                //图片实际宽高
                int bmpWidth = bitmap.getWidth();
                int bmpHeight = bitmap.getHeight();
                //图片绘制的区域宽高
                float drawWidth = bmpRectF.width();
                float drawHeight = bmpRectF.height();

                //当前需要裁剪图片区域的宽高
                float cropWidth = cropRectF.width();
                float cropHeight = cropRectF.height();

                //计算实际的裁剪宽高
                float realCropWidth = (cropWidth / drawWidth) * bmpWidth;
                float realCropHeight = realCropWidth;

                //计算实际裁剪区域的坐标
                float realCropTop = ((cropRectF.top - bmpRectF.top) / drawHeight) * bmpHeight;
                float realCropLeft = ((cropRectF.left - bmpRectF.left) / drawWidth) * bmpWidth;

                //创建实际裁剪区域
                Rect srcRect = new Rect();
                srcRect.top = (int) realCropTop;
                srcRect.left = (int) realCropLeft;
                srcRect.bottom = srcRect.top + (int) realCropWidth;
                srcRect.right = srcRect.left + (int) realCropHeight;
                return srcRect;
            } else {
                return null;
            }
        }

        /**
         * 保存图片
         *
         * @param filePath 保存路径
         * @return
         */
        public boolean restoreBitmap(String filePath) {
            if (!TextUtils.isEmpty(filePath)) {
                //获取图片裁剪区域
                Rect srcRect = getBitmapScaleRect(mBitmap, mCropRecf, getRectfAfterMove(mBitmapRecf));
                //裁剪当前的图片
                Bitmap cropBmp = Bitmap.createBitmap(mBitmap, srcRect.left, srcRect.top, srcRect.width(), srcRect.height());

                try {
                    File bitmapFile = new File(filePath);
                    if (!bitmapFile.exists()) {
                        bitmapFile.createNewFile();
                    }
                    //将图片保存到文件中
                    FileOutputStream out = new FileOutputStream(bitmapFile);
                    cropBmp.compress(Bitmap.CompressFormat.PNG, 50, out);
                    //回收图片
                    mBitmap.recycle();
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    //不管是否裁剪成功,都回收裁剪后的图片,因为这部分是用不到的
                    if (cropBmp != null && !cropBmp.isRecycled()) {
                        cropBmp.recycle();
                    }
                }
            } else {
                return false;
            }
        }

        /**
         * 回收图片
         */
        public void recycleBitmap() {
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
        }

        /**
         * 绘制图片裁剪界面,实际上只是绘制一个透明的蒙层
         *
         * @param canvas
         * @return
         */
        private boolean drawCropRecf(Canvas canvas) {
            if (canvas != null) {
                RectF topRecf = new RectF(0, 0, mViewParams.x, mCropRecf.top);
                RectF bottomRecf = new RectF(0, mCropRecf.bottom, mViewParams.x, mViewParams.y);
                RectF leftRecf = new RectF(0, mCropRecf.top, mCropRecf.left, mCropRecf.bottom);
                RectF rightRecf = new RectF(mCropRecf.right, mCropRecf.top, mViewParams.x, mCropRecf.bottom);

                mPaint.setColor(Color.BLACK);
                mPaint.setAlpha((int) (255 * 0.6));
                canvas.drawRect(topRecf, mPaint);
                canvas.drawRect(bottomRecf, mPaint);
                canvas.drawRect(leftRecf, mPaint);
                canvas.drawRect(rightRecf, mPaint);

                return true;
            } else {
                return false;
            }
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
            float moveDistance = Math.abs(moveDistanceX);
            RectF drawRectf = this.getRectfAfterMove(mBitmapRecf);
            if (moveDistanceX > 0) {
                return moveDistance <= (mCropRecf.left - drawRectf.left);
            } else {
                return moveDistance <= (drawRectf.right - mCropRecf.right);
            }
        }

        @Override
        public boolean isCanMovedOnY(float moveDistacneY, float newOffsetY) {
            float moveDistance = Math.abs(moveDistacneY);
            RectF drawRectf = this.getRectfAfterMove(mBitmapRecf);
            if (moveDistacneY > 0) {
                return moveDistance <= (mCropRecf.top - drawRectf.top);
            } else {
                return moveDistance <= (drawRectf.bottom - mCropRecf.bottom);
            }
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
            if (mTempBmpRectF == null) {
                mTempBmpRectF = new RectF(mBitmapRecf);
            }
            float oldWidth = mTempBmpRectF.width();
            float oldHeigh = mTempBmpRectF.height();
            float newWidth = oldWidth * newScaleRate;
            float newHeight = oldHeigh * newScaleRate;
            //获取绘制区域的最短边
            float smallSize = newWidth > newHeight ? newHeight : newWidth;
            //最短边必须大于裁剪区域的边
            return smallSize > mCropRecf.width();
        }

        @Override
        public void setScaleRate(float newScaleRate, boolean isNeedStoreValue) {
            //更新当前的数据
            //newScaleRate缩放比例一直是相对于按下时的界面的相对比例,所以在移动过程中
            //每一次都是要与按下时的界面进行比例缩放,而不是针对上一次的结果
            //使用这种方式一方面在缩放时的思路处理是比较清晰的
            //另一方面是缩放的比例不会数据很小(若相对于上一次,每一次move移动几个像素,
            //这种情况下缩放的比例相对上一次肯定是0.0XXXX,数据量一小很容易出现一些不必要的问题)
            if (mTempBmpRectF == null) {
                mTempBmpRectF = new RectF(mBitmapRecf);
            }

            float lastWidth = mTempBmpRectF.width();
            float lastHeight = mTempBmpRectF.height();

            float newWidth = lastWidth * newScaleRate;
            float newHeight = lastHeight * newScaleRate;

            mBitmapRecf.left = mTempBmpRectF.centerX() - newWidth / 2;
            mBitmapRecf.top = mTempBmpRectF.centerY() - newHeight / 2;
            mBitmapRecf.right = mBitmapRecf.left + newWidth;
            mBitmapRecf.bottom = mBitmapRecf.top + newHeight;
            //当返回的标志为true时,提醒为已经到了up事件
            //此时应该把最后一次缩放的比例当做最终的数据保存下来
            if (isNeedStoreValue) {
                mTempBmpRectF.set(mBitmapRecf);
            }
        }

        @Override
        public void onScale(int suggestEventAction) {
            if (suggestEventAction == MotionEvent.ACTION_POINTER_UP) {
                //调整缩放后图片的位置必须在裁剪框中
                RectF drawRectf = this.getRectfAfterMove(mBitmapRecf);
                float offsetX = mTouch.getDrawOffsetX();
                float offsetY = mTouch.getDrawOffsetY();
                if (drawRectf.left > mCropRecf.left) {
                    offsetX += mCropRecf.left - drawRectf.left;
                }
                if (drawRectf.top > mCropRecf.top) {
                    offsetY += mCropRecf.top - drawRectf.top;
                }
                if (drawRectf.right < mCropRecf.right) {
                    offsetX += mCropRecf.right - drawRectf.right;
                }
                if (drawRectf.bottom < mCropRecf.bottom) {
                    offsetY += mCropRecf.bottom - drawRectf.bottom;
                }
                mTouch.setOffsetX(offsetX);
                mTouch.setOffsetY(offsetY);
            }
            mDrawView.postInvalidate();
        }

        @Override
        public void onScaleFail(int suggetEventAction) {

        }
    }
}