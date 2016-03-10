package com.coolweather.util;

import android.util.Log;

import com.coolweather.activity.ChooseAreaActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/3/8.
 */
public class HttpUtil {

    public static void sendHttpRequest(final String address,final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                Log.e(ChooseAreaActivity.TAG,"准备回调");

                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();//根据网址获得数据输入流
                    InputStreamReader inputStreamReader = new InputStreamReader(in);//读取输入流的字符，并按指定的格式返回
                    BufferedReader reader = new BufferedReader(inputStreamReader);//把数据读到缓冲区里
                    StringBuilder response = new StringBuilder();//新建一个StringBuilder类对象，用于存储字符串
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener!=null){
                        //回调onFinish()方法
                        Log.e(ChooseAreaActivity.TAG,"回调方法");
                        listener.onFinish(response.toString());
                    }

                } catch (Exception e) {
                    if (listener!=null){
                        //回调onError方法
                        Log.e(ChooseAreaActivity.TAG,"回调方法");
                        listener.onError(e);
                    }
                } finally {
                    if (connection!=null){
                        connection.disconnect();
                    }
                }

            }
        }).start();
    }

}
