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
import com.zhtx.myclass.AccountType;
import com.zhtx.myclass.Product;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RequireProductList extends AppCompatActivity {
	private EditText searchet;
	private ListView productlv;
	private List<Product> productList,productList2,productFilteredList;
	private List<String> pList,pFilteredList;
	private RequestQueue mQueue;
	private BaseAdapter mAdapter;
	private Intent i;
	private int now;
	private boolean status=false;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private String table;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_requireproductlist);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        productlv=(ListView)findViewById(R.id.productlv);
        searchet=(EditText)findViewById(R.id.searchet);
        i=getIntent();
        table=i.getStringExtra("table");
        if (table.equals("sell")) {
        	getSupportActionBar().setTitle("选择客户");
        	searchet.setHint("检索客户名");
        }
        else{
        	getSupportActionBar().setTitle("选择供应商");
        	searchet.setHint("检索供应商");
        }
               
        mQueue= Volley.newRequestQueue(RequireProductList.this);
        mQueue.start();
 
        productList=new ArrayList<Product>();
        productFilteredList=new ArrayList<Product>();
        productList2=new ArrayList<Product>();
        pList=new ArrayList<String>();
        pFilteredList=new ArrayList<String>();
        
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,pFilteredList);
        productlv.setAdapter(mAdapter);
        
        searchet.addTextChangedListener(new TextWatcher() { 
			@Override  
			public void onTextChanged(CharSequence s, int start, int before, int count){
				pFilteredList.clear();
				if (status){
					productFilteredList.clear();
				}
				if (s.toString().length() != 0) {     
					int index=0;
					for (String p:pList){
						if (p.indexOf(s.toString())!=-1){
	  						pFilteredList.add(p);
	  						if (status) productFilteredList.add(productList2.get(index));
	  					}	
						index++;
					}
  				    mAdapter.notifyDataSetChanged();
				}else {    
					int index=0;
					for (String p:pList){
						pFilteredList.add(p);
						if (status) productFilteredList.add(productList2.get(index));		
						index++;
					}
  				    mAdapter.notifyDataSetChanged();
				}  
			}  
			@Override  	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}  
			@Override  
			public void afterTextChanged(Editable s) {    
				
			}
		});
        
        getdata();
	}
    
    private void getdata(){
    	
    	String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action", "ainfo");
    	map.put("db",i.getStringExtra("db"));
    	map.put("table", table);
    	Response.Listener<String> listener=new Response.Listener<String>(){
    		@Override
    		public void onResponse(String response) {
    			progressDialog.dismiss();
    			if ((response!=null)&&(!response.equals("0"))){
    			    Gson gson=new Gson();
    			    Type type=new TypeToken<List<Product>>(){}.getType();
    				productList=gson.fromJson(response, type);
   					if (productList.size()==0){
   						Toast.makeText(getApplicationContext(), "当前没有商品",Toast.LENGTH_SHORT).show();
   					}else{
   						String last="";
   						sortP();
   						for (Product p:productList){
   	
   							String s=p.getClient();
   							if (!s.equals(last)) {
   								pList.add(s);
   								pFilteredList.add(s);
   								last=s;
   							}
   						}
   						mAdapter.notifyDataSetChanged();
   						productlv.setOnItemClickListener(new ItemClickEvent());
   					}
    			}else{
    			    Toast.makeText(getApplicationContext(), "获取数据失败",Toast.LENGTH_SHORT).show();	
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
    	progressDialog = ProgressDialog.show(RequireProductList.this, "请稍等...", "获取数据中...", true);
    	mQueue.add(infoTask.getRequest());	
    		
    }
    
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (!status){
				status=true;
				now=arg2;
				getSupportActionBar().setTitle("选择商品(类型)");	
				String c=pFilteredList.get(now);
				productList2.clear();
				for (Product pr:productList)
				  	if (c.equals(pr.getClient()))
				  	    productList2.add(pr);			  	    
				pList.clear();
				pFilteredList.clear();
			  	for (Product pr:productList2)
			  	  if (c.equals(pr.getClient())){
			  		pList.add(pr.getName()+"("+pr.getType()+")");	  		
			  	}  
			  	searchet.setText("");
				searchet.setHint("检索商品");
			  	mAdapter.notifyDataSetChanged();	  	
			}else{
				RequireDetail.product=productFilteredList.get(arg2);
			    finish();
			}	
	    }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!status){
            	finish();
            }else{
            	status=false;
            	
				String last="";
				pList.clear();
				for (Product p:productList){
						String s=p.getClient();
						if (!s.equals(last)) {
							pList.add(s);
							last=s;
						}
				}
				searchet.setText("");
			    if (table.equals("sell")) {
			    	getSupportActionBar().setTitle("选择客户");
                 	searchet.setHint("检索客户名"); 	
	            } else{
	            	getSupportActionBar().setTitle("选择供应商");
                 	searchet.setHint("检索供应商"); 	
	            }
				mAdapter.notifyDataSetChanged();
				return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void sortP(){
    	int n=productList.size();
    	for (int i=0;i<n-1;i++)
    	    for (int j=i+1; j<n; j++)
    	    {
    	    	String s=productList.get(i).getClient();
    	    	String s2=productList.get(j).getClient();
    	    	if (s.compareTo(s2)<0){
    	    		Product p= productList.get(i);
    	    		productList.remove(i);
    	    		productList.add(i, productList.get(j-1));
        	    	productList.remove(j);
        	    	productList.add(j,p);
    	    	}
    	    	
    	    }
    }
}

