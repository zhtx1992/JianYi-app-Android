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

public class InventoryActivity extends AppCompatActivity {
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
        getSupportActionBar().setTitle("库存管理");
        
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
    	mi=new MenuItem(R.drawable.ic_kczy,"库存转移",String.valueOf(NavigationActivity.task[13]));
    	menuList.add(mi);
    	if (NavigationActivity.workList.get(13)){
    		mi=new MenuItem(R.drawable.ic_scll,"库存领料",String.valueOf(NavigationActivity.task[14]));
        	menuList.add(mi);
		}
    	if (NavigationActivity.workList.get(14)){
    		mi=new MenuItem(R.drawable.ic_scrk,"生产入库",String.valueOf(NavigationActivity.task[15]));
        	menuList.add(mi);
		}
    	if (NavigationActivity.workList.get(18)){
    		mi=new MenuItem(R.drawable.ic_tuihuo,"退货",String.valueOf(NavigationActivity.task[20]));
        	menuList.add(mi);
		}
		mAdapter.notifyDataSetChanged();
		menulv.setOnItemClickListener(new ItemClickEvent());
	}	    	
    	 
    private class ItemClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			MenuItem m=menuList.get(arg2);
			
            if (m.getContent().equals("库存转移")){
            	Intent intent=new Intent(InventoryActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "13");
        		startActivity(intent);
			}
            if (m.getContent().equals("库存领料")){
            	Intent intent=new Intent(InventoryActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "14");
        		startActivity(intent);
			}
            if (m.getContent().equals("生产入库")){
            	Intent intent=new Intent(InventoryActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "15");
        		startActivity(intent);
			}
            if (m.getContent().equals("退货")){
            	Intent intent=new Intent(InventoryActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "20");
        		startActivity(intent);
			}
        }  
    }	

}
