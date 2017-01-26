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
	//��¼�������ķ�����
	private Staff sender;
	//��¼Ҫ������������
	public static Staff reciver=null;
	//��¼ѡ�����Ʒ(�ؼ�51)
	public static Product product=null;
	//��¼ѡ��Ļ�ƿ�Ŀ(�ؼ�54)
	public static AccountType accounttype=null;
	//��¼���ַ�������(�ؼ�81,82,83,55,57,58,61,91)
	public static String stringob=null;
	//��¼ѡ���Ա��(�ؼ�71)
	public static Staff staff=null;
	//��¼ѡ���Account_stock_balance(�ؼ�57)
	public static Account_stock_balance asb=null;
	//��¼ѡ��ĳ��ƺź�˾��(�ؼ�55)
	public static Platenum platenum=null;
	public static Driver driver=null;
	//���ڿؼ�10ѡ������
	private DatePicker dp;
	//����ͨ�ÿؼ�
	private Button btnsend,btnapprove,btnreject,btnreciver;
	private TextView recivertv,sendertv,timetv,titletv,proidtv;
	//��������ʱ����������
	private EditText rejectReasonet;
	//�����ݲ��ֵĲ���
	private LinearLayout mainll;
	//��¼�ϸ����������Ϣ
	private Intent i,intent;
	//��¼��ǰλ�����̵ĵڼ���
	private int step;
	//��¼����������Ķ�̬���ɵİ�ť��id
	private int btnid;
	//������Ŀ����������ִ��ʱͳ������ɵ��������
	private int count;
	//ͳ����Ҫ¼�����ݿ�Ĵ���
	private int tot;
	//��ӡ���,0��ʾ�����ӡ,���ִ������ݿ���ĵڼ�����ӡ���ݱ�, printmust��¼��ӡ�Ƿ��Ǳ����.
	private int printnum=0;
	private boolean printmust=true;
	//����Э�������첽���񣬼�¼ÿ������������
	private boolean task1=false,task2=false,task3=false,task4=false;
	//��¼�����require
	private Require r;
	//��¼�����̵ĵ���
	private String proid;
	//��¼��ӡ��������ַ
	private String bt="";
	//ͳ��Ҫ����ĳ��λ�õ����ֱ�����˼��Σ�����ȷ��һ��Ҫ�ύ����
    private int[] sum=new int[99];
    private int[][] locate=new int[99][2];
    //�ϴ�ͼƬ��result����
    private final int RESULT_LOAD_IMAGE=1;
    //������Ƭ��result����
    private final int TAKE_PHOTO_WITH_DATA=2;
    //������Ƭ�Ĵ����ļ�
    private File cameraFile;
    //��¼���ǵ�ǰ����ڼ����ϴ�ͼƬ
    private int picnum;
    //��¼���ϴ����ļ���
    private List<String> fileNameList; 
    //�õ��ļ�����ɫ
    private ColorStateList etColor=ColorStateList.valueOf(0xFFFF66CC);
    private ColorStateList normalColor=ColorStateList.valueOf(0xFF000000);
	private int btnColor=0xFFFF66CC;
	private int btnColor2=0xFFD3D3D3;
    //�м��
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
        //ѯ�ʷ������õ��������̵Ĳ����Ȩ��,ѯ�ʷ������õ���ǰ����ľ�������
        getBaseData();    
        
	}
	
	//��ʼ������static������ֵ
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

	//ѯ�ʷ������õ��������̵Ĳ����Ȩ��,ѯ�ʷ������õ���ǰ����ľ�������
	private void getBaseData() {
		progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "��ȡ������...", true);
		//�õ����̵Ĳ����Ȩ��
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
		
		//�õ���ǰ���������
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
			    	Toast.makeText(getApplicationContext(), "��ȡ����ʧ��",Toast.LENGTH_SHORT).show();
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
		
		//�õ������˵���Ϣ
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
			    	Toast.makeText(getApplicationContext(), "��ȡ����ʧ��",Toast.LENGTH_SHORT).show();
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

	//�Ա��Ľ�����Ϣ��ʼ��
	private void initRequire() {
		//������Ϣ������
		
		titletv.setText(stepRightList.get(step-1).getDescribe());
		if (step!=1) sendertv.setText("��һ��:"+sender.getName());
		if (step!=1){
			DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			timetv.setText(sdf.format(r.getTime()));
		}
		if (step==1){
			proid=i.getStringExtra("proid");
		    proidtv.setText("����:"+proid);
		}else{
			proidtv.setText("����:"+r.getProid());
		}
		
		//���ݵ�ǰ���������ť�ɼ���
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
			recivertv.setText("��ѡ��");
			btnreciver.setVisibility(View.VISIBLE);
		}

		
		//���ݲ������ݶ�̬���ɿؼ�
        for (StepDetail sd:stepDetailList){
			
			int id=sd.getId(),isNew=sd.getIsnew();
			String type1=sd.getType_1(),type2=sd.getType_2();
			LinearLayout ll=new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			//�ֱ���createTypeView�������������ؼ������
			ll.addView(createTypeView(type1,id*10+1,isNew));
			ll.addView(createTypeView(type2,id*10+2,isNew));
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			ll.setLayoutParams(lytp);
			mainll.addView(ll);
		}
	}	
	
	//�����������type��������view
	private View createTypeView(String type,int pos,int isNew){
		//�õ��ؼ�����id
		int typeid=Integer.parseInt(type.substring(0,type.indexOf(":")));
		//�õ��ؼ�������
		String typedata="";
		if(type.indexOf(":")<type.length())
		    typedata=type.substring(type.indexOf(":")+1);
		
		switch (typeid){
		case 0:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
			list.add("δѡ��");
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
	        //������ʽ
	        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        //����������
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
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
				tv.setText("���ѡ������");
				tv.setTextColor(etColor);
			}else{
				tv.setText(typedata);
			}
			if (i.getBooleanExtra("reactive", false)){
				String date=inheritData(String.valueOf(pos));
				tv.setText(date.substring(0, 4)+"��"+date.substring(4, 6)+"��"+date.substring(6, 8)+"��");
			}
			
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);	
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			tv.setLayoutParams(lytp);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();	
					AlertDialog.Builder b=new Builder(RequireDetail.this);
					b.setTitle("ѡ������");
					dp = new DatePicker(b.getContext());
					b.setView(dp);
					b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							int day=dp.getDayOfMonth();
							int month=dp.getMonth()+1;
							int year=dp.getYear();
							TextView tv=(TextView)findViewById(btnid);
							tv.setText(year+"��"+month+"��"+day+"��");
							tv.setTextColor(normalColor);
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
			});
			return tv;
		}
		case 11:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��ͼƬ");
			btn.setBackgroundResource(R.drawable.bg_button3);
			btn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					btnid=arg0.getId();
					AlertDialog.Builder b=new Builder(RequireDetail.this);
					b.setTitle("��ʾ");
					b.setMessage("��ѡ���ȡ��ʽ");
					b.setPositiveButton("���", new DialogInterface.OnClickListener(){

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
					b.setNegativeButton("ͼ��", new  DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							arg0.dismiss();						
							//ѡ���ļ��ϴ�	
							Intent intent = new Intent(  Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  						   
							startActivityForResult(intent, RESULT_LOAD_IMAGE);  
						}
						
					});
				    b.create().show();			
				}
			});
			LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 12:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			if (inheritData(typedata).equals("")){
				btn.setText("��ͼƬ");
			}else{
				btn.setText("�鿴ͼƬ");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
					if (num.equals("(��)")){
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
			btn.setText("ѡ����Ʒ");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 53:{
			TextView tv=new TextView(this);
			tv.setTextSize(17);
			tv.setId(pos);
	        tv.setVisibility(View.GONE);
	        LinearLayout.LayoutParams lytp = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
			btn.setText("ѡ���ƿ�Ŀ");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 55:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ���ƺ�");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
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
	        lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			tv.setLayoutParams(lytp);
			return tv;
		}
		case 57:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ����Ʒ");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 58:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��ֿ�");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 61:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��Ӧ��");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 62:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ����Ʒ");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 63:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("���׶���");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 71:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��Ա��(APP)");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 72:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��Ա��");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 81:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ�񸶿����");
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
					if (p.equals("δѡ��")){
						Toast.makeText(getApplicationContext(), "����ѡ�񸶿�����", Toast.LENGTH_SHORT).show();
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 82:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��Ǯ���˻�");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 83:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ��Ӧ��");
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		case 91:{
			Button btn=new Button(this);
			btn.setTextSize(17);
			btn.setId(pos);
			btn.setText("ѡ���տ����");
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
					if (p.equals("δѡ��")){
						Toast.makeText(getApplicationContext(), "����ѡ���տ�����", Toast.LENGTH_SHORT).show();
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
			lytp.setMargins(0,lineMargin,0,lineMargin);//4��������˳��ֱ�����������
			btn.setLayoutParams(lytp);
			return btn;
		}
		//switch����
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
		//���Ϊ�������û������
		case 1:{
			EditText et=(EditText)findViewById(pos);
			if (et.getText().toString().equals("")) return false;
			break;
		}
		
		case 8:{
			Spinner sp=(Spinner)findViewById(pos);
			if ((sp.getSelectedItem() == null)||(sp.getSelectedItem().toString().equals("δѡ��"))){
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
			if (s.equals("���ѡ������")||(s.equals(typedata))){
				return false;
			}
			break;
		}
		case 11:{
			Button btn=(Button)findViewById(pos);
			if (btn.getText().toString().equals("�ϴ�ͼƬ")){
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
		//���Ϊ�ܽ��,�����ͳ��
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
		//���Ϊѡ����Ʒ,�ж��Ƿ��Ѿ�ѡ��
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
		//switch����
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
				//����Ǽ̳еĻ���Ҫȥ������ǰ��ӵ�ע��
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
			String year=a.substring(0,a.indexOf("��"));
			String month=a.substring(a.indexOf("��")+1,a.indexOf("��"));
			if (month.length()<2) month="0"+month;
			String day=a.substring(a.indexOf("��")+1,a.indexOf("��"));
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
		//switch����
		}
		//ɾ��������app���������ַ� # �� &
		s.replace("#", "");
		s.replace("&", "");
		return s;
	}
	
	//�ѱ�����Ϣת����data�ַ���
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
	
	//Ϊsum��ֵ��ΪdataType��ֵ
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
	
	//��Ҫ¼�����ݿ����Ϣ�����list,num��ʾ�ǵڼ���¼��
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
	
	//����¼�����ݿ����������
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
			    		//���¼�������Ϣ��ʹ����״̬��Ϊ����
			    		mhandler.sendEmptyMessage(4);
			    	}
			    }else{
			    	 progressDialog.dismiss();
			    	 Toast.makeText(getApplicationContext(), "����ʧ�ܣ���������",Toast.LENGTH_LONG).show();
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
	
	//���̽���ʱ�ѽ���������ݿ�
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
	
    //�����µ�����
	public void send(View v){
		
		AlertDialog.Builder b=new Builder(RequireDetail.this);
		b.setTitle("��ʾ");
		b.setMessage("��ȷ��Ҫ�����������");
		b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//�жϵ�ǰ���Ƿ���д����
				String error="";
				arg0.dismiss();
				boolean isFull=true;
				for (StepDetail sd:stepDetailList)
					if (sd.getIsnew()==0)
					    if (!(isFill(sd.getType_1(),sd.getId()*10+1)&&isFill(sd.getType_2(),sd.getId()*10+2))){
					    	isFull=false;
					    	error="��Ҫ��Ϣ�����������";
					    	break;
					    }
				//���ûѡ��������
				if ((step<stepRightList.size())&&(reciver==null)) {
					isFull=false;
					error="ûѡ��������";
				}

				if (!isFull){
					Toast.makeText(getApplicationContext(), error,Toast.LENGTH_SHORT).show();
				}else{
					//���������в���������,������һ��
					String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
					//��post������װ��Ҫ���͵���Ϣ
					HashMap<String, String> map=new HashMap<String, String>();
					map.put("db", i.getStringExtra("db"));
				    map.put("action","insert");
					map.put("sender",i.getStringExtra("userid"));
					map.put("reciver",String.valueOf(reciver.getId()));
					map.put("previd","0");
					map.put("proid", proid);
					map.put("act", String.valueOf(Integer.parseInt(i.getStringExtra("act"))+100));
					map.put("data",getData());
					//���ڷ��������ؽ���Ĵ���
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
					progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "��ȡ������...", true);
					mQueue.add(insertTask.getRequest());

				}
			  
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
	
	//ͨ��������
	public void approve(View v){
		AlertDialog.Builder b=new Builder(RequireDetail.this);
		b.setTitle("��ʾ");
		b.setMessage("��ȷ��Ҫͨ����������");
		b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				//�жϵ�ǰ���Ƿ���д����
				boolean isFull=true;
				for (StepDetail sd:stepDetailList)
					if (sd.getIsnew()==0)
					    if (!(isFill(sd.getType_1(),sd.getId()*10+1)&&isFill(sd.getType_2(),sd.getId()*10+2))){
					    	isFull=false;
					    	break;
					    }
				//���ûѡ��������
				if ((step<stepRightList.size())&&(reciver==null)) isFull=false;
				if (!isFull){
					Toast.makeText(getApplicationContext(), "��Ҫ��Ϣ��д������",Toast.LENGTH_SHORT).show();
				}else{
					//�жϵ�ǰ�Ƿ��Ѿ����������һ�����Ӷ�����������
				    if (step<stepRightList.size()){
					    //��һ��,���������в���������
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
						//�ڶ��������õ�ǰ����״̬Ϊ����
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
					    progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "��ȡ������...", true);
					    mQueue.add(statusTask.getRequest());
				    }else{
				    	//��һ���������̽��¼�����ݿ�
				    	int type=Integer.parseInt(i.getStringExtra("act")) % 100;
				    	progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "��ȡ������...", true);
				    	recordResult(type);
						
				    }
				}
				
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
	
	//���ظ�����
	public void reject(View v){
		AlertDialog.Builder b=new Builder(RequireDetail.this);
		b.setTitle("��ʾ");
		b.setMessage("��ȷ��Ҫ���ظ�������");
		rejectReasonet=new EditText(b.getContext());
		b.setView(rejectReasonet);
		b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				printnum=0;
				String rejectReason=rejectReasonet.getText().toString();
				arg0.dismiss();
				//���õ�ǰ����״̬Ϊ����
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
			    progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "������...", true);
			    mQueue.add(statusTask.getRequest());
			    //�����һ���ϴ���ͼƬ����
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
				//�жϵ�ǰ�ǲ������̵ĵڶ���
			    if (r.getPrevid()!=0){
				    //����������̵ڶ�������ѵ�ǰ�������һ������������Ϊ������(������������д)
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
		b.setNegativeButton("ȡ��", new  DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.dismiss();
			}
			
		});
	    b.create().show();
	}
	//�ϴ�ͼƬ��������
	public void uploadPicture(Bitmap bitmap){
		byte[] buffer=Functions.imageZoom(bitmap);
        String photo = Base64.encodeToString(buffer, 0, buffer.length,Base64.DEFAULT);  
        progressDialog.dismiss();
        progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "ͼƬ�ϴ���...", true);
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
		        	btn.setText("�ϴ����");
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
	        	btn.setText("�ϴ�ͼƬ");
	        	progressDialog.dismiss();
            }  
        };
		VolleySRUtil uploadTask=new VolleySRUtil(url,map,listener,elistener);	
	    mQueue.add(uploadTask.getRequest());
	}
	
	//��������˰�ť���뵽ѡ�������˵Ľ���
	public void chosereciver(View v){
		Intent intent=new Intent(RequireDetail.this,RequireReciverList.class);
		intent.putExtra("staff", stepRightList.get(step).getStaff());
		intent.putExtra("db", i.getStringExtra("db"));
		startActivity(intent);
	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	     
	    //����ѡ��ͼƬ�Ľ��
	    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {  
	        progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "ͼƬ������...", true);
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
	    //����������Ƭ�Ľ��
	    if (requestCode == TAKE_PHOTO_WITH_DATA && cameraFile.exists()) {
	    	progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "ͼƬ������...", true);
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
		    if (product.isAccount()) tv.setText("��");
		    else tv.setText("��");
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
	
	//�������ؼ�,�������
	//ͬʱɾ���˵��ϴ���ͼƬ
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	AlertDialog.Builder b=new Builder(RequireDetail.this);
        	b.setTitle("��ʾ");
        	String msg="ȷ��Ҫ�˻������б���";
        	if (i.getBooleanExtra("reactive", false)) {
        		msg += "\n"+"�����������·���ȡ������󱾵��Ž�ȫ������";
        	}
			b.setMessage(msg);
			b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
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
						progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "���������...", true);
					    mQueue.add(uploadTask.getRequest());
				    }else{
				    	finish();
				    }
				
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
		    return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    //�����첽�����handler,�յ���Ӧ����Ϣ����ö�Ӧ�ķ���
    private Handler mhandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		switch (msg.what){
    		case 1:{
    			progressDialog.dismiss();
    		    //���ܲ��������Ϣ��ϣ���ʼ�Ա��Ľ�����Ϣ��ʼ��	
    			initRequire();
    			break;
    		}
    		case 2:{
    			//�����ɹ�,�رյ�ǰactivity
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
			                Toast.makeText(getApplicationContext(), "������ӡʧ�ܣ���������",Toast.LENGTH_LONG).show();
			            }  
			        };
					VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
				    progressDialog = ProgressDialog.show(RequireDetail.this, "���Ե�...", "׼����ʼ��ӡ...", true);
				    mQueue.add(statusTask.getRequest());
    			}else{
    				AlertDialog.Builder b=new Builder(RequireDetail.this);
					b.setTitle("��ʾ");
					b.setMessage("�����ɹ�");
					b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

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
    			//���д�ӡҪ����ʱ���ڽ���ǰ��Ҫ������Ϣ����ת����ӡ�
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
    				list.add("����:"+proid);
    			}else{
    				list.add("����:"+r.getProid());
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
				b.setTitle("��ʾ");
				b.setMessage("�����д�ӡ���� ");
				b.setCancelable(false);
				b.setPositiveButton("��ӡ", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							startActivity(intent);
							finish();
						}					
				});
				if (!printmust){
					b.setNegativeButton("����", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							 AlertDialog.Builder c=new Builder(RequireDetail.this);
							 c.setTitle("��ʾ");
							 c.setMessage("ȷ��������ӡ��?");
							 c.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											arg0.dismiss();
											finish();
										}					
							});	
							c.setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){
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
    			//��������״̬Ϊ����(��ֹ�ύrecordʧ�ܵ���������ʧ)
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