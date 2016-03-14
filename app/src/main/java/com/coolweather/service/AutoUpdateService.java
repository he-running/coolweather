package com.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.coolweather.receiver.AutoUpdateReceiver;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                updateWeather();

            }
        }).start();

        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=3*60*60*1000;//3小时更新一次
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateReceiver.class);//从现在开始，3小时候后通知广播准备更新天气信息
        PendingIntent pi=PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
 private void  updateWeather(){

     SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
     String weatherCode=preferences.getString("weather_code", "");
     String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
     HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
         @Override
         public void onFinish(String response) {
             Utility.handleWeatherResponse(AutoUpdateService.this,response);
         }

         @Override
         public void onError(Exception e) {
             e.printStackTrace();
         }
     });

 }















}
