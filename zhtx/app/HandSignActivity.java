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
        getSupportActionBar().setTitle("����");
        
        menulv=(ListView)findViewById(R.id.menulv);
        menuList=new ArrayList<MenuItem>();
        mAdapter=new MenuListAdapter(this,menuList);
        menulv.setAdapter(mAdapter);   
        mQueue= Volley.newRequestQueue(HandSignActivity.this);
        mQueue.start();
        mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
        mLocationClient.registerLocationListener( myListener );    //ע���������   
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
    		mi=new MenuItem(R.drawable.ic_qiandao,"ǩ��","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.user.getId()==NavigationActivity.com.getSigndba_id()){
    		mi=new MenuItem(R.drawable.ic_kqsp,"���ÿ���״̬","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_ssgl,"������Ա����","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_qddgl,"���ڵ����","0");
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
			if (m.getContent().equals("ǩ��")){
	            mLocationClient.start();       	
			}
            if (m.getContent().equals("���ÿ���״̬")){
            	Intent intent=new Intent(HandSignActivity.this,HandSignWork.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
            if (m.getContent().equals("������Ա����")){
            	Intent intent=new Intent(HandSignActivity.this,HandSignItem.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
            if (m.getContent().equals("���ڵ����")){
            	Intent intent=new Intent(HandSignActivity.this,SetLocationActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
        }  
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
              //Toast.makeText(getApplicationContext(), location.getLocationDescribe(), Toast.LENGTH_SHORT).show(); 
              mhandler.sendEmptyMessage(0);
              mLocationClient.stop();
          }
  	}
  	

	//�յ���λ�������
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
			//���ڷ��������ؽ���Ĵ���
			Response.Listener<String> listener=new Response.Listener<String>(){

				@Override
				public void onResponse(String response) {
					progressDialog.dismiss();
				    if (response.equals("0")) {
					    Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_LONG).show(); 
				    }
				    if (response.equals("1")) {
				    	Toast.makeText(getApplicationContext(), "���ǿ�ǩ��ʱ��", Toast.LENGTH_LONG).show(); 

				    }
				    if (response.equals("3")) {
				    	Toast.makeText(getApplicationContext(), "������ǩ���㷶Χ��", Toast.LENGTH_LONG).show();
				    }
				    if (response.equals("4")){
				    	Toast.makeText(getApplicationContext(), "����ǩ���ɹ�", Toast.LENGTH_LONG).show();
				    }
				    if (response.equals("5")){
				    	Toast.makeText(getApplicationContext(), "ֻ��δ����״̬����ǩ��", Toast.LENGTH_LONG).show();
				    }   
				}
			};
			Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
	            }  
	        };
		    VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);
			mQueue.add(deleteTask.getRequest());
			progressDialog = ProgressDialog.show(HandSignActivity.this, "���Ե�...", "ǩ����...", true);
		}
	};
}
