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
import com.zhtx.myclass.Staff;

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

public class StaffManageDelete extends AppCompatActivity {
	private ListView stafflv;
	private List<String> staffNameList;
	private List<Staff> staffList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private Staff staff;
	private boolean task1=false,task2=false,task3=false;
	private int now;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanageadd);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("选择员工");
        stafflv=(ListView)findViewById(R.id.stafflv);
        
        mQueue= Volley.newRequestQueue(StaffManageDelete.this);
        mQueue.start();
        staffNameList =new ArrayList<String>();
        staffList=new ArrayList<Staff>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,staffNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        getdata();
	}
    
    private void getdata(){
        
		//服务器处理页面url
		String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
		//用post方法封装需要发送的信息(公司id)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "allinfo");
		map.put("db",i.getStringExtra("db"));
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
			    	Type type=new TypeToken<List<Staff>>(){}.getType();
					staffList=gson.fromJson(response,type);
					int count=0;
					for (Staff st:staffList){
						count++;
						staffNameList.add(String.valueOf(count)+". "+st.getName());
					}	   					   
                    mAdapter.notifyDataSetChanged();
 					stafflv.setOnItemClickListener(new ItemClickEvent());
			    }else{
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
                finish();
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
	    progressDialog = ProgressDialog.show(StaffManageDelete.this, "请稍等...", "获取数据中...", true);
		mQueue.add(infoTask.getRequest());	
    		    	
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			staff=staffList.get(arg2);
			AlertDialog.Builder b=new Builder(StaffManageDelete.this);
			b.setTitle("确定删除这个用户吗？");
			b.setMessage("强烈建议您先删除该员工的所有权限,再删除员工,以免发生不可预料的错误.");
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();						
					progressDialog = ProgressDialog.show(StaffManageDelete.this, "请稍等...", "处理中...", true);
					//第一步删除用户表信息
				  	String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
					map.put("action","delete");
				    map.put("id",String.valueOf(staff.getId())); 
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {				        	
						    if ((response!=null)&&(response.equals("1"))){
						    	task1=true;
						    	if (task2) {
						    		mhandler.sendEmptyMessage(0);
						    	}
						    	
						    }else{
						    	progressDialog.dismiss();
						    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
				    VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);				    
				    mQueue.add(endTask.getRequest());
				    
				    //第二步清楚员工表数据
				    url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
				  	map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","delete");
				    map.put("id",String.valueOf(staff.getId())); 
					listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {				        	
						    if ((response!=null)&&(response.equals("1"))){
						    	task2=true;
						    	if (task1) {
						    		mhandler.sendEmptyMessage(0);
						    	}
						    	
						    }else{
						    	progressDialog.dismiss();
						    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
						    }
					    }				
				    };	
				    elistener=new Response.ErrorListener(){  
			            @Override  
			            public void onErrorResponse(VolleyError error) {  
			                progressDialog.dismiss();
			                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
			            }  
			        };
				    VolleySRUtil dTask=new VolleySRUtil(url,map,listener,elistener);				    
				    mQueue.add(dTask.getRequest());
				    
				    
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
    
    private Handler mhandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			progressDialog.dismiss();
			super.handleMessage(msg);
			staffList.remove(now); 
	    	staffNameList.remove(now);
	    	task1=false;
	    	task2=false;
	    	mAdapter.notifyDataSetChanged();
	    	Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
		}
	};
}
