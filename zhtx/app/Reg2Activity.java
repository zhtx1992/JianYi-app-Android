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
	
	//��ʾ��˾��
	private TextView comnametv;
	//�ĸ������ֱ�Ϊ�û��������룬���������֤��
	private EditText usernameet,passwordet,passwordet2, cnnameet;
	private Button btnprev,btnsubmit;
	private RequestQueue mQueue;
    private Intent i;
    //�Ƿ�ɹ�����user��
    private boolean isCreate=false;
    //����user����û�id
    private int newid=0;
    //�Ƿ�ɹ���������
    private boolean isSend=false;
    
    private String username,password,password2,cnname,idcard;
    private ProgressDialog progressDialog = null;
    
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_2);
        
        i=getIntent();
        mQueue= Volley.newRequestQueue(Reg2Activity.this);
        mQueue.start();
        //�ҵ��ؼ�
        comnametv=(TextView)findViewById(R.id.comnametv);
        usernameet=(EditText)findViewById(R.id.usernameet);
        passwordet=(EditText)findViewById(R.id.passwordet);
        passwordet2=(EditText)findViewById(R.id.passwordet2);
        cnnameet=(EditText)findViewById(R.id.cnnameet);
        btnprev=(Button)findViewById(R.id.btnprev);
        btnsubmit=(Button)findViewById(R.id.btnsubmit);
        
        //ȡ��reg1�����Ĺ�˾��������textview
        comnametv.setText("���������"+i.getStringExtra("company_name"));
        
	}
	
	//��� ��һ������reg1
	public void toprev(View v){
		finish();
	}
	
	//����������
	public void submit(View v){
		//�õ�������Ϣ
        username=usernameet.getText().toString();
        password=passwordet.getText().toString();
        password2=passwordet2.getText().toString();
        cnname=cnnameet.getText().toString();
		
		int result=isValid(username,password,password2,cnname);
		
		//У����ȷ
		if (result==0) {
			checkname();
		    btnsubmit.setClickable(false);
		}
		//У�����,��ʾ����ĵط�
		else{
			String error="";
			if (result % 2==1){
				error=error+"�����ʽ����   ";
				passwordet.setText("");
				passwordet2.setText("");
			}
			if ((result % 4)/2==1){
				error=error+"�����������벻һ��  ";
				passwordet.setText("");
				passwordet2.setText("");
			}
			if ((result % 8)/4==1){
				error=error+"��Ҫ��Ϣδ��д";
				passwordet.setText("");
				passwordet2.setText("");
			}
			Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();			
		}
		
	}
	
	//�ж��û����ظ�
	private void checkname() {
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//��post������װ��Ҫ���͵���Ϣ
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","check");
	    map.put("username",username);
		map.put("uid", getUniqueId());
		map.put("comid",i.getStringExtra("comid")); 
		//���ڷ��������ؽ���Ĵ���
	    Response.Listener<String> listener=new Response.Listener<String>(){
			@Override
		    public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	doSubmit();
			    }else{
			        progressDialog.dismiss();
			        Toast.makeText(getApplicationContext(), "�ظ�ע��",Toast.LENGTH_LONG).show();
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
	  	progressDialog = ProgressDialog.show(Reg2Activity.this, "���Ե�...", "��ȡ������...", true);
		
	}

	//У��������Ϣ�ĺϷ���
    private int isValid(String username,String password,String password2,String cnname){
    	int result=0;

    	//У������
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
    	//У����������������Ƿ�һ��
    	if (!password.equals(password2)) result+=2;
    	
    	//���������ͳ��������Ƿ���д
    	if (cnname.equals("0")){
    		result+=4;
    	}
		return result;
    }
    
    //��ϢУ��ɹ����ύ�������˾����Ա
	private void doSubmit() {
		
		//��ʼ��
		isCreate=false;
		isSend=false;
		//�����ݿ��û����м�������û�
		//�͵��������û�����servlet
		String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		//��post������װ��Ҫ���͵���Ϣ
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action","create");
	    map.put("username",username);
		map.put("password",password);
		map.put("comid", i.getStringExtra("comid"));
		map.put("uid", getUniqueId());
		//���ڷ��������ؽ���Ĵ���
	    Response.Listener<String> listener=new Response.Listener<String>(){
			@Override
		    public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	//�����û���ɹ� 
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
	
	//�ж����������Ƿ�ɹ�
	private Handler mhandler2=new Handler(){
		@Override
		public void handleMessage(Message msg){
			progressDialog.dismiss();
			super.handleMessage(msg);
			if ((isSend)&&(isCreate)){
			  	    //��ʾ�ɹ���ʾ��
				    Toast.makeText(getApplicationContext(), "�ύ�ɹ�,��ȴ����", Toast.LENGTH_LONG).show();
				    //���ص�¼����
				    Intent i2=new Intent(Reg2Activity.this,LoginActivity.class);
				    startActivity(i2);
				    finish();
			    }else{
			    	Toast.makeText(getApplicationContext(), "�����ύʧ��...", Toast.LENGTH_SHORT).show();
			    	btnsubmit.setClickable(true);
			    }
		}
	};
	
	
	private Handler mhandler1=new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			//�ڹ�˾��������м���������
		  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
			//��post������װ��Ҫ���͵���Ϣ
		  	HashMap<String, String> map2=new HashMap<String, String>();
		  	map2.put("db", i.getStringExtra("db_name"));
			map2.put("action","insert");
		    map2.put("sender",String.valueOf(newid));
			map2.put("reciver",String.valueOf(i.getIntExtra("dba_id",0)));
			map2.put("previd","0");
			map2.put("act", "1");
			map2.put("data",cnname);
			//���ڷ��������ؽ���Ĵ���
			Response.Listener<String> listener=new Response.Listener<String>(){
		        @Override
			    public void onResponse(String response) {
				    if ((response!=null)&&(response.equals("1"))){
				    	//���������Ϣ�ɹ�
					    isSend=true;
					    
				    }
				    //����Ϣ�������̷߳��ʷ��������
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
