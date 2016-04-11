package com.henrytaro.ct.ui;

import android.graphics.Bitmap;

/**
 * Created by taro on 16/4/8.
 */
public interface ICropDrawAction {
    /**
     * 保存图片
     *
     * @param fileNameWithPath 保存路径
     * @param bmpFormat        保存图片的格式
     * @param isRecycleBmp     是否回收图片,若true则回收图片,若false则不回收图片
     * @param bmpQuality       保存图片的质量,在0-100,格式为PNG时此参数无效.
     * @return 若成功保存图片返回true, 否则返回false
     */
    public boolean restoreBitmap(String fileNameWithPath, Bitmap.CompressFormat bmpFormat, boolean isRecycleBmp, int bmpQuality);

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
