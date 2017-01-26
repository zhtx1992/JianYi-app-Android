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
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SignLocationList extends AppCompatActivity {
	
	private List<SignLocation> slList;
	private List<String> locationList;
	private ListView signlocationlv;
	private BaseAdapter mAdapter;
	//定义volley队列
  	private RequestQueue mQueue;
  	private int now;
  	private Intent i;
  	private SignLocation s;
  	private ProgressDialog progressDialog = null;
  	private Toolbar mToolbar;
  	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationlist);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("当前签到点");
        //初始化变量
        mQueue= Volley.newRequestQueue(SignLocationList.this);
        mQueue.start();
        locationList =new ArrayList<String>();
        slList=new ArrayList<SignLocation>();
        i=getIntent();
        //获取控件
        signlocationlv=(ListView)findViewById(R.id.signlocationlv);
        //设置适配器
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,locationList);
        signlocationlv.setAdapter(mAdapter);
        //向服务器申请数据
        getData();
	}

	private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			s =slList.get(now);
			AlertDialog.Builder b=new Builder(SignLocationList.this);
			b.setTitle("提示");
			b.setMessage("你是否要删除这个签到点");
			b.setPositiveButton("删除", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					//服务器处理页面url
					String url="http://"+Constant.ip+"/ZhtxServer/SignLocationServlet";
					HashMap<String, String> map=new HashMap<String, String>();
					map.put("action", "delete");
					map.put("db",i.getStringExtra("db"));
					map.put("id",String.valueOf(s.getId()));
					//对于服务器返回结果的处理
					Response.Listener<String> listener=new Response.Listener<String>(){

						@Override
						public void onResponse(String response) {
							progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	slList.remove(now);
						    	locationList.remove(now);
						    	mAdapter.notifyDataSetChanged();
						    }else{
						    	Toast.makeText(getApplicationContext(), "操作失败", Toast.LENGTH_SHORT).show();
								       		
							}
							
						}
					};
					Response.ErrorListener elistener=new Response.ErrorListener(){  
			            @Override  
			            public void onErrorResponse(VolleyError error) {  
			                progressDialog.dismiss();
			                Toast.makeText(getApplicationContext(),R.string.volley_error1,Toast.LENGTH_LONG).show();
			            }  
			        };
				    VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);
					progressDialog = ProgressDialog.show(SignLocationList.this, "请稍等...", "获取数据中...", true);
					mQueue.add(deleteTask.getRequest());
					
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
	private void getData() {
		//服务器处理页面url
		String url="http://"+Constant.ip+"/ZhtxServer/SignLocationServlet";
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "info");
		map.put("db",i.getStringExtra("db"));
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
					Type type=new TypeToken<List<SignLocation>>(){}.getType();
					slList=gson.fromJson(response, type);
					int no=0;
					for (SignLocation s:slList){
						no++;
						String time=s.getTime().substring(0,2)+"时"+s.getTime().substring(2,4)+"分";
						String des=String.valueOf(no)+"."+s.getDescribe()+"\n"+time;
						locationList.add(des);
					}
					if (locationList.size()==0){
						Toast.makeText(getApplicationContext(), "当前没有签到点",Toast.LENGTH_SHORT).show();
					}else{
					    mAdapter.notifyDataSetChanged();
					    signlocationlv.setOnItemClickListener(new ItemClickEvent());
					}
			    }else{
			    	Toast.makeText(getApplicationContext(), "获取数据失败",Toast.LENGTH_SHORT).show();			    		
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
		mQueue.add(infoTask.getRequest());
		progressDialog = ProgressDialog.show(SignLocationList.this, "请稍等...", "获取数据中...", true);
		
			
	}
	
}
