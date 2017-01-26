package com.zhtx.app;

import java.util.HashMap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends AppCompatActivity {
	
	private Button btnchange;
	private EditText nowpasswordet,passwordet,passwordet2;
	private RequestQueue mQueue;
	private boolean result = false;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepw);
        
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("修改密码");
        
        mQueue= Volley.newRequestQueue(ChangePasswordActivity.this);
        //找到控件
        btnchange=(Button)findViewById(R.id.btnchange);
        nowpasswordet=(EditText)findViewById(R.id.nowpasswordet);
        passwordet=(EditText)findViewById(R.id.passwordet);
        passwordet2=(EditText)findViewById(R.id.passwordet2);
	}
	
	public void change(View v){
		String nowpassword=nowpasswordet.getText().toString();
		String password=passwordet.getText().toString();
		String password2=passwordet2.getText().toString();
		Intent i=getIntent();	
		if (!nowpassword.equals(i.getStringExtra("nowPassword"))){
			Toast.makeText(ChangePasswordActivity.this, "当前密码错误", Toast.LENGTH_SHORT).show();
		    nowpasswordet.setText("");
		}else{
			int rs=isValid(password,password2);
			if (rs==0){
				doChange(i.getStringExtra("id"),password);
				if (result) {
					Toast.makeText(ChangePasswordActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
					finish();
				}else{
					Toast.makeText(ChangePasswordActivity.this, "修改失败...", Toast.LENGTH_SHORT).show();
				}
			}else{
				String error="";
				if (rs% 2==1){
					error=error+"密码格式错误   ";
					passwordet.setText("");
					passwordet2.setText("");
				}
				if ((rs % 4)/2==1){
					error=error+"两次输入密码不一致  ";
					passwordet.setText("");
					passwordet2.setText("");
				}
				Toast.makeText(ChangePasswordActivity.this, error, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	//校验输入信息的合法性
    private int isValid(String password,String password2){
    	int result=0;

    	//校验密码
    	if ((password.length()>16)||(password.length()<6)) {
    		result+=1;
    	}else{
    	    for (int i=0; i<password.length();i++)	
    	    	if ((!Character.isDigit(password.charAt(i)))&&
    	    	(!Character.isLetter(password.charAt(i)))) {
    	    		result+=1;
    	    		break;
    	    	}
    	}
    	//校验两次输入的密码是否一致
    	if (!password.equals(password2)) result+=2;
    	
		return result;
    }
    
    private void doChange(String id,String password){
    	//服务器处理页面url
    	String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
    	//用post方法封装需要发送的信息(用户名密码)
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action","changepw");
    	map.put("id", id);
    	map.put("password",password);
    	//对于服务器返回结果的处理
    	Response.Listener<String> listener=new Response.Listener<String>(){
    		@Override
    		public void onResponse(String response) {
    		    if ((response!=null)&&(response.equals("1"))){
    		    	result=true;
    		    	
    		    }else result=false;
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
    	progressDialog = ProgressDialog.show(ChangePasswordActivity.this, "请稍等...", "获取数据中...", true);
    	mQueue.start();
    }
}
