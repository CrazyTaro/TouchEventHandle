package com.henrytaro.ct.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import com.henrytaro.ct.activity.CropBitmapActivity;

import java.io.*;

/**
 * Created by taro on 16/4/11.
 */
public class GrallyAndPhotoUtils {
    /**
     * 拍照请求
     */
    public static final int REQUEST_CODE_TAKE_PHOTO = 0x1;
    /**
     * 打开相册请求
     */
    public static final int REQUEST_CODE_OPEN_GRALLY = 0x2;
    /**
     * 裁剪图片请求
     */
    public static final int REQUEST_CODE_CROP_PHOTO = 0x3;

    /**
     * 加载缩略图
     *
     * @param filePath 文件路径
     * @param reqSize  预期图片最长边的最大值,加载所有图片的最大值不会超过此值
     * @return
     */
    public static Bitmap decodeBitmapInScale(String filePath, int reqSize) {
        InputStream in = null;
        in = getBmpInputStream(filePath);
        BitmapFactory.Options options = getStreamBitmapOptions(in, reqSize);
        in = getBmpInputStream(filePath);
        return decodeBitmapInScale(in, options);
    }

    /**
     * 加载图片(适用于资源文件)
     *
     * @param res
     * @param resID   资源ID
     * @param reqSize 预期图片最长边的最大值,加载所有图片的最大值不会超过此值
     * @return
     */
    public static Bitmap decodeBitmapInScale(Resources res, int resID, int reqSize) {
        InputStream in = null;
        in = getBmpInputStream(res, resID);
        BitmapFactory.Options options = getStreamBitmapOptions(in, reqSize);
        in = getBmpInputStream(res, resID);
        return decodeBitmapInScale(in, options);
    }

