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
	
	//����ؼ�
	private EditText useret,pwet;
	private Button btnlogin;
	private TextView btnregister,btnuid;
	private CheckBox AutoLogin;
	//����volley����
	private RequestQueue mQueue;
	//����Ҫ�������Ϣ
	private SharedPreferences autoLoginSpSettings=null;
	private SharedPreferences userInfoSpSettings=null,mSpSettings;
	private Editor edit;
	//����
	private ProgressDialog progressDialog = null;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        setContentView(R.layout.activity_login); 
        //��ʼ��bugly
        CrashReport.initCrashReport(getApplicationContext(),Constant.appIdForBugly, true); 
        //���volley�������
        mQueue= Volley.newRequestQueue(LoginActivity.this);
        mQueue.start();
        //�ҵ�����Ҫ�Ŀؼ������������ı��༭ ������ťһ����ѡ��
        useret=(EditText)findViewById(R.id.username);
        pwet=(EditText)findViewById(R.id.password);
        btnlogin=(Button)findViewById(R.id.btnlogin);
        btnregister=(TextView)findViewById(R.id.btnregister);
        btnuid=(TextView)findViewById(R.id.btnuid);
        AutoLogin=(CheckBox)findViewById(R.id.AutoLogin);
        //��ʼ���û���Ϣ�洢�ļ�
        userInfoSpSettings=getSharedPreferences(this.getResources().getString(R.string.UserInfosp),MODE_PRIVATE);
    	edit=userInfoSpSettings.edit();
        //�ж��Ƿ��ס����
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
		//����������ҳ��url
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//�ӿͻ��˴洢���û���Ϣ��ȡ���û���������
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
		//���ڷ��������ؽ���Ĵ���
		Response.Listener<String> listener=new Response.Listener<String>(){
			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if (response==null){
				    Toast.makeText(getApplicationContext(), "�Զ���¼ʧ�ܣ��˻�������", Toast.LENGTH_SHORT).show();
				}else{
					if (response.equals("-1")){
						Toast.makeText(getApplicationContext(), "�Զ���¼ʧ�ܣ��û���Ϣ�ѹ���", Toast.LENGTH_SHORT).show();
					}else{
						doLogin(response);        	
					}	
				}
			    		
			}
			//��¼�ɹ���Ҫ���еĲ���
			private void doLogin(String userJson) {
				User user=new User();
				Gson gson=new Gson();
				user=gson.fromJson(userJson, User.class);
				//�����¼�û����ڹ�˾����Ϣ
				String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("action", "info");
				map.put("comid",user.getCompany_id());
				Response.Listener<String> listener=new Response.Listener<String>(){

					@Override
					public void onResponse(String response) {
						if (response==null){
						    Toast.makeText(getApplicationContext(), "���ʷ�����ʧ��", Toast.LENGTH_SHORT).show();
						    }else{
						      	 
                                 edit.putString("comInfo", response);
                 				 edit.commit();
                 				 //ת����������
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
		                Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
		            }  
		        };
				VolleySRUtil cominfoTask=new VolleySRUtil(url,map,listener,elistener);	
				mQueue.add(cominfoTask.getRequest());
				progressDialog = ProgressDialog.show(LoginActivity.this, "���Ե�...", "��ȡ������...", true);
			}
		};			
		Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil loginTask=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(loginTask.getRequest());
		progressDialog = ProgressDialog.show(LoginActivity.this, "���Ե�...", "��ȡ������...", true);
		
	}*/
	//�����¼��ť��Ĵ���
	public void login(View v){
	    //����ı����������Ϣ
		String username=useret.getText().toString();
		String password=pwet.getText().toString();
		
		//����������ҳ��url
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//��post������װ��Ҫ���͵���Ϣ(�û�������)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","login");
		map.put("username",username);
		map.put("password",password);
		map.put("uid", getUniqueId());
		//���ڷ��������ؽ���Ĵ���
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				if (response==null){
				    Toast.makeText(getApplicationContext(), "��¼ʧ��", Toast.LENGTH_SHORT).show();
				    }else{
					    if (response.equals("-1")){
						        Toast.makeText(getApplicationContext(), "�û������������ ", Toast.LENGTH_SHORT).show();
					        }else{
					        if (response.equals("-2")){
					        	Toast.makeText(getApplicationContext(), "��¼�豸���������ȡ�����벢��ϵ�ͷ�", Toast.LENGTH_LONG).show();
					        }
					        else doLogin(response);	      				       
					}	
				}
				progressDialog.dismiss();
			}
            //��¼�ɹ���Ҫ���еĲ���
			private void doLogin(String userJson) {
				//�ж��Զ���¼�Ƿ�ѡ��
				if (AutoLogin.isChecked()){
				    autoLoginSpSettings=getSharedPreferences(LoginActivity.this.getResources().getString(R.string.AutoLoginsp),MODE_PRIVATE);
			        Editor editor=autoLoginSpSettings.edit();
			        //�趨�Զ���¼Ϊtrue
			        editor.putBoolean("RememberPassword", true);
			        editor.commit();
				}else{
					autoLoginSpSettings=getSharedPreferences(LoginActivity.this.getResources().getString(R.string.AutoLoginsp),MODE_PRIVATE);
				    Editor editor=autoLoginSpSettings.edit();
				    //�趨�Զ���¼Ϊfalse
				    editor.putBoolean("RememberPassword", false);
				    editor.commit();
				}
				//�����¼�û�����Ϣ
				
				edit.putString("userInfo", userJson);
				User user=new User();
				Gson gson=new Gson();
				user=gson.fromJson(userJson, User.class);
				//�����¼�û����ڹ�˾����Ϣ
				String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("action", "info");
				map.put("comid",user.getCompany_id());
				Response.Listener<String> listener=new Response.Listener<String>(){

					@Override
					public void onResponse(String response) {
						if (response==null){
						    Toast.makeText(getApplicationContext(), "���ʷ�����ʧ��", Toast.LENGTH_SHORT).show();
						    }else{
						      	 
                                 edit.putString("comInfo", response);
                 				 edit.commit();
                 				 //ת����������
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
		                Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
		            }  
		        };
				VolleySRUtil cominfoTask=new VolleySRUtil(url,map,listener,elistener);	
				mQueue.add(cominfoTask.getRequest());
				progressDialog = ProgressDialog.show(LoginActivity.this, "���Ե�...", "��ȡ������...", true);
			}
		};
		
		Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil loginTask=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(loginTask.getRequest());
		progressDialog = ProgressDialog.show(LoginActivity.this, "���Ե�...", "��ȡ������...", true);
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
	//����������Ĵ���
    public void uid(View v){
   
    	 AlertDialog.Builder b=new Builder(LoginActivity.this);
		 b.setTitle("��ʾ");
		 b.setMessage("������Ϊ:"+getUniqueId());
		 b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
			@Override
		    public void onClick(DialogInterface arg0, int arg1) {    					
				arg0.dismiss();		
			}			    				
		 });	
		 b.create().show();	 	
	}
	//���ע�ᰴť��Ĵ���
    public void register(View v){
    	//ת����¼����
		Intent i=new Intent(LoginActivity.this,Reg1Activity.class);
		startActivity(i);
	}
}
