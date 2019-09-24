package com.zhongbenshuo.air;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;
import com.zhongbenshuo.air.utils.CrashHandler;
import com.zhongbenshuo.air.utils.LogUtils;

/**
 * Application类
 * Created by Li Yuliang on 2019/03/26.
 *
 * @author LiYuliang
 * @version 2019/03/26
 */

public class AirApplication extends Application {

    private static AirApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 捕捉异常
        CrashHandler.getInstance().init(this);
        //初始化腾讯X5浏览器内核
        initX5WebView();
    }

    /**
     * 单例模式中获取唯一的MyApplication实例
     *
     * @return application实例
     */
    public static AirApplication getInstance() {
        if (instance == null) {
            instance = new AirApplication();
        }
        return instance;
    }

    /**
     * 初始化腾讯X5内核
     */
    private void initX5WebView() {
        //搜集本地TBS内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                LogUtils.d("X5WebView", "onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {

            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

}