    /**
     * 获取图片加载配置
     *
     * @param in      图片流,此方法运行后该流会被关闭
     * @param reqSize 预期图片最长边的最大值
     * @return
     */
    public static BitmapFactory.Options getStreamBitmapOptions(InputStream in, float reqSize) {
        try {
            if (in == null || reqSize <= 0) {
                return null;
            }
            //仅加载图片信息(不加载图片数据),获取配置文件
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);

            //计算图片缩放比例
            int sampleSize = calculateInSampleSize(options, reqSize);
            //正常加载图片
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            return options;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从图片流数据中加载图片
     *
     * @param in      图片流数据,此参数值建议从文件重新加载出来(不要使用过的流)
     * @param options 加载配置,此参数应该来自于{@link #getStreamBitmapOptions(InputStream, float)}
     * @return
     */
    public static Bitmap decodeBitmapInScale(InputStream in, BitmapFactory.Options options) {
        try {
            if (in == null || options == null || in.available() <= 0) {
                return null;
            }
            Log.i("bmp", "sampleSize = " + options.inSampleSize + "\nsrcWidth = " + options.outWidth + "\nsrcHeight = " + options.outHeight);
            return BitmapFactory.decodeStream(in, null, options);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 计算图片的缩放比例;</br>
     * 此值是指图片的宽高的缩放比例,图片面积的缩放比例为返回值的2次方;
     * 返回值必定大于0且为2的N次方(这是由于系统决定了返回值必须是2的N次方)
     *
     * @param options 加载图片所得的配置信息
     * @param reqSize 预期希望的的图片最长边大小,最长边由图片本身决定,可能是宽也可能是高,加载后的图片最长边不会超过此参数值
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, float reqSize) {
        if (reqSize <= 0) {
            throw new RuntimeException("预期边长不可小于0");
        }
        float bmpWidth = options.outWidth;
        float bmpHeight = options.outHeight;
        float largeSizeInBmp = 0;
        int sampleSize = 1;
        //记录最大的边
        if (bmpWidth > bmpHeight) {
            largeSizeInBmp = bmpWidth;
        } else {
            largeSizeInBmp = bmpHeight;
        }
        //将最大边与预期的边大小进行比较计算缩放比
        if (largeSizeInBmp < reqSize) {
            //最大边小于预期,则sampleSize为1
            sampleSize = 1;
        } else {
            //最大边大于预期边
            sampleSize = (int) (largeSizeInBmp / reqSize + 0.5);
            //计算所得缩放值为2的几倍指数,即求 log2(sampleSize)
            double powerNum = Math.log(sampleSize) / Math.log(2);
            int tempPowerNum = (int) powerNum;
            //将所得指数+1,确保尽可能小于指定值
            if (powerNum > tempPowerNum) {
                tempPowerNum += 1;
            }
            //反求sampleSize=2^tempPowerNum
            sampleSize = (int) Math.pow(2, tempPowerNum);
        }
        return sampleSize;
    }

    /**
     * 从图片路径获取图片输入流
     *
     * @param filePath 图片路径
     * @return 获取失败返回null
     */
    public static InputStream getBmpInputStream(String filePath) {
        try {
            return new FileInputStream(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从资源文件获取图片输入流
     *
     * @param res
     * @param resID 资源ID
     * @return 获取失败null
     */
    public static InputStream getBmpInputStream(Resources res, int resID) {
        if (res == null || resID == 0) {
            return null;
        }
        return res.openRawResource(resID);
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotatingBitmap(int angle, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 将图片保存到文件中
     *
     * @param outputPath 图片输出地址,完整的地址包括图片格式
     * @param bitmap     保存的图片对象
     * @param format     图片格式类型
     * @param quality    图片质量,0-100,默认使用50.当类型为PNG时,此参数无效.
     * @return
     */
    public boolean saveBitmapToFile(String outputPath, Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        if (bitmap == null || TextUtils.isEmpty(outputPath)) {
            return false;
        }
        if (quality < 0 || quality > 100) {
            quality = 50;
        }
        try {
            File file = new File(outputPath);
            //文件不存在,创建空文件
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(format, quality, out);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从系统content资源路径中获取图片的真实地址路径
     *
     * @param context
     * @param bmpIntent 系统返回的Intent数据,包含图片的content Uri
     * @return
     */
    public static String getBmpPathFromContent(Context context, Intent bmpIntent) {
        if (bmpIntent != null && bmpIntent.getData() != null) {
            Uri contentUri = bmpIntent.getData();
            String uriPath = contentUri.getPath();
            //content存在时
            if (!TextUtils.isEmpty(uriPath)) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                //加载系统数据库
                Cursor cursor = context.getContentResolver().query(contentUri,
                        filePathColumn, null, null, null);
                //移动到第一行,不移动将越界
                cursor.moveToFirst();
                //加载查询数据的列号
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //获取图片路径
                String picturePath = cursor.getString(columnIndex);
                //游标应该关闭
                cursor.close();
                return picturePath;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 打开照相机并拍照(默认将照片存放到相册中)
     *
     * @param act 启动照相机的activity
     * @return 返回用于存放拍照完的图像的Uri
     */
    public static Uri openCamera(Activity act, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath can not be null");
        }
        //尝试创建文件夹及用于存储拍照后的文件
        File outputImage = new File(filePath);
        if (!outputImage.exists()) {
            try {
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        //将File对象转换为Uri并启动照相程序
        Uri photoUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); //指定图片输出地址
        try {
            act.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO); //启动照相
        } catch (SecurityException se) {
            se.printStackTrace();
            return null;
        }
        return photoUri;
    }

    /**
     * 打开相册
     *
     * @param act      用于启用系统相册的Activity
     * @param filePath 打开相册获取图片存储的位置
     * @return
     */
    public static Uri openGrally(Activity act, String filePath) {
        //尝试创建文件
        File outputImage = new File(filePath);
        if (!outputImage.exists()) {
            try {
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        //将File对象转换为Uri并启动照相程序
        Uri avatarUri = Uri.fromFile(outputImage);
//        //此action也可以使用,此action是选择任何指定类型的文件
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //存在多个相册类型的应用时,显示给用户选择的打个相册的应用界面
        act.startActivityForResult(Intent.createChooser(intent, "请选择"), REQUEST_CODE_OPEN_GRALLY);
        return avatarUri;
    }

    /**
     * 调用系统裁剪功能裁剪,部分机型可能不适用.
     *
     * @param act          启动裁剪功能的activity
     * @param bitmapIntent
     * @param photoUri     拍照所得的uri
     * @param width        头像宽度
     * @param height       头像高度
     * @deprecated
     */
    public static void cropPhoto(Activity act, Intent bitmapIntent, Uri photoUri, int width, int height) {
        if (photoUri == null) {
            //若当前uri不存在(可能被系统清除了)
            return;
        }

        if (width <= 0) {
            width = 300;
        }
        if (height <= 0) {
            height = 300;
        }

        Uri inputUri = photoUri;
        if (bitmapIntent != null && bitmapIntent.getData() != null) {
            inputUri = bitmapIntent.getData();
        } else {
            inputUri = photoUri;
        }

        //广播刷新相册
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(inputUri);
        act.sendBroadcast(intentBc);

        Intent intent = new Intent("com.android.camera.action.CROP"); //剪裁
        intent.setDataAndType(inputUri, "image/*");
        intent.putExtra("crop", false);
        intent.putExtra("scale", true);
        //设置宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //设置裁剪图片宽高
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        act.startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO); //设置裁剪参数显示图片至ImageView
    }

    /**
     * 处理拍照/打开相册后进入裁剪工作
     *
     * @param requestCode 连接
     * @param resultCode
     * @param data
     * @param act
     * @param inputPath
     * @param outputPath
     * @return
     */
    public static boolean onActivityResult(int requestCode, int resultCode, Intent data, Activity act, String inputPath, String outputPath) {
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO:
            case REQUEST_CODE_OPEN_GRALLY:
                //当启动使用的activity不存在或者是拍照/打开相册失败时(resultCode不为RESULT_OK),不处理
                if (act == null || resultCode != Activity.RESULT_OK) {
                    return false;
                }
                //若当前为拍照回调,尝试读取图片的旋转角度
                int degree = requestCode == REQUEST_CODE_TAKE_PHOTO ? readPictureDegree(inputPath) : 0;
                //获取intent中返回的图片路径(如果是打开相册部分机型可能将图片路径存放在intent中返回)
                String tempPath = getBmpPathFromContent(act, data);
                //若intent路径存在,使用该路径,否则使用inputPath
                inputPath = TextUtils.isEmpty(tempPath) ? inputPath : tempPath;

                //源图片路径或者输出路径为无效时,不处理
                if (TextUtils.isEmpty(inputPath) || TextUtils.isEmpty(outputPath)) {
                    return false;
                } else {
                    Intent cropIntent = new Intent(act, CropBitmapActivity.class);
                    cropIntent.putExtra("inputPath", inputPath);
                    cropIntent.putExtra("outputPath", outputPath);
                    cropIntent.putExtra("degree", degree);
                    //启动裁剪工具
                    act.startActivityForResult(cropIntent, REQUEST_CODE_CROP_PHOTO);
                    return true;
                }
            default:
                return false;
        }
    }

}
