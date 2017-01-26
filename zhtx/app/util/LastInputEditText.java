package com.zhtx.app.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class LastInputEditText extends EditText {
	public LastInputEditText(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    public LastInputEditText(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public LastInputEditText(Context context) {  
        super(context);  
    }  
      
    @Override  
    protected void onSelectionChanged(int selStart, int selEnd) {  
        super.onSelectionChanged(selStart, selEnd);  
        //保证光标始终在最后面  
        if(selStart==selEnd){
        	String s=getText().toString();
            if (s.length()>1) setSelection(s.indexOf("("));  
            else setSelection(getText().length());
        }  
          
    }  
}
