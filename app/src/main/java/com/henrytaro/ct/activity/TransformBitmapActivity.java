package com.henrytaro.ct.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.henrytaro.ct.R;
import com.henrytaro.ct.ui.CropView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xuhaolin on 16/1/11.
 */
public class TransformBitmapActivity extends Activity implements View.OnClickListener {
    private ImageView mIvPhoto;
    private Button mBtnLeftRotate;
    private Button mBtnRightRotate;
    private Button mBtnConfirm;
    private Button mBtnCancel;
    private CropView mCropView;

    Handler mHandler = null;
    ProgressDialog mDialog = null;
    Bitmap mPhoto = null;
    String mFilePath = null;
    String mOutputPath = null;

    /**
     * 启动此Activity
     *
     * @param act
     * @param srcBitmapPath 来源图片的路径
     * @param outputPath    裁剪后输出的图片路径
     * @param degree        图片旋转了的角度
     */
    public static void startThisActivitySelf(Activity act, String srcBitmapPath, String outputPath, int degree) {
        Intent intent = new Intent(act, TransformBitmapActivity.class);
        intent.putExtra("inputPath", srcBitmapPath);
        intent.putExtra("outputPath", outputPath);
        intent.putExtra("degree", degree);
        act.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform_bitmap);
        mIvPhoto = (ImageView) findViewById(R.id.transform_bitmap_iv);
        mBtnLeftRotate = (Button) findViewById(R.id.transform_left_rotate_btn);
        mBtnRightRotate = (Button) findViewById(R.id.transform_right_rotate_btn);
        mBtnConfirm = (Button) findViewById(R.id.transform_confirm_btn);
        mBtnCancel = (Button) findViewById(R.id.transform_cancel_btn);
        mCropView = (CropView) findViewById(R.id.transform_bitmap_cv);

        mBtnLeftRotate.setOnClickListener(this);
        mBtnRightRotate.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        //输入地址
        mFilePath = getIntent().getStringExtra("inputPath");
        //输出地址
        mOutputPath = getIntent().getStringExtra("outputPath");
        int degree = getIntent().getIntExtra("degree", 0);
        InputStream in = null;
        try {
            if (mFilePath == null) {
                AssetManager manager = this.getAssets();
                in = manager.open("pkq.png");
            } else {
                in = new FileInputStream(mFilePath);
            }
        } catch (IOException e) {
            Toast.makeText(this, "无法加载图片", Toast.LENGTH_SHORT).show();
            return;
        }
        mPhoto = decodeBitmapInScale(in, 720);

        //存在旋转角度,对图片进行旋转
        if (degree != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            Bitmap originalBitmap = Bitmap.createBitmap(mPhoto, 0, 0, mPhoto.getWidth(), mPhoto.getHeight(), matrix, true);
            mPhoto.recycle();
            mPhoto = originalBitmap;
        }
        mCropView.setImageBitmap(mPhoto);

        mDialog = new ProgressDialog(this);
        mDialog.setTitle("正在处理图片...");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x1:
                        mDialog.show();
                        break;
                    case 0x2:
                        mDialog.dismiss();
                        finish();
                        break;
                    case 0x3:
                        mDialog.dismiss();
                        break;
                    case 0x4:
                        Toast.makeText(TransformBitmapActivity.this, msg
                                .getData().getString("toast"), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoto != null && !mPhoto.isRecycled()) {
            mPhoto.recycle();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transform_confirm_btn:
                //显示加载对话框
                mHandler.sendEmptyMessage(0x1);
                new Thread() {
                    @Override
                    public void run() {
                        if (mCropView.restoreBitmap(mOutputPath)) {
                            setResult(RESULT_OK);
                            mPhoto.recycle();
                            String toast = "裁剪图片保存到: " + mOutputPath;
                            Bundle data = new Bundle();
                            data.putString("toast", toast);
                            Message msg = Message.obtain();
                            msg.what = 0x4;
                            msg.setData(data);
                            mHandler.sendMessage(msg);
                            mHandler.sendEmptyMessageDelayed(0x2, Toast.LENGTH_LONG);
                        } else {
                            //仅取消对话框
                            mHandler.sendEmptyMessage(0x3);
                        }
                    }
                }.start();
                break;
            case R.id.transform_cancel_btn:
                //取消时需要回收图片资源
                mCropView.recycleBitmap();
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    /**
     * 加载缩放后的图片
     *
     * @param in        图片流数据
     * @param largeSize 图片最大边的长度
     * @return 返回缩放加载后的图片, 但图片的长度并不一定是最大边长度的,只是近似这个值
     */
    public static Bitmap decodeBitmapInScale(InputStream in, int largeSize) {
        if (in == null || largeSize <= 0) {
            return null;
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            //图片原始宽高
            float width = options.outWidth;
            float height = options.outHeight;
            //缩放比例
            int sampleSize = 1;
            float largeSizeInBmp = width;

            //记录最大的边
            if (width > height) {
                largeSizeInBmp = width;
            } else {
                largeSizeInBmp = height;
            }
            //将最大边与预期的边大小进行比较计算缩放比
            if (largeSizeInBmp < largeSize) {
                //最大边小于预期,则sampleSize为1
                sampleSize = 1;
            } else {
                //最大边大于预期边
                sampleSize = (int) (largeSizeInBmp / largeSize + 0.5);
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
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            options.inMutable = true;
            System.out.println("sampleSize = " + sampleSize + "\nsrcWidth = " + width + "\nsrcHeight = " + height);
            return BitmapFactory.decodeStream(in, null, options);
        }
    }
}