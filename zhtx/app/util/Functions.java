package com.zhtx.app.util;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Functions {
	//���ɵ���(������)
	public static String createProid(){
    	Date date=new Date();
    	DateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    	String s=sdf.format(date);
    	s.substring(1,8);
    	int x=(int)(Math.random()*8999)+1000;
    	s=s+String.valueOf(x);
		return s;
    }

    //�ж��������
    public static boolean checkNet(Context context) {     
    	 try {     
    	        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);     
    	        if (connectivity != null) {     
    	             
    	            NetworkInfo info = connectivity.getActiveNetworkInfo();     
    	            if (info != null && info.isConnected()) {     
    	                 
    	                if (info.getState() == NetworkInfo.State.CONNECTED) {     
    	                    return true;     
    	                }     
    	            }     
    	        }     
    	    } catch (Exception e) {     
    	        return false;     
    	    }     
    	  return false;     
    }    
    //�ж��Ƿ�������
    public static boolean isDouble(String value) {
    	  try {
    	      Double.parseDouble(value);
    	  } catch (NumberFormatException e) {
    	       return false;
    	  }
    	  return true;
     }
    //ѹ��ͼƬ
    public static byte[] imageZoom(Bitmap bitMap) {  
        //ͼƬ�������ռ�   ��λ��KB  
        double maxSize =300.00;  
        //��bitmap���������У�����bitmap�Ĵ�С����ʵ�ʶ�ȡ��ԭ�ļ�Ҫ��    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        bitMap.compress(Bitmap.CompressFormat.PNG, 100, baos);  
        byte[] b = baos.toByteArray();  
        Log.e("ѹ��", String.valueOf(b.length/1024));
        //���ֽڻ���KB  
        double mid = b.length/1024;  
        //�ж�bitmapռ�ÿռ��Ƿ�����������ռ�  ���������ѹ�� С����ѹ��  
        if (mid > maxSize) {  
                //��ȡbitmap��С ����������С�Ķ��ٱ�  
                double i = mid / maxSize;  
                //��ʼѹ��  �˴��õ�ƽ���� ������͸߶�ѹ������Ӧ��ƽ������ ��1.���̶ֿȺ͸߶Ⱥ�ԭbitmap����һ�£�ѹ����Ҳ�ﵽ������Сռ�ÿռ�Ĵ�С��  
                bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),  
                                bitMap.getHeight() / Math.sqrt(i));  
        }   
        baos = new ByteArrayOutputStream(); 
        bitMap.compress(Bitmap.CompressFormat.PNG, 100, baos);  
        b = baos.toByteArray();  
        return b;
    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,  
                double newHeight) {  
        // ��ȡ���ͼƬ�Ŀ�͸�  
        float width = bgimage.getWidth();  
        float height = bgimage.getHeight();  
        // ��������ͼƬ�õ�matrix����  
        Matrix matrix = new Matrix();  
        // ������������  
        float scaleWidth = ((float) newWidth) / width;  
        float scaleHeight = ((float) newHeight) / height;  
        // ����ͼƬ����  
        matrix.postScale(scaleWidth, scaleHeight);  
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,  
                        (int) height, matrix, true);  
        return bitmap;  
    }  
    
    //�ж�������ʽ��ȷ���
    public static Boolean isBlueTooth(String s){
    	if (s.length() != 17) return false;
    	int index=2;
    	for (int i=1; i<=5; i++){
    		if (s.charAt(index) != ':' ) return false;
    		index+=3;
    	}
    	return true;
    }
    
    //ɾ������еķǷ��ַ�(�ؼ�13)
    public static String deleteInvalidCharPrice(String s){
    	if (s.length()>1) s=s.substring(0,s.indexOf("("));
    	//С������ܴ�����λ
    	if ((s.indexOf(".")!=-1)&&(s.indexOf(".")<s.length()-3)){
    		s=s.substring(0,s.indexOf(".")+3);
    	}
    	
    	return s;
    }
    
    //������ʽ��(�ؼ�13)
    public static String formatPrice(String s){
    	String a=s;
    	//�õ�������
    	while (a.indexOf(",")!=-1){
    		a=a.substring(0,a.indexOf(","))+a.substring(a.indexOf(",")+1);
    	}
    	if (a.charAt(0)=='.'){
    		a="0"+a;
    	}
    	//��Ӷ���
    	int index=s.indexOf(".");
    	if (index==-1){
    		if (a.length()>3){
    			int p=a.length()-3;
    			while (p>0){
    				a=a.substring(0,p)+","+a.substring(p);
    				p=p-3;
    			}
    		}
    	}else{
    		a=a.substring(0,a.indexOf("."));
    		if (a.length()>3){
    			int p=a.length()-3;
    			while (p>0){
    				a=a.substring(0,p)+","+a.substring(p);
    				p=p-3;
    			}
    		}
    		a=a+s.substring(index);
    	}
    	String b="";
    	//���������λ��ʾ
    	if (a.indexOf(".")!=-1) b=a.substring(0, a.indexOf("."));
    	else b=a;
    	a+="(";
    	switch (b.length()){
    	case 1:
    		a+="��";
    		break;
    	case 2:
    		a+="ʮ";
    		break;
    	case 3:
    		a+="��";
    		break;
    	case 5:
    		a+="ǧ";
    		break;
    	case 6:
    		a+="��";
    		break;
    	case 7:
    		a+="ʮ��";
    		break;
    	case 9:
    		a+="����";
    		break;
    	case 10:
    		a+="ǧ��";
    		break;
    	case 11:
    		a+="��";
    		break;
    	case 13:
    		a+="ʮ��";
    		break;
    	}
    	a+=")";
    	return a;
    }
    
    //�Ѹ�ʽ���Ľ���Ϊ����
    public static String DeformatPrice(String s){
    	if (s.indexOf("(")!=-1) s=s.substring(0,s.indexOf("("));
		while (s.indexOf(",")!=-1){
    		s=s.substring(0,s.indexOf(","))+s.substring(s.indexOf(",")+1);
    	}
		return s;
    }
}
