package com.zhtx.app;

import java.util.HashMap;
import java.util.UUID;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Reg2Activity extends Activity {
	
	//显示公司名
	private TextView comnametv;
	//四个输入框分别为用户名，密码，姓名，身份证号
	private EditText usernameet,passwordet,passwordet2, cnnameet;
	private Button btnprev,btnsubmit;
	private RequestQueue mQueue;
    private Intent i;
    //是否成功加入user表
    private boolean isCreate=false;
    //加入user表的用户id
    private int newid=0;
    //是否成功发送申请
    private boolean isSend=false;
    
    private String username,password,password2,cnname,idcard;
    private ProgressDialog progressDialog = null;
    
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_2);
        
        i=getIntent();
        mQueue= Volley.newRequestQueue(Reg2Activity.this);
        mQueue.start();
        //找到控件
        comnametv=(TextView)findViewById(R.id.comnametv);
        usernameet=(EditText)findViewById(R.id.usernameet);
        passwordet=(EditText)findViewById(R.id.passwordet);
        passwordet2=(EditText)findViewById(R.id.passwordet2);
        cnnameet=(EditText)findViewById(R.id.cnnameet);
        btnprev=(Button)findViewById(R.id.btnprev);
        btnsubmit=(Button)findViewById(R.id.btnsubmit);
        
        //取出reg1传来的公司名，设置textview
        comnametv.setText("您申请加入"+i.getStringExtra("company_name"));
        
	}
	
	//点击 上一步返回reg1
	public void toprev(View v){
		finish();
	}
	
	//点击申请加入
	public void submit(View v){
		//得到输入信息
        username=usernameet.getText().toString();
        password=passwordet.getText().toString();
        password2=passwordet2.getText().toString();
        cnname=cnnameet.getText().toString();
		
		int result=isValid(username,password,password2,cnname);
		
		//校验正确
		if (result==0) {
			checkname();
		    btnsubmit.setClickable(false);
		}
		//校验错误,提示错误的地方
		else{
			String error="";
			if (result % 2==1){
				error=error+"密码格式错误   ";
				passwordet.setText("");
				passwordet2.setText("");
			}
			if ((result % 4)/2==1){
				error=error+"两次输入密码不一致  ";
				passwordet.setText("");
				passwordet2.setText("");
			}
			if ((result % 8)/4==1){
				error=error+"必要信息未填写";
				passwordet.setText("");
				passwordet2.setText("");
			}
			Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();			
		}
		
	}
	
	//判断用户名重复
	private void checkname() {
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//用post方法封装需要发送的信息
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","check");
	    map.put("username",username);
		map.put("uid", getUniqueId());
		map.put("comid",i.getStringExtra("comid")); 
		//对于服务器返回结果的处理
	    Response.Listener<String> listener=new Response.Listener<String>(){
			@Override
		    public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	doSubmit();
			    }else{
			        progressDialog.dismiss();
			        Toast.makeText(getApplicationContext(), "重复注册",Toast.LENGTH_LONG).show();
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
		VolleySRUtil createTask=new VolleySRUtil(url,map,listener,elistener);	
	  	mQueue.add(createTask.getRequest());
	  	progressDialog = ProgressDialog.show(Reg2Activity.this, "请稍等...", "获取数据中...", true);
		
	}

	//校验输入信息的合法性
    private int isValid(String username,String password,String password2,String cnname){
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
    	
    	//检验姓名和出生日期是否填写
    	if (cnname.equals("0")){
    		result+=4;
    	}
		return result;
    }
    
    //信息校验成功，提交申请给公司管理员
	private void doSubmit() {
		
		//初始化
		isCreate=false;
		isSend=false;
		//在数据库用户表中加入待审用户
		//送到服务器用户处理servlet
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//用post方法封装需要发送的信息
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","create");
	    map.put("username",username);
		map.put("password",password);
		map.put("comid", i.getStringExtra("comid"));
		map.put("uid", getUniqueId());
		//对于服务器返回结果的处理
	    Response.Listener<String> listener=new Response.Listener<String>(){
			@Override
		    public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	//加入用户表成功 
				    isCreate=true;
				    newid=Integer.parseInt(response);
				    mhandler1.sendEmptyMessage(0);
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
		VolleySRUtil createTask=new VolleySRUtil(url,map,listener,elistener);	
	  	mQueue.add(createTask.getRequest());
	  	
	  		    
	 }
	
	//判断整个过程是否成功
	private Handler mhandler2=new Handler(){
		@Override
		public void handleMessage(Message msg){
			progressDialog.dismiss();
			super.handleMessage(msg);
			if ((isSend)&&(isCreate)){
			  	    //显示成功提示语
				    Toast.makeText(getApplicationContext(), "提交成功,请等待审核", Toast.LENGTH_LONG).show();
				    //返回登录界面
				    Intent i2=new Intent(Reg2Activity.this,LoginActivity.class);
				    startActivity(i2);
				    finish();
			    }else{
			    	Toast.makeText(getApplicationContext(), "申请提交失败...", Toast.LENGTH_SHORT).show();
			    	btnsubmit.setClickable(true);
			    }
		}
	};
	
	
	private Handler mhandler1=new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			//在公司的申请表中加入新申请
		  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
			//用post方法封装需要发送的信息
		  	HashMap<String, String> map2=new HashMap<String, String>();
		  	map2.put("db", i.getStringExtra("db_name"));
			map2.put("action","insert");
		    map2.put("sender",String.valueOf(newid));
			map2.put("reciver",String.valueOf(i.getIntExtra("dba_id",0)));
			map2.put("previd","0");
			map2.put("act", "1");
			map2.put("data",cnname);
			//对于服务器返回结果的处理
			Response.Listener<String> listener=new Response.Listener<String>(){
		        @Override
			    public void onResponse(String response) {
				    if ((response!=null)&&(response.equals("1"))){
				    	//添加审批信息成功
					    isSend=true;
					    
				    }
				    //发消息告诉主线程访问服务器完成
				    mhandler2.sendEmptyMessage(0);
			    }				
		    };	
		    Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
	            }  
	        };
			VolleySRUtil infoTask=new VolleySRUtil(url,map2,listener,elistener);	
		    mQueue.add(infoTask.getRequest());
		}
	};	
	
	private String getUniqueId(){
    	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    	final String tmDevice, tmSerial, tmPhone, androidId;
  	    tmDevice = "" + tm.getDeviceId();
  	    tmSerial = "" + tm.getSimSerialNumber();
  	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
  	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
  	    String uniqueId = deviceUuid.toString();
		return uniqueId; 
    }
}
