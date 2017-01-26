package com.zhtx.printer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.EscCommand.ENABLE;
import com.gprinter.command.EscCommand.FONT;
import com.gprinter.command.EscCommand.HEIGHT_ZOOM;
import com.gprinter.command.EscCommand.JUSTIFICATION;
import com.gprinter.command.EscCommand.WIDTH_ZOOM;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;
import com.zhtx.app.R;
import com.zhtx.app.R.id;
import com.zhtx.app.R.layout;
import com.zhtx.app.require.RequireDetail;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.Company;
import com.zhtx.myclass.Product;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PrintActivity extends Activity {
	
	private GpService mGpService = null;
	private PrinterServiceConnection conn = null;
	private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;
	private static final int REQUEST_PRINT_RECEIPT = 0xfc;
	public static final String CONNECT_STATUS = "connect.status";
	private Intent ii;
	private List<String> list;
	private Button btnprint;
	private TextView tv1,tv2;
	public static int targetPrint=0;
	public static boolean connected=false;
	//检测是否实际打印成功过
	private boolean printed=false;
	private RequestQueue mQueue;
	//记录删除审批的完成情况
	private boolean task1,task2,normalClose=false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printtest);
        list=new ArrayList<String>();
        btnprint=(Button)findViewById(R.id.btnprint);
        tv1=(TextView)findViewById(R.id.tv1);
        tv2=(TextView)findViewById(R.id.tv2);
        ii=getIntent();
        connected=false;
        mQueue= Volley.newRequestQueue(PrintActivity.this);
        Gson gson=new Gson();
	    Type type=new TypeToken<List<String>>(){}.getType();
        list=gson.fromJson(ii.getStringExtra("list"), type);
        connection();
        // 注册实时状态查询广播
     	registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS));
    }
	
	 @Override
	 protected void onResume() {
	    super.onResume();
	    if (connected){
	       tv1.setText("设备已连接,确认打印机状态正常后点击打印");
	       tv2.setText("点击连接设备可以更换打印机");
	       btnprint.setText("打印");
	       btnprint.setBackgroundResource(R.drawable.bg_button);
		   btnprint.setClickable(true);  	
	    }else{
	       tv1.setText("请先点击连接设备按钮进入连接页面");
	       tv2.setText("");
	       btnprint.setText("未连接");
	       btnprint.setBackgroundColor(0xFFD3D3D3);
	       btnprint.setClickable(false);      
	    }
	 }
	
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	AlertDialog.Builder b=new Builder(PrintActivity.this);
				b.setTitle("提示");
				b.setMessage("您确定要结束打印吗？");
				b.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (!printed){
							task1=false;
							task2=false;
							normalClose=true;
							deleteRequire();
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
	 
	
	 private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("TAG", action);
				// GpCom.ACTION_DEVICE_REAL_STATUS 为广播的IntentFilter
				if (action.equals(GpCom.ACTION_DEVICE_REAL_STATUS)) {

					// 业务逻辑的请求码，对应哪里查询做什么操作
					int requestCode = intent.getIntExtra(GpCom.EXTRA_PRINTER_REQUEST_CODE, -1);
					// 判断请求码，是则进行业务操作
					if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

						int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
						String str;
						if (status == GpCom.STATE_NO_ERR) {
							str = "打印机正常";
						} else {
							str = "打印机 ";
							if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
								str += "脱机";
							}
							if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
								str += "缺纸";
							}
							if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
								str += "打印机开盖";
							}
							if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
								str += "打印机出错";
							}
							if ((byte) (status & GpCom.STATE_TIMES_OUT) > 0) {
								str += "查询超时";
							}
						}

						Toast.makeText(PrintActivity.this, "打印机状态：" + str, Toast.LENGTH_SHORT)
								.show();
				 } 
				 if (requestCode == REQUEST_PRINT_RECEIPT) {
						int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
						if (status == GpCom.STATE_NO_ERR) {
							sendCommand();
						} else {
							btnprint.setText("打印");
							btnprint.setClickable(true);
							Toast.makeText(PrintActivity.this, "打印机状态异常", Toast.LENGTH_SHORT).show();
						}
					}
				}				
		}
	 };	
	public void print(View v) {
		BluetoothAdapter blueadapter=BluetoothAdapter.getDefaultAdapter(); 
		if ((!(blueadapter==null))&&(blueadapter.isEnabled())){
			try {
				mGpService.queryPrinterStatus(targetPrint, 1000, REQUEST_PRINT_RECEIPT);
				btnprint.setText("正在打印");
				btnprint.setClickable(false);
			} catch (RemoteException e) {
				e.printStackTrace();
			}	        
		}else{
			Toast.makeText(getApplicationContext(),"蓝牙未开启", Toast.LENGTH_SHORT).show();
		}		
	}
	
	private void sendCommand(){
		 
		    EscCommand esc = new EscCommand();
            esc.addInitializePrinter();
            esc.addPrintAndLineFeed(); 
            //esc.addCutAndFeedPaper((byte)3);
            esc.addSelectJustification(JUSTIFICATION.CENTER);
            esc.addSelectCharacterFont(FONT.FONTA);
            esc.addTurnDoubleStrikeOnOrOff(ENABLE.ON);
            esc.addSetQuadrupleModeForKanji(ENABLE.ON);
            esc.addSetCharcterSize(WIDTH_ZOOM.MUL_2, HEIGHT_ZOOM.MUL_2);
            //设置为倍高倍宽 
            esc.addText(ii.getStringExtra("title")+"\n"); 
            // 打印文字 
            esc.addPrintAndLineFeed();     
            esc.addSetCharcterSize(WIDTH_ZOOM.MUL_1, HEIGHT_ZOOM.MUL_1);
            esc.addSelectJustification(JUSTIFICATION.LEFT);
            esc.addSetLeftMargin((short)45);
            for (String s:list){
            	esc.addText(s+"\n"); 
            	if (!s.equals("")) esc.addText("\n");
            }
            esc.addCutPaper();
            Vector<Byte> datas = esc.getCommand(); 
            //发送数据 
            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes); 
            String str = Base64.encodeToString(bytes, Base64.DEFAULT);
            int rel=0; 
            try { 
            	rel = mGpService.sendEscCommand(targetPrint, str);
            	GpCom.ERROR_CODE r=GpCom.ERROR_CODE.values()[rel];
                if(r != GpCom.ERROR_CODE.SUCCESS){ 
                    Toast.makeText(getApplicationContext(),"发送打印命令出错", Toast.LENGTH_LONG).show(); 
                    btnprint.setText("打印");
                    btnprint.setClickable(true);
                }else{
                	printed=true;
                	btnprint.setText("打印");
                    btnprint.setClickable(true);
                }
            }catch (RemoteException e) { 
        		e.printStackTrace(); 
        	}
	}
	public boolean[] getConnectState() {
		boolean[] state = new boolean[GpPrintService.MAX_PRINTER_CNT];
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
			state[i] = false;
		}
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
			try {
				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
					state[i] = true;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return state;
	}
	public void conn(View v){
		BluetoothAdapter blueadapter=BluetoothAdapter.getDefaultAdapter(); 
		if ((!(blueadapter==null))&&(blueadapter.isEnabled())){
			Intent intent = new Intent(this, PrinterConnectDialog.class);
			boolean[] state = getConnectState();
			intent.putExtra(CONNECT_STATUS, state);
			this.startActivity(intent);
		}else{
			Toast.makeText(getApplicationContext(),"蓝牙未开启", Toast.LENGTH_SHORT).show();
		}	
	}
	
	
	private void connection() {
        conn = new PrinterServiceConnection();
        Intent intent = new Intent(this, GpPrintService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
       
    }
	
	class PrinterServiceConnection implements ServiceConnection {
	        @Override
	        public void onServiceDisconnected(ComponentName name) {
	            Log.i("ServiceConnection", "onServiceDisconnected() called");
	            mGpService = null;
	        }
	        @Override
	        public void onServiceConnected(ComponentName name, IBinder service) {
	            mGpService =GpService.Stub.asInterface(service);
	        } 
	};    
	
	//如果没有成功打印，删除已经提交的审批信息
	private void deleteRequire(){
		int step=Integer.parseInt(ii.getStringExtra("act") ) / 100 -1;
		if (ii.getStringExtra("last").equals("false")){
			String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
			HashMap<String, String>map=new HashMap<String, String>();
		  	map.put("db", ii.getStringExtra("db"));
			map.put("action","delete2");
		    map.put("act",ii.getStringExtra("act"));   
		    map.put("proid", ii.getStringExtra("proid"));
		    Response.Listener<String> listener=new Response.Listener<String>(){
		        @Override
			    public void onResponse(String response) {
		        	 task1=true;
		             if (normalClose){
		            	 if (task1&&task2){
		            		 printed=true;
		            		 finish();
		            	 }
		             }
			    }				
		    };
		    Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	               
	            }  
	        };
			VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
		    mQueue.add(statusTask.getRequest());
		    if (step>1){
		    	url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
			  	map=new HashMap<String, String>();
			  	map.put("db", ii.getStringExtra("db"));
				map.put("action","active");
			    map.put("id",ii.getStringExtra("id")); 
				listener=new Response.Listener<String>(){
			        @Override
				    public void onResponse(String response) {
			        	 task2=true;
			             if (normalClose){
			            	 if (task1&&task2){
			            		 printed=true;
			            		 finish();
			            	 }
			             }
				    }				
			    };
			    elistener=new Response.ErrorListener(){  
		            @Override  
		            public void onErrorResponse(VolleyError error) {  
		      
		            }  
		        };
				VolleySRUtil statusTask2=new VolleySRUtil(url,map,listener,elistener);	
			    mQueue.add(statusTask2.getRequest());
		    }else{
		    	task2=true;
		    	if (normalClose){
	            	 if (task1&&task2){
	            		 printed=true;
	            		 finish();
	            	 }
	            }
		    }
		}else{
			String url="http://"+Constant.ip+"/ZhtxServer/RequireServlet";
			HashMap<String, String>map=new HashMap<String, String>();
		  	map.put("db", ii.getStringExtra("db"));
			map.put("action","active");
		    map.put("id",ii.getStringExtra("id")); 
		    Response.Listener<String> listener=new Response.Listener<String>(){
		        @Override
			    public void onResponse(String response) {
		        	task1=true;
		        	if (normalClose){
		            	 if (task1&&task2){
		            		 printed=true;
		            		 finish();
		            	 }
		            }
			    }				
		    };
		    Response.ErrorListener elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	      
	            }  
	        };
	        if (step>1){
	        	VolleySRUtil statusTask2=new VolleySRUtil(url,map,listener,elistener);	
			    mQueue.add(statusTask2.getRequest());
	        }else{
	        	task1=true;
	        }
			
		    
		    url="http://"+Constant.ip+"/ZhtxServer/RecordServlet";
		  	map=new HashMap<String, String>();
		  	map.put("db", ii.getStringExtra("db"));
			map.put("action","delete");
		    map.put("proid",ii.getStringExtra("proid")); 
		    map.put("type",Constant.action[Integer.parseInt(ii.getStringExtra("act")) % 100 -1]);
			listener=new Response.Listener<String>(){
		        @Override
			    public void onResponse(String response) {
		        	task2=true;
		        	if (normalClose){
		            	 if (task1&&task2){
		            		 printed=true;
		            		 finish();
		            	 }
		            }
			    }				
		    };
		    elistener=new Response.ErrorListener(){  
	            @Override  
	            public void onErrorResponse(VolleyError error) {  
	      
	            }  
	        };
			VolleySRUtil statusTask=new VolleySRUtil(url,map,listener,elistener);	
		    mQueue.add(statusTask.getRequest());
		}
	}
     
	  @Override
	  public void onDestroy() {
		super.onDestroy();
		//如果并没有打印成功，作废之前提交的审批内容
		if (!printed){
			deleteRequire();
		}
		try {
			mGpService.closePort(targetPrint);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if (conn != null) {
		    unbindService(conn); // unBindService
	    }	
	 }   		
	  
	  
}
