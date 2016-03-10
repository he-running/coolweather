package com.coolweather.util;

/**
 * Created by Administrator on 2016/3/8.
 */
public interface HttpCallbackListener {


    void onFinish(String response);
    void onError(Exception e);


}
