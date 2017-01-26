package com.zhtx.app.require;

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
import com.zhtx.app.R;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Driver;
import com.zhtx.myclass.Platenum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

//用于简单对象的列表，列表只显示该对象的一个属性，是一个字符串列表.

public class RequireSimpleObjectList extends AppCompatActivity {
	private EditText searchet;
	private ListView productlv;
	//用于55号控件的list
	private List<Platenum> plateList,plateFilteredList;
	private List<String> sList,sFilteredList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_requireproductlist);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        productlv=(ListView)findViewById(R.id.productlv);
        searchet=(EditText)findViewById(R.id.searchet);
        i=getIntent();
        getSupportActionBar().setTitle("点击选择");
        searchet.setHint("输入检索内容");          
        mQueue= Volley.newRequestQueue(RequireSimpleObjectList.this);
        mQueue.start();

        initList();
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,sFilteredList);	
		productlv.setAdapter(mAdapter); 
        setSearch();
        
        
        getdata();
	}
    
	//初始化要用的List
	private void initList(){
		switch (i.getStringExtra("action")) {
		 case "act55":{
			 plateList=new ArrayList<Platenum>();
			 plateFilteredList=new ArrayList<Platenum>();
		     sList=new ArrayList<String>();
		     sFilteredList=new ArrayList<String>();    
		     break;
		 }
		 }  	
	}
	//设置检索方法
	private void setSearch(){
		searchet.addTextChangedListener(new TextWatcher() { 
			@Override  
			public void onTextChanged(CharSequence s, int start, int before, int count){
				sFilteredList.clear();
				plateFilteredList.clear();
				if (s.toString().length() != 0) {
					int index=0;
					for (String p:sList){
						if (p.indexOf(s.toString())!=-1){
	  						sFilteredList.add(p);
	  						switch (i.getStringExtra("action")) {
	  						case "act55":{
	  							 plateFilteredList.add(plateList.get(index));
	  							 break;
	  						}
	  						}  	
	  					}
						index++;
					}		    
				}else {    
					sFilteredList=sList;
					plateFilteredList=plateList;
				}  
				mAdapter.notifyDataSetChanged();
			}  
			@Override  	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {	
			}  
			@Override  
			public void afterTextChanged(Editable s) {    			
			}
		});
	}
	
    private void getdata(){
    	String url="http://"+Constant.ip+"/ZhtxServerBeta/RequireServlet";
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action", i.getStringExtra("action"));
    	map.put("db",i.getStringExtra("db"));
    	Response.Listener<String> listener=new Response.Listener<String>(){
    		@Override
    		public void onResponse(String response) {
    			progressDialog.dismiss();
    			if ((response!=null)&&(!response.equals("0"))){
    			    Gson gson=new Gson();
    			    switch (i.getStringExtra("action")) {
    				case "act55":{
    					Type type=new TypeToken<List<Platenum>>(){}.getType();
        				plateList=gson.fromJson(response, type);
        				for (Platenum p:plateList){
   							plateFilteredList.add(p);
   							sList.add(p.getPlatenum());
   							sFilteredList.add(p.getPlatenum());
   						}
    				    break;
    				}
    				}  	
    			    mAdapter.notifyDataSetChanged();
					productlv.setOnItemClickListener(new ItemClickEvent());	
   				}
    			else{
    			    Toast.makeText(getApplicationContext(), "获取数据失败",Toast.LENGTH_SHORT).show();	
    			    finish();
    			} 				
    		}
    	};
    	Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
	            }  
	        };
		VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
    	progressDialog = ProgressDialog.show(RequireSimpleObjectList.this, "请稍等...", "获取数据中...", true);
    	mQueue.add(infoTask.getRequest());	
    		
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (i.getStringExtra("action").equals("act55")){
				RequireDetail.platenum=plateFilteredList.get(arg2);
				String url="http://"+Constant.ip+"/ZhtxServerBeta/DriverDaoServlet";
		    	HashMap<String, String> map=new HashMap<String, String>();
		    	map.put("action","info");
		    	map.put("db",i.getStringExtra("db"));
		    	map.put("id",String.valueOf(plateFilteredList.get(arg2).getId()));
		    	Response.Listener<String> listener=new Response.Listener<String>(){
		    		@Override
		    		public void onResponse(String response) {
		    			progressDialog.dismiss();
		    			if ((response!=null)&&(!response.equals("0"))){
		    			    Gson gson=new Gson();
		    			    RequireDetail.driver=gson.fromJson(response, Driver.class);
		    			    finish();
		   				}
		    			else{	
		    			    finish();
		    			} 				
		    		}
		    	};
		    	Response.ErrorListener elistener=new Response.ErrorListener(){  
			            @Override  
			            public void onErrorResponse(VolleyError error) {  
			                progressDialog.dismiss();
			                RequireDetail.platenum=null;
			                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
			            }  
			        };
				VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
		    	progressDialog = ProgressDialog.show(RequireSimpleObjectList.this, "请稍等...", "获取数据中...", true);
		    	mQueue.add(infoTask.getRequest());	
			}
			
	    }
    }
    
   
}
