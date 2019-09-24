package com.zhongbenshuo.air.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.zhongbenshuo.air.R;
import com.zhongbenshuo.air.utils.NetworkUtil;
import com.zhongbenshuo.air.utils.RegexUtils;

/**
 * 通用HTML页面
 * Created at 2018/11/20 13:37
 *
 * @author LiYuliang
 * @version 1.0
 */

public class HtmlActivity extends AppCompatActivity {

    private Context mContext;
    private WebView webViewProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html);
        mContext = this;
        String url = "http://106.12.55.177:80/ZBSAttendance/echartsCondition.jsp";
        webViewProtocol = findViewById(R.id.webView_protocol);
        initSettings();
        loadProtocol(RegexUtils.formatUrl(url));
    }

    /**
     * 加载页面配置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initSettings() {
        com.tencent.smtt.sdk.WebSettings settings = webViewProtocol.getSettings();
        settings.setJavaScriptEnabled(true);
        // 设置缓存模式
        if (NetworkUtil.isNetworkAvailable(mContext)) {
            settings.setCacheMode(com.tencent.smtt.sdk.WebSettings.LOAD_DEFAULT);
        } else {
            settings.setCacheMode(com.tencent.smtt.sdk.WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        // 提高渲染的优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 支持多窗口
        settings.setSupportMultipleWindows(true);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启 Application Caches 功能
        settings.setAppCacheEnabled(true);
    }

    private void loadProtocol(String url) {
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webViewProtocol.loadUrl(url);
        webViewProtocol.setWebViewClient(new WebViewClient() {

            private String startUrl;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                startUrl = url;
            }

            public boolean shouldOverrideUrlLoading(com.tencent.smtt.sdk.WebView view, String url) {
                // return true;  webview处理url是根据程序来执行的
                // return false; webview处理url是在webview内部执行
                // 如果是重定向的呢，我们就return false,不是重定向就return true
                if (startUrl != null && startUrl.equals(url)) {
                    view.loadUrl(url);
                } else {
                    //交给系统处理
                    return super.shouldOverrideUrlLoading(view, url);
                }
                return true;
            }

            @SuppressLint("NewApi")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.startsWith("http") || url.startsWith("https")) {
                    return super.shouldInterceptRequest(view, url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
//                    view.loadUrl(url);
                    return null;
                }
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
            }
        });

        //监听网页加载
        webViewProtocol.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成

                } else {
                    // 加载中

                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webViewProtocol.canGoBack()) {
            webViewProtocol.goBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}