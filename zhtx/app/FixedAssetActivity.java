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

public class FixedAssetActivity extends AppCompatActivity {
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
        getSupportActionBar().setTitle("�̶��ʲ�����");
        
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
    	mi=new MenuItem(R.drawable.ic_gdzcgr,"����̶��ʲ�/������̯����",String.valueOf(NavigationActivity.task[17]));
        menuList.add(mi);
        mi=new MenuItem(R.drawable.ic_gdzccz,"���ù̶��ʲ�",String.valueOf(NavigationActivity.task[18]));
        menuList.add(mi);
		mAdapter.notifyDataSetChanged();
		menulv.setOnItemClickListener(new ItemClickEvent());
	}	    	
    	 
    private class ItemClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			MenuItem m=menuList.get(arg2);
            if (m.getContent().equals("����̶��ʲ�/������̯����")){
            	Intent intent=new Intent(FixedAssetActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "17");
        		startActivity(intent);
			}
            if (m.getContent().equals("���ù̶��ʲ�")){
            	Intent intent=new Intent(FixedAssetActivity.this,RequireListActivity.class);
        		intent.putExtra("db",i.getStringExtra("db"));
        		intent.putExtra("userid",i.getStringExtra("userid"));
        		intent.putExtra("act", "18");
        		startActivity(intent);
			}
        }  
    }	
}
