package com.zhtx.app;

import java.util.HashMap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Company;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Reg1Activity extends Activity {
	
	//����ؼ�
	private EditText comidet;
	private Button btncancel,btnnext;
	//����volley����
	private RequestQueue mQueue;
	private ProgressDialog progressDialog = null;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_1);
        
        //���volley�������
        mQueue= Volley.newRequestQueue(Reg1Activity.this);
        mQueue.start();
        //�ҵ�����Ҫ�Ŀؼ�������һ���ı��༭ ������ť
        comidet=(EditText)findViewById(R.id.comidet);
        btncancel=(Button)findViewById(R.id.btncancel);
        btnnext=(Button)findViewById(R.id.btnnext);
        
	}
	
	//���ȡ��������activity,���ص�¼ҳ��
	public void cancel(View v){
		finish();
	}
	
	//�����һ��
	public void tonext(View v){
		String comid=comidet.getText().toString();
		//�ж�����id�ǲ���6λ����
		if (isValid(comid)){
			//����������ҳ��url
			String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
			//��post������װ��Ҫ���͵���Ϣ(��˾id)
			HashMap<String, String> map=new HashMap<String, String>();
			map.put("action", "info");
			map.put("comid",comid);
			//���ڷ��������ؽ���Ĵ���
			Response.Listener<String> listener=new Response.Listener<String>(){

				@Override
				public void onResponse(String response) {
					if (response==null){
					    Toast.makeText(getApplicationContext(), "���ʷ�����ʧ��", Toast.LENGTH_SHORT).show();
					    }else{
						    if (response.equals("0")){
							    Toast.makeText(getApplicationContext(), "id������", Toast.LENGTH_SHORT).show();
						        }else{
						        //�����õ���json�ַ���Ϊһ��company����
						        Gson gson=new Gson();
						        Company com=gson.fromJson(response, Company.class);
						        //�ѹ�˾��Ϣ��Ϊextra����ͬʱ���������û����Ͻ���
						        Intent i=new Intent(Reg1Activity.this,Reg2Activity.class);
						        i.putExtra("company_name",com.getName());
						        i.putExtra("comid", com.getId());
						        i.putExtra("dba_id", com.getDba_id());
						        i.putExtra("db_name",com.getDb_name());
						    	startActivity(i);
						}	
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
			VolleySRUtil Reg1Task=new VolleySRUtil(url,map,listener,elistener);	
			mQueue.add(Reg1Task.getRequest());
			
			progressDialog = ProgressDialog.show(Reg1Activity.this, "���Ե�...", "��ȡ������...", true);
			
		}else{
			Toast.makeText(getApplicationContext(), "id����", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isValid(String comid) {
		if (comid.length()!=6) return false;
		for (int i=0; i<comid.length();i++)
			if (!Character.isDigit(comid.charAt(i))) return false;
		return true;
	}
}
