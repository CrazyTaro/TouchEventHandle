package com.taro.touchevent.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.taro.touchevent.R;
import com.taro.touchevent.ui.TestView;
import com.taro.touchevent.utils.GrallyAndPhotoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/4/8.
 */
public class MainActivity extends Activity {
    private ListView mLvList;
    private String mInputPath;
    private String mOutputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLvList = (ListView) findViewById(R.id.main_lv);

        String[] itemStrArr = new String[]{"圆形拖动与缩放", "矩形拖动与缩放", "拍照", "选择图片", "示例图片裁剪", "查看示例裁剪图片", "查看拍照/相册裁剪图片"};
        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>(itemStrArr.length);
        for (int i = 0; i < itemStrArr.length; i++) {
            Map<String, String> itemMap = new HashMap<String, String>();
            itemMap.put("key", itemStrArr[i]);
            mapList.add(itemMap);
        }
        ListAdapter adapter = new SimpleAdapter(this, mapList, R.layout.item_main_list, new String[]{"key"}, new int[]{R.id.item_tv});
        mLvList.setAdapter(adapter);
        mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent activityIntent = null;
                switch (position) {
                    //圆形拖动与缩放示例
                    case 0:
                        activityIntent = new Intent(MainActivity.this, TouchEventActivity.class);
                        activityIntent.putExtra("whichDraw", TestView.CIRCLE_DRAW);
                        startActivity(activityIntent);
                        break;
                    //矩形拖动与缩放示例
                    case 1:
                        activityIntent = new Intent(MainActivity.this, TouchEventActivity.class);
                        activityIntent.putExtra("whichDraw", TestView.RECTANGLE_DRAW);
                        startActivity(activityIntent);
                        break;
                    //拍照
                    case 2:
                        String photoFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/photoBmp.png";
                        mInputPath = photoFile;
                        GrallyAndPhotoUtils.openCamera(MainActivity.this, photoFile);
                        break;
                    //相册选择图片
                    case 3:
                        GrallyAndPhotoUtils.openGrally(MainActivity.this);
                        break;
                    //示例图片裁剪
                    case 4:
                        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/transformBitmap.png";
                        CropBitmapActivity.startThisActivitySelf(MainActivity.this, null, outputPath, 0);
                        break;
                    //查看示例裁剪的图片
                    case 5:
                        String cropBmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/transformBitmap.png";
                        File bmpFile = new File(cropBmpPath);
                        if (bmpFile.exists()) {
                            Intent bmpOpenIntent = getImageFileIntent(cropBmpPath);
                            startActivity(bmpOpenIntent);
                        } else {
                            Toast.makeText(MainActivity.this, "没有裁剪图片,请裁剪后查看", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    //查看拍照/相册裁剪后的图片
                    case 6:
                        String outputBmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/outputBmp.png";
                        File outputBmpFile = new File(outputBmpPath);
                        if (outputBmpFile.exists()) {
                            Intent bmpOpenIntent = getImageFileIntent(outputBmpPath);
                            startActivity(bmpOpenIntent);
                        } else {
                            Toast.makeText(MainActivity.this, "没有裁剪图片,请裁剪后查看", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mOutputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/outputBmp.png";
        GrallyAndPhotoUtils.onActivityResult(requestCode, resultCode, data, this, mInputPath, mOutputPath);
    }

    //android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }
}
