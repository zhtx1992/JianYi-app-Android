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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HandSignStaffList extends AppCompatActivity {
	private ListView stafflv;
	private List<String> staffNameList,iList;
	private List<Staff> staffList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now,count;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanageadd);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("选择员工");
        stafflv=(ListView)findViewById(R.id.stafflv);
        
        mQueue= Volley.newRequestQueue(HandSignStaffList.this);
        mQueue.start();
        staffNameList =new ArrayList<String>();
        staffList=new ArrayList<Staff>();
        iList=new ArrayList<String>();
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,staffNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        Gson gson=new Gson();
	    Type type=new TypeToken<List<String>>(){}.getType();
	    iList=gson.fromJson(i.getStringExtra("exist"),  type);
        getdata();
	}
    
    private void getdata(){
    	
    	//首先取得所有员工的信息，再从中去掉已经有权限的员工
    	String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
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
    			    //得到全部员工的列表
    				staffList=gson.fromJson(response, type);
   					for (Staff st:staffList){
   						boolean b=true;
   						for (String s:iList)
   							if (s.equals(st.getName())) {
   								b=false;
   								break;
   							}
   						if (b) {
   							staffNameList.add(st.getName());
   						}
   					}	
   					if (staffNameList.size()>0)	{
                        mAdapter.notifyDataSetChanged();
     					stafflv.setOnItemClickListener(new ItemClickEvent());
   					}else{
   					    Toast.makeText(getApplicationContext(), "没有员工可以给于权限",Toast.LENGTH_SHORT).show();
   					    finish();
   					}
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
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
    	progressDialog = ProgressDialog.show(HandSignStaffList.this, "请稍等...", "获取数据中...", true);
    	mQueue.add(infoTask.getRequest());	
    		
    }
    
    //点击名字可以删除权限
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			AlertDialog.Builder b=new Builder(HandSignStaffList.this);
			b.setTitle("提示");
			b.setMessage("把该员工加入考勤表?");
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();						
					String url="http://"+Constant.ip+"/ZhtxServer/HandSignServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","insert");
					map.put("name",staffNameList.get(now));
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	staffNameList.remove(now);
						    	mAdapter.notifyDataSetChanged();
						    	Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show();
						    }else{
						    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
				    VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);
				    progressDialog = ProgressDialog.show(HandSignStaffList.this, "请稍等...", "获取数据中...", true);
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
    

}
