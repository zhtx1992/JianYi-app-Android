package com.zhtx.app.require;


import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhtx.app.R;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.Functions;
import com.zhtx.app.util.LastInputEditText;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.AccountType;
import com.zhtx.myclass.Account_stock_balance;
import com.zhtx.myclass.Company;
import com.zhtx.myclass.Driver;
import com.zhtx.myclass.Platenum;
import com.zhtx.myclass.Product;
import com.zhtx.myclass.Require;
import com.zhtx.myclass.Staff;
import com.zhtx.myclass.StepDetail;
import com.zhtx.myclass.StepRight;
import com.zhtx.printer.PrintActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RequireDetail extends Activity {
	private SharedPreferences mSpSettings=null,mBlueToothSettings=null;
	private ProgressDialog progressDialog = null;
	private RequestQueue mQueue;
	private List<StepDetail> stepDetailList;
	private List<StepRight> stepRightList;
	private List<String> printList;
	//记录该审批的发起人
	private Staff sender;
	//记录要发给的审批人
	public static Staff reciver=null;
	//记录选择的商品(控件51)
	public static Product product=null;
	//记录选择的会计科目(控件54)
	public static AccountType accounttype=null;
	//记录简单字符串对象(控件81,82,83,55,57,58,61,91)
	public static String stringob=null;
	//记录选择的员工(控件71)
	public static Staff staff=null;
	//记录选择的Account_stock_balance(控件57)
	public static Account_stock_balance asb=null;
	//记录选择的车牌号和司机(控件55)
	public static Platenum platenum=null;
	public static Driver driver=null;
	//用于控件10选择日期
	private DatePicker dp;
	//表单的通用控件
	private Button btnsend,btnapprove,btnreject,btnreciver;
	private TextView recivertv,sendertv,timetv,titletv,proidtv;
	//驳回审批时输入理由用
	private EditText rejectReasonet;
	//表单内容部分的布局
	private LinearLayout mainll;
	//记录上个活动传来的信息
	private Intent i,intent;
	//记录当前位于流程的第几步
	private int step;
	//记录最近操作过的动态生成的按钮的id
	private int btnid;
	//不定数目的网络任务执行时统计已完成的任务个数
	private int count;
	//统计需要录入数据库的次数
	private int tot;
	//打印标记,0表示无需打印,数字代表数据库里的第几个打印数据表, printmust记录打印是否是必须的.
	private int printnum=0;
	private boolean printmust=true;
	//用来协调几个异步任务，记录每个任务完成与否
	private boolean task1=false,task2=false,task3=false,task4=false;
	//记录传入的require
	private Require r;
	//记录新流程的单号
	private String proid;
	//记录打印机蓝牙地址
	private String bt="";
	//统计要填入某个位置的量分别出现了几次，用来确定一共要提交几次
    private int[] sum=new int[99];
    private int[][] locate=new int[99][2];
    //上传图片的result代码
    private final int RESULT_LOAD_IMAGE=1;
    //拍摄照片的result代码
    private final int TAKE_PHOTO_WITH_DATA=2;
    //拍摄照片的存贮文件
    private File cameraFile;
    //记录这是当前步骤第几次上传图片
    private int picnum;
    //记录已上传的文件名
    private List<String> fileNameList; 
    //用到的几种颜色
    private ColorStateList etColor=ColorStateList.valueOf(0xFFFF66CC);
    private ColorStateList normalColor=ColorStateList.valueOf(0xFF000000);
	private int btnColor=0xFFFF66CC;
	private int btnColor2=0xFFD3D3D3;
    //行间距
	private int lineMargin=10;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.require_layout);
        initView();
        mSpSettings=getSharedPreferences("UserInfoSpSettings",this.MODE_PRIVATE);
        mBlueToothSettings=getSharedPreferences("BlueToothSpSettings",this.MODE_PRIVATE);
        i=getIntent();
        step=Integer.parseInt(i.getStringExtra("act")) / 100;
        if (i.getStringExtra("require")!=null){
        	Gson gson=new Gson();
        	r=gson.fromJson(i.getStringExtra("require"), Require.class);
        }
        mQueue= Volley.newRequestQueue(RequireDetail.this);
        
        stepRightList=new ArrayList<StepRight>();
        stepDetailList=new ArrayList<StepDetail>();
        printList=new ArrayList<String>();
        fileNameList=new ArrayList<String>();
        sender=new Staff();
       	picnum=0;
        initStatic();
        //询问服务器得到整个流程的步骤和权限,询问服务器得到当前步骤的具体内容
        getBaseData();    
        
	}
	
	//初始化几个static参数的值
	private void initStatic() {
		reciver=null;
		product=null;
		accounttype=null;
		stringob=null;
		staff=null;
		asb=null;
		platenum=null;
		driver=null;
	}

	private void initView() {
		btnsend=(Button)findViewById(R.id.btnsend);
		btnapprove=(Button)findViewById(R.id.btnapprove);
		btnreject=(Button)findViewById(R.id.btnreject);
		btnreciver=(Button)findViewById(R.id.btnreciver);
		btnreciver.setBackgroundColor(btnColor);
		recivertv=(TextView)findViewById(R.id.recivertv);
		sendertv=(TextView)findViewById(R.id.sendertv);
		timetv=(TextView)findViewById(R.id.timetv);
		titletv=(TextView)findViewById(R.id.titletv);
		proidtv=(TextView)findViewById(R.id.proidtv);
		mainll=(LinearLayout)findViewById(R.id.mainll);
	}

	//询问服务器得到整个流程的步骤和权限,询问服务器得到当前步骤的具体内容
	private void getBaseData() {
		progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "获取数据中...", true);
		//得到流程的步骤和权限
		String url="http://"+Constant.ip+"/ZhtxServer/StepRightServlet";

		HashMap<String, String> map=new HashMap<String, String>();
		map.put("action", "allinfo");
		map.put("db",i.getStringExtra("db"));
		map.put("tab", "right_"+i.getStringExtra("type"));

		Response.Listener<String> listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	Gson gson=new Gson();
					Type type=new TypeToken<List<StepRight>>(){}.getType();
					stepRightList=gson.fromJson(response, type);
					task1=true;
					if ((task2)&&(task3)) {
						mhandler.sendEmptyMessage(1);
						task1=false;
						task2=false;
						task3=false;
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
		VolleySRUtil info1Task=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(info1Task.getRequest());	
		
		//得到当前步骤的内容
		url="http://"+Constant.ip+"/ZhtxServer/StepDetailServlet";

		map=new HashMap<String, String>();
		map.put("action", "info");
		map.put("db",i.getStringExtra("db"));
		map.put("tab", "step"+String.valueOf(step)+"_"+i.getStringExtra("type"));

		listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	
			    	Gson gson=new Gson();
					Type type=new TypeToken<List<StepDetail>>(){}.getType();
					stepDetailList=gson.fromJson(response, type);
					task2=true;
					if ((task1)&&(task3)) {
						mhandler.sendEmptyMessage(1);
						task1=false;
						task2=false;
						task3=false;
					}
			    }else{
			    	Toast.makeText(getApplicationContext(), "获取数据失败",Toast.LENGTH_SHORT).show();
			    	finish();
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
		VolleySRUtil info2Task=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(info2Task.getRequest());
		
		//得到发送人的信息
		url="http://"+Constant.ip+"/ZhtxServer/StaffServlet";

		map=new HashMap<String, String>();
		map.put("action", "info");
		map.put("db",i.getStringExtra("db"));
	    if (step==1){
	    	map.put("id",i.getStringExtra("userid"));
	    }else{
	    	map.put("id", String.valueOf(r.getSender()));
	    }
	    
	    listener=new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	
			    	Gson gson=new Gson();
					sender=gson.fromJson(response, Staff.class);
					task3=true;
					if ((task1)&&(task2)) {
						
						mhandler.sendEmptyMessage(1);
						
						task1=false;
						task2=false;
						task3=false;
					}
			    }else{
			    	Toast.makeText(getApplicationContext(), "获取数据失败",Toast.LENGTH_SHORT).show();
			    	finish();
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
		VolleySRUtil info3Task=new VolleySRUtil(url,map,listener,elistener);	
		mQueue.add(info3Task.getRequest());
	}

	//对表单的界面信息初始化
	private void initRequire() {
		//基本信息的填入
		
		titletv.setText(stepRightList.get(step-1).getDescribe());
		if (step!=1) sendertv.setText("上一步:"+sender.getName());
		if (step!=1){
			DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			timetv.setText(sdf.format(r.getTime()));
		}
		if (step==1){
			proid=i.getStringExtra("proid");
		    proidtv.setText("单号:"+proid);
		}else{
			proidtv.setText("单号:"+r.getProid());
		}
		
		//根据当前步骤决定按钮可见性
		if (step==1) {
			btnsend.setVisibility(View.VISIBLE);
		}
		if (step>1){
			btnreject.setVisibility(View.VISIBLE);
			btnreject.setText(stepRightList.get(step-1).getBtn2());
			btnsend.setVisibility(View.INVISIBLE);
			btnapprove.setVisibility(View.VISIBLE);
			btnapprove.setText(stepRightList.get(step-1).getBtn1());
		}
		if (step<stepRightList.size()){
			recivertv.setVisibility(View.VISIBLE);
			recivertv.setText("待选择");
			btnreciver.setVisibility(View.VISIBLE);
		}

		
		//根据步骤内容动态生成控件
        for (StepDetail sd:stepDetailList){
			
			int id=sd.getId(),isNew=sd.getIsnew();
			String type1=sd.getType_1(),type2=sd.getType_2();
			LinearLayout ll=new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			//分别交由createTypeView方法处理两个控件并添加
			ll.addView(createTypeView(type1,id*10+1,isNew));
			ll.addView(createTypeView(type2,id*10+2,isNew));
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			ll.setLayoutParams(lytp);
			mainll.addView(ll);
		}
	}	
	
	//这个方法根据type编码生成view
	private View createTypeView(String type,int pos,int isNew){
		//得到控件类型id
		int typeid=Integer.parseInt(type.substring(0,type.indexOf(":")));
		//得到控件的内容
		String typedata="";
		if(type.indexOf(":")<type.length())
		    typedata=type.substring(type.indexOf(":")+1);
		
		switch (typeid){
		case 0:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			
			if (i.getBooleanExtra("reactive", false)){
				tv.setText(inheritData(String.valueOf(pos)));
				if (isNew==3){
					tv.setVisibility(View.GONE);
				}
				return tv;
			}
			if ((isNew!=1)&&(isNew!=3)){
			    tv.setText(typedata);
			}else{
				String s=inheritData(typedata);
				if ((s.equals(""))||(isNew==3)) {
					tv.setText(s);
					tv.setVisibility(View.GONE);
				}else{
					tv.setText(s);
				}
			}
			return tv;
		}		
		case 1:{
			EditText et=new EditText(this);
			et.setTextSize(17);
			et.setHint(typedata);
			et.setId(pos);
			et.setText("");			
			et.setHintTextColor(etColor);
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			et.setLayoutParams(lytp);
			
			if (i.getBooleanExtra("reactive", false)){
				et.setText(inheritData(String.valueOf(pos)));
				return et;
			}
			
			if (isNew==1){
				String s=inheritData(typedata);
				et.setText(s);
			}
			return et;
		}
		case 3:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			tv.setText("");
			tv.setVisibility(View.GONE);
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 4:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
	        tv.setText(sender.getName());
	        if (isNew==3) {
	        	tv.setVisibility(View.GONE);
	        }
	        LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 5:{
			if (typedata.indexOf("&")!=-1){
				printmust=false;
				typedata=typedata.substring(0, typedata.length()-1);
			}
			printnum=Integer.parseInt(typedata);
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			tv.setText("");
			tv.setVisibility(View.GONE);
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 6:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			if (isNew==3) {
	        	tv.setVisibility(View.GONE);
	        }
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 7:{
			CheckBox cb=new CheckBox(this);
			cb.setTextSize(17);
			cb.setId(pos);
			cb.setTextColor(etColor);
	        String s=typedata;
	        cb.setText(s.substring(0,s.indexOf("+")));
	        cb.setChecked(false);
	        LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			cb.setLayoutParams(lytp);
			cb.setOnCheckedChangeListener(
					new CompoundButton.OnCheckedChangeListener(){ 
	            @Override 
	            public void onCheckedChanged(CompoundButton buttonView, 
	                    boolean isChecked) { 
	                int pos = buttonView.getId();
	                StepDetail sd= stepDetailList.get(pos / 10-1);
	                String type;
	                if (pos % 10==1) type= sd.getType_1();
	                else type=sd.getType_2();
	                String typedata="";
	        		if(type.indexOf(":")<type.length())
	        		    typedata=type.substring(type.indexOf(":")+1);
	                if(isChecked){ 
	                    buttonView.setText(typedata.substring(typedata.indexOf("+")+1)); 
	                }else{ 
	                    buttonView.setText(typedata.substring(0,typedata.indexOf("+"))); 
	                } 
	            } 
	        }); 
	        return cb;
		}
		case 8:{
			Spinner sp=new Spinner(this);
			sp.setId(pos);
			List<String> list=new ArrayList<String>();
			list.add("未选择");
			String s=typedata;
			while (s.indexOf("+")!=-1){
				list.add(s.substring(0, s.indexOf("+")));
				if (s.indexOf("+") == s.length()-1){
					s="";
				}else{
					s=s.substring(s.indexOf("+")+1);
				}
			}
			ArrayAdapter<String> arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
	        //设置样式
	        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        //加载适配器
	        sp.setAdapter(arr_adapter);
	        if (i.getBooleanExtra("reactive", false)){
				String item=inheritData(String.valueOf(pos));
				int index=0;
				for (String ss:list){
					if (ss.equals(item)){
						sp.setSelection(index);
						break;
					}
					index++;
				}
			}
	        LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			sp.setLayoutParams(lytp);
			if (pos/10 == stepDetailList.size()) return sp;
			StepDetail sd=stepDetailList.get(pos / 10);
			if (sd.getType_1().equals("81:") || sd.getType_1().equals("91:")){
				sp.setOnItemSelectedListener(new OnItemSelectedListener(){ 
					@Override 
					public void onItemSelected(AdapterView<?> adapterView, View view, int position,
				                long id) {
				        int pos=adapterView.getId();
				        TextView tv=(TextView)findViewById(pos+20);
				        Button btn=(Button)findViewById(pos+9);
				        btn.setBackgroundResource(R.drawable.bg_button3);
				        tv.setText("");
				    }
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						
					}
				});
			}
	        return sp;
		}
		case 9:{
			EditText et=new EditText(this);
			et.setTextSize(17);
			et.setHint(typedata);
			et.setId(pos);
			et.setText("");			
			et.setHintTextColor(etColor);
			et.setInputType(8194);
			if (i.getBooleanExtra("reactive", false)){
				et.setText(inheritData(String.valueOf(pos)));
			}
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			et.setLayoutParams(lytp);
			
			return et;
		}
		case 10:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			if (typedata.equals("")){
				tv.setText("点击选择日期");
				tv.setTextColor(etColor);
			}else{
				tv.setText(typedata);
			}
			if (i.getBooleanExtra("reactive", false)){
				String date=inheritData(String.valueOf(pos));
				tv.setText(date.substring(0, 4)+"年"+date.substring(4, 6)+"月"+date.substring(6, 8)+"日");
			}
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);	
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();	
					AlertDialog.Builder b=new Builder(RequireDetail.this);
					b.setTitle("选择日期");
					dp = new DatePicker(b.getContext());
					b.setView(dp);
					b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							int day=dp.getDayOfMonth();
							int month=dp.getMonth()+1;
							int year=dp.getYear();
							TextView tv=(TextView)findViewById(btnid);
							tv.setText(year+"年"+month+"月"+day+"日");
							tv.setTextColor(normalColor);
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
			});
			return tv;
		}
		case 11:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择图片");
			btn.setBackgroundResource(R.drawable.bg_button3);
			btn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					AlertDialog.Builder b=new Builder(RequireDetail.this);
					b.setTitle("提示");
					b.setMessage("请选择获取方式");
					b.setPositiveButton("相机", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							String filename;
							if (step==1) {
								filename= proid+".jpg";
							}else{
								filename=r.getProid()+".jpg";
							}
							File storePath= new File(Environment.getExternalStorageDirectory().toString()+"/"+"jianyiTemp");
							if (!storePath.exists()){
								storePath.mkdir();
							}
							cameraFile = new File(Environment.getExternalStorageDirectory().toString()+"/"+"jianyiTemp"+"/"+filename);
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
							startActivityForResult(intent,TAKE_PHOTO_WITH_DATA);
						}
						
					});
					b.setNegativeButton("图库", new  DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							arg0.dismiss();						
							//选择文件上传	
							Intent intent = new Intent(  Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  						   
							startActivityForResult(intent, RESULT_LOAD_IMAGE);  
						}
						
					});
				    b.create().show();			
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 12:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			if (inheritData(typedata).equals("")){
				btn.setText("无图片");
			}else{
				btn.setText("查看图片");
				btn.setBackgroundResource(R.drawable.bg_button3);
				btn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						btnid=arg0.getId();
						StepDetail sd=stepDetailList.get(btnid / 10 -1);
			        	String type;
			        	if (btnid % 10 ==1) type=sd.getType_1();
			        	else type=sd.getType_2();
			        	String typedata="";
			        	if(type.indexOf(":")<type.length())
			    		    typedata=type.substring(type.indexOf(":")+1);
						Intent intent=new Intent(RequireDetail.this,RequirePicture.class);
						intent.putExtra("url",Constant.serverPath+"pictures/"+i.getStringExtra("db")+"/"+inheritData(typedata)+".png");
						startActivity(intent);
						
					}
				});
			}	
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;	
		}
		case 13:{
			LastInputEditText et=new LastInputEditText(this);
			et.setTextSize(17);
			et.setHint(typedata);
			et.setId(pos);
			et.setHintTextColor(etColor);
			et.setInputType(8194);
			if (i.getBooleanExtra("reactive", false)){
				et.setText(inheritData(String.valueOf(pos)));
			}
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);	
			et.setLayoutParams(lytp);		
			et.addTextChangedListener(new TextWatcher() { 
				@Override  
				public void onTextChanged(CharSequence s, int start, int before, int count){
					String num=s.toString();
					if (num.equals("(个)")){
						num="";
						View rootview = RequireDetail.this.getWindow().getDecorView();
						LastInputEditText et = (LastInputEditText)findViewById(rootview.findFocus().getId());
						et.setText("");
					}					
					if (!num.equals("")){
						num=Functions.deleteInvalidCharPrice(num);				
						num=Functions.formatPrice(num);        
						View rootview = RequireDetail.this.getWindow().getDecorView();
						LastInputEditText et = (LastInputEditText)findViewById(rootview.findFocus().getId());
						if (!num.equals(et.getText().toString())) et.setText(num);
					}
					
				}  
				@Override  	
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					
				}  
				@Override  
				public void afterTextChanged(Editable s) {    
									
				}
			});
			return et;
		}
		case 41:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			if (isNew==3) {
	        	tv.setVisibility(View.GONE);
	        }
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
            tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					StepDetail sd=stepDetailList.get(arg0.getId() / 10 -1);
		        	String type;
		        	if (arg0.getId() % 10 ==1) type=sd.getType_1();
		        	else type=sd.getType_2();
		        	String typedata="";
		    		if(type.indexOf(":")<type.length())
		    		    typedata=type.substring(type.indexOf(":")+1);
					double sum=0;
					boolean sub;
					while (typedata.indexOf("+")!=-1){
						sub=false;
						int id=Integer.parseInt(typedata.substring(0,typedata.indexOf("+")));
						if (typedata.indexOf("+")<typedata.length()-1){
							if (typedata.indexOf("+")==typedata.indexOf("+-")) {
								sub=true;
								if (typedata.indexOf("+")+1<typedata.length()) typedata=typedata.substring(typedata.indexOf("+")+2);
								else typedata="";
							}else
							  typedata=typedata.substring(typedata.indexOf("+")+1);
						}else{
							typedata="";
						}
						String s;
						if (id % 10==1)  s=stepDetailList.get((id / 10)-1).getType_1();
						else s=stepDetailList.get((id / 10)-1).getType_2();
						int tid=Integer.parseInt(s.substring(0,s.indexOf(":")));
						if (tid==1 || tid==9 || tid == 13){
						    EditText et=(EditText)findViewById(id);
						    String x=et.getText().toString();
						    if (tid==13){
						    	x=Functions.DeformatPrice(x);
						    }
						    if (!x.equals("")) {
						    	if (sub)  sum -= Double.parseDouble(x);
						    	else sum += Double.parseDouble(x);
						    }
						}else{
							TextView tv=(TextView)findViewById(id);
						    if (!tv.getText().toString().equals("")) {
						    	if (sub) sum -= Double.parseDouble((tv.getText().toString()));
						    	else sum += Double.parseDouble((tv.getText().toString()));
						    }
						}
					}
					TextView tv=(TextView)findViewById(arg0.getId());
					tv.setText(String.valueOf(sum));
				}
			});
			return tv;
		}
		case 51:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择商品");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireProductList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("table", "sell");
					startActivity(intent);
					
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 52:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			tv.setText("");
			if (isNew==3) {
	        	tv.setVisibility(View.GONE);
	        }
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 53:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
	        tv.setVisibility(View.GONE);
	        LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			tv.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						StepDetail sd=stepDetailList.get(arg0.getId() / 10 -1);
			        	String type;
			        	if (arg0.getId() % 10 ==1) type=sd.getType_1();
			        	else type=sd.getType_2();
			        	String typedata="";
			    		if(type.indexOf(":")<type.length())
			    		    typedata=type.substring(type.indexOf(":")+1);
			    		double sum=1;
						boolean isok=true;
						int deal=1;
						if (typedata.indexOf("&")!=-1){
							deal=Integer.parseInt(typedata.substring(0,1));
							typedata=typedata.substring(2);
						}
						while (typedata.indexOf("*")!=-1){
							int id=Integer.parseInt(typedata.substring(0,typedata.indexOf("*")));
							if (typedata.indexOf("*")<typedata.length()){
								typedata=typedata.substring(typedata.indexOf("*")+1);
							}else{
								typedata="";
							}
							String s;
							if (id % 10==1)  s=stepDetailList.get((id / 10)-1).getType_1();
							else s=stepDetailList.get((id / 10)-1).getType_2();
							int tid=Integer.parseInt(s.substring(0,s.indexOf(":")));
							if (tid==1 || tid == 9 || tid == 13){
							    EditText et=(EditText)findViewById(id);
							    String x=et.getText().toString();
							    if (tid==13){
							    	x=Functions.DeformatPrice(x);
							    }
							    if (!x.equals("")) {
							    	sum*=Double.parseDouble(x);
							    	isok=false;
							    }
							}else{
								TextView tv=(TextView)findViewById(id);
							    if (!tv.getText().toString().equals("")) {
							    	sum*=Double.parseDouble((tv.getText().toString()));
							    	isok=false;
							    }
							}
							
						}
						java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
						     
						if  (!isok){
							TextView tv=(TextView)findViewById(arg0.getId());
							if (deal==2) {
								sum=Math.round(sum);
								df=new java.text.DecimalFormat("#");
							}
							if (deal==3) {
								sum=Math.floor(sum);
								df=new java.text.DecimalFormat("#");
							}
							if (deal==4){
								sum=Math.ceil(sum);
								df=new java.text.DecimalFormat("#");
							}
							tv.setText(df.format(sum));
						}
					}
			});
			return tv;
		}
		case 54:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择会计科目");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireAccountTypeList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 55:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择车牌号");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireSimpleObjectList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act55");
					/*Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act55");*/
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 56:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setText("20"+proid.substring(0,6));
			tv.setId(pos);
	        if (isNew==3) {
	        	tv.setVisibility(View.GONE);
	        }
	        LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 57:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择商品");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireAct57List.class);
					intent.putExtra("db", i.getStringExtra("db"));
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 58:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择仓库");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act58");
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 61:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择供应商");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act61");
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 62:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择商品");
			btn.setBackgroundColor(btnColor);
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireProductList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("table", "stock");
					startActivity(intent);
					
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 63:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("交易对象");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act63");
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 71:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择员工(APP)");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStaffList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 72:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择员工");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act72");
					startActivity(intent);				
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 81:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择付款对象");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Spinner sp=(Spinner)findViewById(btnid-9);
					String p=sp.getSelectedItem().toString();
					if (p.equals("未选择")){
						Toast.makeText(getApplicationContext(), "请先选择付款类型", Toast.LENGTH_SHORT).show();
					}else{
						Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
						intent.putExtra("db", i.getStringExtra("db"));
						intent.putExtra("action", "act81");
						intent.putExtra("mode", p);
						startActivity(intent);	
					}	
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 82:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择钱款账户");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act82");
					startActivity(intent);
					
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 83:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择供应商");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
					intent.putExtra("db", i.getStringExtra("db"));
					intent.putExtra("action", "act83");
					startActivity(intent);
					
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 91:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("选择收款对象");
			btn.setBackgroundResource(R.drawable.bg_button3);
			if ((i.getBooleanExtra("reactive", false))&&(!inheritData(String.valueOf(pos+11)).equals(""))){
				btn.setBackgroundColor(btnColor2);
			}
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					Spinner sp=(Spinner)findViewById(btnid-9);
					String p=sp.getSelectedItem().toString();
					if (p.equals("未选择")){
						Toast.makeText(getApplicationContext(), "请先选择收款类型", Toast.LENGTH_SHORT).show();
					}else{
						Intent intent=new Intent(RequireDetail.this,RequireStringList.class);
						intent.putExtra("db", i.getStringExtra("db"));
						intent.putExtra("action", "act91");
						intent.putExtra("mode", p);
						startActivity(intent);	
					}			
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4个参数按顺序分别是左上右下
			btn.setLayoutParams(lytp);
			return btn;
		}
		//switch结束
		}
		return null;
		
	}
	
	private String inheritData(String typedata){
		String id,info="";
		if (typedata.indexOf("&")==-1){
			id=typedata;	
		}else{
			id=typedata.substring(0,typedata.indexOf("&"));
			info=typedata.substring(typedata.indexOf("&")+1);
		}
		String s=r.getData();
		String ss;
		int pos1=s.indexOf("&"+id+"#");
		ss=s.substring(pos1+1);
		int pos2=ss.indexOf("&");
		if (s.substring(pos1+id.length()+2,pos2+1+pos1).equals("")){
			return "";
		}else{
			return info+s.substring(pos1+id.length()+2,pos2+1+pos1);
		}
		
	}
	
	private boolean isFill(String type,int pos){
		int typeid=Integer.parseInt(type.substring(0,type.indexOf(":")));
		String typedata="";
		if(type.indexOf(":")<type.length())
			typedata=type.substring(type.indexOf(":")+1);
		switch (typeid){
		//如果为填入项而没有填入
		case 1:{
			EditText et=(EditText)findViewById(pos);
			if (et.getText().toString().equals("")) return false;
			break;
		}
		
		case 8:{
			Spinner sp=(Spinner)findViewById(pos);
			if ((sp.getSelectedItem() == null)||(sp.getSelectedItem().toString().equals("未选择"))){
				return false;
			}
			break;
		}
		case 9:{
			EditText et=(EditText)findViewById(pos);
			String s=et.getText().toString();
			if (s.equals("")) return false;
			Pattern pattern1 = Pattern.compile("^[-\\+]?[.\\d]*$"); 
			Pattern pattern2 = Pattern.compile("^[-\\+]?[\\d]*$");
			Matcher isNum1 = pattern1.matcher(s);
			Matcher isNum2 = pattern2.matcher(s);
			if( !isNum1.matches() && !isNum2.matches()){
			       return false; 
			} 
			break;
		}
		case 10:{
			TextView tv=(TextView)findViewById(pos);
			String s=tv.getText().toString();
			if (s.equals("点击选择日期")||(s.equals(typedata))){
				return false;
			}
			break;
		}
		case 11:{
			Button btn=(Button)findViewById(pos);
			if (btn.getText().toString().equals("上传图片")){
				return false;
			}
			break;
		}
		case 13:{
			LastInputEditText et=(LastInputEditText)findViewById(pos);
			if (et.getText().toString().equals("")) return false;
			String t=et.getText().toString();
			if (t.indexOf(".")!=-1){
				while (t.indexOf(".")>t.length()-3){
					t+="0";
				}
				et.setText(t);
			}
			break;
		}
		//如果为总金额,则进行统计
		case 41:{
			double sum=0;
			boolean sub;
			while (typedata.indexOf("+")!=-1){
				sub=false;
				int id=Integer.parseInt(typedata.substring(0,typedata.indexOf("+")));
				if (typedata.indexOf("+")<typedata.length()-1){
					if (typedata.indexOf("+")==typedata.indexOf("+-")) {
						sub=true;
						if (typedata.indexOf("+")+1<typedata.length()) typedata=typedata.substring(typedata.indexOf("+")+2);
						else typedata="";
					}else
					  typedata=typedata.substring(typedata.indexOf("+")+1);
				}else{
					typedata="";
				}
				String s;
				if (id % 10==1)  s=stepDetailList.get((id / 10)-1).getType_1();
				else s=stepDetailList.get((id / 10)-1).getType_2();
				int tid=Integer.parseInt(s.substring(0,s.indexOf(":")));
				if (tid==1 || tid==9 || tid == 13){
				    EditText et=(EditText)findViewById(id);
				    String x=et.getText().toString();
				    if (tid==13){
				    	x=Functions.DeformatPrice(x);
				    }
				    if (!x.equals("")) {
				    	if (sub)  sum -= Double.parseDouble(x);
				    	else sum += Double.parseDouble(x);
				    }
				}else{
					TextView tv=(TextView)findViewById(id);
				    if (!tv.getText().toString().equals("")) {
				    	if (sub) sum -= Double.parseDouble((tv.getText().toString()));
				    	else sum += Double.parseDouble((tv.getText().toString()));
				    }
				}
			}
			TextView tv=(TextView)findViewById(pos);
			tv.setText(String.valueOf(sum));
			break;
		}
		//如果为选择商品,判断是否已经选择
		case 51:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 52:{
			TextView tv=(TextView)findViewById(pos);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 53:{
			double sum=1;
			boolean isok=true;
			int deal=1;
			if (typedata.indexOf("&")!=-1){
				deal=Integer.parseInt(typedata.substring(0,1));
				typedata=typedata.substring(2);
			}
			while (typedata.indexOf("*")!=-1){
				int id=Integer.parseInt(typedata.substring(0,typedata.indexOf("*")));
				if (typedata.indexOf("*")<typedata.length()){
					typedata=typedata.substring(typedata.indexOf("*")+1);
				}else{
					typedata="";
				}
				String s;
				if (id % 10==1)  s=stepDetailList.get((id / 10)-1).getType_1();
				else s=stepDetailList.get((id / 10)-1).getType_2();
				int tid=Integer.parseInt(s.substring(0,s.indexOf(":")));
				if (tid==1 || tid == 9 || tid == 13){
				    EditText et=(EditText)findViewById(id);
				    String x=et.getText().toString();
				    if (tid==13){
				    	x=Functions.DeformatPrice(x);
				    }
				    if (!x.equals("")) {
				    	sum*=Double.parseDouble(x);
				    	isok=false;
				    }
				}else{
					TextView tv=(TextView)findViewById(id);
				    if (!tv.getText().toString().equals("")) {
				    	sum*=Double.parseDouble((tv.getText().toString()));
				    	isok=false;
				    }
				}
				
			}
			java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
			     
			if  (!isok){
				TextView tv=(TextView)findViewById(pos);
				if (deal==2) {
					sum=Math.round(sum);
					df=new java.text.DecimalFormat("#");
				}
				if (deal==3) {
					sum=Math.floor(sum);
					df=new java.text.DecimalFormat("#");
				}
				if (deal==4){
					sum=Math.ceil(sum);
					df=new java.text.DecimalFormat("#");
				}
				tv.setText(df.format(sum));
			}
			break;
		}
		case 54:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 55:{
			if (typedata.equals("&")){
				EditText et=(EditText)findViewById(pos+11);
				if (et.getText().toString().equals("")) return false;	
			}else{
				TextView tv=(TextView)findViewById(pos+11);
				if (tv.getText().toString().equals("")) return false;
			}
			break;
		}
		case 57:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 58:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 61:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 62:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 63:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 71:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 72:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 81:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 82:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 83:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		case 91:{
			TextView tv=(TextView)findViewById(pos+11);
			if (tv.getText().toString().equals("")) return false;
			break;
		}
		//switch结束
		}
		return true;
	}
	
	private String typeData(String type,int pos){
		int typeid=Integer.parseInt(type.substring(0,type.indexOf(":")));
		String typedata="";
		if(type.indexOf(":")<type.length())
			typedata=type.substring(type.indexOf(":")+1);
		String s="";
		switch (typeid){
		case 0:{
			TextView tv=(TextView)findViewById(pos);
			if (stepDetailList.get((pos / 10)-1).getIsnew()!=1){
				s=tv.getText().toString();
			}else{
				//如果是继承的话需要去掉数据前面加的注释
				s=tv.getText().toString();
				if (s.indexOf(":")+1<s.length()) s=s.substring(s.indexOf(":")+1);
			}
			break;
		}
		case 1:{
			EditText et=(EditText)findViewById(pos);
			s=et.getText().toString();
			break;
		}
		case 3:{
			s="";
			break;
		}
		case 4:{
			TextView tv=(TextView)findViewById(pos);
			s=tv.getText().toString();
			break;
		}
		case 6:{
			TextView tv=(TextView)findViewById(pos);
			s=tv.getText().toString();
			break;
		}
		case 7:{
			CheckBox cb=(CheckBox)findViewById(pos);
			s=cb.getText().toString();
			break;
		}
		case 8:{
			Spinner sp=(Spinner)findViewById(pos);
			s=sp.getSelectedItem().toString();
			break;
		}
		case 9:{
			EditText et=(EditText)findViewById(pos);
			s=et.getText().toString();
			break;
		}
		case 10:{
			TextView tv=(TextView)findViewById(pos);
			String a=tv.getText().toString();
			String year=a.substring(0,a.indexOf("年"));
			String month=a.substring(a.indexOf("年")+1,a.indexOf("月"));
			if (month.length()<2) month="0"+month;
			String day=a.substring(a.indexOf("月")+1,a.indexOf("日"));
			if (day.length()<2) day="0"+day;
			s=year+month+day;
			break;
		}
		case 11:{
			for (int j=fileNameList.size()-1;j>=0; j--){
				int x=fileNameList.get(j).indexOf("&");
				int id=Integer.parseInt(fileNameList.get(j).substring(0, x));
				if (id==pos) {
					s=fileNameList.get(j).substring(x+1);
					break;
				}
			}
			break;
		}
		case 12:{
			s=inheritData(typedata);
			break;
		}
		case 13:{
			LastInputEditText et=(LastInputEditText)findViewById(pos);
			s=Functions.DeformatPrice(et.getText().toString());
			break;
		}
		case 41:{
			TextView tv=(TextView)findViewById(pos);
			s=tv.getText().toString();
			break;
		}
		case 52:{
			TextView tv=(TextView)findViewById(pos);
			s=tv.getText().toString();
			break;
		}
		case 53:{
			TextView tv=(TextView)findViewById(pos);
			s=tv.getText().toString();
			break;
		}
		case 56:{
			TextView tv=(TextView)findViewById(pos);
			s=tv.getText().toString();
			break;
		}
		//switch结束
		}
		//删除掉会让app引起误会的字符 # 和 &
		s.replace("#", "");
		s.replace("&", "");
		return s;
	}
	
	//把表单的信息转换个data字符串
	private String getData(){
		String data="&";
		for (StepDetail sd:stepDetailList){
			data+=String.valueOf(sd.getId()*10+1)+"#";
			data+=typeData(sd.getType_1(),sd.getId()*10+1);
			data+="&";
			data+=String.valueOf(sd.getId()*10+2)+"#";
			data+=typeData(sd.getType_2(),sd.getId()*10+2);
			data+="&";
		}
		return data;
	}
	
	//为sum赋值，为dataType赋值
	private void getSum(){
		tot=0;
		for (int i=0;i<99;i++){
			sum[i]=0;
			locate[i][0]=-1;
			locate[i][1]=-1;
		}
		String data=getData();
		for (StepDetail sd:stepDetailList)
		    if (!sd.getRecord().equals("")){
			String s=sd.getRecord();
			String id=String.valueOf(sd.getId())+"1";
			int pos=s.indexOf("1:");
			if (pos!=-1){
				String ss;
				int pos1=data.indexOf("&"+id+"#");
				ss=data.substring(pos1+1);
				int pos2=ss.indexOf("&");
				if (!data.substring(pos1+id.length()+2,pos2+1+pos1).equals("")){
					sum[Integer.parseInt(s.substring(pos+2,s.indexOf(";")))]++;
					if (sum[Integer.parseInt(s.substring(pos+2,s.indexOf(";")))]>tot) tot=sum[Integer.parseInt(s.substring(pos+2,s.indexOf(";")))];	
					locate[sd.getId()][0]=Integer.parseInt(s.substring(pos+2,s.indexOf(";")));
				}
				if (s.indexOf(";")<s.length()) s=s.substring(s.indexOf(";")+1);
				else s="";
			}
			id=String.valueOf(sd.getId())+"2";
			if (s.length()>0){
				String ss;
				int pos1=data.indexOf("&"+id+"#");
				ss=data.substring(pos1+1);
				int pos2=ss.indexOf("&");
				if (!data.substring(pos1+id.length()+2,pos2+1+pos1).equals("")){
					sum[Integer.parseInt(s.substring(2,s.indexOf(";")))]++;
					if (sum[Integer.parseInt(s.substring(2,s.indexOf(";")))]>tot) tot=sum[Integer.parseInt(s.substring(2,s.indexOf(";")))];
				    locate[sd.getId()][1]=Integer.parseInt(s.substring(2,s.indexOf(";")));
				}	
			}
		}
	}
	
	//把要录入数据库的信息整理成list,num表示是第几次录入
	private List<String> getRecordList(int num){
		List<String> list=new ArrayList<String>();
		String data=getData();
		for (int i=0; i<99; i++)
		   if (sum[i]>0){
			  if (sum[i]==1){
				  for (int j=0;j<stepDetailList.size();j++){
					  if (locate[j+1][0]==i){
						  String id=String.valueOf(j+1)+"1";
						  String ss;
						  int pos1=data.indexOf("&"+id+"#");
						  ss=data.substring(pos1+1);
						  int pos2=ss.indexOf("&");	  
						  list.add(data.substring(pos1+id.length()+2,pos2+1+pos1));
					  }
					  if (locate[j+1][1]==i){
						  String id=String.valueOf(j+1)+"2";
						  String ss;
						  int pos1=data.indexOf("&"+id+"#");
						  ss=data.substring(pos1+1);
						  int pos2=ss.indexOf("&");	  
						  list.add(data.substring(pos1+id.length()+2,pos2+1+pos1));
					  }
				  }
			 }
			 if (sum[i]>1){
				 int x=0;
				 for (int j=0;j<stepDetailList.size();j++){
					 if (locate[j+1][0]==i){
						  x++;
						  if (x==num) {
						      String id=String.valueOf(j+1)+"1";
						      String ss;
						      int pos1=data.indexOf("&"+id+"#");
						      ss=data.substring(pos1+1);
						      int pos2=ss.indexOf("&");	  
						      list.add(data.substring(pos1+id.length()+2,pos2+1+pos1));
						  }
					  }
					 if (locate[j+1][1]==i){
						  x++;
						  if (x==num) {
						      String id=String.valueOf(j+1)+"2";
						      String ss;
						      int pos1=data.indexOf("&"+id+"#");
						      ss=data.substring(pos1+1);
						      int pos2=ss.indexOf("&");	  
						      list.add(data.substring(pos1+id.length()+2,pos2+1+pos1));
						  }
					  }
				 }
			 }
			
		  }
		return list;
	}
	
	//启动录入数据库的网络任务
	private void doRecord(String type,List<String> list){
		
		String url="http://"+Constant.ip+"/ZhtxServer/RecordServlet";
		HashMap<String, String>map=new HashMap<String, String>();
	  	map.put("db", i.getStringExtra("db"));
		map.put("action","record");
		map.put("type", type);
	    Gson gson=new Gson();
	    Type type1=new TypeToken<List<String>>(){}.getType();
	    map.put("list", gson.toJson(list,type1));
	    Log.e("debug",gson.toJson(list,type1) );
	    Response.Listener<String> listener=new Response.Listener<String>(){
	        @Override
		    public void onResponse(String response) {
			    if ((response!=null)&&(response.equals("1"))){
			    	count++;
			    	if (count==tot){
			    		//完成录入后发送消息，使审批状态变为结束
			    		mhandler.sendEmptyMessage(4);
			    	}
			    }else{
			    	 progressDialog.dismiss();
			    	 Toast.makeText(getApplicationContext(), "连接失败，请检查网络",Toast.LENGTH_LONG).show();
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
		VolleySRUtil recordTask=new VolleySRUtil(url,map,listener,elistener);	
	    mQueue.add(recordTask.getRequest());
	}
	
	//流程结束时把结果计入数据库
	private void recordResult(int type){
		
		tot=0;
		count=0;
		String typeName=Constant.action[type-1];
		getSum();
	    for (int j=1;j<=tot;j++){
	    	List<String> list=new ArrayList<String>();
			list=getRecordList(j);
			list.add(0, r.getProid());
			doRecord(typeName,list);	
	    }
	}
	
    //发起新的流程
	public void send(View v){
		
		AlertDialog.Builder b=new Builder(RequireDetail.this);
		b.setTitle("提示");
		b.setMessage("您确定要发起该申请吗？");
		b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//判断当前表单是否填写完整
				String error="";
				arg0.dismiss();
				boolean isFull=true;
				for (StepDetail sd:stepDetailList)
					if (sd.getIsnew()==0)
					    if (!(isFill(sd.getType_1(),sd.getId()*10+1)&&isFill(sd.getType_2(),sd.getId()*10+2))){
					    	isFull=false;
					    	error="必要信息不完整或填错";
					    	break;
					    }
				//如果没选择审批人
				if ((step<stepRightList.size())&&(reciver==null)) {
					isFull=false;
					error="没选择审批人";
				}

				if (!isFull){
					Toast.makeText(getApplicationContext(), error,Toast.LENGTH_SHORT).show();
				}else{
					//在审批表中插入新内容,传递下一步
					String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					//用post方法封装需要发送的信息
					HashMap<String, String> map=new HashMap<String, String>();
					map.put("db", i.getStringExtra("db"));
				    map.put("action","insert");
					map.put("sender",i.getStringExtra("userid"));
					map.put("reciver",String.valueOf(reciver.getId()));
					map.put("previd","0");
					map.put("proid", proid);
					map.put("act", String.valueOf(Integer.parseInt(i.getStringExtra("act"))+100));
					map.put("data",getData());
					//对于服务器返回结果的处理
					Response.Listener<String> listener=new Response.Listener<String>(){
					    @Override
						public void onResponse(String response) {
					    	progressDialog.dismiss();
							if ((response!=null)&&(response.equals("1"))){
								mhandler.sendEmptyMessage(2);
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
					VolleySRUtil insertTask=new VolleySRUtil(url,map,listener,elistener);	
					progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "获取数据中...", true);
					mQueue.add(insertTask.getRequest());

				}
			  
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
	
	//通过该审批
	public void approve(View v){
		AlertDialog.Builder b=new Builder(RequireDetail.this);
		b.setTitle("提示");
		b.setMessage("您确定要通过该申请吗？");
		b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				//判断当前表单是否填写完整
				boolean isFull=true;
				for (StepDetail sd:stepDetailList)
					if (sd.getIsnew()==0)
					    if (!(isFill(sd.getType_1(),sd.getId()*10+1)&&isFill(sd.getType_2(),sd.getId()*10+2))){
					    	isFull=false;
					    	break;
					    }
				//如果没选择审批人
				if ((step<stepRightList.size())&&(reciver==null)) isFull=false;
				if (!isFull){
					Toast.makeText(getApplicationContext(), "必要信息填写不完整",Toast.LENGTH_SHORT).show();
				}else{
					//判断当前是否已经是流程最后一步，从而决定处理方法
				    if (step<stepRightList.size()){
					    //第一步,在审批表中插入新内容
				    	String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
						HashMap<String, String> map=new HashMap<String, String>();
						map.put("db", i.getStringExtra("db"));
					    map.put("action","insert");
						map.put("sender",i.getStringExtra("userid"));
						map.put("reciver",String.valueOf(reciver.getId()));
						map.put("previd",String.valueOf(r.getId()));
						map.put("act", String.valueOf(Integer.parseInt(i.getStringExtra("act"))+100));
						map.put("data",getData());
						map.put("proid", r.getProid());
						Response.Listener<String> listener=new Response.Listener<String>(){
						    @Override
							public void onResponse(String response) {
								if ((response!=null)&&(response.equals("1"))){
									task1=true;
									if (task2){
										mhandler.sendEmptyMessage(2);
									}
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
						VolleySRUtil insertTask=new VolleySRUtil(url,map,listener,elistener);	
						mQueue.add(insertTask.getRequest());
						//第二步，设置当前申请状态为结束
						url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					  	map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
						map.put("action","end");
					    map.put("id",String.valueOf(r.getId())); 
						listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							    if ((response!=null)&&(response.equals("1"))){
							    	task2=true;
							    	if (task1) {
							    		mhandler.sendEmptyMessage(2);
							    	}
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
						VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
					    progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "获取数据中...", true);
					    mQueue.add(statusTask.getRequest());
				    }else{
				    	//第一步，把流程结果录入数据库
				    	int type=Integer.parseInt(i.getStringExtra("act")) % 100;
				    	progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "获取数据中...", true);
				    	recordResult(type);
						
				    }
				}
				
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
	
	//驳回该审批
	public void reject(View v){
		AlertDialog.Builder b=new Builder(RequireDetail.this);
		b.setTitle("提示");
		b.setMessage("您确定要驳回该申请吗？");
		rejectReasonet=new EditText(b.getContext());
		b.setView(rejectReasonet);
		b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				printnum=0;
				String rejectReason=rejectReasonet.getText().toString();
				arg0.dismiss();
				//设置当前申请状态为驳回
				String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
				HashMap<String, String>map=new HashMap<String, String>();
			  	map.put("db", i.getStringExtra("db"));
				map.put("action","reject");
			    map.put("id",String.valueOf(r.getId()));   
			    map.put("info", r.getData()+"rj^"+rejectReason);
			    Response.Listener<String> listener=new Response.Listener<String>(){
			        @Override
				    public void onResponse(String response) {
					    if ((response!=null)&&(response.equals("1"))){
					    	task1=true;
					    	if (task2&&task3) {
					    		mhandler.sendEmptyMessage(2);
					    	}
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
				VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
			    progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "处理中...", true);
			    mQueue.add(statusTask.getRequest());
			    //清除上一步上传的图片数据
			    url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
				map=new HashMap<String, String>();
			  	map.put("db", i.getStringExtra("db"));
				map.put("action","deletePic");
			    map.put("proid",r.getProid());   
			    map.put("step", String.valueOf(step-1));
			    listener=new Response.Listener<String>(){
			        @Override
				    public void onResponse(String response) {
			        	task3=true;
				    	if (task1&&task2) {
				    		mhandler.sendEmptyMessage(2);
				    	}
				    }				
			    };
			    elistener=new Response.ErrorListener(){  
		            @Override  
		            public void onErrorResponse(VolleyError error) {  
		                task3=true;
		                if (task1&&task2) {
				    		mhandler.sendEmptyMessage(2);
				    	}
		            }  
		        };
				VolleySRUtil deleteTask=new VolleySRUtil(url,map,listener,elistener);	
			    mQueue.add(deleteTask.getRequest());
				//判断当前是不是流程的第二步
			    if (r.getPrevid()!=0){
				    //如果不是流程第二步，则把当前申请的上一步的申请设置为待审批(即让其重新填写)
					url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
				  	map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","active");
				    map.put("id",String.valueOf(r.getPrevid())); 
					listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
						    if ((response!=null)&&(response.equals("1"))){
						    	task2=true;
						    	if (task1&&task3) {
						    		mhandler.sendEmptyMessage(2);
						    	}
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
					VolleySRUtil statusTask2=new VolleySRUtil(url,map,listener,elistener);	
				    mQueue.add(statusTask2.getRequest());
			    }else{
			    	task2=true;
			    }
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
	//上传图片到服务器
	public void uploadPicture(Bitmap bitmap){
		byte[] buffer=Functions.imageZoom(bitmap);
        String photo = Base64.encodeToString(buffer, 0, buffer.length,Base64.DEFAULT);  
        progressDialog.dismiss();
        progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "图片上传中...", true);
		String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
		HashMap<String, String>map=new HashMap<String, String>();
	  	map.put("db", i.getStringExtra("db"));
	  	map.put("action", "uploadPic");
	  	if (step==1) map.put("proid", proid);
	  	else map.put("proid", r.getProid());
	  	map.put("num", String.valueOf(step)+String.valueOf(picnum));
	  	map.put("data", photo);
	    Response.Listener<String> listener=new Response.Listener<String>(){
	        @Override
		    public void onResponse(String response) {
			    if ((response!=null)&&(!response.equals("0"))){
			    	Button btn=(Button)findViewById(btnid);
		        	btn.setBackgroundColor(btnColor2);
		        	btn.setText("上传完毕");
		        	String s;
		        	if (step==1) s=proid;
		    	  	else s= r.getProid();
		        	fileNameList.add(String.valueOf(btnid)+"&"+s+"_"+String.valueOf(step)+String.valueOf(picnum));
		        	btn.setEnabled(false);
		        	progressDialog.dismiss();
			    }
		    }				
	    };
	    Response.ErrorListener elistener=new Response.ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                Toast.makeText(getApplicationContext(), R.string.volley_error1,Toast.LENGTH_LONG).show();
                Button btn=(Button)findViewById(btnid);
	        	btn.setText("上传图片");
	        	progressDialog.dismiss();
            }  
        };
		VolleySRUtil uploadTask=new VolleySRUtil(url,map,listener,elistener);	
	    mQueue.add(uploadTask.getRequest());
	}
	
	//点击审批人按钮进入到选择审批人的界面
	public void chosereciver(View v){
		Intent intent=new Intent(RequireDetail.this,RequireReciverList.class);
		intent.putExtra("staff", stepRightList.get(step).getStaff());
		intent.putExtra("db", i.getStringExtra("db"));
		startActivity(intent);
	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	     
	    //处理选择图片的结果
	    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {  
	        progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "图片处理中...", true);
	        picnum+=1;
	        Uri selectedImage = data.getData();  
	        String[] filePathColumn = { MediaStore.Images.Media.DATA };  	  
	        Cursor cursor = getContentResolver().query(selectedImage,  
	        filePathColumn, null, null, null);  
	        cursor.moveToFirst();  
	        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
	        String picturePath = cursor.getString(columnIndex);  
	        cursor.close();  
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
	        uploadPicture(bitmap);
	        }
	    //处理拍摄照片的结果
	    if (requestCode == TAKE_PHOTO_WITH_DATA && cameraFile.exists()) {
	    	progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "图片处理中...", true);
	    	picnum+=1;
	    	Bitmap bitmap = null;
    		try {
    		    FileInputStream fis = new FileInputStream(cameraFile);
    		    bitmap = BitmapFactory.decodeStream(fis);
    		    fis.close();
    		} catch (Exception e) { 
    		    e.printStackTrace(); 
    		}
    		if (bitmap!=null) {
    			uploadPicture(bitmap);
    		}	
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}    
	@Override
	protected void onResume() {  
	    super.onResume(); 
        if (reciver!=null) {
        	recivertv.setText(reciver.getName());
        	btnreciver.setBackgroundColor(btnColor2);
        	for (StepDetail sd:stepDetailList){
        		String type=sd.getType_2();
        		int typeid=Integer.parseInt(type.substring(0,type.indexOf(":")));
        		if (typeid==52){
        			int pos=sd.getId()*10+2;
        			TextView tv=(TextView)findViewById(pos);
        			tv.setText(reciver.getPlatenum());
        		}
        		if (typeid==6){
        			int pos=sd.getId()*10+2;
        			TextView tv=(TextView)findViewById(pos);
        			tv.setText(reciver.getName());
        		}
        	}
        }
        if (product!=null){
        	Button btn=(Button)findViewById(btnid);
        	btn.setBackgroundColor(btnColor2);
		    TextView tv=(TextView)findViewById(btnid+11);
		    if ((product.getClient()!=null)&&(!product.getClient().equals("")))
		        tv.setText(product.getClient());
		    else tv.setText("-");
		    tv=(TextView)findViewById(btnid+21);
		    tv.setText(product.getName());
		    tv=(TextView)findViewById(btnid+31);
		    tv.setText(product.getType());
		    tv=(TextView)findViewById(btnid+41);
		    tv.setText(String.valueOf(product.getPrice()));
		    tv=(TextView)findViewById(btnid+51);
		    if (product.isAccount()) tv.setText("是");
		    else tv.setText("否");
		    product=null;
		}
        if (staff!=null){
        	Button btn=(Button)findViewById(btnid);
        	btn.setBackgroundColor(btnColor2);
		    TextView tv=(TextView)findViewById(btnid+11);	
		    tv.setText(staff.getName());
		    staff=null;
		}
        if (asb!=null){
        	Button btn=(Button)findViewById(btnid);
        	btn.setBackgroundColor(btnColor2);
		    TextView tv=(TextView)findViewById(btnid+11);	
		    tv.setText(asb.getType());
		    tv=(TextView)findViewById(btnid+21);	
		    tv.setText(asb.getModel());
		    tv=(TextView)findViewById(btnid+31);	
		    tv.setText(asb.getName());
		    tv=(TextView)findViewById(btnid+41);	
		    tv.setText(String.valueOf(asb.getAmount()));
		    tv=(TextView)findViewById(btnid+51);	
		    tv.setText(new java.text.DecimalFormat("0.00").format(asb.getCost()/asb.getAmount()));
		    asb=null;
        }
        if (accounttype!=null){
        	Button btn=(Button)findViewById(btnid);
        	btn.setBackgroundColor(btnColor2);
		    TextView tv=(TextView)findViewById(btnid+11);	
		    tv.setText(accounttype.getClass1());
		    tv=(TextView)findViewById(btnid+21);	
		    tv.setText(accounttype.getClass2());
		    accounttype=null;
		}
        if (platenum!=null){
        	Button btn=(Button)findViewById(btnid);
        	btn.setBackgroundColor(btnColor2);
        	StepDetail sd=stepDetailList.get(btnid / 10 -1);
        	String type;
        	if (btnid % 10 ==1) type=sd.getType_1();
        	else type=sd.getType_2();
        	String typedata="";
    		if(type.indexOf(":")<type.length())
    		    typedata=type.substring(type.indexOf(":")+1);
    		if (typedata.equals("&")){
    			EditText et=(EditText)findViewById(btnid+11);	
    		    et.setText(platenum.getPlatenum());   	
    		    et=(EditText)findViewById(btnid+21);
    		    if (driver==null) et.setText(platenum.getPlatenum());  
    		    else et.setText(driver.getName());
    		}else{
    			TextView tv=(TextView)findViewById(btnid+11);	
    		    tv.setText(platenum.getPlatenum());
    		    tv=(TextView)findViewById(btnid+21);
    		    if (driver==null) tv.setText(platenum.getPlatenum());  
    		    else tv.setText(driver.getName());
    		}	    
        	platenum=null;
        	driver=null;
        }
        if (stringob!=null){
        	Button btn=(Button)findViewById(btnid);
        	btn.setBackgroundColor(btnColor2);
        	StepDetail sd=stepDetailList.get(btnid / 10 -1);
        	String type;
        	if (btnid % 10 ==1) type=sd.getType_1();
        	else type=sd.getType_2();
        	String typedata="";
    		if(type.indexOf(":")<type.length())
    		    typedata=type.substring(type.indexOf(":")+1);
    		if (typedata.equals("&")){
    			EditText et=(EditText)findViewById(btnid+11);	
    		    et.setText(stringob);
    		    stringob=null;
    		}else{
    			TextView tv=(TextView)findViewById(btnid+11);	
    		    tv.setText(stringob);
    		    stringob=null;
    		}	    
		}
        mQueue.start();
	}
	
	//监听返回键,避免误点
	//同时删除此单上传的图片
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	AlertDialog.Builder b=new Builder(RequireDetail.this);
        	b.setTitle("提示");
        	String msg="确认要退回审批列表吗";
        	if (i.getBooleanExtra("reactive", false)) {
        		msg += "\n"+"本单属于重新发起，取消发起后本单号将全部作废";
        	}
			b.setMessage(msg);
			b.setPositiveButton("确定", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					printnum=0;
					if (picnum>0){
						String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
						HashMap<String, String>map=new HashMap<String, String>();
					  	map.put("db", i.getStringExtra("db"));
					  	map.put("action", "deletePic");
					  	if (step==1) map.put("proid", proid);
					  	else map.put("proid", r.getProid());
					  	map.put("step", String.valueOf(step));
					    Response.Listener<String> listener=new Response.Listener<String>(){
					        @Override
						    public void onResponse(String response) {
							   mhandler.sendEmptyMessage(2);
						    }				
					    };
					    Response.ErrorListener elistener=new Response.ErrorListener(){  
				            @Override  
				            public void onErrorResponse(VolleyError error) {  
				            	mhandler.sendEmptyMessage(2);
				            }  
				        };
						VolleySRUtil uploadTask=new VolleySRUtil(url,map,listener,elistener);
						progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "清除数据中...", true);
					    mQueue.add(uploadTask.getRequest());
				    }else{
				    	finish();
				    }
				
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
		    return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    //处理异步任务的handler,收到对应的信息会调用对应的方法
    private Handler mhandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		switch (msg.what){
    		case 1:{
    			progressDialog.dismiss();
    		    //接受步骤相关信息完毕，开始对表单的界面信息初始化	
    			initRequire();
    			break;
    		}
    		case 2:{
    			//操作成功,关闭当前activity
    			task1=false;
    			task2=false;
    			task3=false;
    			task4=false;
    			progressDialog.dismiss();
    			if (printnum!=0){
    				String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					HashMap<String, String>map=new HashMap<String, String>();
				  	map.put("db", i.getStringExtra("db"));
					map.put("action","print");
				    map.put("id",String.valueOf(printnum));   
				    Response.Listener<String> listener=new Response.Listener<String>(){
				        @Override
					    public void onResponse(String response) {
						    if (response!=null){
						        Gson gson=new Gson();
						        Type type=new TypeToken<List<String>>(){}.getType();
						        printList=gson.fromJson(response, type);						        
						        mhandler.sendEmptyMessage(3);
						    }
						    progressDialog.dismiss();
					    }				
				    };
				    Response.ErrorListener elistener=new Response.ErrorListener(){  
			            @Override  
			            public void onErrorResponse(VolleyError error) {  
			                progressDialog.dismiss();
			                Toast.makeText(getApplicationContext(), "启动打印失败，请检查网络",Toast.LENGTH_LONG).show();
			            }  
			        };
					VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
				    progressDialog = ProgressDialog.show(RequireDetail.this, "请稍等...", "准备开始打印...", true);
				    mQueue.add(statusTask.getRequest());
    			}else{
    				AlertDialog.Builder b=new Builder(RequireDetail.this);
					b.setTitle("提示");
					b.setMessage("操作成功");
					b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							finish();
						}					
					});
				    b.create().show();	
    			}
			    break;
    		}
    		case 3:{
    			//当有打印要处理时，在结束前需要处理信息并跳转到打印活动
    			intent=new Intent(RequireDetail.this,PrintActivity.class);
    			List<String> list=new ArrayList<String>();
    			Date date=new Date();
    			DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    			list.add(sdf.format(date));
    			String comjson=mSpSettings.getString("comInfo","");
    			Gson gson=new Gson();
    			Company com=gson.fromJson(comjson,Company.class);
    		    list.add(com.getName());
    			if (step==1){
    				list.add("单号:"+proid);
    			}else{
    				list.add("单号:"+r.getProid());
    			}
    			String data=getData();
				for (String s:printList){
					
					int id=Integer.parseInt(s.substring(0,s.indexOf(":")));
					String typedata="";
					if(s.indexOf(":")<s.length())
					    typedata=s.substring(s.indexOf(":")+1);
					if (id==1){
						intent.putExtra("title",typedata);
					}
					if (id==2){
						list.add(typedata);
					}
					if (id==0){
						String num,info;
						if (typedata.indexOf("&")==-1){
							num=typedata;
							info="";
						}else{
							num=typedata.substring(0,typedata.indexOf("&"));
							info=typedata.substring(typedata.indexOf("&")+1);
						}
						String ss;
						int pos1=data.indexOf("&"+num+"#");
						ss=data.substring(pos1+1);
						int pos2=ss.indexOf("&");
					    if (pos1+num.length()+2<pos2+1+pos1) {
					    	list.add( info+data.substring(pos1+num.length()+2,pos2+1+pos1));					
					    }else{
					    	list.add(info);
					    }
					}
				}
			    Type type=new TypeToken<List<String>>(){}.getType();
			    intent.putExtra("list",gson.toJson(list,type));
			    intent.putExtra("act",String.valueOf(Integer.parseInt(i.getStringExtra("act"))+100) );
			    intent.putExtra("db",i.getStringExtra("db"));
			    if (step==1){
			    	intent.putExtra("proid",i.getStringExtra("proid"));
			    }else{
			    	intent.putExtra("proid",r.getProid());
			    	intent.putExtra("id",String.valueOf(r.getId()));
			    }
			    if (step<stepRightList.size()){
			    	intent.putExtra("last","false");
			    }else{
			    	intent.putExtra("last","true");
			    }
			    AlertDialog.Builder b=new Builder(RequireDetail.this);
				b.setTitle("提示");
				b.setMessage("本表单有打印需求 ");
				b.setCancelable(false);
				b.setPositiveButton("打印", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							startActivity(intent);
							finish();
						}					
				});
				if (!printmust){
					b.setNegativeButton("放弃", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							 AlertDialog.Builder c=new Builder(RequireDetail.this);
							 c.setTitle("提示");
							 c.setMessage("确定放弃打印吗?");
							 c.setPositiveButton("确定", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											arg0.dismiss();
											finish();
										}					
							});	
							c.setNegativeButton("取消", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										arg0.dismiss();	
									}										
							});			
					   }					
				});
				}
				b.create().show();
    		    break;
    		}
    		case 4:{
    			//设置审批状态为结束(防止提交record失败导致审批消失)
    			String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
				HashMap<String, String>map=new HashMap<String, String>();
			  	map.put("db", i.getStringExtra("db"));
				map.put("action","end");
			    map.put("id",String.valueOf(r.getId()));   
			    Response.Listener<String> listener=new Response.Listener<String>(){
			        @Override
				    public void onResponse(String response) {
					    if ((response!=null)&&(response.equals("1"))){					
					    	mhandler.sendEmptyMessage(2);
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
				VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
			    
			    mQueue.add(statusTask.getRequest());
    		}
    		//case end
    		}
    	}	
    	
    };
    
}