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
	//����volley����
	private RequestQueue mQueue;
	//����require����
	private Require mrequire;
	//��������button�ؼ�
	private Button btnreject,btnapprove;
	//����ҳ�涥��������textview�ؼ�
	private TextView titletv,sendertv,timetv;
	//����ҳ���в���������ʾ�ؼ�
	private TextView datatv;
	private Intent i;
	//����������
	private String name="δ֪";
	//��ǵ����ť�������Ƿ����
	private boolean task1=false,task2=false,task3=false,task4=false;
	private EditText dialoget;
	private ProgressDialog progressDialog = null;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue= Volley.newRequestQueue(RequireDetailActivity.this);
        mQueue.start();
        //������������ͣ��Ӷ�ȷ����ʾ������
        i=getIntent();
        String json=i.getStringExtra("require");
        Gson gson=new Gson();
        mrequire=gson.fromJson(json, Require.class);
        
        //�����������Ϳ�ʼ�趨�����
        switch (mrequire.getAction()){
        //����Ϊ���빫˾����
        case 1:{
        	setContentView(R.layout.require_joincompany);
        	doJoinCompany();
        	break;
        }
        //����Ϊ��ٻ��߳���
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
        //case����
        } 
	}

	//���빫˾����ģ��
	private void doJoinCompany() {
		//����Ҫ�Ŀؼ���ʼ��
		btnreject=(Button)findViewById(R.id.btnreject);
		btnapprove=(Button)findViewById(R.id.btnapprove);
		titletv=(TextView)findViewById(R.id.titletv);
		sendertv=(TextView)findViewById(R.id.sendertv);
		timetv=(TextView)findViewById(R.id.timetv);
		datatv=(TextView)findViewById(R.id.datatv);
		
		//��mrequire�е���Ϣ����ؼ�
		titletv.setText(Constant.requireAction.get(String.valueOf(mrequire.getAction())));
		timetv.setText(sdf.format(mrequire.getTime()));
		name=mrequire.getData();
		sendertv.setText("������:"+name);
		datatv.setText("����:"+name);
		
		//���ð�ťЧ��
		btnreject.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder b=new Builder(RequireDetailActivity.this);
				b.setTitle("��ʾ");
				b.setMessage("��ȷ��Ҫ���ظ�������");
				b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//��һ�����޸��������и�������״̬Ϊ����
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
						//�ڶ�����ɾ���û����������û�����Ϣ
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
					    progressDialog = ProgressDialog.show(RequireDetailActivity.this, "���Ե�...", "��ȡ������...", true);
					    mQueue.add(rejectTask2.getRequest());
						
					}
					
				});
				b.setNegativeButton("ȡ��", new  DialogInterface.OnClickListener(){

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
				b.setTitle("��ʾ");
				b.setMessage("��ȷ��Ҫͬ���������");
				b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						 progressDialog = ProgressDialog.show(RequireDetailActivity.this, "���Ե�...", "��ȡ������...", true);
						//��һ�����޸�������״̬Ϊͬ��
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
						//�ڶ������޸��û����������û���״̬Ϊ����
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
						//������������������Ϣ����Ա����
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
				b.setNegativeButton("ȡ��", new  DialogInterface.OnClickListener(){

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
	
	//������ٺͳ����ģ��
	private void doAttendence(){
		//����Ҫ�Ŀؼ���ʼ��
		btnreject=(Button)findViewById(R.id.btnreject);
		btnapprove=(Button)findViewById(R.id.btnapprove);
		titletv=(TextView)findViewById(R.id.titletv);
		sendertv=(TextView)findViewById(R.id.sendertv);
		timetv=(TextView)findViewById(R.id.timetv);
		datatv=(TextView)findViewById(R.id.datatv);
				
		//��mrequire�е���Ϣ����ؼ�
		titletv.setText(Constant.requireAction.get(String.valueOf(mrequire.getAction())));
		timetv.setText(sdf.format(mrequire.getTime()));
		name=i.getStringExtra("name");
		sendertv.setText("������:"+name);
		datatv.setText(mrequire.getData());
		
		//��ťЧ��
		btnreject.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder b=new Builder(RequireDetailActivity.this);
				b.setTitle("��ʾ");
				b.setMessage("��ȷ��Ҫ���ظ�������");
				dialoget=new EditText(b.getContext());
				b.setView(dialoget);
				b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String data=dialoget.getText().toString();
						arg0.dismiss();
						//��һ�����޸�������״̬Ϊ����
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
					    progressDialog = ProgressDialog.show(RequireDetailActivity.this, "���Ե�...", "��ȡ������...", true);
					    mQueue.add(rejectTask1.getRequest());
					    
						
					}	
					
				});
				b.setNegativeButton("ȡ��", new  DialogInterface.OnClickListener(){

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
				b.setTitle("��ʾ");
				b.setMessage("��ȷ��Ҫͬ���������");
				b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						 progressDialog = ProgressDialog.show(RequireDetailActivity.this, "���Ե�...", "��ȡ������...", true);
						//��һ�����޸�������״̬Ϊͬ��
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
					    //�ڶ������޸�ǩ�����������û���״̬
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
							    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
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
				b.setNegativeButton("ȡ��", new  DialogInterface.OnClickListener(){

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
    		Toast.makeText(getApplicationContext(), "�������", Toast.LENGTH_SHORT).show();
    		finish();
    	}
    };
	
	
}
