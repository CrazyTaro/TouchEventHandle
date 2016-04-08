package com.henrytaro.ct.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.henrytaro.ct.R;
import com.henrytaro.ct.ui.TestView;

public class TouchEventActivity extends Activity {
    TestView mTestView;
    Button mBtnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_event);
        mTestView = (TestView) findViewById(R.id.view_test);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            int whichDraw = intent.getExtras().getInt("whichDraw");
            mTestView.setWhickDraw(whichDraw);
        }
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestView.rollback();
            }
        });
    }

}
