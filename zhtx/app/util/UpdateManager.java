package com.zhtx.app.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateManager {
    private static final int DOWN_NOSDCARD=0;
    private static final int DOWN_UPDATE=1;
    private static final int DOWN_OVER=2;
    
    private Context mContext;
    private Dialog downloadDialog;
    private ProgressBar mProgress;
    private TextView mProgressText;
    private int progress;
    private boolean interceptFlag=false;
    //apk的下载地址
    private String apkUrl="http://"+Constant.ip+"/ZhtxServer/appDemo.apk";
    //apk的保存地址
    private String apkFilePath="";
    //下载包保存路径
    private String savePath="";
    //临时下载文件路径 
    private String tmpFilePath="";
    //下载文件大小
    private String apkFileSize;
    //已下载文件大小
    private String tmpFileSize;
    
    public UpdateManager(Context context){
    	this.mContext=context;
    	
    }
    
    public void showDownloadDialog(){
    	AlertDialog.Builder b=new Builder(mContext);
    	LinearLayout ll=new LinearLayout(b.getContext());
    	ll.setOrientation(LinearLayout.VERTICAL);
    	mProgress=new ProgressBar(b.getContext());
    	mProgressText=new TextView(b.getContext());
    	mProgressText.setGravity(Gravity.CENTER);
    	mProgressText.setTextSize(15);
    	ll.addView(mProgress);
    	ll.addView(mProgressText);
    	b.setTitle("下载中");
    	b.setView(ll);
    	b.setNegativeButton("取消", new  DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				interceptFlag=true;
				arg0.dismiss();
			}
			
		});
    	downloadDialog=b.create();
    	downloadDialog.setCanceledOnTouchOutside(false);
    	downloadDialog.show();
    	downloadApk();
    }
    
    private Handler mHandler = new Handler(){
    	public void handleMessage(Message msg){
    		switch (msg.what){
    		case DOWN_UPDATE:{
    			mProgress.setProgress(progress);
    			mProgressText.setText(tmpFileSize+"/"+apkFileSize);
    			break;
    		}
    		case DOWN_OVER:{
    			downloadDialog.dismiss();
    			installApk();
    			break;
    		}
    		case DOWN_NOSDCARD:{
    			downloadDialog.dismiss();
    			Toast.makeText(mContext, "无法下载安装文件，请检查SD卡是否挂载", 3000).show();
    			break;
    		}
    		}
    	}

		
    };
    
    private Runnable mdownApkRunnable =new Runnable(){
    	@Override
    	public void run(){
    		try{
    			String apkName="appdemo.apk";
    			String tmpName="appdemo.tmp";
    			String storageState=Environment.getExternalStorageState();
    			if (storageState.equals(Environment.MEDIA_MOUNTED)){
    				savePath=Environment.getExternalStorageDirectory().getAbsolutePath()
    						+"/Update/";
    				File file =new File (savePath);
    				if (!file.exists()){
    					file.mkdirs();
    				}   			
    			    apkFilePath=savePath+apkName;
    				tmpFilePath=savePath+tmpName;
    			}
    			
    			if ((apkFilePath==null)||(apkFilePath=="")){
    				mHandler.sendEmptyMessage(DOWN_NOSDCARD);
    				return;
    			}
    			File ApkFile =new File(apkFilePath);
    			if (ApkFile.exists()){
    				ApkFile.delete();
    			}
    			
    			File tmpFile =new File (tmpFilePath);
    			FileOutputStream fos=new FileOutputStream(tmpFile);
    			URL url=new URL(apkUrl);
    			HttpURLConnection conn=(HttpURLConnection)url.openConnection();
    			conn.connect();
    			int length=conn.getContentLength();
    			InputStream is=conn.getInputStream();
    			
    			DecimalFormat df=new DecimalFormat("0.00");
    			apkFileSize=df.format((float)length/1024/1024)+"MB";
    			int count=0;
    			byte buf[]=new byte[1024];
    			do{
    				int numread=is.read(buf);
    				count+=numread;
    				tmpFileSize=df.format((float)count/1024/1024)+"MB";
    				progress=(int)(((float)count/length)*100);
    				mHandler.sendEmptyMessage(DOWN_UPDATE);
    				if (numread<=0){
    					if (tmpFile.renameTo(ApkFile)){
    						mHandler.sendEmptyMessage(DOWN_OVER);
    					}
    					break;
    				}
    				fos.write(buf,0,numread);
    			}while (!interceptFlag)	;
    			fos.close();
    			is.close();			
    		}catch(MalformedURLException e){
    			e.printStackTrace();
    		}catch(IOException e){
    			e.printStackTrace();
    		}
    		
    	}
    };
    
    private void installApk() {
		File apkFile =new File(apkFilePath);
		if (!apkFile.exists()){
			return;
		}
		Intent i= new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://"+apkFile.toString()),"application/vnd.android.package_archive");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(i);
		
	}
    
    private void downloadApk(){
    	Thread downloadThread=new Thread(mdownApkRunnable);
    	downloadThread.start();
    }
}
