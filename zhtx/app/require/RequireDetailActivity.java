package com.zhtx.app.require;

import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zhtx.app.NavigationActivity;
import com.zhtx.app.R;
import com.zhtx.app.R.id;
import com.zhtx.app.R.layout;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Require;
import com.zhtx.myclass.RequireCard;
import com.zhtx.myclass.Staff;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RequireDetailActivity extends Activity {
	
	private final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	//定义volley队列
	private RequestQueue mQueue;
	//定义require对象
	private Require mrequire;
	//定义两个button控件
	private Button btnreject,btnapprove;
	//定义页面顶部的三个textview控件
	private TextView titletv,sendertv,timetv;
	//定义页面中部的数据显示控件
	private TextView datatv;
	private Intent i;
	//申请人姓名
	private String name="未知";
	//标记点击按钮后任务是否完成
	private boolean task1=false,task2=false,task3=false,task4=false;
	private EditText dialoget;
	private ProgressDialog progressDialog = null;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue= Volley.newRequestQueue(RequireDetailActivity.this);
        mQueue.start();
        //获得审批的类型，从而确定显示的内容
        i=getIntent();
        String json=i.getStringExtra("require");
        Gson gson=new Gson();
        mrequire=gson.fromJson(json, Require.class);
        
        //根据审批类型开始设定活动内容
        switch (mrequire.getAction()){
        //类型为加入公司申请
        case 1:{
        	setContentView(R.layout.require_joincompany);
        	doJoinCompany();
        	break;
        }
        //类型为请假或者出差
        case 2:{
        	setContentView(R.layout.require_joincompany);
        	doAttendence();
        	break;
        }
        case 3:{
        	setContentView(R.layout.require_joincompany);
        	doAttendence();
        	break;
        }
        //case结束
        } 
	}

	//加入公司申请模块
	private void doJoinCompany() {
		//对需要的控件初始化
		btnreject=(Button)findViewById(R.id.btnreject);
		btnapprove=(Button)findViewById(R.id.btnapprove);
		titletv=(TextView)findViewById(R.id.titletv);
		sendertv=(TextView)findViewById(R.id.sendertv);
		timetv=(TextView)findViewById(R.id.timetv);
		datatv=(TextView)findViewById(R.id.datatv);
		
		//把mrequire中的信息放入控件
		titletv.setText(Constant.requireAction.get(String.valueOf(mrequire.getAction())));
		timetv.setText(sdf.format(mrequire.getTime()));
		name=mrequire.getData();
		sendertv.setText("发起人:"+name);
		datatv.setText("姓名:"+name);
		
		//设置按钮效果
		btnreject.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder b=new Builder(RequireDetailActivity.this);
				b.setTitle("提示");
				b.setMessage("您确定要驳回该申请吗？");
				b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//第一步，修改审批表中该审批的状态为结束
					  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					  	HashMap<String, String> map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","end");
					    map.put("id",String.valueOf(mrequire.getId())); 
						Response.Listener<String> listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task1=true;
							    	if (task2) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
						VolleySRUtil rejectTask1=new VolleySRUtil(url,map,listener,elistener);	
					    mQueue.add(rejectTask1.getRequest());
						//第二步，删除用户表中申请用户的信息
					    url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
					  	map=new HashMap<String, String>();
						map.put("action","delete");
					    map.put("id",String.valueOf(mrequire.getSender())); 
						listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task2=true;
							    	if (task1) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
							    }
						    }				
					    };	
					    elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				                progressDialog.dismiss();
				                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
				            }  
				        };
						VolleySRUtil rejectTask2=new VolleySRUtil(url,map,listener,elistener);	
					    progressDialog = ProgressDialog.show(RequireDetailActivity.this, "请稍等...", "获取数据中...", true);
					    mQueue.add(rejectTask2.getRequest());
						
					}
					
				});
				b.setNegativeButton("取消", new  DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
					
				});
			    b.create().show();	
			}	
		});
		
		btnapprove.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder b=new Builder(RequireDetailActivity.this);
				b.setTitle("提示");
				b.setMessage("您确定要同意该申请吗？");
				b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						 progressDialog = ProgressDialog.show(RequireDetailActivity.this, "请稍等...", "获取数据中...", true);
						//第一步，修改审批表状态为同意
					  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					  	HashMap<String, String> map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","end");
					    map.put("id",String.valueOf(mrequire.getId())); 
						Response.Listener<String> listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task1=true;
							    	if (task2&&task3) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
						VolleySRUtil aprroveTask1=new VolleySRUtil(url,map,listener,elistener);	;
					    mQueue.add(aprroveTask1.getRequest());
						//第二步，修改用户表中申请用户的状态为激活
					    url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
					  	map=new HashMap<String, String>();
						map.put("action","pass");
					    map.put("id",String.valueOf(mrequire.getSender())); 
						listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task2=true;
							    	if (task1&&task3) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
							    }
						    }				
					    };	
					    elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				                progressDialog.dismiss();
				                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
				            }  
				        };
						VolleySRUtil aprroveTask2=new VolleySRUtil(url,map,listener,elistener);	;
					    mQueue.add(aprroveTask2.getRequest());
						//第三步，把申请人信息加入员工表
					    url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
					  	map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","insert");
					    map.put("id",String.valueOf(mrequire.getSender())); 
					    map.put("name", name);
					    String id_card = "";
					    for (int i=0; i<mrequire.getData().length();i++)
				    	    if (Character.isDigit(mrequire.getData().charAt(i))) {
				    	    	id_card=mrequire.getData().substring(i+1);
				    	    	break;
				    	    }
					    map.put("id_card", id_card);
						listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task3=true;
							    	if (task1&&task2) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
							    }
						    }				
					    };	
					    elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				                progressDialog.dismiss();
				                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
				            }  
				        };
						VolleySRUtil aprroveTask3=new VolleySRUtil(url,map,listener,elistener);	;
					    mQueue.add(aprroveTask3.getRequest());
					}
					
				});
				b.setNegativeButton("取消", new  DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
					
				});
			    b.create().show();	
			}	
		});
	}
	
	//审批请假和出差的模块
	private void doAttendence(){
		//对需要的控件初始化
		btnreject=(Button)findViewById(R.id.btnreject);
		btnapprove=(Button)findViewById(R.id.btnapprove);
		titletv=(TextView)findViewById(R.id.titletv);
		sendertv=(TextView)findViewById(R.id.sendertv);
		timetv=(TextView)findViewById(R.id.timetv);
		datatv=(TextView)findViewById(R.id.datatv);
				
		//把mrequire中的信息放入控件
		titletv.setText(Constant.requireAction.get(String.valueOf(mrequire.getAction())));
		timetv.setText(sdf.format(mrequire.getTime()));
		name=i.getStringExtra("name");
		sendertv.setText("发起人:"+name);
		datatv.setText(mrequire.getData());
		
		//按钮效果
		btnreject.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder b=new Builder(RequireDetailActivity.this);
				b.setTitle("提示");
				b.setMessage("您确定要驳回该申请吗？");
				dialoget=new EditText(b.getContext());
				b.setView(dialoget);
				b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String data=dialoget.getText().toString();
						arg0.dismiss();
						//第一步，修改审批表状态为驳回
					  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					  	HashMap<String, String> map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","reject");
					    map.put("id",String.valueOf(mrequire.getId())); 
					    map.put("info", mrequire.getData()+"rj^"+data);
						Response.Listener<String> listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	mhandler.sendEmptyMessage(0);
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
						VolleySRUtil rejectTask1=new VolleySRUtil(url,map,listener,elistener);	
					    progressDialog = ProgressDialog.show(RequireDetailActivity.this, "请稍等...", "获取数据中...", true);
					    mQueue.add(rejectTask1.getRequest());
					    
						
					}	
					
				});
				b.setNegativeButton("取消", new  DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
					
				});
			    b.create().show();	
			}	
		});
		
		btnapprove.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder b=new Builder(RequireDetailActivity.this);
				b.setTitle("提示");
				b.setMessage("您确定要同意该申请吗？");
				b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						 progressDialog = ProgressDialog.show(RequireDetailActivity.this, "请稍等...", "获取数据中...", true);
						//第一步，修改审批表状态为同意
					  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					  	HashMap<String, String> map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","approve");
					    map.put("id",String.valueOf(mrequire.getId())); 
						Response.Listener<String> listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task1=true;
							    	if (task2) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
						VolleySRUtil aprroveTask1=new VolleySRUtil(url,map,listener,elistener);
					    mQueue.add(aprroveTask1.getRequest());
					    //第二步，修改签到表中申请用户的状态
					    url="http://"+Constant.ip+"/ZhtxServer/AttendenceServlet";
					  	map=new HashMap<String, String>();
						map.put("action","status");
						map.put("db", i.getStringExtra("db"));
					    map.put("userid",String.valueOf(mrequire.getSender())); 
					    if (mrequire.getAction()==2) {
					    	map.put("status", String.valueOf(4));
					    }else{
					    	map.put("status", String.valueOf(1));
					    }
						listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task2=true;
							    	if (task1) {
							    		mhandler.sendEmptyMessage(0);
							    	}
							    }else{
							    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
							    }
						    }				
					    };	
					    elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				                progressDialog.dismiss();
				                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
				            }  
				        };
						VolleySRUtil aprroveTask2=new VolleySRUtil(url,map,listener,elistener);
					    mQueue.add(aprroveTask2.getRequest());
					}
					
				});
				b.setNegativeButton("取消", new  DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
					
				});
			    b.create().show();	
			}	
		});
	}
	
	private Handler mhandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		progressDialog.dismiss();
    		NavigationActivity.task[mrequire.getAction()]--;
    		Toast.makeText(getApplicationContext(), "处理完成", Toast.LENGTH_SHORT).show();
    		finish();
    	}
    };
	
	
}
