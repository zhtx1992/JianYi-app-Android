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
import com.zhtx.myclass.StepRight;

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

public class RightManageStaffList extends AppCompatActivity {
	private ListView stafflv;
	private List<String> staffNameList;
	private List<Staff> staffList,allstaffList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	public static String staff;
	private int count,now;
	private Button btnadd;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanage);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("有权限的员工");
        stafflv=(ListView)findViewById(R.id.stafflv);
        btnadd=(Button)findViewById(R.id.btnadd);
        
        mQueue= Volley.newRequestQueue(RightManageStaffList.this);
        staffNameList =new ArrayList<String>();
        staffList=new ArrayList<Staff>();
        allstaffList=new ArrayList<Staff>();
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,staffNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        staff=i.getStringExtra("staff");
	}
	 
	//去除掉已经不存在于公司中的员工
	/*
    private String removeStaff(String s){
    	Gson gson=new Gson();
		Type type=new TypeToken<List<Staff>>(){}.getType();
		allstaffList=gson.fromJson(i.getStringExtra("allstaff"), type);
		String staff="";
		while (s.indexOf("+")!=-1) {
    		int staffid=Integer.parseInt(s.substring(0, s.indexOf("+")));
    		for (Staff st:allstaffList){
    			if (st.getId()==staffid) {
    				staff+=String.valueOf(staffid)+"+";
    				break;
    			}
    		}
    		if (s.indexOf("+")==s.length()){
    			s="";
    		}else{
    			s=s.substring(s.indexOf("+")+1);
    		}
		}
		return staff;
    }
	*/
    //当页面重新回到前台时，更新审批列表
    @Override  
    protected void onResume() {  
        super.onResume(); 
        mQueue.start();
        staffNameList.clear();
        staffList.clear();
        getdata();
    }
    
    private void getdata(){
    	//得到现在有几个员工有权限
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
    		Toast.makeText(getApplicationContext(), "无人有此权限",Toast.LENGTH_SHORT).show();
    	}else{
        s=staff;
        progressDialog = ProgressDialog.show(RightManageStaffList.this, "请稍等...", "获取数据中...", true);
    	//获取有哪些员工有权限
    	while (s.indexOf("+")!=-1) {
    		int staffid=Integer.parseInt(s.substring(0, s.indexOf("+")));
    		//服务器处理页面url
    		String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    		//用post方法封装需要发送的信息(公司id)
    		HashMap<String, String> map=new HashMap<String, String>();
    		map.put("action", "info");
    		map.put("db",i.getStringExtra("db"));
    		map.put("id", String.valueOf(staffid));
    		//对于服务器返回结果的处理
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
    			    		Toast.makeText(getApplicationContext(), "获取数据失败",Toast.LENGTH_SHORT).show();	
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
	                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
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
    
    //点击名字可以删除权限
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			AlertDialog.Builder b=new Builder(RightManageStaffList.this);
			b.setTitle("提示");
			b.setMessage("是否要删除这个用户的权限");
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();						
					//改变staff串
					Staff st=staffList.get(now);
					int pos=staff.indexOf(String.valueOf(st.getId()));
					String s="";
					if (pos+String.valueOf(st.getId()).length()<staff.length()){
						s=staff.substring(pos+String.valueOf(st.getId()).length()+1);
					}
					staff=staff.substring(0,pos)+s;
					//把修改后的staff串传到服务器
				  	String url="http://"+Constant.ip+"/ZhtxServer/StepRightServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","change");
				    map.put("stepid",String.valueOf(i.getStringExtra("stepid"))); 
				    map.put("staff", staff);
				    map.put("tab", i.getStringExtra("table"));
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	staffList.remove(now); 
						    	staffNameList.remove(now);
						    	mAdapter.notifyDataSetChanged();
						    }else{
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
				    progressDialog = ProgressDialog.show(RightManageStaffList.this, "请稍等...", "获取数据中...", true);
				    mQueue.add(endTask.getRequest());
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
    
    //添加按钮
    public void add(View v){
    	Intent intent=new Intent(RightManageStaffList.this,RightManageStaffAdd.class);
		intent.putExtra("stepid", i.getStringExtra("stepid"));
		intent.putExtra("db", i.getStringExtra("db"));
		intent.putExtra("table", i.getStringExtra("table"));
		startActivity(intent);	 
    }
}
