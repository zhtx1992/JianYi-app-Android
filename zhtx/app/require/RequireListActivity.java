package com.zhtx.app.require;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhtx.app.NavigationActivity;
import com.zhtx.app.R;
import com.zhtx.app.R.id;
import com.zhtx.app.R.layout;
import com.zhtx.app.adapter.RequireCardAdapter;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Require;
import com.zhtx.myclass.RequireCard;
import com.zhtx.myclass.Staff;
import com.zhtx.myclass.StepRight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//�����б�ģ��
public class RequireListActivity extends  AppCompatActivity {
	
	private RequestQueue mQueue;
	private ListView requirelv;
	private EditText searchet;
	private TextView pagetv;
	private Intent i,riseIntent;
	//�洢������Ϣ�Ķ���
	private List<Require> requireList;
	//�洢�������������ֵĶ���
 	private List<Staff> senderList;
	private List<StepRight> stepRightList;
	//�洢������Ƭ��Ϣ�Ķ���
	private List<RequireCard> cardList;
	private RequireCardAdapter mAdapter;
	private Require r;
	private int count=0;
	private int now=0,act=0;
	private Button btnsend,btnnext,btnprev,btnfilter;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private int pageNow,pageNum;
	private String filter;
	private boolean task1,task2;
	private int btnColor=0xFFFF66CC;
	private int btnColor2=0xFFD3D3D3;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requirelist);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("�����б�");
        //��ʼ��
        mQueue= Volley.newRequestQueue(RequireListActivity.this);
        mQueue.start();
        requireList=new ArrayList<Require>();
        cardList=new ArrayList<RequireCard>();
        senderList=new ArrayList<Staff>();
        stepRightList=new ArrayList<StepRight>();
        count=0;
        //���view
        requirelv=(ListView) findViewById(R.id.requirelv);
        btnsend=(Button)findViewById(R.id.btnsend);
        btnnext=(Button)findViewById(R.id.btnnext);
        btnprev=(Button)findViewById(R.id.btnprev);
        btnfilter=(Button)findViewById(R.id.btnfilter);
        searchet=(EditText)findViewById(R.id.searchet);
        pagetv=(TextView)findViewById(R.id.pagetv);
        btnprev.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pageNow-=1;
				btnnext.setClickable(true);
				btnnext.setBackgroundColor(btnColor);
				if (pageNow==1) {
					btnprev.setClickable(false);
					btnprev.setBackgroundColor(btnColor2);
				}
				checkdata();
			}
		});
        btnnext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pageNow+=1;
				btnprev.setClickable(true);
				btnprev.setBackgroundColor(btnColor);
				if (pageNow == (pageNum-1) /10 +1 ) {
					btnnext.setClickable(false);
					btnnext.setBackgroundColor(btnColor2);
				}
				checkdata();
			}
		});
        btnfilter.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				filter=searchet.getText().toString();
				pageNow=1;
				checkdata();			
			}
		}); 
        //�õ������洫��������Ϣ
        i=getIntent();
        //�õ��б�������ʾ�������͵�����
        act=Integer.parseInt(i.getStringExtra("act"));
        if (act>3) {
        	//�жϵ�ǰ�û�����Ȩ�޷�������
        	isSendAble();  
        }
        //�趨listview��������
        mAdapter=new RequireCardAdapter(this, cardList);  
        requirelv.setAdapter(mAdapter);
        filter="";
        pageNow=1;
        btnprev.setClickable(false);
        btnnext.setClickable(false);
	}
	
	private void isSendAble() {
		String url="http://"+Constant.ip+"/ZhtxServer/StepRightServlet";

		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "info");
		map.put("db",i.getStringExtra("db"));
		map.put("tab", "right_"+getProcedureType(i.getStringExtra("act")));
        map.put("step","1");
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
					StepRight sr=gson.fromJson(response, StepRight.class);
                    String s=sr.getStaff();
                    if ((s.indexOf(i.getStringExtra("userid")+"+")==0)||(s.indexOf("+"+i.getStringExtra("userid")+"+")!=-1)){
                    	btnsend.setVisibility(View.VISIBLE);
                    }
			    }	
			}
		};
		Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil info1Task=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(info1Task.getRequest());	
		
	}

	//��ҳ�����»ص�ǰ̨ʱ�����������б�
    @Override  
    protected void onResume() {  
        super.onResume();  
        checkdata();
    }  
    
	//�̳�OnItemClickListener��������Ŀ�������ʱ�򴥷�
    private class ItemClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			//��ȡ��Ӧ��require����r
			r=requireList.get(arg2);
			if (r.getStatus()==0){
			    //������ת����ϸҳ���intent
				if (r.getAction()<4){
					Intent intent=new Intent(RequireListActivity.this,RequireDetailActivity.class);
				    Gson gson=new Gson();
				    String json=gson.toJson(r);
				    for (Staff st:senderList){
    		        	if (st.getId()==r.getSender()){
    		        		intent.putExtra("name", st.getName());
    		        		break;
    		        	}
    		        }
				    intent.putExtra("require", json);
				    intent.putExtra("db", i.getStringExtra("db"));
				    intent.putExtra("reactive", false);
				    startActivity(intent);
				}else{
					Intent intent=new Intent(RequireListActivity.this,RequireDetail.class);
				    Gson gson=new Gson();
				    String json=gson.toJson(r);
				    intent.putExtra("require", json);
				    String act="";
				    int step=r.getAction() / 100;
			    	if (i.getStringExtra("act").length()==1) {   		
			    		act=String.valueOf(step)+"0"+i.getStringExtra("act");
			    	}else {
			    		act=String.valueOf(step)+i.getStringExtra("act");
			    	}
			    	intent.putExtra("act", act);
			    	intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("userid", i.getStringExtra("userid"));
				    intent.putExtra("type", getProcedureType(i.getStringExtra("act")));
				    intent.putExtra("reactive", false);
				    startActivity(intent);
				}
			}else{
				now=arg2;
				if (r.getStatus()==2){
				//����ǲ��أ���ʾ��������
				AlertDialog.Builder b=new Builder(RequireListActivity.this);
				b.setTitle("��ʾ");
				String rinfo = null;
				rinfo=r.getData().substring(r.getData().indexOf("rj^")+3);
				b.setMessage(rinfo);
				b.setPositiveButton("ɾ��", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();	
						AlertDialog.Builder c=new Builder(RequireListActivity.this);
						c.setTitle("��ʾ");
						c.setMessage("ȷ��Ҫɾ����");
						c.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								//ɾ���������еĸ�����¼
							  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
							  	HashMap<String, String> map=new HashMap<String, String>();
							  	map.put("db", i.getStringExtra("db"));
								map.put("action","delete");
							    map.put("id",String.valueOf(r.getId())); 
								Response.Listener<String> listener=new Response.Listener<String>(){
							        @Override
								    public void onResponse(String response) {
							        	progressDialog.dismiss();
									    if ((response!=null)&&(response.equals("1"))){
									
									    	checkdata();
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
								VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);
							    progressDialog = ProgressDialog.show(RequireListActivity.this, "���Ե�...", "��ȡ������...", true);
							    progressDialog.setCancelable(true);
							    mQueue.add(endTask.getRequest());
							}
						});
						c.setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						c.create().show();
					}
				});
				if (r.getAction() / 100 == 2){
					 b.setNegativeButton("���·���", new  DialogInterface.OnClickListener(){
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
								//ɾ���������еĸ�����¼,�����·���
							  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
							  	HashMap<String, String> map=new HashMap<String, String>();
							  	map.put("db", i.getStringExtra("db"));
								map.put("action","deleter");
							    map.put("id",String.valueOf(r.getId())); 
								Response.Listener<String> listener=new Response.Listener<String>(){
							        @Override
								    public void onResponse(String response) {
							        	progressDialog.dismiss();
									    if ((response!=null)&&(response.equals("1"))){
									    	Intent intent=new Intent(RequireListActivity.this,RequireDetail.class);
										    Gson gson=new Gson();
										    //ɾ���������ɲ���
										    r.setData(r.getData().substring(0,r.getData().indexOf("rj^")));
										    String json=gson.toJson(r);
										    intent.putExtra("require", json);
										    //intent�д��±�ʾ�������·������Ϣ
										    intent.putExtra("reactive", true);
										    String act="";
									    	if (i.getStringExtra("act").length()==1) {   		
									    		act="10"+i.getStringExtra("act");
									    	}else {
									    		act="1"+i.getStringExtra("act");
									    	}
									    	intent.putExtra("act", act);
									    	intent.putExtra("db", i.getStringExtra("db"));
									    	intent.putExtra("proid", r.getProid());
											intent.putExtra("userid", i.getStringExtra("userid"));
										    intent.putExtra("type", getProcedureType(i.getStringExtra("act")));
										    startActivity(intent);
									    }else{
									    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
									    }
								    }				
							    };	
							    Response.ErrorListener elistener=new Response.ErrorListener(){  
						            @Override  
						            public void onErrorResponse(VolleyError error) {  
						                progressDialog.dismiss();
						                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
						            }  
						        };
								VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);
							    progressDialog = ProgressDialog.show(RequireListActivity.this, "���Ե�...", "��ȡ������...", true);
							    progressDialog.setCancelable(true);
							    mQueue.add(endTask.getRequest());
								
							}
							
						});
				}
               
				b.create().show();
				}else{
					//�����ͬ�⣬�����ֱ����ʧ
				  	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","end");
				    map.put("id",String.valueOf(r.getId())); 
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	checkdata();
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
					VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);
				    progressDialog = ProgressDialog.show(RequireListActivity.this, "���Ե�...", "��ȡ������...", true);
				    progressDialog.setCancelable(true);
				    mQueue.add(endTask.getRequest());
				}
			}
        }  
    }
    
	//ѯ�ʷ�������ǰ�û�����Щ�����������
    private void checkdata() {	
    	requireList.clear();
        cardList.clear();
        senderList.clear();
    	task1=false;
    	task2=false;
      	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
      	HashMap<String, String> map=new HashMap<String, String>();
      	map.put("action","checkPage");
      	map.put("db",i.getStringExtra("db"));
      	map.put("userid",i.getStringExtra("userid"));
      	map.put("type", String.valueOf(act));
      	map.put("page",String.valueOf(pageNow));
      	map.put("filter",filter);
      	//���ڷ��������ؽ���Ĵ���
      	Response.Listener<String> listener=new Response.Listener<String>(){
      		@Override
      		public void onResponse(String response) {
      			
      			if (response.equals("0")){
      				progressDialog.dismiss();
      				Toast.makeText(getApplicationContext(), "��ѯʧ�ܣ����Ժ�����", Toast.LENGTH_SHORT).show();
      				finish();
      			}else{
      				Gson gson=new Gson();
      				Type type=new TypeToken<List<Require>>(){}.getType();
      				requireList=gson.fromJson(response,type);
      				if (requireList.size()==0 && pageNow>1){
      					progressDialog.dismiss();
      					pageNow=pageNow-1;
      					checkdata();
      				}else{
      					task1=true;
          				if (task1&&task2){
          					mhandler1.sendEmptyMessage(0);
          				}     
      				}
      								
      			}
      		}		
      			
      	}; 	
      	 Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
	            }  
	        };
		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);
      	progressDialog = ProgressDialog.show(RequireListActivity.this, "���Ե�...", "��ȡ������...", true);
      	progressDialog.setCancelable(true);
      	mQueue.add(CheckTask.getRequest());
      	
      	url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
      	map=new HashMap<String, String>();
      	map.put("action","checkCount");
      	map.put("db",i.getStringExtra("db"));
      	map.put("userid",i.getStringExtra("userid"));
      	map.put("type", String.valueOf(act));
      	map.put("filter",filter);
      	listener=new Response.Listener<String>(){
      		@Override
      		public void onResponse(String response) {
      			
      			if (response.equals("-1")){
      				progressDialog.dismiss();
      				Toast.makeText(getApplicationContext(), "��ѯʧ�ܣ����Ժ�����", Toast.LENGTH_SHORT).show();
      				finish();
      			}else{
      				pageNum=Integer.parseInt(response);
  		    		NavigationActivity.task[Integer.parseInt(i.getStringExtra("act"))]=pageNum;
  		    		task2=true;
  		    		if (task1&&task2){
      					mhandler1.sendEmptyMessage(0);
      				}     	
      			}
      		}		
      			
      	}; 	
      	elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
	            }  
	        };
		CheckTask=new VolleySRUtil(url,map,listener,elistener);
      	mQueue.add(CheckTask.getRequest());
		
	}   	
    
    //ȡ�����������˵�����
    private Handler mhandler1=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		count=0;
    		for (Require re:requireList){
    			if (re.getAction()==1){
    				count++;
    				if (count==requireList.size()+1){
			    		mhandler2.sendEmptyMessage(0);
			    	}
    				continue;
    			}
    			//ѯ�ʷ�����sender��Ӧ����
    		  	String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    			//��post������װ��Ҫ���͵���Ϣ
    		  	HashMap<String, String> map=new HashMap<String, String>();
    		  	map.put("db", i.getStringExtra("db"));
    			map.put("action","info");
    		    map.put("id",String.valueOf(re.getSender())); 
    		    
    			//���ڷ��������ؽ���Ĵ���
    			Response.Listener<String> listener=new Response.Listener<String>(){
    		        @Override
    			    public void onResponse(String response) {
    				    if (response!=null){
    				    	Gson gson=new Gson();   				    	
    				    	Staff staff=gson.fromJson(response,Staff.class);
    				    	senderList.add(staff);
    				    };
    				    count++;
				    	if (count==requireList.size()+1){
				    		mhandler2.sendEmptyMessage(0);
				    	}
    			    }				
    		    };	
    		    Response.ErrorListener elistener=new Response.ErrorListener(){  
		            @Override  
		            public void onErrorResponse(VolleyError error) { 
		                progressDialog.dismiss();
		                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
		                finish();
		            }  
		        };
				VolleySRUtil askTask=new VolleySRUtil(url,map,listener,elistener);
    		    mQueue.add(askTask.getRequest());
    		}
    		if (act==1){
    			count++;
    			if (count==requireList.size()+1){
		    		mhandler2.sendEmptyMessage(0);
		    	}
    		}else{
    			if (stepRightList.size()==0){
    				String url="http://"+Constant.ip+"/ZhtxServer/StepRightServlet";
        			HashMap<String, String> map=new HashMap<String, String>();
        			map.put("action", "allinfo");
        			map.put("db",i.getStringExtra("db"));
        			map.put("tab", "right_"+getProcedureType(i.getStringExtra("act")));

        			Response.Listener<String> listener=new Response.Listener<String>(){

        				@Override
        				public void onResponse(String response) {
        				    if ((response!=null)&&(!response.equals("0"))){
        				    	Gson gson=new Gson();
        						Type type=new TypeToken<List<StepRight>>(){}.getType();
        						stepRightList=gson.fromJson(response, type);
        						count++;
        						if (count==requireList.size()+1){
        				    		mhandler2.sendEmptyMessage(0);
        				    	}
        				    }else{
        				    	Toast.makeText(getApplicationContext(), "��ȡ����ʧ��",Toast.LENGTH_SHORT).show();
        				    	finish();
        				    }			
        				}
        			};
        			Response.ErrorListener elistener=new Response.ErrorListener(){  
        	            @Override  
        	            public void onErrorResponse(VolleyError error) {  
        	                progressDialog.dismiss();
        	                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
        	            }  
        	        };
        			VolleySRUtil info1Task=new VolleySRUtil(url,map,listener,elistener);	
        			mQueue.add(info1Task.getRequest());	 
    			}else{
    				count++;
					if (count==requireList.size()+1){
			    		mhandler2.sendEmptyMessage(0);
			    	}
    			}		 
    		}
    			 
    	}
    };
    //����Ϣ���뿨Ƭ����
    private Handler mhandler2=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		progressDialog.dismiss();
    		pagetv.setText(String.valueOf(pageNow)+"/"+String.valueOf((pageNum-1) / 10 +1));
    		if (pageNow==1){
    			btnprev.setClickable(false);
    			btnprev.setBackgroundColor(btnColor2);
    		}else{
    			btnprev.setClickable(true);
    			btnprev.setBackgroundColor(btnColor);
    		}
    		if (pageNow>=(pageNum-1) / 10 +1){
    			btnnext.setClickable(false);
    			btnnext.setBackgroundColor(btnColor2);
    		}else{
    			btnnext.setClickable(true);
    			btnnext.setBackgroundColor(btnColor);
    		}
    		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    		for (Require re:requireList){
    		    //��������Ϣ�����б�
    		    String actInfo="",timeInfo="",senderInfo="";
    		    if (re.getAction()==1) senderInfo="������:"+re.getData();
    		    else{
    		    	senderInfo="��" +String.valueOf(re.getAction()/100)+"��  "+stepRightList.get(re.getAction()/100-1).getDescribe();
    		    }
    		    if (!((re.getAction()==1)||(re.getAction()==2)||(re.getAction()==3))){
    		    	actInfo="����:"+re.getProid() ;
    		    }else{
    		    	actInfo=Constant.requireAction.get(String.valueOf(re.getAction()));
    		    }
    		  /*  if (re.getAction()==1){
    		    	for (int i=0; i<re.getData().length();i++)
    		    	    if (Character.isDigit(re.getData().charAt(i))) {
    		    	    	senderInfo+=re.getData().substring(0,i);
    		    	    	break;
    		    	    }
    		    }else{
    		        for (Staff st:senderList){
    		        	if (st.getId()==re.getSender()){
    		        		senderInfo+=st.getName();
    		        		break;
    		        	}
    		        }
    		    }*/
    		    timeInfo=sdf.format(re.getTime());
    		    RequireCard mCard=new RequireCard(actInfo,senderInfo,timeInfo,re.getStatus());
    		    cardList.add(mCard);  
    		}
    		mAdapter.notifyDataSetChanged();
    		requirelv.setOnItemClickListener(new ItemClickEvent());
    		
    	}
    };
    
    //���������������ʱ
    public void send(View v){
    	riseIntent=new Intent(RequireListActivity.this,RequireDetail.class);
    	String act="";
    	if (i.getStringExtra("act").length()==1) act="10"+i.getStringExtra("act");
    	else act="1"+i.getStringExtra("act");
    	riseIntent.putExtra("act", act);
    	riseIntent.putExtra("db", i.getStringExtra("db"));
    	riseIntent.putExtra("userid", i.getStringExtra("userid"));
    	riseIntent.putExtra("type", getProcedureType(i.getStringExtra("act")));
    	riseIntent.putExtra("reactive", false);
	    //�����������õ�proid
	    String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
	  	HashMap<String, String> map=new HashMap<String, String>();
	  	map.put("db", i.getStringExtra("db"));
		map.put("action","newproid");
		//���ڷ��������ؽ���Ĵ���
		Response.Listener<String> listener=new Response.Listener<String>(){
	        @Override
		    public void onResponse(String response) {
			    if ((response != null)&&(!response.equals("0"))){
			    	riseIntent.putExtra("proid", response);
			    	startActivity(riseIntent);
			    }else{
			    	Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
			    }
			    progressDialog.dismiss();
		    }				
	    };	
	    Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) { 
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.volley_error2,Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil askTask=new VolleySRUtil(url,map,listener,elistener);
		progressDialog = ProgressDialog.show(RequireListActivity.this, "���Ե�...", "��ȡ������...", true);
		progressDialog.setCancelable(true);
	    mQueue.add(askTask.getRequest());
    }

	private String getProcedureType(String act) {
		int action=Integer.parseInt(act);
		return Constant.action[action-1];
	}
}
