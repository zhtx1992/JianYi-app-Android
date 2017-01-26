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
import com.zhtx.app.util.UpdateManager;
import com.zhtx.app.util.VolleySRUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

 
 
public class FragmentConfig extends PreferenceFragment {
 
	private RequestQueue mQueue;
	private ProgressDialog progressDialog = null;
	private String version,newversion;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences); 
        mQueue= Volley.newRequestQueue(getActivity());
        mQueue.start();
        PackageManager pm = getActivity().getPackageManager();//context为当前Activity上下文 
        PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        version = pi.versionName;
    }
 
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         //如果“保存个人信息”这个按钮被选中，将进行括号里面的操作
         if("change_password".equals(preference.getKey())) {  
        	
        	Intent intent=new Intent(getActivity(),ChangePasswordActivity.class);
			intent.putExtra("id",String.valueOf(NavigationActivity.user.getId()));
			 intent.putExtra("nowPassword",NavigationActivity.user.getPassword());
			 startActivity(intent);
         }
         if("uploadCsv".equals(preference.getKey())) {  
         	if (NavigationActivity.user.getId()==NavigationActivity.com.getDba_id()){
         		
         	}else{
         	   Toast.makeText(getActivity(), "您没有相关权限", Toast.LENGTH_LONG).show(); 
         	}
         	
         }
         if("check_update".equals(preference.getKey())) {          
        	 String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
			 HashMap<String, String> map=new HashMap<String, String>();
			 map.put("action", "ver");
			 map.put("version",String.valueOf(Constant.version));
			 Response.Listener<String> listener=new Response.Listener<String>(){
			     @Override
				 public void onResponse(String response) {
					 progressDialog.dismiss();
					 if (response==null){
					     Toast.makeText(getActivity(), "询问服务器失败", Toast.LENGTH_SHORT).show(); 
					 }else{
					     if (response.equals("1")){
					     AlertDialog.Builder b=new Builder(getActivity());
					     b.setTitle("提示");
					     b.setMessage("已经是最新版本"+version);
			    		 b.setPositiveButton("确定", new DialogInterface.OnClickListener(){
			    			@Override
			    		    public void onClick(DialogInterface arg0, int arg1) {    					
			    				arg0.dismiss();
			    					
			    			}
			    				
			    		 });				
			    		 b.create().show();	 
					 }else{
						 AlertDialog.Builder b=new Builder(getActivity());
			    		 b.setTitle("有新版本");
			    		 String s="是否现在下载更新?";
			    		 List<String> list=new ArrayList<String>();
						 Gson gson=new Gson();
						 Type type=new TypeToken<List<String>>(){}.getType();
						 list=gson.fromJson(response, type);
						 newversion=list.get(0);
			    		 for (String content:list){
			    			 if (!content.equals("m"))
			    			     s+="\n"+content;
			    		 }
			    		 b.setMessage(s);
			    		 b.setPositiveButton("确定", new DialogInterface.OnClickListener(){
			    			@Override
			    		    public void onClick(DialogInterface arg0, int arg1) {    					
			    				arg0.dismiss();
			    			//	UpdateManager um=new UpdateManager(getActivity());
							//	um.showDownloadDialog();		
			    				Intent intent = new Intent();        
			    			    intent.setAction("android.intent.action.VIEW");    
			    			    Uri content_url = Uri.parse("http://www.onless.cn/ZhtxServer/jianyi"+newversion+".apk");   
			    			    intent.setData(content_url);  
			    			    startActivity(intent);
			    			}			    				
			    		 });	
			    		 b.setNegativeButton("取消", new DialogInterface.OnClickListener(){
				    			@Override
				    		    public void onClick(DialogInterface arg0, int arg1) {    					
				    				arg0.dismiss();	
				    			}			    				
				    		 });
			    		 b.create().show();	 								 
					 }
			         }
			    }
		    };
		    Response.ErrorListener elistener=new Response.ErrorListener(){  
                @Override  
                public void onErrorResponse(VolleyError error) {  
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), R.string.volley_error2,Toast.LENGTH_LONG).show();
                }  
            };
	     	VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);	
	     	mQueue.add(deleteTask.getRequest());
	    	progressDialog = ProgressDialog.show(getActivity(), "请稍等...", "获取数据中...", true);					
        }
   /*     if("set_info".equals(preference.getKey())) { 
        	Intent intent=new Intent(getActivity(),SetInfoActivity.class);
			intent.putExtra("id",String.valueOf(NavigationActivity.user.getId()));
			intent.putExtra("bankid",NavigationActivity.staff.getBankid());
			intent.putExtra("platenum",NavigationActivity.staff.getPlatenum());
			startActivity(intent);
        }*/
        return super.onPreferenceTreeClick(preferenceScreen, preference); 
    }
 
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
 
    @Override
    public void onDetach() {
        super.onDetach();
    }
}
