package com.zhtx.app;



import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.notification.BasicCustomPushNotification;
import com.alibaba.sdk.android.push.notification.CustomNotificationBuilder;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.bugly.crashreport.CrashReport;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.UpdateManager;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Company;
import com.zhtx.myclass.Staff;
import com.zhtx.myclass.User;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
 
public class NavigationActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {
 
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;;
    private SharedPreferences mSpSettings=null;
    public static User user=new User();
    public static Staff staff=new Staff();
	public static Company com=new Company();
	private RequestQueue mQueue;
	private ProgressDialog progressDialog = null;
	// 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    public static int[] task=new int[100];
    //标志导航活动是否是第一次被创建
    private boolean create=false;
    //标志当前内容是否是我的工作
    private boolean isMyWork=true;
    private int count=0,signstatus=0;
    //记录菜单是否显示
    public static List<Boolean> workList;
    //多任务协同标记
    private boolean task1=false,task2=false;
    //版本号
    private String version;
    //阿里云推送服务
    private CloudPushService pushService;
    public static String deviceId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
      
        //初始化volley队列
        mQueue= Volley.newRequestQueue(NavigationActivity.this);
        mQueue.start();
        //得到登录用户的个人信息以及公司信息
        mSpSettings=getSharedPreferences(NavigationActivity.this.getResources().getString(R.string.UserInfosp),this.MODE_PRIVATE);
		String userjson=mSpSettings.getString("userInfo","");
		String comjson=mSpSettings.getString("comInfo","");
		Gson gson=new Gson();
	    user=gson.fromJson(userjson, User.class);
		com=gson.fromJson(comjson,Company.class);
		workList=new ArrayList<Boolean>();
		//初始化工具栏
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        create=true;
        
