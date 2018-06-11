package ru.kotrik.bugtracker.VKActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import ru.kotrik.bugtracker.R;

public class ActivityLogin extends AppCompatActivity {

    WebView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.webview_auth);

        login.getSettings().setJavaScriptEnabled(true);
        login.loadUrl("https://oauth.vk.com/authorize?client_id=5994230&display=page&scope=groups,docs,email&response_type=token&v=5.78");
        final Intent intent = new Intent();
        login.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(url.contains("blank.html")){
                    login.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.contains("blank.html")) {
                    String cookies2 = CookieManager.getInstance().getCookie("https://login.vk.com");

                    Map<String, String> fparams = new HashMap<String, String>();
                    String[] params = url.split("#")[1].split("&");
                    for (String i: params) {
                        String[] s = i.split("=");
                        fparams.put(s[0], s[1]);
                    }
                    intent.putExtra("cookies", cookies2);
                    intent.putExtra("token", fparams.get("access_token"));
                    intent.putExtra("user_id", fparams.get("user_id"));


                    setResult(RESULT_OK, intent);
                    finish();

                }
            }

        });
        login.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
