package com.zhtx.app.require;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import com.zhtx.app.R;

import com.zhtx.app.util.DragImageView;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import android.view.Window;

public class RequirePicture extends Activity {
		private int window_width, window_height;// �ؼ����
		private DragImageView dragImageView;// �Զ���ؼ�
		private int state_height;// ״̬���ĸ߶�
		private ViewTreeObserver viewTreeObserver;
        Intent i;
        Bitmap bmp;
        private ProgressDialog progressDialog = null;
        
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.require_picture);
			i=getIntent();
			/** ��ȡ��Ҋ����߶� **/
			DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            window_width = dm.widthPixels;
            window_height = dm.heightPixels;

			dragImageView = (DragImageView) findViewById(R.id.pic);
			progressDialog = ProgressDialog.show(RequirePicture.this, "���Ե�...", "ͼƬ������...", true);
			getPicThread t=new getPicThread();
			t.start();

		}
		
		class getPicThread extends Thread{
		   
		    public void run(){
		    	
		    	BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Config.ARGB_8888;
				options.inInputShareable = true;
				options.inPurgeable = true;
				try {
			            URL myFileUrl = new URL(i.getStringExtra("url"));
			            HttpURLConnection conn = (HttpURLConnection) myFileUrl
			                    .openConnection();
			            conn.setDoInput(true);
			            conn.connect();
			            InputStream is = conn.getInputStream();
			            bmp = BitmapFactory.decodeStream(is);
			            is.close();
			            bmp=getBitmap(bmp,  window_width, window_height);
			            mhandler.sendEmptyMessage(0);
			        } catch (OutOfMemoryError e) {
			            e.printStackTrace();
			            bmp = null;
			        } catch (IOException e) {
			            e.printStackTrace();
			            bmp = null;
			        }
			   
		    }
		}
		
		/***
		 * �ȱ���ѹ��ͼƬ
		 * 
		 * @param bitmap
		 * @param screenWidth
		 * @param screenHight
		 * @return
		 */
		private Bitmap getBitmap(Bitmap bitmap, int screenWidth,
				int screenHight) {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			Matrix matrix = new Matrix();
			float scale = (float) screenWidth / w;
			float scale2 = (float) screenHight / h;

			scale = scale < scale2 ? scale : scale2;
			// ��֤ͼƬ������.
			matrix.postScale(scale, scale);
			// w,h��ԭͼ������.
			return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
			
		}
		
		private Handler mhandler=new Handler(){
	    	@Override
	    	public void handleMessage(Message msg){
	    		super.handleMessage(msg);
	    		progressDialog.dismiss();
	    		// ����ͼƬ
				dragImageView.setImageBitmap(bmp);
				dragImageView.setmActivity(RequirePicture.this);//ע��Activity.
				/** ����״̬���߶� **/
				viewTreeObserver = dragImageView.getViewTreeObserver();
				viewTreeObserver
						.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

							@Override
							public void onGlobalLayout() {
								if (state_height == 0) {
									// ��ȡ״�����߶�
									Rect frame = new Rect();
									getWindow().getDecorView()
											.getWindowVisibleDisplayFrame(frame);
									state_height = frame.top;
									dragImageView.setScreen_H(window_height-state_height);
									dragImageView.setScreen_W(window_width);
								}

							}
						});
	    	}	
	    };
}