        getWorkMenu();
        checkUpdate();
        initCloudChannel(this);
    }
 
    
    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    private void initCloudChannel(Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext,
        		new CommonCallback() {
            @Override
            public void onSuccess(String response) {
            	
                deviceId=pushService.getDeviceId();
                if (!user.getDeviceId().equals(deviceId)){
                	refreshDeviceId();
                }
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.e("navi", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
        BasicCustomPushNotification notification = new BasicCustomPushNotification();
        notification.setRemindType(BasicCustomPushNotification.REMIND_TYPE_SOUND);
        notification.setStatusBarDrawable(R.drawable.ic_launcher);
        boolean res = CustomNotificationBuilder.getInstance().setCustomNotification(1, notification);
        notification = new BasicCustomPushNotification();
        notification.setRemindType(BasicCustomPushNotification.REMIND_TYPE_SILENT);
        notification.setStatusBarDrawable(R.drawable.ic_launcher);
        res = CustomNotificationBuilder.getInstance().setCustomNotification(2, notification);
    }
    //收到通知时回调
    
    
	private void refreshNavi(String response) {
		String s=response.substring(response.indexOf("#")+1);
		for (int i=0;i<100;i++) task[i]=0;
		int act=0;
		count=0;
		while (s.indexOf("+")!=-1){
			count++;
			act=Integer.parseInt(s.substring(0,s.indexOf("+")));
			task[act%100]++;
			if (s.indexOf("+")<s.length())
				s=s.substring(s.indexOf("+")+1);
			else s="";
		}
		signstatus=Integer.parseInt(response.substring(0,response.indexOf("#")));
		if (isMyWork)displayView(0);
	    drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar,
		    staff.getName(),com.getName(),signstatus,count);
	}	
	
    private void initNavi() {
    	progressDialog.dismiss();
    	//初始化抽屉菜单
        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar,
        		staff.getName(),com.getName(),signstatus,count);
        drawerFragment.setDrawerListener(NavigationActivity.this);
        if (isMyWork)displayView(0);
		
	}
    
    @Override
    protected void onResume() {
        super.onResume();
        //如果导航活动是第一次开启
        if (create){
        	create=false;
        	//第一步得到员工信息
        	progressDialog = ProgressDialog.show(NavigationActivity.this, "请稍等...", "获取数据中...", true);
        	String url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";
    		HashMap<String, String> map=new HashMap<String, String>();
    		map.put("action", "info");
    		map.put("db",com.getDb_name());
    		map.put("id", String.valueOf(user.getId()));
    		Response.Listener<String> listener=new Response.Listener<String>(){

    			@Override
    			public void onResponse(String response) {
    				
    			    if ((response!=null)&&(!response.equals("0"))){
    			    	Gson gson=new Gson();
    					staff=gson.fromJson(response, Staff.class);
    					task1=true;
    					if (task1&&task2){
    						initNavi();
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
                    Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
                }  
            };
    		VolleySRUtil infoTask=new VolleySRUtil(url,map,listener,elistener);		
    		mQueue.add(infoTask.getRequest());	
    		//第二步得到待处理任务数
        	
        	url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
          	map=new HashMap<String, String>();
          	map.put("action","getnum");
          	map.put("db",com.getDb_name());
          	map.put("userid",String.valueOf(user.getId()));
          	listener=new Response.Listener<String>(){
          		@Override
          		public void onResponse(String response) {
          			if (!(response==null)&&(!response.substring(response.indexOf("#")+1).equals("0"))){	
          				String s=response.substring(response.indexOf("#")+1);
          				for (int i=0;i<100;i++) task[i]=0;
          				int act=0;
          				count=0;
          				while (s.indexOf("+")!=-1){
          					count++;
          					act=Integer.parseInt(s.substring(0,s.indexOf("+")));
          					task[act%100]++;
          					if (s.indexOf("+")<s.length())
          						s=s.substring(s.indexOf("+")+1);
          					else s="";
          				}
          				signstatus=Integer.parseInt(response.substring(0,response.indexOf("#")));
          				task2=true;
    					if (task1&&task2){
    						initNavi();
    					}
          			}else{
          				if (!(response==null)){
          					count=0;
              				signstatus=Integer.parseInt(response.substring(0,response.indexOf("#")));
              				for (int i=0;i<100;i++) task[i]=0;
              				task2=true;
        					if (task1&&task2){
        						initNavi();
        					}
          				}else{
          					progressDialog.dismiss();
          					Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
          				}
          				
          			}
          		}	
          	}; 	
          	elistener=new Response.ErrorListener(){  
                @Override  
                public void onErrorResponse(VolleyError error) {  
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
                }  
            };
    		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);		
          	mQueue.add(CheckTask.getRequest());
        }else{
        
        	progressDialog = ProgressDialog.show(NavigationActivity.this, "请稍等...", "获取数据中...", true);
        	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
          	HashMap<String, String> map=new HashMap<String, String>();
          	map.put("action","getnum");
          	map.put("db",com.getDb_name());
          	map.put("userid",String.valueOf(user.getId()));
          	Response.Listener<String> listener=new Response.Listener<String>(){
          		@Override
          		public void onResponse(String response) {
          			progressDialog.dismiss();
          			if (!(response==null)&&(!response.substring(response.indexOf("#")+1).equals("0"))){	
          				refreshNavi(response);
          			}else{
          				//没有任务时的处理
          				if (!(response==null)){
          					count=0;
              				signstatus=Integer.parseInt(response.substring(0,response.indexOf("#")));
              				for (int i=0;i<100;i++) task[i]=0;
              				if (isMyWork)displayView(0);
              				drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar,
              				    staff.getName(),com.getName(),signstatus,count);
          				}else{
          					 Toast.makeText(getApplicationContext(), "更新状态失败，请检查网络",Toast.LENGTH_LONG).show();
          				}
          				
          			}
          		}	
          	}; 	
          	Response.ErrorListener elistener=new Response.ErrorListener(){  
                @Override  
                public void onErrorResponse(VolleyError error) {  
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
                }  
            };
    		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);		
          	mQueue.add(CheckTask.getRequest());
        }
        
        
        
    }
    
    private void checkUpdate() {
    	 String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		 HashMap<String, String> map=new HashMap<String, String>();
		 map.put("action", "ver");
		 map.put("version",String.valueOf(Constant.version));
		 Response.Listener<String> listener=new Response.Listener<String>(){
		     @Override
			 public void onResponse(String response) {
				 if (response==null){
				     Toast.makeText(NavigationActivity.this, "检查版本信息失败", Toast.LENGTH_SHORT).show(); 
				 }else{
				     if (response.equals("1")){
				         isExit=false;
				     }else{
				    	 List<String> list=new ArrayList<String>();
						 Gson gson=new Gson();
						 Type type=new TypeToken<List<String>>(){}.getType();
						 list=gson.fromJson(response, type);
						 version=list.get(0);
						 if (list.size()>1){
							 if (list.get(1).equals("m")){
								 AlertDialog.Builder b=new Builder(NavigationActivity.this);
					    		 b.setTitle("有新版本");
					    		 b.setMessage("您的版本已经无法使用");
					    		 b.setPositiveButton("下载更新", new DialogInterface.OnClickListener(){
					    			@Override
					    		    public void onClick(DialogInterface arg0, int arg1) {    					
					    				arg0.dismiss();	
					    				Intent intent = new Intent();        
					    			    intent.setAction("android.intent.action.VIEW");    
					    			    Uri content_url = Uri.parse("http://www.onless.cn/ZhtxServer/jianyi"+version+".apk");   
					    			    intent.setData(content_url);  
					    			    startActivity(intent);
					    			}			    				
					    		 });	
					    		 b.setNegativeButton("关闭", new DialogInterface.OnClickListener(){
						    			@Override
						    		    public void onClick(DialogInterface arg0, int arg1) {    					
						    				finish();
						    	            System.exit(0);	
						    			}			    				
						    		 });
					    		 b.create().show();	 								 
							 }
							 else{
								 Toast.makeText(NavigationActivity.this, "有新版本，请及时去设置页面更新",Toast.LENGTH_LONG).show();	
							 }
						 }else{
							 Toast.makeText(NavigationActivity.this, "有新版本，请及时去设置页面更新",Toast.LENGTH_LONG).show();	 
						 }
					     						 
				     }
		         }
		    }
	    };
	    Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                Toast.makeText(NavigationActivity.this, "检查版本信息失败",Toast.LENGTH_SHORT).show();
            }  
        };
     	VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);	
     	mQueue.add(deleteTask.getRequest());			
	}

    //更新deviceId
    private void refreshDeviceId(){
    	 String url="http://"+Constant.ip+"/ZhtxServer/UserDaoServlet";
		 HashMap<String, String> map=new HashMap<String, String>();
		 map.put("action", "refreshDeviceId");
		 map.put("id",String.valueOf(user.getId()));
		 map.put("deviceId", deviceId);
		 Response.Listener<String> listener=new Response.Listener<String>(){
       		@Override
       		public void onResponse(String response) {
       			if ((response==null)||(response.equals("0"))){
       				Log.e("Navi", "refreshDeviceId failed");
       			}
       		}	
       	}; 	
       	Response.ErrorListener elistener=new Response.ErrorListener(){  
             @Override  
             public void onErrorResponse(VolleyError error) {  
                 Log.e("Navi", "refreshDeviceId failed");
             }  
         };
 		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);		
       	mQueue.add(CheckTask.getRequest());
		
    }
    
	private void getWorkMenu() {
    	String url="http://"+Constant.ip+"/ZhtxServer/ComInfoServlet";
       	HashMap<String, String> map=new HashMap<String, String>();
       	map.put("action","work");
       	map.put("db",NavigationActivity.com.getDb_name());
       	Response.Listener<String> listener=new Response.Listener<String>(){
       		@Override
       		public void onResponse(String response) {

       			if (!(response==null)&&(!response.equals("0"))){	
       				Gson gson=new Gson();
     				Type type=new TypeToken<List<Boolean>>(){}.getType();
       			    workList=gson.fromJson(response, type);
       			}
       		}	
       	}; 	
       	Response.ErrorListener elistener=new Response.ErrorListener(){  
             @Override  
             public void onErrorResponse(VolleyError error) {  
  
             }  
         };
 		VolleySRUtil CheckTask=new VolleySRUtil(url,map,listener,elistener);		
       	mQueue.add(CheckTask.getRequest());
		
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
 
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
 
        return super.onOptionsItemSelected(item);
    }
 
    //抽屉菜单监听器
    @Override
    public void onDrawerItemSelected(View view, int position) {
        switch (position){
        case 0:{
        	displayView(0);
        	break;
        }
        case 1:{
        	displayView(1);
        	break;
        }
        case 2:{
        	displayView(2);
        	break;
        }
        //点击 退出登录 按钮
        case 3:{
        	//修改自动登录文件的值
    		mSpSettings=getSharedPreferences(NavigationActivity.this.getResources().getString(R.string.AutoLoginsp),MODE_PRIVATE);
    	    Editor edit=mSpSettings.edit();
    	    edit.putBoolean("isAutoLogin", false);
    	    edit.commit();
    	    //返回到登录界面
    	    Intent i=new Intent(NavigationActivity.this,LoginActivity.class);
    	    startActivity(i);
    	    finish();
        	break;
        }
        }
    }
    
    //切换当前内容fragment的方法
    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:{
            	fragment=new FragmentMyWork();
            	isMyWork=true;
                break;
            }
            case 1:{
            	fragment=new FragmentQuery();
            	title="我发起的流程";
            	isMyWork=false;
                break;
            }
            case 2:{
            	fragment = new FragmentConfig();     
            	title="设置";
            	isMyWork=false;
                break;
            }
            default:
                break;
        }
        
        if (fragment != null) {
        	FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();
            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    

    private void exit() {
        if (!isExit) {
            isExit = true;
           
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mExitHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }
    Handler mExitHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };


}