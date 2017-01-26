package com.zhtx.app.adapter;

import java.util.List;

import com.zhtx.app.R;
import com.zhtx.myclass.MenuItem;
import com.zhtx.myclass.RequireCard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {

	private List<MenuItem> List;  
	private Context mContext; 
	
	public MenuListAdapter(Context mContext,List<MenuItem> list)  
	{  
	        this.mContext=mContext;  
	        this.List=list;  
	    }  
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return List.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return List.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int Index, View mView, ViewGroup parent) {
		 ViewHolder mHolder=new ViewHolder();  
		 mView=LayoutInflater.from(mContext).inflate(R.layout.menu_item, null);  
		 mHolder.menu_picture=(ImageView)mView.findViewById(R.id.menu_picture);
		 mHolder.menu_content=(TextView)mView.findViewById(R.id.menu_content); 
		 mHolder.menu_num=(TextView)mView.findViewById(R.id.menu_num); 
		 mHolder.menu_picture.setBackgroundResource(List.get(Index).getImage());
		 mHolder.menu_content.setText(List.get(Index).getContent());  
		 if (!List.get(Index).getNum().equals("0")){
			 mHolder.menu_num.setText(List.get(Index).getNum());
		 }else{
			 mHolder.menu_num.setVisibility(View.GONE);
		 }		    		    
		 return mView;     	   
	}
	
	private static class ViewHolder  
	{  
	    ImageView menu_picture;  
	    TextView menu_content;
	    TextView menu_num;
	    }    
	
}
