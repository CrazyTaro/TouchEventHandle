package com.taro.touchevent.launch;

/**
 * Created by taro on 16/7/5.
 */
public class SingleInstanceActivity extends BaseActivity {
    @Override
    public String getLaunchMode() {
        return BaseActivity.ACTION_SINGLE_INSTANCE;
    }
}
