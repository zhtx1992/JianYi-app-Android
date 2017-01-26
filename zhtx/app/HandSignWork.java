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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HandSignWork extends AppCompatActivity {
	private ListView stafflv;
	private List<String> itemList,nameList,sList,nameList2,sList2;
	private List<String> iList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private Spinner sp;
	private CheckBox cbsign,cball;
	private String s;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handsign);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("设置考勤状态");
        stafflv=(ListView)findViewById(R.id.stafflv);
        cball=(CheckBox)findViewById(R.id.cball);
        cbsign=(CheckBox)findViewById(R.id.cbsign);
        
        mQueue= Volley.newRequestQueue(HandSignWork.this);
        mQueue.start();
        
        itemList =new ArrayList<String>();
        nameList =new ArrayList<String>();
        sList =new ArrayList<String>();
        nameList2 =new ArrayList<String>();
        sList2 =new ArrayList<String>();
        iList=new ArrayList<String>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,itemList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        cball.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) { 
                // TODO Auto-generated method stub 
                if(isChecked){ 
                   cbsign.setChecked(false);
                   nameList2.clear();
                   sList2.clear();
                   itemList.clear();
                   for (int j=0;j<nameList.size();j++){
                	   nameList2.add(nameList.get(j));
                	   sList2.add(sList.get(j));
                	   itemList.add(getStatusStr(nameList.get(j),Integer.parseInt(sList.get(j))));
                   }
                   mAdapter.notifyDataSetChanged();
                }
            } 
        }); 
        
        cbsign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) { 
                // TODO Auto-generated method stub 
                if(isChecked){ 
                	cball.setChecked(false);
                	nameList2.clear();
                    sList2.clear();
                    itemList.clear();
                    for (int j=0;j<nameList.size();j++)
                    if (sList.get(j).equals("0")){
                 	   nameList2.add(nameList.get(j));
                 	   sList2.add(sList.get(j));
                 	   itemList.add(getStatusStr(nameList.get(j),Integer.parseInt(sList.get(j))));
                    }
                    mAdapter.notifyDataSetChanged();
                }
            } 
        }); 
        
        getdata();
        
        
	}
    
    private void getdata(){        
        String url="http://"+Constant.ip+"/ZhtxServer/HandSignServlet";
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "signlist");
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
                    	String name=s.substring(0,s.indexOf("&"));
                    	nameList.add(name);
                    	nameList2.add(name);
                    	String status=s.substring(s.indexOf("&")+1);
                    	sList.add(status);
                    	sList2.add(status);
                    	itemList.add(getStatusStr(name,Integer.parseInt(status)));
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
                Toast.makeText(getApplicationContext(),R.string.volley_error2,Toast.LENGTH_LONG).show();
                finish();
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
	    progressDialog = ProgressDialog.show(HandSignWork.this, "请稍等...", "获取数据中...", true);
		mQueue.add(infoTask.getRequest());	
    	
    }
    
    
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			AlertDialog.Builder b=new Builder(HandSignWork.this);
			sp=new Spinner(b.getContext());
			List<String> list=new ArrayList<String>();
			list.add("未考勤");
			list.add("已考勤");
			ArrayAdapter<String> arr_adapter= new ArrayAdapter<String>(HandSignWork.this, android.R.layout.simple_spinner_item, list);
	        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        sp.setAdapter(arr_adapter);
			b.setTitle("设置考勤状态");
			b.setView(sp);
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					s=sp.getSelectedItem().toString();
					if (s.equals("未考勤")) s="0";
					if (s.equals("已考勤")) s="1";
					arg0.dismiss();						
					//把修改后的staff串传到服务器
				  	String url="http://"+Constant.ip+"/ZhtxServer/HandSignServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","sign");
					map.put("name",nameList2.get(now));
					map.put("status",s);
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	sList2.remove(now);
						    	String name=nameList2.get(now);
						    	nameList2.remove(now);
						    	sList2.add(s);
						    	nameList2.add(name);
						    	itemList.remove(now);
						    	itemList.add(getStatusStr(name,Integer.parseInt(s)));
						    	for (int i=0;i<nameList.size();i++){
						    		if (nameList.get(i).equals(name)){
						    		    nameList.remove(i);
						    		    sList.remove(i);
						    		    nameList.add(name);
						    		    sList.add(s);
						    			break;
						    		}
						    	}
						        mAdapter.notifyDataSetChanged();
						    	Toast.makeText(getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
						    }else{
						    	Toast.makeText(getApplicationContext(), "处理失败,请重新尝试", Toast.LENGTH_SHORT).show();
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
				    progressDialog = ProgressDialog.show(HandSignWork.this, "请稍等...", "获取数据中...", true);
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
    
    private String getStatusStr(String name,int status){
    	String r=name;
    	while (r.length()<10){
    		r+="  ";
    	}
        switch (status){
        	case 0:{
        		r+="未考勤";
        		break;
        	}
        	case 1:{
        		r+="已考勤";
        		break;
        	}
        }
		return r;
    	
    }
}
