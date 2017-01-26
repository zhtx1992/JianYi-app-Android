package com.zhtx.app.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


public class VolleySRUtil {

	private StringRequest myRequest;
	private HashMap<String, String> map2 = new HashMap<String, String>();
	public VolleySRUtil(String url,final HashMap<String, String> map,Response.Listener listener,Response.ErrorListener elistener){
		
		for (Entry<String, String> entry : map.entrySet()) {
			String key=entry.getKey();
			String value=entry.getValue();
			value.replace("\n","");
			map2.put(key, value);
		}
		myRequest = new StringRequest(Method.POST, url, listener,elistener) {  
			@Override  
		    protected Map<String, String> getParams() throws AuthFailureError {    
		        return map2;  
			}
		};
		myRequest.setRetryPolicy(new DefaultRetryPolicy(7000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));  		
	}
	public StringRequest getRequest(){
		return myRequest;
	}
}
