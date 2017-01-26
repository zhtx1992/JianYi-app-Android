package com.zhtx.app;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Staff;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RightManageStaffAdd extends AppCompatActivity {
	private ListView stafflv;
	private List<String> staffNameList;
	private List<Staff> staffList,allStaffList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now,count;
	private int[] qstaff;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanageadd);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("ѡ��Ա��");
        stafflv=(ListView)findViewById(R.id.stafflv);
        
        mQueue= Volley.newRequestQueue(RightManageStaffAdd.this);
        mQueue.start();
        staffNameList =new ArrayList<String>();
        staffList=new ArrayList<Staff>();
        allStaffList=new ArrayList<Staff>();
        qstaff=new int[500];
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,staffNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        getdata();
	}
    
    private void getdata(){
    	//��������Ѿ���Ȩ�޵�Ա��id�洢����
    	String s=RightManageStaffList.staff;
    	count=0;
    	while (s.indexOf("+")!=-1){
    		qstaff[count]=Integer.parseInt(s.substring(0, s.indexOf("+")));
    		if (s.indexOf("+")==s.length()){
    			s="";
    		}else{
    			s=s.substring(s.indexOf("+")+1);
    		}
    		count++;
    	}	
    	//����ȡ������Ա������Ϣ���ٴ���ȥ���Ѿ���Ȩ�޵�Ա��
    	String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action", "allinfo");
    	map.put("db",i.getStringExtra("db"));
    	//���ڷ��������ؽ���Ĵ���
    	Response.Listener<String> listener=new Response.Listener<String>(){
    		@Override
    		public void onResponse(String response) {
    			progressDialog.dismiss();
    			if ((response!=null)&&(!response.equals("0"))){
    			    Gson gson=new Gson();
    			    Type type=new TypeToken<List<Staff>>(){}.getType();
    			    //�õ�ȫ��Ա�����б�
    				allStaffList=gson.fromJson(response, type);
   					for (Staff st:allStaffList){
   						//�жϸ�Ա���Ƿ��Ѿ���Ȩ��
   						boolean b=true;
   						for (int i=0;i<count;i++)
   							if (st.getId()==qstaff[i]) {
   								b=false;
   								break;
   							}
   						if (b) {
   							staffList.add(st);
   							staffNameList.add(st.getName());
   						}
   					}	
   					if (staffList.size()>0)	{
                        mAdapter.notifyDataSetChanged();
     					stafflv.setOnItemClickListener(new ItemClickEvent());
   					}else{
   					    Toast.makeText(getApplicationContext(), "û��Ա�����Ը���Ȩ��",Toast.LENGTH_SHORT).show();
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
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
    	progressDialog = ProgressDialog.show(RightManageStaffAdd.this, "���Ե�...", "��ȡ������...", true);
    	mQueue.add(infoTask.getRequest());	
    		
    }
    
    //������ֿ���ɾ��Ȩ��
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			AlertDialog.Builder b=new Builder(RightManageStaffAdd.this);
			b.setTitle("��ʾ");
			b.setMessage("ȷ��Ҫ��������û�Ȩ����");
			b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();						
					//�ı�staff��
					Staff st=staffList.get(now);
					RightManageStaffList.staff+=String.valueOf(st.getId())+"+";
					//���޸ĺ��staff������������
				  	String url="http://"+Constant.ip+"/ZhtxServer/StepRightServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","change");
				    map.put("stepid",String.valueOf(i.getStringExtra("stepid"))); 
				    map.put("staff", RightManageStaffList.staff);
				    map.put("tab", i.getStringExtra("table"));
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	staffList.remove(now); 
						    	staffNameList.remove(now);
						    	mAdapter.notifyDataSetChanged();
						    }else{
						    	Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
						    }
					    }				
				    };	
				    Response.ErrorListener elistener=new Response.ErrorListener(){  
			            @Override  
			            public void onErrorResponse(VolleyError error) {  
			                progressDialog.dismiss();
			                Toast.makeText(getApplicationContext(),R.string.volley_error1,Toast.LENGTH_LONG).show();
			            }  
			        };
				    VolleySRUtil endTask=new VolleySRUtil(url,map,listener,elistener);
				    progressDialog = ProgressDialog.show(RightManageStaffAdd.this, "���Ե�...", "��ȡ������...", true);
				    mQueue.add(endTask.getRequest());
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
    }
    
}

