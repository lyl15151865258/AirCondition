package com.zhongbenshuo.air.activity;

import android.content.Context;
import android.os.Bundle;

import com.zhongbenshuo.air.R;
import com.zhongbenshuo.air.bean.RSAResult;
import com.zhongbenshuo.air.bean.Result;
import com.zhongbenshuo.air.contentprovider.SPHelper;
import com.zhongbenshuo.air.network.ExceptionHandle;
import com.zhongbenshuo.air.network.NetClient;
import com.zhongbenshuo.air.network.NetworkObserver;
import com.zhongbenshuo.air.utils.ActivityController;
import com.zhongbenshuo.air.utils.GsonUtils;
import com.zhongbenshuo.air.utils.LogUtils;
import com.zhongbenshuo.air.utils.NetworkUtil;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Logo页面
 * Created at 2019/9/24 18:21
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LogoActivity extends BaseActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        mContext = this;
        getRSAPublicKey();
    }

    /**
     * 获取服务器的RSA公钥（不需要加解密）
     */
    private void getRSAPublicKey() {
        Observable<Result> observable = NetClient.getInstance(NetClient.getBaseUrlProject(),false, false).getZbsApi().getRSAPublicKey();
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkObserver<Result>(this) {

            @Override
            public void onSubscribe(Disposable d) {
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast("网络不可用");
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                openActivity(MainActivity.class);
                ActivityController.finishActivity(LogoActivity.this);
            }

            @Override
            public void onNext(Result result) {
                RSAResult rsaResult = GsonUtils.parseJSON(GsonUtils.convertJSON(result.getData()), RSAResult.class);
                LogUtils.d(TAG, "服务器的RSA公钥是：" + rsaResult.getRsaPublicKey());
                SPHelper.save("serverPublicKey", rsaResult.getRsaPublicKey());
                openActivity(MainActivity.class);
                ActivityController.finishActivity(LogoActivity.this);
            }
        });
    }
}
