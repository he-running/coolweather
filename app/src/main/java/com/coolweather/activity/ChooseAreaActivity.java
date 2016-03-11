package com.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.R;
import com.coolweather.model.City;
import com.coolweather.model.CoolWeatherDB;
import com.coolweather.model.County;
import com.coolweather.model.Province;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    public static final String TAG="Cool ! cool";

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    //省、市、县列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    //选中的省市
    private Province selectedProvince;
    private City selectedCity;


    //当前选中的级别
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //开机先执行这一段，先查看是否已存在天气情况
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("city_selected",false)){

            Log.e(TAG,"进入WeatherActivity！");
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Log.e(TAG,"还没进入WeatherActivity！");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        coolWeatherDB = CoolWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {

                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(index);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(index);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){

                    Log.e(TAG,"进入else if县级");
                    String countyCode=countyList.get(index).getCountyCode();
                    Intent intent =new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);

                    Log.e(TAG, ""+countyCode);

                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();//加载省级数据

    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器查询
     */
    private void queryProvinces() {

        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询选中省的所有市，优先从数据库查询，如果没有查询到再去服务器查询
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 查询选中市的所有县，优先从数据库查询，如果没有查询到再去服务器查询
     */
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());


        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {

            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 根据输入的代号和类型从服务器上查询省市县数据
     */
     private void queryFromServer(final String code, final String type) {

        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";//查询省份信息
        }

        showProgressDialog();

        Log.e(ChooseAreaActivity.TAG, "A类调用B类方法");

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {//此处相当于活动实现了HttpCallbackListener这个接口，并在此实现它的方法

                Log.e(ChooseAreaActivity.TAG, "被调用，实现方法");

                boolean result = false;           //，是属于本活动的，到最后被HttpUtil调用，即onFinish,onError是回调方法
                if ("province".equals(type)) {    //这里用了回调机制，本活动为A类，HttpUtil为B类
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());

                }

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {//回到主线程
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度显示对话框
     */
    private  void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获back按键，根据当前的级别来进行返回上一级页面还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvinces();
        }else {
            finish();
        }
    }
}
