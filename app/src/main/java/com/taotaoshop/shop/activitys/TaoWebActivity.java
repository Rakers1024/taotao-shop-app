package com.taotaoshop.shop.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.just.agentweb.core.AgentWeb;
import com.just.agentweb.core.client.DefaultWebClient;
import com.just.agentweb.core.web.AgentWebConfig;
import com.taotaoshop.shop.R;
import com.taotaoshop.shop.StaticData;
import com.taotaoshop.shop.utils.AppInfo;
import com.taotaoshop.shop.utils.Util;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TaoWebActivity extends AppCompatActivity implements StaticData {

    protected AgentWeb mAgentWeb;
    SwipeRefreshLayout swipeRefreshLayout;

    float preX, preY;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_agentweb);
        ininView();
    }

    /**
     * 初始化权限
     */
    public void initPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(TaoWebActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
//            return false;
        }
    }

    /**
     * 检查更新
     */
    public void checkVesion(){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(SERVER_URL+"getAppInfo")
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                AppInfo.Terror(getApplicationContext(), "版本校验失败! 请检查网络配置!");
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String body = Objects.requireNonNull(response.body()).string();
                try {
                    final JSONObject json = new JSONObject(body);
                    if(json.getLong("version") > Util.getAppVersionCode(getApplicationContext())) {
                        Looper.prepare();
                        new MaterialDialog.Builder(TaoWebActivity.this)
                                .iconRes(R.mipmap.icon_tip)
                                .title("更新通知")
                                .content(json.getString("message"))
                                .positiveText("点击下载")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Intent intent= new Intent();
                                        intent.setAction("android.intent.action.VIEW");
                                        Uri content_url = null;
                                        try {
                                            content_url = Uri.parse(json.getString("downloadUrl"));
                                            intent.setData(content_url);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                })
                                .cancelable(false)
                                .show();
                        Looper.loop();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 初始化视图层
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void ininView(){
        //检测版本
        checkVesion();

        //初始化权限
        initPermission();
        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(0xff0099cc, 0xffff4444, 0xff669900, 0xffaa66cc, 0xffff8800);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webRefresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent((ViewGroup) findViewById(R.id.webLinearLayout), -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                .useDefaultIndicator(0, 0)
                .setWebViewClient(mWebViewClient)
                .setWebChromeClient(mWebChromeClient)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)
                .interceptUnkownUrl()
                .createAgentWeb()
                .ready()//设置 WebSettings。
                .go(SERVER_URL);
        AgentWebConfig.debug();
        // AgentWeb 没有把WebView的功能全面覆盖 ，所以某些设置 AgentWeb 没有提供，请从WebView方面入手设置。
        mAgentWeb.getWebCreator().getWebView().setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        mAgentWeb.getWebCreator().getWebView().setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i("test", scrollX+" "+scrollY+" "+oldScrollX+" "+oldScrollY);
                if(scrollY < 0){
                    swipeRefreshLayout.setEnabled(true);
                }else{
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        mAgentWeb.getWebCreator().getWebView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        preX = event.getX();
                        preY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(event.getY() - preY > 1000)
                            swipeRefreshLayout.setEnabled(true);
                        else
                            swipeRefreshLayout.setEnabled(false);
                        break;
                }
                return false;
            }
        });
    }


    /**
     * 和浏览器相关，包括和JS的交互
     */
    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            //网页加载进度
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };


    /**
     * 和网页url加载相关，统计加载时间
     */
    protected WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            return shouldOverrideUrlLoading(view, request.getUrl() + "");
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        /**
         * 拦截
         * @return true就是拦截
         */
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    };

    /**
     * 刷新网页
     */
    public void webRefresh() {
        if (mAgentWeb != null) {
            mAgentWeb.getUrlLoader().reload(); // 刷新
        }
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    /**
     * 设置返回为web回退
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mAgentWeb.back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
