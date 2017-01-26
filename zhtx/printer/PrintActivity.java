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
	//����Ƿ�ʵ�ʴ�ӡ�ɹ���
	private boolean printed=false;
	private RequestQueue mQueue;
	//��¼ɾ��������������
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
        // ע��ʵʱ״̬��ѯ�㲥
     	registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS));
    }
	
	 @Override
	 protected void onResume() {
	    super.onResume();
	    if (connected){
	       tv1.setText("�豸������,ȷ�ϴ�ӡ��״̬����������ӡ");
	       tv2.setText("��������豸���Ը�����ӡ��");
	       btnprint.setText("��ӡ");
	       btnprint.setBackgroundResource(R.drawable.bg_button);
		   btnprint.setClickable(true);  	
	    }else{
	       tv1.setText("���ȵ�������豸��ť��������ҳ��");
	       tv2.setText("");
	       btnprint.setText("δ����");
	       btnprint.setBackgroundColor(0xFFD3D3D3);
	       btnprint.setClickable(false);      
	    }
	 }
	
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	AlertDialog.Builder b=new Builder(PrintActivity.this);
				b.setTitle("��ʾ");
				b.setMessage("��ȷ��Ҫ������ӡ��");
				b.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){

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
	 
	
	 private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("TAG", action);
				// GpCom.ACTION_DEVICE_REAL_STATUS Ϊ�㲥��IntentFilter
				if (action.equals(GpCom.ACTION_DEVICE_REAL_STATUS)) {

					// ҵ���߼��������룬��Ӧ�����ѯ��ʲô����
					int requestCode = intent.getIntExtra(GpCom.EXTRA_PRINTER_REQUEST_CODE, -1);
					// �ж������룬�������ҵ�����
					if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

						int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
						String str;
						if (status == GpCom.STATE_NO_ERR) {
							str = "��ӡ������";
						} else {
							str = "��ӡ�� ";
							if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
								str += "�ѻ�";
							}
							if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
								str += "ȱֽ";
							}
							if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
								str += "��ӡ������";
							}
							if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
								str += "��ӡ������";
							}
							if ((byte) (status & GpCom.STATE_TIMES_OUT) > 0) {
								str += "��ѯ��ʱ";
							}
						}

						Toast.makeText(PrintActivity.this, "��ӡ��״̬��" + str, Toast.LENGTH_SHORT)
								.show();
				 } 
				 if (requestCode == REQUEST_PRINT_RECEIPT) {
						int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
						if (status == GpCom.STATE_NO_ERR) {
							sendCommand();
						} else {
							btnprint.setText("��ӡ");
							btnprint.setClickable(true);
							Toast.makeText(PrintActivity.this, "��ӡ��״̬�쳣", Toast.LENGTH_SHORT).show();
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
				btnprint.setText("���ڴ�ӡ");
				btnprint.setClickable(false);
			} catch (RemoteException e) {
				e.printStackTrace();
			}	        
		}else{
			Toast.makeText(getApplicationContext(),"����δ����", Toast.LENGTH_SHORT).show();
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
            //����Ϊ���߱��� 
            esc.addText(ii.getStringExtra("title")+"\n"); 
            // ��ӡ���� 
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
            //�������� 
            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes); 
            String str = Base64.encodeToString(bytes, Base64.DEFAULT);
            int rel=0; 
            try { 
            	rel = mGpService.sendEscCommand(targetPrint, str);
            	GpCom.ERROR_CODE r=GpCom.ERROR_CODE.values()[rel];
                if(r != GpCom.ERROR_CODE.SUCCESS){ 
                    Toast.makeText(getApplicationContext(),"���ʹ�ӡ�������", Toast.LENGTH_LONG).show(); 
                    btnprint.setText("��ӡ");
                    btnprint.setClickable(true);
                }else{
                	printed=true;
                	btnprint.setText("��ӡ");
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
			Toast.makeText(getApplicationContext(),"����δ����", Toast.LENGTH_SHORT).show();
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
	
	//���û�гɹ���ӡ��ɾ���Ѿ��ύ��������Ϣ
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
		//�����û�д�ӡ�ɹ�������֮ǰ�ύ����������
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
