package com.zhtx.app;

import java.util.HashMap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.Functions;
import com.zhtx.app.util.VolleySRUtil;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;


public class SetLocationActivity extends Activity {
	
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private TextView locationtv;
	private Button btnconfirm,btntolist;
	private Double latitude=(double) 0,longitude=(double) 0;
	// 创建地理编码检索实例  
    private GeoCoder geoCoder;
    //定义volley队列
  	private RequestQueue mQueue;
  	private ProgressDialog progressDialog = null;
  	private Intent i;
  	private TimePicker tp;
  	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
    
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_setlocation);
        //获取地图控件引用  
        mMapView = (MapView) findViewById(R.id.bmapView);  
        locationtv=(TextView) findViewById(R.id.locationtv);
        btnconfirm=(Button) findViewById(R.id.btnconfirm);
        btntolist=(Button) findViewById(R.id.btntolist);
        i=getIntent();
        //获得volley请求队列
        mQueue= Volley.newRequestQueue(SetLocationActivity.this);
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数   
        initLocation();
        mLocationClient.start();  
		
    }  
	
	OnMapLongClickListener mListener=new OnMapLongClickListener(){

		@Override
		public void onMapLongClick(LatLng point) {
			latitude=point.latitude;
			longitude=point.longitude;
	        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {  
	        // 反地理编码查询结果回调函数  
	        @Override  
	        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {  
	            if (result == null  
	               || result.error != SearchResult.ERRORNO.NO_ERROR) {  
	               // 没有检测到结果  
	               Toast.makeText(getApplicationContext(), "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();  
	                }else { 
                   locationtv.setText(result.getAddress()); 
	            }

	        }

			@Override
			public void onGetGeoCodeResult(GeoCodeResult arg0) {
				// TODO Auto-generated method stub
				
			}
		    };
	        // 设置地理编码检索监听者  
	        geoCoder.setOnGetGeoCodeResultListener(listener);   
	        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(point));  
	         
		}

	};
	
	public void confirm(View v){
		if (!latitude.equals((double)0)){
			AlertDialog.Builder b=new Builder(SetLocationActivity.this);
			b.setTitle("提示");
			b.setMessage("设置签到时间");
			tp=new TimePicker(b.getContext());
			tp.setIs24HourView(true);
			b.setView(tp);
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){
            
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					int currentapiVersion=android.os.Build.VERSION.SDK_INT;
					String hour,minute;
					if (currentapiVersion<23) {
						hour=String.valueOf(tp.getCurrentHour());
						if (hour.length() == 1) hour="0"+hour;
						minute=String.valueOf(tp.getCurrentMinute());
						if (minute.length() == 1) minute="0"+minute;
					}else{
						hour=setHour();
						minute=setMinute();
					    if (hour.length() == 1) hour="0"+hour;
					    if (minute.length() == 1) minute="0"+minute;
					}		
					arg0.dismiss();
					String url="http://"+Constant.ip+"/ZhtxServer/SignLocationServlet";
					HashMap<String, String> map=new HashMap<String, String>();
					map.put("action", "insert");
					map.put("db", i.getStringExtra("db"));
					map.put("la",String.valueOf(latitude));
					map.put("lo",String.valueOf(longitude));
					map.put("de",locationtv.getText().toString());
					map.put("time", hour+minute+"00");
					Response.Listener<String> listener=new Response.Listener<String>(){

						@Override
						public void onResponse(String response) {
							progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	Toast.makeText(getApplicationContext(), "提交签到点成功",Toast.LENGTH_SHORT).show(); 
						    	finish();
						    }else{
								Toast.makeText(getApplicationContext(),"提交失败",Toast.LENGTH_SHORT).show();       
								
							}
							
						}
					};
					Response.ErrorListener elistener=new Response.ErrorListener(){  
			            @Override  
			            public void onErrorResponse(VolleyError error) {  
			                progressDialog.dismiss();
			                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
			            }  
			        };
				    VolleySRUtil insertTask=new VolleySRUtil(url,map,listener,elistener);
					progressDialog = ProgressDialog.show(SetLocationActivity.this, "请稍等...", "处理中...", true);
					mQueue.add(insertTask.getRequest());
					mQueue.start();
				}
				
			});
			b.setNegativeButton("取消", new  DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					arg0.dismiss();
				}
				
			});
			b.create().show();	
		}	
	}
    public void tolist(View v){
    	Intent intent=new Intent(SetLocationActivity.this,SignLocationList.class);
	    intent.putExtra("db", i.getStringExtra("db"));
    	startActivity(intent);	
	}
    @TargetApi(23)
    private String setHour(){
    	return String.valueOf(tp.getHour());
    }
    @TargetApi(23)
	private String setMinute(){
    	return String.valueOf(tp.getMinute());
    }
    //初始化定位参数
  	private void initLocation(){
          LocationClientOption option = new LocationClientOption();
          option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
          option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
          option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
          option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
          option.setOpenGps(true);//可选，默认false,设置是否使用gps
          option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
          option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
          option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
          option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
          option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
          option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
          mLocationClient.setLocOption(option);
      }
  	
  	//定位结果监听器
  	public class MyLocationListener implements BDLocationListener {
  		 
          @Override
          public void onReceiveLocation(BDLocation location) {
              latitude=location.getLatitude();
              longitude=location.getLongitude(); 
              mhandler.sendEmptyMessage(0);
              mLocationClient.stop();
          }
  	}
  	
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();  
        // 释放地理编码检索实例  
        geoCoder.destroy();
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();  
        }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();  
          
    }
    
    private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			//设置地图长按监听器
	        mBaiduMap = mMapView.getMap();
	        mBaiduMap.setOnMapLongClickListener(mListener);
	        geoCoder = GeoCoder.newInstance();
	        LatLng cenpt = new LatLng(latitude,longitude); 
	        //定义地图状态
	        MapStatus mMapStatus = new MapStatus.Builder()
	       .target(cenpt)
	       .zoom(18)
	       .build();
	       //定义MapStatusUpdate对象，以便描述地图状态将要发生的变
	       MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
	       //改变地图状态
	       mBaiduMap.setMapStatus(mMapStatusUpdate);
		}
	};
}