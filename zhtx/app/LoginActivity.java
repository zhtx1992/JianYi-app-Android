package com.zhtx.app;

import java.util.HashMap;
import java.util.UUID;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.tencent.bugly.crashreport.CrashReport;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.UpdateManager;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Company;
import com.zhtx.myclass.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends Activity {
	
	//定义控件
	private EditText useret,pwet;
	private Button btnlogin;
	private TextView btnregister,btnuid;
	private CheckBox AutoLogin;
	//定义volley队列
	private RequestQueue mQueue;
	//定义要保存的信息
	private SharedPreferences autoLoginSpSettings=null;
	private SharedPreferences userInfoSpSettings=null,mSpSettings;
	private Editor edit;
	//载入
	private ProgressDialog progressDialog = null;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        setContentView(R.layout.activity_login); 
        //初始化bugly
        CrashReport.initCrashReport(getApplicationContext(),Constant.appIdForBugly, true); 
        //获得volley请求队列
        mQueue= Volley.newRequestQueue(LoginActivity.this);
        mQueue.start();
        //找到所需要的控件，包括两个文本编辑 两个按钮一个单选框
        useret=(EditText)findViewById(R.id.username);
        pwet=(EditText)findViewById(R.id.password);
        btnlogin=(Button)findViewById(R.id.btnlogin);
        btnregister=(TextView)findViewById(R.id.btnregister);
        btnuid=(TextView)findViewById(R.id.btnuid);
        AutoLogin=(CheckBox)findViewById(R.id.AutoLogin);
        //初始化用户信息存储文件
        userInfoSpSettings=getSharedPreferences(this.getResources().getString(R.string.UserInfosp),MODE_PRIVATE);
    	edit=userInfoSpSettings.edit();
        //判断是否记住密码
        autoLoginSpSettings=getSharedPreferences(this.getResources().getString(R.string.AutoLoginsp),MODE_PRIVATE);
        if ((autoLoginSpSettings!=null)&&(autoLoginSpSettings.getBoolean("RememberPassword", false))){
        	String userjson=userInfoSpSettings.getString("userInfo","");
    		Gson gson=new Gson();
    		User user=new User();
    		user=gson.fromJson(userjson, User.class);
    		useret.setText(user.getUsername());
    		pwet.setText(user.getPassword());
    		AutoLogin.setChecked(true);
        } 
        
        getUniqueId();
        
	}
	
	/*private void autoLogin(){
		//服务器处理页面url
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//从客户端存储的用户信息中取出用户名和密码
		mSpSettings=getSharedPreferences("UserInfoSpSettings",this.MODE_PRIVATE);
		String userjson=mSpSettings.getString("userInfo","");
		Gson gson=new Gson();
		User user=new User();
		user=gson.fromJson(userjson, User.class);
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","login");
		map.put("username",user.getUsername());
		map.put("password",user.getPassword());
		map.put("uid", getUniqueId());
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){
			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if (response==null){
				    Toast.makeText(getApplicationContext(), "自动登录失败，账户被锁定", Toast.LENGTH_SHORT).show();
				}else{
					if (response.equals("-1")){
						Toast.makeText(getApplicationContext(), "自动登录失败，用户信息已过期", Toast.LENGTH_SHORT).show();
					}else{
						doLogin(response);        	
					}	
				}
			    		
			}
			//登录成功后要进行的操作
			private void doLogin(String userJson) {
				User user=new User();
				Gson gson=new Gson();
				user=gson.fromJson(userJson, User.class);
				//保存登录用户所在公司的信息
				String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("action", "info");
				map.put("comid",user.getCompany_id());
				Response.Listener<String> listener=new Response.Listener<String>(){

					@Override
					public void onResponse(String response) {
						if (response==null){
						    Toast.makeText(getApplicationContext(), "访问服务器失败", Toast.LENGTH_SHORT).show();
						    }else{
						      	 
                                 edit.putString("comInfo", response);
                 				 edit.commit();
                 				 //转换到主界面
                 				 Intent i=new Intent(LoginActivity.this,NavigationActivity.class);
                 				 startActivity(i);		 
                 				 finish();
							}	
						progressDialog.dismiss();	
						}
					    
				};
				Response.ErrorListener elistener=new Response.ErrorListener(){  
		            @Override  
		            public void onErrorResponse(VolleyError error) {  
		                progressDialog.dismiss();
		                Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
		            }  
		        };
				VolleySRUtil cominfoTask=new VolleySRUtil(url,map,listener,elistener);	
				mQueue.add(cominfoTask.getRequest());
				progressDialog = ProgressDialog.show(LoginActivity.this, "请稍等...", "获取数据中...", true);
			}
		};			
		Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil loginTask=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(loginTask.getRequest());
		progressDialog = ProgressDialog.show(LoginActivity.this, "请稍等...", "获取数据中...", true);
		
	}*/
	//点击登录按钮后的处理
	public void login(View v){
	    //获得文本框的输入信息
		String username=useret.getText().toString();
		String password=pwet.getText().toString();
		
		//服务器处理页面url
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//用post方法封装需要发送的信息(用户名密码)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","login");
		map.put("username",username);
		map.put("password",password);
		map.put("uid", getUniqueId());
		//对于服务器返回结果的处理
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				if (response==null){
				    Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_SHORT).show();
				    }else{
					    if (response.equals("-1")){
						        Toast.makeText(getApplicationContext(), "用户名或密码错误 ", Toast.LENGTH_SHORT).show();
					        }else{
					        if (response.equals("-2")){
					        	Toast.makeText(getApplicationContext(), "登录设备不符，请获取机器码并联系客服", Toast.LENGTH_LONG).show();
					        }
					        else doLogin(response);	      				       
					}	
				}
				progressDialog.dismiss();
			}
            //登录成功后要进行的操作
			private void doLogin(String userJson) {
				//判断自动登录是否被选择
				if (AutoLogin.isChecked()){
				    autoLoginSpSettings=getSharedPreferences(LoginActivity.this.getResources().getString(R.string.AutoLoginsp),MODE_PRIVATE);
			        Editor editor=autoLoginSpSettings.edit();
			        //设定自动登录为true
			        editor.putBoolean("RememberPassword", true);
			        editor.commit();
				}else{
					autoLoginSpSettings=getSharedPreferences(LoginActivity.this.getResources().getString(R.string.AutoLoginsp),MODE_PRIVATE);
				    Editor editor=autoLoginSpSettings.edit();
				    //设定自动登录为false
				    editor.putBoolean("RememberPassword", false);
				    editor.commit();
				}
				//保存登录用户的信息
				
				edit.putString("userInfo", userJson);
				User user=new User();
				Gson gson=new Gson();
				user=gson.fromJson(userJson, User.class);
				//保存登录用户所在公司的信息
				String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("action", "info");
				map.put("comid",user.getCompany_id());
				Response.Listener<String> listener=new Response.Listener<String>(){

					@Override
					public void onResponse(String response) {
						if (response==null){
						    Toast.makeText(getApplicationContext(), "访问服务器失败", Toast.LENGTH_SHORT).show();
						    }else{
						      	 
                                 edit.putString("comInfo", response);
                 				 edit.commit();
                 				 //转换到主界面
                 				 Intent i=new Intent(LoginActivity.this,NavigationActivity.class);
                 				 startActivity(i);		 
                 				 finish();
							}	
						progressDialog.dismiss();	
						}
					    
				};
				Response.ErrorListener elistener=new Response.ErrorListener(){  
		            @Override  
		            public void onErrorResponse(VolleyError error) {  
		                progressDialog.dismiss();
		                Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
		            }  
		        };
				VolleySRUtil cominfoTask=new VolleySRUtil(url,map,listener,elistener);	
				mQueue.add(cominfoTask.getRequest());
				progressDialog = ProgressDialog.show(LoginActivity.this, "请稍等...", "获取数据中...", true);
			}
		};
		
		Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil loginTask=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(loginTask.getRequest());
		progressDialog = ProgressDialog.show(LoginActivity.this, "请稍等...", "获取数据中...", true);
	}
    
	private String getUniqueId(){
    	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    	String tmDevice, tmSerial, tmPhone, androidId;
  	    tmDevice = "" + tm.getDeviceId();
  	    tmSerial = "" + tm.getSimSerialNumber();
  	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
  	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
  	    String uniqueId = deviceUuid.toString();
  	 
		return uniqueId; 	
    }
	//点击机器码后的处理
    public void uid(View v){
   
    	 AlertDialog.Builder b=new Builder(LoginActivity.this);
		 b.setTitle("提示");
		 b.setMessage("机器码为:"+getUniqueId());
		 b.setPositiveButton("确定", new DialogInterface.OnClickListener(){
			@Override
		    public void onClick(DialogInterface arg0, int arg1) {    					
				arg0.dismiss();		
			}			    				
		 });	
		 b.create().show();	 	
	}
	//点击注册按钮后的处理
    public void register(View v){
    	//转换登录界面
		Intent i=new Intent(LoginActivity.this,Reg1Activity.class);
		startActivity(i);
	}
}
