package com.henrytaro.ct.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by taro on 16/4/8.
 */
public interface ICropDrawAction {
    /**
     * 保存图片
     *
     * @param filePath     保存路径
     * @param isRecycleBmp 是否回收图片,若true则回收图片,若false则不回收图片
     * @return
     */
    public boolean restoreBitmap(String filePath, boolean isRecycleBmp);

    /**
     * 回收图片
     */
    public void recycleBitmap();

    /**
     * 设置裁剪区域的宽高大小,此方法应该在界面绘制之前调用有效
     *
     * @param eachSize 边长
     * @return
     */
    public boolean setCropWidthAndHeight(int eachSize);

    /**
     * 设置imageBitmap
     *
     * @param src
     */
    public void setImageBitmap(Bitmap src);
}
