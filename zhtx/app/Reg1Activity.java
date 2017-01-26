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
	
	//定义控件
	private EditText comidet;
	private Button btncancel,btnnext;
	//定义volley队列
	private RequestQueue mQueue;
	private ProgressDialog progressDialog = null;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_1);
        
        //获得volley请求队列
        mQueue= Volley.newRequestQueue(Reg1Activity.this);
        mQueue.start();
        //找到所需要的控件，包括一个文本编辑 两个按钮
        comidet=(EditText)findViewById(R.id.comidet);
        btncancel=(Button)findViewById(R.id.btncancel);
        btnnext=(Button)findViewById(R.id.btnnext);
        
	}
	
	//点击取消，结束activity,返回登录页面
	public void cancel(View v){
		finish();
	}
	
	//点击下一步
	public void tonext(View v){
		String comid=comidet.getText().toString();
		//判断输入id是不是6位数字
		if (isValid(comid)){
			//服务器处理页面url
			String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
			//用post方法封装需要发送的信息(公司id)
			HashMap<String, String> map=new HashMap<String, String>();
			map.put("action", "info");
			map.put("comid",comid);
			//对于服务器返回结果的处理
			Response.Listener<String> listener=new Response.Listener<String>(){

				@Override
				public void onResponse(String response) {
					if (response==null){
					    Toast.makeText(getApplicationContext(), "访问服务器失败", Toast.LENGTH_SHORT).show();
					    }else{
						    if (response.equals("0")){
							    Toast.makeText(getApplicationContext(), "id不存在", Toast.LENGTH_SHORT).show();
						        }else{
						        //解析得到的json字符串为一个company对象
						        Gson gson=new Gson();
						        Company com=gson.fromJson(response, Company.class);
						        //把公司信息作为extra量，同时启动创建用户资料界面
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
			
			progressDialog = ProgressDialog.show(Reg1Activity.this, "请稍等...", "获取数据中...", true);
			
		}else{
			Toast.makeText(getApplicationContext(), "id错误", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isValid(String comid) {
		if (comid.length()!=6) return false;
		for (int i=0; i<comid.length();i++)
			if (!Character.isDigit(comid.charAt(i))) return false;
		return true;
	}
}
