package com.zhtx.app.require;

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
import com.zhtx.app.R;
import com.zhtx.app.R.id;
import com.zhtx.app.R.layout;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Staff;

import android.app.ProgressDialog;
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

public class RequireStaffList extends AppCompatActivity {
	private ListView stafflv;
	private List<String> staffNameList;
	private List<Staff> staffList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
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
        
        mQueue= Volley.newRequestQueue(RequireStaffList.this);
        mQueue.start();
        staffNameList =new ArrayList<String>();
        staffList=new ArrayList<Staff>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,staffNameList);
        stafflv.setAdapter(mAdapter);
        
        i=getIntent();
        
        getdata();
	}
    
    private void getdata(){
        
		//����������ҳ��url
		String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
		//��post������װ��Ҫ���͵���Ϣ(��˾id)
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
					staffList=gson.fromJson(response,type);
					int count=0;
					for (Staff st:staffList){
						count++;
						staffNameList.add(String.valueOf(count)+". "+st.getName());
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
	    progressDialog = ProgressDialog.show(RequireStaffList.this, "���Ե�...", "��ȡ������...", true);
		mQueue.add(infoTask.getRequest());	
    		    	
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			RequireDetail.staff=staffList.get(arg2);
			finish();
		}
    }
}


