package com.zhtx.app.require;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zhtx.app.R;
import com.zhtx.app.R.id;
import com.zhtx.app.R.layout;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Staff;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RequireReciverList extends AppCompatActivity {
	private ListView stafflv;
	private List<String> staffNameList;
	private List<Staff> staffList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private String staff;
	private int count,now;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanageadd);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("ѡ��Ա��");
        stafflv=(ListView)findViewById(R.id.stafflv);
        
        mQueue= Volley.newRequestQueue(RequireReciverList.this);
        mQueue.start();
        staffNameList =new ArrayList<String>();
        staffList=new ArrayList<Staff>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,staffNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        staff=i.getStringExtra("staff");
        
        getdata();
	}
    
    private void getdata(){
    	//�õ������м���Ա����Ȩ��
    	String s=staff;
    	count=0;
    	while (s.indexOf("+")!=-1){
    		if (s.indexOf("+")==s.length()){
    			s="";
    		}else{
    			s=s.substring(s.indexOf("+")+1);
    		}
    		count++;
    	}
    	if (count==0) {
    		Toast.makeText(getApplicationContext(), "�����д�Ȩ��",Toast.LENGTH_SHORT).show();
    	}else{
        s=staff;
    	//��ȡ����ЩԱ����Ȩ��
        progressDialog = ProgressDialog.show(RequireReciverList.this, "���Ե�...", "��ȡ������...", true);
    	while (s.indexOf("+")!=-1) {
    		int staffid=Integer.parseInt(s.substring(0, s.indexOf("+")));
    		//����������ҳ��url
    		String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    		//��post������װ��Ҫ���͵���Ϣ(��˾id)
    		HashMap<String, String> map=new HashMap<String, String>();
    		map.put("action", "info");
    		map.put("db",i.getStringExtra("db"));
    		map.put("id", String.valueOf(staffid));
    		//���ڷ��������ؽ���Ĵ���
    		Response.Listener<String> listener=new Response.Listener<String>(){

    			@Override
    			public void onResponse(String response) {
    				
    			    if ((response!=null)&&(!response.equals("0"))){
    			    	Gson gson=new Gson();
    					Staff st=gson.fromJson(response, Staff.class);
    					staffList.add(st);
    					staffNameList.add(st.getName());   					   
                        count--;
                        if (count==0){
                        	progressDialog.dismiss();
                        	mAdapter.notifyDataSetChanged();
     					    stafflv.setOnItemClickListener(new ItemClickEvent());
                        }
    			    }else{
    			    	if (response==null){
    			    		Toast.makeText(getApplicationContext(), "��ȡ����ʧ��",Toast.LENGTH_SHORT).show();	
        			    	finish();
    			    	}else{
    			    		count--;
    			    		 if (count==0){
    	                        	progressDialog.dismiss();
    	                        	mAdapter.notifyDataSetChanged();
    	     					    stafflv.setOnItemClickListener(new ItemClickEvent());
    	                        }
    			    	}
    			    	
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
    		
    		mQueue.add(infoTask.getRequest());	
    		if (s.indexOf("+")==s.length()){
    			s="";
    		}else{
    			s=s.substring(s.indexOf("+")+1);
    		}
    	}
    	}
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			RequireDetail.reciver=staffList.get(arg2);
			finish();
		}
    }
}
