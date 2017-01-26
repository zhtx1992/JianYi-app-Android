package com.zhtx.app;

import java.util.HashMap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetInfoActivity extends AppCompatActivity {
	
	private Button btnchange;
	private EditText bankidet,platenumet;
	private RequestQueue mQueue;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private Intent i;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setinfo);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("修改信息");
        
        mQueue= Volley.newRequestQueue(SetInfoActivity.this);
        mQueue.start(); 		
        
        //找到控件
        btnchange=(Button)findViewById(R.id.btnchange);
        bankidet=(EditText)findViewById(R.id.bankidet);
        platenumet=(EditText)findViewById(R.id.platenumet);
        
        i=getIntent();
        bankidet.setText(i.getStringExtra("bankid"));
        platenumet.setText(i.getStringExtra("platenum"));
	}
	
	public void change(View v){
		//服务器处理页面url
    	String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action","setinfo");
    	map.put("id", i.getStringExtra("id"));
    	map.put("bankid",bankidet.getText().toString());
    	map.put("bankid",platenumet.getText().toString());
    	//对于服务器返回结果的处理
    	Response.Listener<String> listener=new Response.Listener<String>(){
    		@Override
    		public void onResponse(String response) {
    		    if ((response!=null)&&(response.equals("1"))){
    		    	Toast.makeText(getApplicationContext(), "修改成功",Toast.LENGTH_LONG).show();
    		    	finish();
    		    }
    		    progressDialog.dismiss();
    		}
    	};
    	Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil loginTask=new VolleySRUtil(url,map,listener,elistener);	
    	mQueue.add(loginTask.getRequest());
    	progressDialog = ProgressDialog.show(SetInfoActivity.this, "请稍等...", "获取数据中...", true);
	}
}
