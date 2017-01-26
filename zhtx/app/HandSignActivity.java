package com.zhtx.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.zhtx.app.adapter.MenuListAdapter;
import com.zhtx.app.require.RequireListActivity;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.MenuItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HandSignActivity extends AppCompatActivity {
	private Intent i;
	private ListView menulv;
 	private List<MenuItem> menuList;
 	private MenuListAdapter mAdapter;
 	private RequestQueue mQueue;
 	private ProgressDialog progressDialog = null;
    private Toolbar mToolbar;
    public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
    private double latitude,longitude;
    
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_list);
        
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("考勤");
        
        menulv=(ListView)findViewById(R.id.menulv);
        menuList=new ArrayList<MenuItem>();
        mAdapter=new MenuListAdapter(this,menuList);
        menulv.setAdapter(mAdapter);   
        mQueue= Volley.newRequestQueue(HandSignActivity.this);
        mQueue.start();
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数   
        i=getIntent();
        setDate();
        initLocation();
    }
	
	protected void onResume(){
		super.onResume();		
	}
    private void setDate() {
    	menuList.clear();
    	MenuItem mi;
    	if (NavigationActivity.workList.get(0)){
    		mi=new MenuItem(R.drawable.ic_qiandao,"签到","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.user.getId()==NavigationActivity.com.getSigndba_id()){
    		mi=new MenuItem(R.drawable.ic_kqsp,"设置考勤状态","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_ssgl,"考勤人员管理","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_qddgl,"考勤点管理","0");
    		menuList.add(mi);
    	}
		mAdapter.notifyDataSetChanged();
		menulv.setOnItemClickListener(new ItemClickEvent());
	}	    	
    
    private class ItemClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			MenuItem m=menuList.get(arg2);
			if (m.getContent().equals("签到")){
	            mLocationClient.start();       	
			}
            if (m.getContent().equals("设置考勤状态")){
            	Intent intent=new Intent(HandSignActivity.this,HandSignWork.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
            if (m.getContent().equals("考勤人员管理")){
            	Intent intent=new Intent(HandSignActivity.this,HandSignItem.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
            if (m.getContent().equals("考勤点管理")){
            	Intent intent=new Intent(HandSignActivity.this,SetLocationActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
        }  
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
              //Toast.makeText(getApplicationContext(), location.getLocationDescribe(), Toast.LENGTH_SHORT).show(); 
              mhandler.sendEmptyMessage(0);
              mLocationClient.stop();
          }
  	}
  	

	//收到定位结果后处理
	private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			String url="http://"+Constant.ip+"/ZhtxServer/AttendenceServlet";
			HashMap<String, String> map=new HashMap<String, String>();
			map.put("action", "sign");
			map.put("db",i.getStringExtra("db"));
			map.put("name", i.getStringExtra("name")); 
			map.put("lo", String.valueOf(longitude));
			map.put("la", String.valueOf(latitude));
			//对于服务器返回结果的处理
			Response.Listener<String> listener=new Response.Listener<String>(){

				@Override
				public void onResponse(String response) {
					progressDialog.dismiss();
				    if (response.equals("0")) {
					    Toast.makeText(getApplicationContext(), "操作失败", Toast.LENGTH_LONG).show(); 
				    }
				    if (response.equals("1")) {
				    	Toast.makeText(getApplicationContext(), "不是可签到时间", Toast.LENGTH_LONG).show(); 

				    }
				    if (response.equals("3")) {
				    	Toast.makeText(getApplicationContext(), "您不在签到点范围内", Toast.LENGTH_LONG).show();
				    }
				    if (response.equals("4")){
				    	Toast.makeText(getApplicationContext(), "今日签到成功", Toast.LENGTH_LONG).show();
				    }
				    if (response.equals("5")){
				    	Toast.makeText(getApplicationContext(), "只有未考勤状态才能签到", Toast.LENGTH_LONG).show();
				    }   
				}
			};
			Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
	            }  
	        };
		    VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);
			mQueue.add(deleteTask.getRequest());
			progressDialog = ProgressDialog.show(HandSignActivity.this, "请稍等...", "签到中...", true);
		}
	};
}
