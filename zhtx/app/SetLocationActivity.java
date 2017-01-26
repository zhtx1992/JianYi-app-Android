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
	// ��������������ʵ��  
    private GeoCoder geoCoder;
    //����volley����
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
        //��ȡ��ͼ�ؼ�����  
        mMapView = (MapView) findViewById(R.id.bmapView);  
        locationtv=(TextView) findViewById(R.id.locationtv);
        btnconfirm=(Button) findViewById(R.id.btnconfirm);
        btntolist=(Button) findViewById(R.id.btntolist);
        i=getIntent();
        //���volley�������
        mQueue= Volley.newRequestQueue(SetLocationActivity.this);
        mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
        mLocationClient.registerLocationListener( myListener );    //ע���������   
        initLocation();
        mLocationClient.start();  
		
    }  
	
	OnMapLongClickListener mListener=new OnMapLongClickListener(){

		@Override
		public void onMapLongClick(LatLng point) {
			latitude=point.latitude;
			longitude=point.longitude;
	        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {  
	        // ����������ѯ����ص�����  
	        @Override  
	        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {  
	            if (result == null  
	               || result.error != SearchResult.ERRORNO.NO_ERROR) {  
	               // û�м�⵽���  
	               Toast.makeText(getApplicationContext(), "��Ǹ��δ���ҵ����", Toast.LENGTH_SHORT).show();  
	                }else { 
                   locationtv.setText(result.getAddress()); 
	            }

	        }

			@Override
			public void onGetGeoCodeResult(GeoCodeResult arg0) {
				// TODO Auto-generated method stub
				
			}
		    };
	        // ���õ���������������  
	        geoCoder.setOnGetGeoCodeResultListener(listener);   
	        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(point));  
	         
		}

	};
	
	public void confirm(View v){
		if (!latitude.equals((double)0)){
			AlertDialog.Builder b=new Builder(SetLocationActivity.this);
			b.setTitle("��ʾ");
			b.setMessage("����ǩ��ʱ��");
			tp=new TimePicker(b.getContext());
			tp.setIs24HourView(true);
			b.setView(tp);
			b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
            
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
						    	Toast.makeText(getApplicationContext(), "�ύǩ����ɹ�",Toast.LENGTH_SHORT).show(); 
						    	finish();
						    }else{
								Toast.makeText(getApplicationContext(),"�ύʧ��",Toast.LENGTH_SHORT).show();       
								
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
					progressDialog = ProgressDialog.show(SetLocationActivity.this, "���Ե�...", "������...", true);
					mQueue.add(insertTask.getRequest());
					mQueue.start();
				}
				
			});
			b.setNegativeButton("ȡ��", new  DialogInterface.OnClickListener(){
				
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
    //��ʼ����λ����
  	private void initLocation(){
          LocationClientOption option = new LocationClientOption();
          option.setLocationMode(LocationMode.Hight_Accuracy);//��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
          option.setCoorType("bd09ll");//��ѡ��Ĭ��gcj02�����÷��صĶ�λ�������ϵ
          option.setScanSpan(0);//��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
          option.setIsNeedAddress(true);//��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
          option.setOpenGps(true);//��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
          option.setLocationNotify(false);//��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
          option.setIsNeedLocationDescribe(true);//��ѡ��Ĭ��false�������Ƿ���Ҫλ�����廯�����������BDLocation.getLocationDescribe��õ�����������ڡ��ڱ����찲�Ÿ�����
          option.setIsNeedLocationPoiList(false);//��ѡ��Ĭ��false�������Ƿ���ҪPOI�����������BDLocation.getPoiList��õ�
          option.setIgnoreKillProcess(false);//��ѡ��Ĭ��false����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ��ɱ��
          option.SetIgnoreCacheException(false);//��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
          option.setEnableSimulateGps(false);//��ѡ��Ĭ��false�������Ƿ���Ҫ����gps��������Ĭ����Ҫ
          mLocationClient.setLocOption(option);
      }
  	
  	//��λ���������
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
        //��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onDestroy();  
        // �ͷŵ���������ʵ��  
        geoCoder.destroy();
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onResume();  
        }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���  
        mMapView.onPause();  
          
    }
    
    private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			//���õ�ͼ����������
	        mBaiduMap = mMapView.getMap();
	        mBaiduMap.setOnMapLongClickListener(mListener);
	        geoCoder = GeoCoder.newInstance();
	        LatLng cenpt = new LatLng(latitude,longitude); 
	        //�����ͼ״̬
	        MapStatus mMapStatus = new MapStatus.Builder()
	       .target(cenpt)
	       .zoom(18)
	       .build();
	       //����MapStatusUpdate�����Ա�������ͼ״̬��Ҫ�����ı�
	       MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
	       //�ı��ͼ״̬
	       mBaiduMap.setMapStatus(mMapStatusUpdate);
		}
	};
}