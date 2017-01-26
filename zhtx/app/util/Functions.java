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
	//生成单号(已作废)
	public static String createProid(){
    	Date date=new Date();
    	DateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
    	String s=sdf.format(date);
    	s.substring(1,8);
    	int x=(int)(Math.random()*8999)+1000;
    	s=s+String.valueOf(x);
		return s;
    }

    //判断联网情况
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
    //判断是否是数字
    public static boolean isDouble(String value) {
    	  try {
    	      Double.parseDouble(value);
    	  } catch (NumberFormatException e) {
    	       return false;
    	  }
    	  return true;
     }
    //压缩图片
    public static byte[] imageZoom(Bitmap bitMap) {  
        //图片允许最大空间   单位：KB  
        double maxSize =300.00;  
        //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        bitMap.compress(Bitmap.CompressFormat.PNG, 100, baos);  
        byte[] b = baos.toByteArray();  
        Log.e("压缩", String.valueOf(b.length/1024));
        //将字节换成KB  
        double mid = b.length/1024;  
        //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩  
        if (mid > maxSize) {  
                //获取bitmap大小 是允许最大大小的多少倍  
                double i = mid / maxSize;  
                //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）  
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
        // 获取这个图片的宽和高  
        float width = bgimage.getWidth();  
        float height = bgimage.getHeight();  
        // 创建操作图片用的matrix对象  
        Matrix matrix = new Matrix();  
        // 计算宽高缩放率  
        float scaleWidth = ((float) newWidth) / width;  
        float scaleHeight = ((float) newHeight) / height;  
        // 缩放图片动作  
        matrix.postScale(scaleWidth, scaleHeight);  
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,  
                        (int) height, matrix, true);  
        return bitmap;  
    }  
    
    //判断蓝牙格式正确与否
    public static Boolean isBlueTooth(String s){
    	if (s.length() != 17) return false;
    	int index=2;
    	for (int i=1; i<=5; i++){
    		if (s.charAt(index) != ':' ) return false;
    		index+=3;
    	}
    	return true;
    }
    
    //删除金额中的非法字符(控件13)
    public static String deleteInvalidCharPrice(String s){
    	if (s.length()>1) s=s.substring(0,s.indexOf("("));
    	//小数点后不能大于两位
    	if ((s.indexOf(".")!=-1)&&(s.indexOf(".")<s.length()-3)){
    		s=s.substring(0,s.indexOf(".")+3);
    	}
    	
    	return s;
    }
    
    //给金额格式化(控件13)
    public static String formatPrice(String s){
    	String a=s;
    	//得到纯数字
    	while (a.indexOf(",")!=-1){
    		a=a.substring(0,a.indexOf(","))+a.substring(a.indexOf(",")+1);
    	}
    	if (a.charAt(0)=='.'){
    		a="0"+a;
    	}
    	//添加逗号
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
    	//添加中文数位提示
    	if (a.indexOf(".")!=-1) b=a.substring(0, a.indexOf("."));
    	else b=a;
    	a+="(";
    	switch (b.length()){
    	case 1:
    		a+="个";
    		break;
    	case 2:
    		a+="十";
    		break;
    	case 3:
    		a+="百";
    		break;
    	case 5:
    		a+="千";
    		break;
    	case 6:
    		a+="万";
    		break;
    	case 7:
    		a+="十万";
    		break;
    	case 9:
    		a+="百万";
    		break;
    	case 10:
    		a+="千万";
    		break;
    	case 11:
    		a+="亿";
    		break;
    	case 13:
    		a+="十亿";
    		break;
    	}
    	a+=")";
    	return a;
    }
    
    //把格式化的金额变为数字
    public static String DeformatPrice(String s){
    	if (s.indexOf("(")!=-1) s=s.substring(0,s.indexOf("("));
		while (s.indexOf(",")!=-1){
    		s=s.substring(0,s.indexOf(","))+s.substring(s.indexOf(",")+1);
    	}
		return s;
    }
}
