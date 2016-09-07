package com.taro.touchevent.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.taro.touchevent.R;
import com.taro.touchevent.utils.GrallyAndPhotoUtils;

import java.io.InputStream;

/**
 * Created by taro on 16/1/11.
 */
public class CropBitmapActivity extends Activity implements View.OnClickListener {
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
        Intent intent = new Intent(act, CropBitmapActivity.class);
        intent.putExtra("inputPath", srcBitmapPath);
        intent.putExtra("outputPath", outputPath);
        intent.putExtra("degree", degree);
        act.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform_bitmap);
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
        //不存在源图片路径时,加载默认的示例图片资源
        if (mFilePath == null) {
            mPhoto = GrallyAndPhotoUtils.decodeBitmapInScale(getResources(), R.raw.pkq, 720);
        } else {
            mPhoto = GrallyAndPhotoUtils.decodeBitmapInScale(mFilePath, 720);
        }

        //存在旋转角度,对图片进行旋转
        if (degree != 0) {
            //旋转图片
            Bitmap originalBitmap = GrallyAndPhotoUtils.rotatingBitmap(degree, mPhoto);
            //回收旧图片
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
                        Toast.makeText(CropBitmapActivity.this, msg
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
            mPhoto = null;
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
                        if (mCropView.restoreBitmap(mOutputPath, Bitmap.CompressFormat.PNG, true, 50)) {
                            setResult(RESULT_OK);
                            mPhoto.recycle();
                            mPhoto = null;
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
}