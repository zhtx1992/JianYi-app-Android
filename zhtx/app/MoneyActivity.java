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

public class MoneyActivity extends AppCompatActivity {
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
        getSupportActionBar().setTitle("�ո���");
        
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
		MenuItem mi=new MenuItem(R.drawable.ic_payment,"����",String.valueOf(NavigationActivity.task[8]));
		menuList.add(mi);	
		mi=new MenuItem(R.drawable.ic_collection,"�տ�",String.valueOf(NavigationActivity.task[9]));
		menuList.add(mi);
		mi=new MenuItem(R.drawable.ic_transmoney,"����ת��",String.valueOf(NavigationActivity.task[10]));
		menuList.add(mi);
		if (NavigationActivity.user.getId()==NavigationActivity.com.getDba_id()) {
			if (NavigationActivity.workList.get(9)){
				mi=new MenuItem(R.drawable.ic_baoxiao,"����������","0");
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
			
            if (m.getContent().equals("����")){
            	Intent intent=new Intent(MoneyActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "8");
        		startActivity(intent);
			}
            if (m.getContent().equals("�տ�")){
            	Intent intent=new Intent(MoneyActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "9");
        		startActivity(intent);
			}
            if (m.getContent().equals("����ת��")){
            	Intent intent=new Intent(MoneyActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "10");
        		startActivity(intent);
			}
            if (m.getContent().equals("����������")){
            	Intent intent=new Intent(MoneyActivity.this,MoneyProviderManage.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		startActivity(intent);
			}
        }  
    }	

}
