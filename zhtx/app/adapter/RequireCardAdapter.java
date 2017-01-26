package com.zhtx.app.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;  

import com.zhtx.app.R;
import com.zhtx.myclass.RequireCard;

import android.content.Context;  
import android.content.res.ColorStateList;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.TextView; 

public class RequireCardAdapter extends BaseAdapter {
	private List<RequireCard> mCards;  
	private Context mContext;  
	      
	public RequireCardAdapter(Context mContext,List<RequireCard> mCards)  
	{  
	        this.mContext=mContext;  
	        this.mCards=mCards;  
	    }  
	@Override  
	public int getCount()   
	{  
	        return mCards.size();  
	    }  
	  
	@Override  
	public Object getItem(int Index)   
	{  
	    return mCards.get(Index);  
	    }  
	  
	@Override  
	public long getItemId(int Index)   
	{  
	    return Index;  
	    }  
	@Override  
	public View getView(int Index, View mView, ViewGroup mParent)   
	{  
	    ViewHolder mHolder=new ViewHolder();  
	    mView=LayoutInflater.from(mContext).inflate(R.layout.require_item, null);  
	    mHolder.Card_act=(TextView)mView.findViewById(R.id.Card_act);
	    mHolder.Card_sender=(TextView)mView.findViewById(R.id.Card_sender); 
	    mHolder.Card_time=(TextView)mView.findViewById(R.id.Card_time); 
	    mHolder.Card_status=(TextView)mView.findViewById(R.id.Card_status);
	    mHolder.Card_act.setText(mCards.get(Index).getAct());  
	    mHolder.Card_sender.setText(mCards.get(Index).getSender()); 
	    mHolder.Card_time.setText(mCards.get(Index).getTime());
	    if  (mCards.get(Index).getStatus()==0) {
	    	mHolder.Card_status.setText("待审核");
	    }
	    if (mCards.get(Index).getStatus()==1){
	    	mHolder.Card_status.setText("已通过");
	    	ColorStateList green=ColorStateList.valueOf(0xFF00FF00);
	    	mHolder.Card_status.setTextColor(green);
	    }
	    if (mCards.get(Index).getStatus()==2) {
	    	mHolder.Card_status.setText("已驳回");
	    	ColorStateList red=ColorStateList.valueOf(0xFFFF0000);
	    	mHolder.Card_status.setTextColor(red);
	    }
	    if (mCards.get(Index).getStatus()==3) {
	    	mHolder.Card_status.setText("进行中");
	    	ColorStateList green=ColorStateList.valueOf(0xFF00FF00);
	    	mHolder.Card_status.setTextColor(green);
	    }
	    if (mCards.get(Index).getStatus()==4) {
	    	mHolder.Card_status.setText("已完成");
	    }
	    return mView;  
	    }  
	  
	private static class ViewHolder  
	{  
	    TextView Card_act;  
	    TextView Card_sender;
	    TextView Card_time;
	    TextView Card_status;
	    }    

}
