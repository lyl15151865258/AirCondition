package com.zhongbenshuo.air.activity;

import android.os.Bundle;

import com.zhongbenshuo.air.R;

/**
 * Logo页面
 * Created at 2019/9/24 18:21
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LogoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            openActivity(MainActivity.class);
        }
    }
}
