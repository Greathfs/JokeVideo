package com.hfs.jokevideo;

import android.app.Application;

import com.hfs.libnetwork.ApiService;

/**
 * 项目在线Api文档地址：http://123.56.232.18:8080/serverdemo/swagger-ui.html#/
 */
public class JokeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApiService.init("http://123.56.232.18:8080/serverdemo", null);
    }
}
