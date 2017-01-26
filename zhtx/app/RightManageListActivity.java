package com.zhtx.app;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.SignLocation;
import com.zhtx.myclass.Staff;
import com.zhtx.myclass.StepRight;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class RightManageListActivity extends AppCompatActivity {
	private ListView rightmanagelv;
	private List<String> stepList;
	private List<StepRight> stepRightList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rightmanagelist);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("���̲���");
        rightmanagelv=(ListView)findViewById(R.id.rightmanagelv);
        
        mQueue= Volley.newRequestQueue(RightManageListActivity.this);
        stepList =new ArrayList<String>();
        stepRightList=new ArrayList<StepRight>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,stepList);
        rightmanagelv.setAdapter(mAdapter);
        
        i=getIntent();
        
        
    }
	
	
	//��ҳ�����»ص�ǰ̨ʱ�����������б�
    @Override  
    protected void onResume() {  
        super.onResume(); 
        mQueue.start();
        stepRightList.clear();
        stepList.clear();

        getdata();
    }

    //�õ���ǰ��˾��ȫ��Ա������Ϣ����ֹ����Ȩ�ޱ������ѱ�ɾ����Ա�������
    /*
    private void getAllStaff(){
    	String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action", "allinfo");
    	map.put("db",i.getStringExtra("db"));
    	//���ڷ��������ؽ���Ĵ���
    	Response.Listener<String> listener=new Response.Listener<String>(){
    		@Override
    		public void onResponse(String response) {
    			progressDialog.dismiss();
    			if ((response!=null)&&(!response.equals("0"))){
    				allStaff=response;
    			   
   					task2=true;
   					if (task1) {
   						mhandler.sendEmptyMessage(0);
   					}
    			}else{
    			    Toast.makeText(getApplicationContext(), "��ȡ����ʧ��",Toast.LENGTH_SHORT).show();	
    			    finish();
    			} 				
    		}
    	};
    	Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
                finish();
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
    	mQueue.add(infoTask.getRequest());	
    }
    */
	private void getdata() {
		//����������ҳ��url
		String url="http://"+Constant.ip+"/ZhtxServer/StepRightServlet";
		//��post������װ��Ҫ���͵���Ϣ(��˾id)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "allinfo");
		map.put("db",i.getStringExtra("db"));
		map.put("tab", i.getStringExtra("table"));
		//���ڷ��������ؽ���Ĵ���
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
					Type type=new TypeToken<List<StepRight>>(){}.getType();
					stepRightList=gson.fromJson(response, type);
					int no=0;
					for (StepRight s:stepRightList){
						no++;
						String des=String.valueOf(no)+"."+s.getDescribe();
						stepList.add(des);
					}
					if (stepList.size()==0){
						Toast.makeText(getApplicationContext(), "��ǰû����Ϣ",Toast.LENGTH_SHORT).show();
					}else{
					    mAdapter.notifyDataSetChanged();
					    rightmanagelv.setOnItemClickListener(new ItemClickEvent());
					}
			    }else{		    	
			    	Toast.makeText(getApplicationContext(), "��ȡ����ʧ��",Toast.LENGTH_SHORT).show();	
			    	finish();
			    }				
			}
		};
		Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),R.string.volley_error2,Toast.LENGTH_LONG).show();
                finish();
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);	
        progressDialog = ProgressDialog.show(RightManageListActivity.this, "���Ե�...", "��ȡ������...", true);
		mQueue.add(infoTask.getRequest());	
	} 
	
	  
	private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
				StepRight sr=stepRightList.get(arg2);
				Intent intent=new Intent(RightManageListActivity.this,RightManageStaffList.class);
				intent.putExtra("stepid", String.valueOf(sr.getId()));
				intent.putExtra("staff", sr.getStaff());
				intent.putExtra("db", i.getStringExtra("db"));
				intent.putExtra("table", i.getStringExtra("table"));
				startActivity(intent);	
			
	    }
    }
}