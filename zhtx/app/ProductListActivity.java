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
import com.zhtx.app.adapter.ProductAdapter;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.Functions;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Product;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ProductListActivity extends AppCompatActivity {
	private EditText searchet;
	private ListView productlv;
	private TextView clienttv;
	private Button btnadd;
	private List<Product> productList,pList,pFilteredList;
	private RequestQueue mQueue;
	private ProductAdapter mAdapter;
	private Intent i;
	private int now,count;
	private View addView;
	private String name,type,price,client;
	private ProgressDialog progressDialog = null;
	private Toolbar mToolbar;
	private boolean ac;
	//记录当前是显示名字 还是详细信息
	private boolean isname=true;
	private String nowName="";
	private String table;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_productlist);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("商品列表");
        
        productlv=(ListView)findViewById(R.id.productlv);
        btnadd=(Button)findViewById(R.id.btnadd);
        searchet=(EditText)findViewById(R.id.searchet);
        clienttv=(TextView)findViewById(R.id.clienttv);
        
        mQueue= Volley.newRequestQueue(ProductListActivity.this);
        mQueue.start();
 
        productList=new ArrayList<Product>();
        pList=new ArrayList<Product>();
        pFilteredList=new ArrayList<Product>();
        
        mAdapter=new ProductAdapter(this,pFilteredList);
        productlv.setAdapter(mAdapter);
        
        searchet.addTextChangedListener(new TextWatcher() { 
				@Override  
				public void onTextChanged(CharSequence s, int start, int before, int count){
					if (s.toString().length() != 0) {     
						pFilteredList.clear();
						if (isname){
							for (Product p:pList)
			  					if (p.getClient().indexOf(s.toString())!=-1){
			  						pFilteredList.add(p);
			  					}
						}else{
							for (Product p:pList)
			  					if (p.getName().indexOf(s.toString())!=-1){
			  						pFilteredList.add(p);
			  					}
						}
	  				    mAdapter.notifyDataSetChanged();
					}else {    
						pFilteredList.clear();
						for (Product p:pList){
							pFilteredList.add(p);
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
        
        i=getIntent();
        table=i.getStringExtra("table");
        if (table.equals("sell")) clienttv.setText("客户");
        else clienttv.setText("供应商");
        
        getdata();
	}
    
    private void getdata(){
    	isname=true;
    	if (table.equals("sell")) searchet.setHint("检索客户名");
    	else searchet.setHint("检索供应商名");
    	pList.clear();
    	String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
    	HashMap<String, String> map=new HashMap<String, String>();
    	map.put("action", "allinfo");
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
                        String lastName="";
   						for (Product p:productList)
   						if (!p.getClient().equals(lastName)){
   							lastName=p.getClient();
   							Product pr=new Product();
   							pr.setId(p.getId());
   							pr.setName("");
   						    pr.setPrice(0);
   							pr.setType("");
   							pr.setClient(p.getClient());
   							pr.setAccount(true);
   							pList.add(pr);
   							pFilteredList.add(pr);
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
                Toast.makeText(getApplicationContext(),R.string.volley_error2,Toast.LENGTH_LONG).show();
            }  
        };
		VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);	
    	mQueue.add(infoTask.getRequest());	
    	progressDialog = ProgressDialog.show(ProductListActivity.this, "请稍等...", "获取数据中...", true);	
    }
    
    //点击名字可以删除
    private class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (isname){
				nowName=pFilteredList.get(arg2).getClient();
				isname=false;
				pList.clear();
				pFilteredList.clear();
				for (Product p:productList)
				    if (p.getClient().equals(nowName)){
							Product pr=new Product();
							pr.setId(p.getId());
							pr.setName(p.getName());
							pr.setPrice(p.getPrice());
						    pr.setType(p.getType());
							pr.setClient(p.getClient());
							pr.setAccount(p.isAccount());
							pList.add(pr);
					} 
				searchet.setHint("检索商品名");
				searchet.setText("");
				mAdapter.notifyDataSetChanged();
			}else{
				now=arg2;
				AlertDialog.Builder b=new Builder(ProductListActivity.this);
				b.setTitle("提示");
				b.setMessage("您要做什么操作？");
				b.setPositiveButton("删除", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();						
					  	String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
					  	HashMap<String, String> map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","delete");
						map.put("table", table);
	                    map.put("id", String.valueOf(pFilteredList.get(now).getId()));
						Response.Listener<String> listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
					        	progressDialog.dismiss();
							    if ((response!=null)&&(response.equals("1"))){	
							    	int loc=0;
							    	for (Product p:productList){
							    		if (p.getId()==pList.get(now).getId()){
											break;
										} 
							    		loc++;
							    	}	   
							    	productList.remove(loc);
							    	pList.clear();
									for (Product p:productList)
									    if (p.getClient().equals(nowName)){
												Product pr=new Product();
												pr.setId(p.getId());
												pr.setName(p.getName());
												pr.setPrice(p.getPrice());
											    pr.setType(p.getType());
												pr.setClient(p.getClient());
												pr.setAccount(p.isAccount());
												pList.add(pr);
									} 
							    	pFilteredList.remove(now); 
							    	mAdapter.notifyDataSetChanged();
							    	Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
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
						VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);	
					    mQueue.add(deleteTask.getRequest());
					    progressDialog = ProgressDialog.show(ProductListActivity.this, "请稍等...", "获取数据中...", true);
					}
				});
				b.setNeutralButton("修改",new  DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
						AlertDialog.Builder c=new Builder(ProductListActivity.this);
						c.setTitle("修改信息");
						addView=LayoutInflater.from(c.getContext()).inflate(R.layout.sell_productadd, null);  
						if (!table.equals("sell")) {
							EditText et=(EditText)addView.findViewById(R.id.clientet);
							et.setHint("供应商");		
						}
					    c.setView(addView);
					    EditText et=(EditText)addView.findViewById(R.id.nameet);
					    et.setText(pList.get(now).getName());
					    et=(EditText)addView.findViewById(R.id.typeet);
					    et.setText(pList.get(now).getType());
					    et=(EditText)addView.findViewById(R.id.priceet);
					    et.setText(String.valueOf(pList.get(now).getPrice()));
					    et=(EditText)addView.findViewById(R.id.clientet);
					    et.setText(pList.get(now).getClient());
					    CheckBox cb=(CheckBox)addView.findViewById(R.id.accountcb);
					    if (pList.get(now).isAccount()) cb.setChecked(true);
					    c.setPositiveButton("确定", new  DialogInterface.OnClickListener(){
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								EditText et=(EditText)addView.findViewById(R.id.nameet);
								name=et.getText().toString();
								et=(EditText)addView.findViewById(R.id.typeet);
								type=et.getText().toString();
								et=(EditText)addView.findViewById(R.id.priceet);
								price=et.getText().toString();
								et=(EditText)addView.findViewById(R.id.clientet);
								client=et.getText().toString();
							    CheckBox cb=(CheckBox)addView.findViewById(R.id.accountcb);
							    ac=cb.isChecked();
								arg0.dismiss();
								if (Functions.isDouble(price) && (Double.parseDouble(price) != 0)){
									String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
								  	HashMap<String, String> map=new HashMap<String, String>();
								  	map.put("db", i.getStringExtra("db"));
									map.put("action","change");
				                    map.put("id", String.valueOf(pList.get(now).getId()));
				                    map.put("name",name);
				                    map.put("type", type);
				                    map.put("price",price);
				                    map.put("table", table);
				                    map.put("client",client);
				                    if (ac) map.put("account", "1");
				                    else map.put("account", "0");
									Response.Listener<String> listener=new Response.Listener<String>(){
								        @Override
									    public void onResponse(String response) {
								        	progressDialog.dismiss();
										    if ((response!=null)&&(response.equals("1"))){
										    	int loc=0;
										    	for (Product p:productList){
										    		if (p.getId()==pList.get(now).getId()){
														break;
													} 
										    		loc++;
										    	}	   
										    	pFilteredList.get(now).setName(name);
										    	pFilteredList.get(now).setType(type);
										    	pFilteredList.get(now).setPrice(Double.parseDouble(price));
										    	pFilteredList.get(now).setClient(client);
										    	pFilteredList.get(now).setAccount(ac);
										    	productList.get(loc).setName(name);
										    	productList.get(loc).setType(type);
										    	productList.get(loc).setPrice(Double.parseDouble(price));
										    	productList.get(loc).setClient(client);
										    	productList.get(loc).setAccount(ac);
										    	pList.clear();
												for (Product p:productList)
												    if (p.getClient().equals(nowName)){
															Product pr=new Product();
															pr.setId(p.getId());
															pr.setName(p.getName());
															pr.setPrice(p.getPrice());
														    pr.setType(p.getType());
															pr.setClient(p.getClient());
															pr.setAccount(p.isAccount());
															pList.add(pr);
												} 
										    	mAdapter.notifyDataSetChanged();
										    	Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
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
									VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);	
								    mQueue.add(deleteTask.getRequest());
								    progressDialog = ProgressDialog.show(ProductListActivity.this, "请稍等...", "获取数据中...", true);
								}else{
									Toast.makeText(getApplicationContext(), "商品信息有误",Toast.LENGTH_LONG).show();
								}
								
							}
							
						});
					    c.setNegativeButton("取消", new  DialogInterface.OnClickListener(){
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								arg0.dismiss();
							}
							
						});
						c.create().show();
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
			
	    }
    }

    public void add(View v){
    	AlertDialog.Builder c=new Builder(ProductListActivity.this);
		c.setTitle("填写信息");
		addView=LayoutInflater.from(c.getContext()).inflate(R.layout.sell_productadd, null);  
		if (!table.equals("sell")) {
			EditText et=(EditText)addView.findViewById(R.id.clientet);
			et.setHint("供应商");		
		}
	    c.setView(addView);
	    c.setPositiveButton("确定", new  DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				EditText et=(EditText)addView.findViewById(R.id.nameet);
				name=et.getText().toString();
				et=(EditText)addView.findViewById(R.id.typeet);
				type=et.getText().toString();
				et=(EditText)addView.findViewById(R.id.priceet);
				price=et.getText().toString();
				et=(EditText)addView.findViewById(R.id.clientet);
				client=et.getText().toString();
				CheckBox cb=(CheckBox)addView.findViewById(R.id.accountcb);
				ac=cb.isChecked();
				arg0.dismiss();
				if (Functions.isDouble(price) && (Double.parseDouble(price) != 0)) {
					String url="http://"+Constant.ip+"/ZhtxServer/ProductServlet";
				  	HashMap<String, String> map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","insert");
	                map.put("name",name);
	                map.put("type", type);
	                map.put("price",price);
	                map.put("table", table);
	                map.put("client", client);
	                if (ac) map.put("account", "1");
	                else map.put("account", "0");
					Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
				        	progressDialog.dismiss();
						    if ((response!=null)&&(!response.equals("0"))){
						    	Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show();
						    	productList.clear();
						    	pList.clear();
						    	pFilteredList.clear();
						    	getdata();
						    }else{
						    	Toast.makeText(getApplicationContext(), "处理失败", Toast.LENGTH_SHORT).show();
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
					VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);	
				    mQueue.add(infoTask.getRequest());
				    progressDialog = ProgressDialog.show(ProductListActivity.this, "请稍等...", "获取数据中...", true);
				}else{
					Toast.makeText(getApplicationContext(), "商品信息有误",Toast.LENGTH_LONG).show();
				}
				
			}
			
		});
	    c.setNegativeButton("取消", new  DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.dismiss();
			}
			
		});
		c.create().show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isname){
            	return super.onKeyDown(keyCode, event);
            }else{
            	isname=true;
            	pList.clear();
            	String lastName="";
				for (Product p:productList)
				if (!p.getClient().equals(lastName)){
						lastName=p.getClient();
						Product pr=new Product();
						pr.setId(p.getId());
						pr.setName("");
					    pr.setPrice(0);
						pr.setType("");
						pr.setClient(p.getClient());
						pr.setAccount(true);
						pList.add(pr);
				}
				if (table.equals("sell")) searchet.setHint("检索客户名");
		    	else searchet.setHint("检索供应商名");
				searchet.setText("");
				mAdapter.notifyDataSetChanged();
				return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
   
}
