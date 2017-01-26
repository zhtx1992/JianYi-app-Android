package com.zhtx.app;

import java.util.ArrayList;
import java.util.List;

import com.zhtx.app.adapter.MenuListAdapter;
import com.zhtx.app.require.RequireListActivity;
import com.zhtx.myclass.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SellActivity extends AppCompatActivity {
	
	private Intent i;
	private ListView menulv;
 	private List<MenuItem> menuList;
 	private MenuListAdapter mAdapter;
 	private Toolbar mToolbar;
 	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_list);
        
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("销售");
        
        menulv=(ListView)findViewById(R.id.menulv);
        menuList=new ArrayList<MenuItem>();
        mAdapter=new MenuListAdapter(this,menuList);
        menulv.setAdapter(mAdapter);   
        i=getIntent();
        
    }
	
	protected void onResume(){
		super.onResume();
		setDate();
	}
    private void setDate() {
    	menuList.clear();
    	MenuItem mi;
    	if (NavigationActivity.workList.get(6)){
    		mi=new MenuItem(R.drawable.ic_fahuo,"赊销/预收款销售",String.valueOf(NavigationActivity.task[5]));
    		menuList.add(mi);
		}
    	if (NavigationActivity.workList.get(7)){
    		mi=new MenuItem(R.drawable.ic_fahuo,"现金销售",String.valueOf(NavigationActivity.task[11]));
    		menuList.add(mi);
		}
		
		//管理员专属按钮
		if (NavigationActivity.user.getId()==NavigationActivity.com.getDba_id()){
			if (NavigationActivity.workList.get(8)){
				mi=new MenuItem(R.drawable.ic_xsspgl,"销售商品管理","0");
				menuList.add(mi);
			}			
		}
		mAdapter.notifyDataSetChanged();
		menulv.setOnItemClickListener(new ItemClickEvent());
	}	    	
    	 
    private class ItemClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			MenuItem m=menuList.get(arg2);
			
            if (m.getContent().equals("赊销/预收款销售")){
            	Intent intent=new Intent(SellActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "5");
        		startActivity(intent);
			}
            if (m.getContent().equals("现金销售")){
            	Intent intent=new Intent(SellActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "11");
        		startActivity(intent);
			}
            if (m.getContent().equals("销售商品管理")){
            	Intent intent=new Intent(SellActivity.this,ProductListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("table", "sell");
        		startActivity(intent);
			}
        }  
    }	

}
