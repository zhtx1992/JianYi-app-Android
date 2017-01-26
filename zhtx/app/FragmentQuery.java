package com.zhtx.app;

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
import com.zhtx.app.adapter.RequireCardAdapter;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.RequireCard;
import com.zhtx.myclass.RequireStatus;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FragmentQuery extends Fragment {
	
	private EditText searchet;
	private RequestQueue mQueue;
	private ListView requirelv;
	private TextView pagetv;
	private List<RequireStatus> requireList;
	//存储审批卡片信息的队列
	private List<RequireCard> cardList;
	private RequireCardAdapter mAdapter;
	private Button btnprev,btnnext,btnfilter;
	private boolean day1=true,first=true;
	private ProgressDialog progressDialog = null;
	private RequireStatus requireStatus;
	private boolean task1=false,task2=false;
	private int pageNow,pageNum;
	private String filter;
	private int btnColor=0xFFFF66CC;
	private int btnColor2=0xFFD3D3D3;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.fragment_query, container, false);
        requirelv=(ListView)layout.findViewById(R.id.requirelv);
        pagetv=(TextView)layout.findViewById(R.id.pagetv);
        btnprev=(Button)layout.findViewById(R.id.btnprev);
        btnnext=(Button)layout.findViewById(R.id.btnnext);
        btnfilter=(Button)layout.findViewById(R.id.btnfilter);
        searchet=(EditText)layout.findViewById(R.id.searchet);
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
				setPage(pageNow);
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
				setPage(pageNow);
			}
		});
        btnfilter.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				filter=searchet.getText().toString();
				setData();			
			}
		});
        mQueue= Volley.newRequestQueue(getActivity());
        mQueue.start();
        requireList=new ArrayList<RequireStatus>();
        cardList=new ArrayList<RequireCard>();
        mAdapter=new RequireCardAdapter(getActivity(), cardList);  
        requirelv.setAdapter(mAdapter);
        btnprev.setClickable(false);
        btnnext.setClickable(false);
        btnprev.setBackgroundColor(btnColor2);
        btnnext.setBackgroundColor(btnColor2);
        filter="";
        setData();
        return layout;
    }

    @Override
    public void onResume(){
    	super.onResume();
    }
    
    private void FilterList(String s){
    	
    }
    
    private void setPage(int page){
    	cardList.clear();
		String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
      	HashMap<String, String> map=new HashMap<String, String>();
      	map.put("action","mrequirePage");
      	map.put("db",NavigationActivity.com.getDb_name());
      	map.put("userid",String.valueOf(NavigationActivity.user.getId()));
      	map.put("filter",filter);
      	map.put("page",String.valueOf(page));
      	//对于服务器返回结果的处理
      	Response.Listener<String> listener=new Response.Listener<String>(){
      		@Override
      		public void onResponse(String response) {
      			
      			if (response.equals("0")){
      				progressDialog.dismiss();
      				Toast.makeText(getActivity(), "未找到记录", Toast.LENGTH_SHORT).show();
      				cardList.clear();
      				mAdapter.notifyDataSetChanged();
      			}else{
      				progressDialog.dismiss();
      				pagetv.setText(String.valueOf(pageNow)+"/"+String.valueOf((pageNum-1) / 10 + 1));
      				Gson gson=new Gson();
      				Type type=new TypeToken<List<RequireStatus>>(){}.getType();
      				requireList=gson.fromJson(response,type);
      				if  (requireList.size()!=0) {				
      					DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
      					for (RequireStatus rs:requireList){		
      						RequireCard mCard=new RequireCard(Constant.actionName[rs.getAct()-1],"单号:"+rs.getProid(),sdf.format(rs.getTime()),rs.getStatus());
          	    		    cardList.add(mCard);  
      					}		
      				}
      			    mAdapter.notifyDataSetChanged();
 	        		requirelv.setOnItemClickListener(new ItemClickEvent());
      			}
      		}		
      			
      	}; 	
        //生成StringRequest工具类实例，并加入volley队列
      	 Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getActivity(), R.string.volley_error2,Toast.LENGTH_LONG).show();
	            }  
	     };
		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);
      	progressDialog = ProgressDialog.show(getActivity(), "请稍等...", "获取数据中...", true);
      	mQueue.add(CheckTask.getRequest());	
    }
    
	private void setData() {
		task1=false;
		task2=false;
		cardList.clear();
		String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
      	HashMap<String, String> map=new HashMap<String, String>();
      	map.put("action","mrequireCount");
      	map.put("db",NavigationActivity.com.getDb_name());
      	map.put("userid",String.valueOf(NavigationActivity.user.getId()));
      	map.put("filter",filter);
      	//对于服务器返回结果的处理
      	Response.Listener<String> listener=new Response.Listener<String>(){
      		@Override
      		public void onResponse(String response) {
      			
      			if (response.equals("0")){
      				progressDialog.dismiss();
      				Toast.makeText(getActivity(), "未找到记录", Toast.LENGTH_SHORT).show();
      				cardList.clear();
      				mAdapter.notifyDataSetChanged();
      			}else{
      				task1=true;
      				pageNum=Integer.parseInt(response);
      				if (task1&&task2){
      					mhandler.sendEmptyMessage(1);
      				}
      			}
      		}		
      			
      	}; 	
        //生成StringRequest工具类实例，并加入volley队列
      	 Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getActivity(), R.string.volley_error2,Toast.LENGTH_LONG).show();
	            }  
	     };
		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);
      	progressDialog = ProgressDialog.show(getActivity(), "请稍等...", "获取数据中...", true);
      	mQueue.add(CheckTask.getRequest());	
		
      	url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
      	map=new HashMap<String, String>();
      	map.put("action","mrequirePage");
      	map.put("db",NavigationActivity.com.getDb_name());
      	map.put("userid",String.valueOf(NavigationActivity.user.getId()));
      	map.put("page","1");
      	map.put("filter",filter);
      	//对于服务器返回结果的处理
      	listener=new Response.Listener<String>(){
      		@Override
      		public void onResponse(String response) {
      			
      			if (response.equals("0")){
      				progressDialog.dismiss();
      				Toast.makeText(getActivity(), "未找到记录", Toast.LENGTH_SHORT).show();
      				cardList.clear();
      				mAdapter.notifyDataSetChanged();
      			}else{
      				task2=true;
      				Gson gson=new Gson();
      				Type type=new TypeToken<List<RequireStatus>>(){}.getType();
      				requireList=gson.fromJson(response,type);
      				if  (requireList.size()!=0) {				
      					DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
      					for (RequireStatus rs:requireList){		
      						RequireCard mCard=new RequireCard(Constant.actionName[rs.getAct()-1],"单号:"+rs.getProid(),sdf.format(rs.getTime()),rs.getStatus());
          	    		    cardList.add(mCard);  
      					}		
      				}
      				if (task1&&task2){
      					mhandler.sendEmptyMessage(1);
      				}
      			}
      			
      		}		
      			
      	}; 	
      	elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	                progressDialog.dismiss();
	                Toast.makeText(getActivity(), R.string.volley_error2,Toast.LENGTH_LONG).show();
	            }  
	        };
	    VolleySRUtil CheckTask2=new VolleySRUtil(url,map,listener,elistener);
      	mQueue.add(CheckTask2.getRequest());	
	}	    
	 
	private Handler mhandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		switch (msg.what){
    		case 1:{
    			progressDialog.dismiss();
    		    task1=false;
    		    task2=false;
    		    pageNow=1;
    		    pagetv.setText(String.valueOf(pageNow)+"/"+String.valueOf((pageNum-1) / 10 + 1));
    		    mAdapter.notifyDataSetChanged();
        		requirelv.setOnItemClickListener(new ItemClickEvent());
        		btnprev.setClickable(false);
        		btnprev.setBackgroundColor(btnColor2);
        		if ((pageNum-1) / 10>0){
        			btnnext.setClickable(true);
        			btnnext.setBackgroundColor(btnColor);
        		}
    			break;
    		}
    		}
    	}
     };
	 private class ItemClickEvent implements OnItemClickListener{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				requireStatus=requireList.get(arg2);
				if (requireStatus.getStatus()==3){
					//如果还在进行，显示进度
					String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
			      	HashMap<String, String> map=new HashMap<String, String>();
			      	map.put("action","proidstatus");
			      	map.put("db",NavigationActivity.com.getDb_name());
			      	map.put("proid",requireStatus.getProid());
			      	//对于服务器返回结果的处理
			      	Response.Listener<String> listener=new Response.Listener<String>(){
			      		@Override
			      		public void onResponse(String response) {
			      			progressDialog.dismiss();
			      			AlertDialog.Builder b=new Builder(getActivity());
							b.setTitle("流程状态");
							if ((response==null)||(response.equals("0"))) b.setMessage("获取流程信息失败");
							else {
								int pos=response.indexOf("&");
								String s="当前进度:"+response.substring(0,pos)+"\n"+"审批人:"+response.substring(pos+1);
								b.setMessage(s);
							}
							b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();						
								}	
							});
							b.create().show();
			      		}		
			      			
			      	}; 	
			      	Response.ErrorListener elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				                progressDialog.dismiss();
				                Toast.makeText(getActivity(), R.string.volley_error2,Toast.LENGTH_LONG).show();
				            }  
				        };
					VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);
			      	progressDialog = ProgressDialog.show(getActivity(), "请稍等...", "获取数据中...", true);
			      	mQueue.add(CheckTask.getRequest());	
				}else{
					//如果已经完成，显示record表中的结果
					String url="http://"+Constant.ip+"/ZhtxServer/RecordServlet";
			      	HashMap<String, String> map=new HashMap<String, String>();
			      	map.put("action","query");
			      	map.put("db",NavigationActivity.com.getDb_name());
			      	map.put("proid",requireStatus.getProid());
			      	map.put("act", String.valueOf(requireStatus.getAct()));
			      	//对于服务器返回结果的处理
			      	Response.Listener<String> listener=new Response.Listener<String>(){
			      		@Override
			      		public void onResponse(String response) {
			      			progressDialog.dismiss();
			      			AlertDialog.Builder b=new Builder(getActivity());
							b.setTitle("流程记录");
							if ((response==null)||(response.equals("0"))) b.setMessage("获取流程信息失败");
							else {
								//根据规则取出数据
								int count=0;
								String s="";
								while (response.indexOf(";")!=-1){
									count++;
									if (count>1) s+="\n";
									s+="记录"+String.valueOf(count)+"\n"+"\n";
									String r=response.substring(0,response.indexOf(";"));
									while (r.indexOf("&")!=-1){
										s+=r.substring(0,r.indexOf("&"))+"\n";
										if (r.indexOf("&")<r.length()) r=r.substring(r.indexOf("&")+1);
										else r="";
									}
									if (response.indexOf(";")<response.length()) response=response.substring(response.indexOf(";")+1);
									else response="";
								}
								b.setMessage(s);
							}
							b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();						
								}	
							});
							b.create().show();
			      		}		
			      			
			      	}; 	
			      	Response.ErrorListener elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				                progressDialog.dismiss();
				                Toast.makeText(getActivity(),R.string.volley_error2,Toast.LENGTH_LONG).show();
				            }  
				        };
					VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);
			      	progressDialog = ProgressDialog.show(getActivity(), "请稍等...", "获取数据中...", true);
			      	mQueue.add(CheckTask.getRequest());	
				}
                
	        }  
	 }
	 
	 
	 
}
