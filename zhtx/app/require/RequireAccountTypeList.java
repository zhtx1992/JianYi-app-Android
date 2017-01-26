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
import com.zhtx.app.R.id;
import com.zhtx.app.R.layout;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.AccountType;
import com.zhtx.myclass.Staff;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RequireAccountTypeList extends AppCompatActivity {
	private ListView stafflv;
	private List<String> atNameList;
	private List<AccountType> atList,atList2;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	//判断当前是在选择一级科目还是二级科目
	private boolean status=false;
	private AccountType accountType;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanageadd);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("选择会计科目");
        stafflv=(ListView)findViewById(R.id.stafflv);
        
        mQueue= Volley.newRequestQueue(RequireAccountTypeList.this);
        mQueue.start();
        
        atNameList =new ArrayList<String>();
        atList=new ArrayList<AccountType>();
        atList2=new ArrayList<AccountType>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,atNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        getdata();
	}
    
    private void getdata(){
        
		//服务器处理页面url
		String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
		//用post方法封装需要发送的信息(公司id)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "atype");
		map.put("db",i.getStringExtra("db"));
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
			    	Type type=new TypeToken<List<AccountType>>(){}.getType();
					atList=gson.fromJson(response,type);
					int count=0;
					String last="";
					for (AccountType at:atList){					
						if (!at.getClass1().equals(last)){
							count++;
							last=at.getClass1();
							atNameList.add(String.valueOf(count)+".  "+at.getClass1());
							atList2.add(at);
						}
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
	    progressDialog = ProgressDialog.show(RequireAccountTypeList.this, "请稍等...", "获取数据中...", true);
		mQueue.add(infoTask.getRequest());	
    		    	
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (!status){
				//选择一级科目
				accountType=atList2.get(arg2);
				atList2.clear();
				atNameList.clear();
				status=true;
				for (AccountType at:atList){
					if (at.getClass1().equals(accountType.getClass1())){
						atList2.add(at);
						atNameList.add(at.getClass2());
					}
				}
				mAdapter.notifyDataSetChanged();
			}else{
				//选择二级科目
				RequireDetail.accounttype=atList2.get(arg2);
				finish();
			}	
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!status){
            	finish();
            }else{
            	status=false;
            	int count=0;
				String last="";
				atList2.clear();
				atNameList.clear();
				for (AccountType at:atList){					
					if (!at.getClass1().equals(last)){
						count++;
						last=at.getClass1();
						atNameList.add(String.valueOf(count)+".  "+at.getClass1());
						atList2.add(at);
					}
				}	   					   
                mAdapter.notifyDataSetChanged();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}


