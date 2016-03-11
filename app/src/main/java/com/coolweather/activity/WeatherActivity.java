package com.coolweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.R;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

public class WeatherActivity extends Activity {

    private LinearLayout weatherInforLayout;
    private TextView cityNameText;//显示城市名
    private TextView publishText;//显示发布时间
    private TextView weatherDespText;//显示天气描述信息
    private TextView temp1Text;//显示气温1
    private TextView temp2Text;//显示气温2
    private TextView currentDateText;//显示当前日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInforLayout= (LinearLayout) findViewById(R.id.weather_info_layout);
        publishText= (TextView) findViewById(R.id.publish_text);
        weatherDespText= (TextView) findViewById(R.id.weather_desp);
        cityNameText= (TextView) findViewById(R.id.city_name);
        temp1Text= (TextView) findViewById(R.id.temp1);
        temp2Text= (TextView) findViewById(R.id.temp2);
        currentDateText= (TextView) findViewById(R.id.current_date);

        String countyCode=getIntent().getStringExtra("county_code");

        Log.e(ChooseAreaActivity.TAG, "weatheractivity收到的县级代号：" + countyCode);

        if (!TextUtils.isEmpty(countyCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInforLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);

            Log.e(ChooseAreaActivity.TAG, "准备进入查询天气代号");

            queryWeatherCode(countyCode);
        }else {
            //没有县级代号时就直接显示本地天气
            showWeather();
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     */
    private void queryWeatherCode(String countryCode){
        String address="http://www.weather.com.cn/data/list3/city"+countryCode+".xml";

        Log.e(ChooseAreaActivity.TAG, "查询县级天气的天气代号网址" +address);

        queryFromServer(address,"countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode){
        String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";

        Log.e(ChooseAreaActivity.TAG, "查询天气的网址" +address);

        queryFromServer(address,"weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address,final String type){

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {

                Log.e(ChooseAreaActivity.TAG, "回调的参数response" +response);
                Log.e(ChooseAreaActivity.TAG, "type是什么" +type);

                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据中解析出天气代号

                        Log.e(ChooseAreaActivity.TAG, "返回的县级天气代号信息：" +response);

                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理服务器返回的天气信息

                    Log.e(ChooseAreaActivity.TAG, "返回的天气信息：" +response);

                    Utility.handleWeatherResponse(WeatherActivity.this, response);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather(){

        Log.e(ChooseAreaActivity.TAG, "显示信息");

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(preferences.getString("city_name",""));
        temp1Text.setText(preferences.getString("temp1",""));
        temp2Text.setText(preferences.getString("temp2",""));
        weatherDespText.setText(preferences.getString("weather_desp",""));
        publishText.setText("今天"+preferences.getString("publish_time","")+"发布");
        currentDateText.setText(preferences.getString("current_date",""));

        weatherInforLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }
}
