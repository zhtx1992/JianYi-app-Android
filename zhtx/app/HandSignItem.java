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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HandSignItem extends AppCompatActivity {
	private ListView stafflv;
	private List<String> itemList;
	private List<String> iList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now;
	private Button btnadd;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private EditText et;
	private String s;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanage);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("考勤对象管理");
        stafflv=(ListView)findViewById(R.id.stafflv);
        btnadd=(Button)findViewById(R.id.btnadd);
        
        mQueue= Volley.newRequestQueue(HandSignItem.this);
        mQueue.start();
        
        itemList =new ArrayList<String>();
        iList=new ArrayList<String>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,itemList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
	}
    
	protected void onResume() {
        super.onResume();
        itemList.clear();
        getdata();
	}
    private void getdata(){
    	
        
        String url="http://"+Constant.ip+"/ZhtxServer/HandSignServlet";
		//用post方法封装需要发送的信息(公司id)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "signitem");
		map.put("db",i.getStringExtra("db"));
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
			    	Type type=new TypeToken<List<String>>(){}.getType();
					iList=gson.fromJson(response, type); 	
                    for (String s:iList){
                    	itemList.add(s);
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
                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
                finish();
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
	    progressDialog = ProgressDialog.show(HandSignItem.this, "请稍等...", "获取数据中...", true);
		mQueue.add(infoTask.getRequest());	
    	
    }
    
    //点击名字可以删除
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			AlertDialog.Builder b=new Builder(HandSignItem.this);
			b.setTitle("提示");
			b.setMessage("是否要删除这个考勤对象");
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();						
					//把修改后的staff串传到服务器
				  	String url="http://"+Constant.ip+"/ZhtxServer/HandSignServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","delete");
					map.put("name",iList.get(now));
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	iList.remove(now); 
						    	itemList.remove(now);
						    	mAdapter.notifyDataSetChanged();
						    	Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
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
				    progressDialog = ProgressDialog.show(HandSignItem.this, "请稍等...", "获取数据中...", true);
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
    	AlertDialog.Builder b=new Builder(HandSignItem.this);
		b.setTitle("输入对象名");
		et=new EditText(b.getContext());
		b.setView(et);
		b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				s=et.getText().toString();
				boolean isok=true;
				for (String name:iList){
					if (name.equals(s)){
						Toast.makeText(getApplicationContext(), "该成员已存在", Toast.LENGTH_SHORT).show();
						isok=false;
						break;
					}
				}
				if (isok){
					arg0.dismiss();						
				  	String url="http://"+Constant.ip+"/ZhtxServer/HandSignServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","insert");
					map.put("name",s);
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	iList.add(s); 
						    	itemList.add(s);
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
			                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
			            }  
			        };
				    VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);
				    progressDialog = ProgressDialog.show(HandSignItem.this, "请稍等...", "获取数据中...", true);
				    mQueue.add(endTask.getRequest());
				}
				
			}
		});
		b.setNeutralButton("职员表", new  DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			    Intent intent = new Intent(HandSignItem.this,HandSignStaffList.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		Gson gson=new Gson();
			    Type type=new TypeToken<List<String>>(){}.getType();
			    intent.putExtra("exist",gson.toJson(iList, type));
        		startActivity(intent);
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
