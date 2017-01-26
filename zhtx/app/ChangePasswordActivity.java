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
        getSupportActionBar().setTitle("�޸�����");
        
        mQueue= Volley.newRequestQueue(ChangePasswordActivity.this);
        //�ҵ��ؼ�
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
			Toast.makeText(ChangePasswordActivity.this, "��ǰ�������", Toast.LENGTH_SHORT).show();
		    nowpasswordet.setText("");
		}else{
			int rs=isValid(password,password2);
			if (rs==0){
				doChange(i.getStringExtra("id"),password);
				if (result) {
					Toast.makeText(ChangePasswordActivity.this, "�����޸ĳɹ�", Toast.LENGTH_SHORT).show();
					finish();
				}else{
					Toast.makeText(ChangePasswordActivity.this, "�޸�ʧ��...", Toast.LENGTH_SHORT).show();
				}
			}else{
				String error="";
				if (rs% 2==1){
					error=error+"�����ʽ����   ";
					passwordet.setText("");
					passwordet2.setText("");
				}
				if ((rs % 4)/2==1){
					error=error+"�����������벻һ��  ";
					passwordet.setText("");
					passwordet2.setText("");
				}
				Toast.makeText(ChangePasswordActivity.this, error, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	//У��������Ϣ�ĺϷ���
    private int isValid(String password,String password2){
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
    	
		return result;
    }
    
    private void doChange(String id,String password){
    	//����������ҳ��url
    	String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
    	//��post������װ��Ҫ���͵���Ϣ(�û�������)
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action","changepw");
    	map.put("id", id);
    	map.put("password",password);
    	//���ڷ��������ؽ���Ĵ���
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
    	progressDialog = ProgressDialog.show(ChangePasswordActivity.this, "���Ե�...", "��ȡ������...", true);
    	mQueue.start();
    }
}
