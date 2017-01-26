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

public class RequireStringList extends AppCompatActivity {
	private ListView stafflv;
	private EditText searchet;
	private List<String> ObList,oList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.require_stringlist);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("点击选择项目");
        stafflv=(ListView)findViewById(R.id.stafflv);
        searchet=(EditText)findViewById(R.id.searchet);
        
        mQueue= Volley.newRequestQueue(RequireStringList.this);
        mQueue.start();
        ObList =new ArrayList<String>();
        oList =new ArrayList<String>();
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,oList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        getdata();
        
     
	}
    
    private void getdata(){
        
		//服务器处理页面url
		String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
		//用post方法封装需要发送的信息(公司id)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", i.getStringExtra("action"));
		if (i.getStringExtra("action").equals("act81") || i.getStringExtra("action").equals("act91")){
			map.put("mode",i.getStringExtra("mode"));
		}
		map.put("db",i.getStringExtra("db"));
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
			    	Type type=new TypeToken<List<String>>(){}.getType();
					ObList=gson.fromJson(response,type);
					int count=0;
  					for (String s:ObList){
  						count++;
  						oList.add(String.valueOf(count)+". "+s);
  					}
                    mAdapter.notifyDataSetChanged();
 					stafflv.setOnItemClickListener(new ItemClickEvent());
 					/*searchet.setOnKeyListener(new View.OnKeyListener() {
						
						@Override
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							switch (event.getAction()){
							case KeyEvent.ACTION_DOWN:{
								if (searchet.getText().toString().length() != 0) {     
	 								int count2=0;
	 								oList.clear();
	 			  					for (String x:ObList)
	 			  					if (x.contains(searchet.getText().toString())){
	 			  						count2++;
	 			  						oList.add(String.valueOf(count2)+". "+x);
	 			  					}
	 							}else {      
	 								int count2=0;
	 								oList.clear();
	 			  					for (String x:ObList){
	 			  						count2++;
	 			  						oList.add(String.valueOf(count2)+". "+x);
	 			  					}
	 							}  
								break;
							}
							case KeyEvent.ACTION_UP:{
								break;
							}
							}
							return false;
						}
					}); */
 					searchet.addTextChangedListener(new TextWatcher() { 
 						@Override  
 						public void onTextChanged(CharSequence s, int start, int before, int count){
 							if (s.toString().length() != 0) {     
 								int count2=0;
 								oList.clear();
 			  					for (String x:ObList)
 			  					if (x.indexOf(s.toString())!=-1){
 			  						count2++;
 			  						oList.add(String.valueOf(count2)+". "+x);
 			  					}
 			  				    mAdapter.notifyDataSetChanged();
 							}else {      
 								int count2=0;
 								oList.clear();
 			  					for (String x:ObList){
 			  						count2++;
 			  						oList.add(String.valueOf(count2)+". "+x);
 			  					}
 			  				    mAdapter.notifyDataSetChanged();
 							}  
 						}  
 						@Override  	
 						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 							
 						}  
 						@Override  
 						public void afterTextChanged(Editable s) {    
 							
 						}
 					});
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
	    progressDialog = ProgressDialog.show(RequireStringList.this, "请稍等...", "获取数据中...", true);
		mQueue.add(infoTask.getRequest());	
    		    	
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			RequireDetail.stringob=oList.get(arg2).substring(oList.get(arg2).indexOf(".")+2);
			finish();
		}
    }
}