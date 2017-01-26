package com.zhtx.app.adapter;

import java.util.List;

import com.zhtx.app.R;
import com.zhtx.myclass.Product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProductAdapter extends BaseAdapter {
	
	private List<Product> productList;
	private Context mContext;

	public ProductAdapter(Context mContext,List<Product> productList){
		this.mContext=mContext;
		this.productList=productList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return productList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return productList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int Index, View mView, ViewGroup arg2) {
		ViewHolder mHolder=new ViewHolder();  
	    mView=LayoutInflater.from(mContext).inflate(R.layout.sell_productitem, null);  
	    mHolder.nametv=(TextView)mView.findViewById(R.id.nametv);
	    mHolder.typetv=(TextView)mView.findViewById(R.id.typetv); 
	    mHolder.pricetv=(TextView)mView.findViewById(R.id.pricetv); 
	    mHolder.clienttv=(TextView)mView.findViewById(R.id.clienttv); 
	    mHolder.accounttv=(TextView)mView.findViewById(R.id.accounttv);
	    if (productList.get(Index).getPrice()==0){
	    	mHolder.nametv.setText("");  
	 	    mHolder.typetv.setText(""); 
	 	    mHolder.pricetv.setText("");
	 	    mHolder.clienttv.setText(productList.get(Index).getClient());
	 	    mHolder.accounttv.setText("");
	    }else{
	    	mHolder.nametv.setText(productList.get(Index).getName());  
	 	    mHolder.typetv.setText(productList.get(Index).getType()); 
	 	    mHolder.pricetv.setText(String.valueOf(productList.get(Index).getPrice()));
	 	    mHolder.clienttv.setText(productList.get(Index).getClient()); 
	 	    if (productList.get(Index).isAccount()) mHolder.accounttv.setText("ÊÇ"); 
	 	    else mHolder.accounttv.setText("·ñ");
	    }
	   
		return mView;
	}
	
	private static class ViewHolder  
	{  
	    TextView nametv;  
	    TextView typetv;
	    TextView pricetv;
	    TextView clienttv;
	    TextView accounttv;
	    }    

}
