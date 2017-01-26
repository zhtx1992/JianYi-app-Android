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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MoneyProviderManage extends AppCompatActivity {
	private ListView stafflv;
	private List<String> providerList;
	private List<String> pList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now;
	private Button btnadd;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private EditText et;
	private String s;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_rightmanage);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("����������");
        stafflv=(ListView)findViewById(R.id.stafflv);
        btnadd=(Button)findViewById(R.id.btnadd);
        
        mQueue= Volley.newRequestQueue(MoneyProviderManage.this);
        mQueue.start();
        
        providerList =new ArrayList<String>();
        pList=new ArrayList<String>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,providerList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        getdata();
	}
    
    private void getdata(){
    	
        
        String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
		//��post������װ��Ҫ���͵���Ϣ(��˾id)
		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "infop");
		map.put("db",i.getStringExtra("db"));
		//���ڷ��������ؽ���Ĵ���
		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
			    	Type type=new TypeToken<List<String>>(){}.getType();
					pList=gson.fromJson(response, type); 					   
                    for (String s:pList){
                    	providerList.add(s);
                    }
                    mAdapter.notifyDataSetChanged();
                    stafflv.setOnItemClickListener(new ItemClickEvent());
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
                finish();
            }  
        };
	    VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);
	    progressDialog = ProgressDialog.show(MoneyProviderManage.this, "���Ե�...", "��ȡ������...", true);
		mQueue.add(infoTask.getRequest());	
    	
    }
    
    //������ֿ���ɾ��Ȩ��
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			now=arg2;
			AlertDialog.Builder b=new Builder(MoneyProviderManage.this);
			b.setTitle("��ʾ");
			b.setMessage("�Ƿ�Ҫɾ������������");
			b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();						
					//���޸ĺ��staff������������
				  	String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","deletep");
					map.put("content",providerList.get(now));
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(response.equals("1"))){
						    	pList.remove(now); 
						    	providerList.remove(now);
						    	mAdapter.notifyDataSetChanged();
						    	Toast.makeText(getApplicationContext(), "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
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
				    progressDialog = ProgressDialog.show(MoneyProviderManage.this, "���Ե�...", "��ȡ������...", true);
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
    
    //��Ӱ�ť
    public void add(View v){
    	AlertDialog.Builder b=new Builder(MoneyProviderManage.this);
		b.setTitle("���������");
		et=new EditText(b.getContext());
		b.setView(et);
		b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				s=et.getText().toString();
				arg0.dismiss();						
				//���޸ĺ��staff������������
			  	String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
			  	HashMap<String, String> map=new HashMap<String, String>();
			  	map.put("db", i.getStringExtra("db"));
				map.put("action","addp");
				map.put("content",s);
				Response.Listener<String> listener=new Response.Listener<String>(){
			        @Override
				    public void onResponse(String response) {
			        	progressDialog.dismiss();
					    if ((response!=null)&&(response.equals("1"))){
					    	pList.add(s); 
					    	providerList.add(s);
					    	mAdapter.notifyDataSetChanged();
					    	Toast.makeText(getApplicationContext(), "��ӳɹ�", Toast.LENGTH_SHORT).show();
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
			    progressDialog = ProgressDialog.show(MoneyProviderManage.this, "���Ե�...", "��ȡ������...", true);
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
