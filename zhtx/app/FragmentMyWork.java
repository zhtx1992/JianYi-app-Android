package com.zhtx.app;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhtx.app.adapter.MenuListAdapter;
import com.zhtx.app.require.RequireListActivity;
import com.zhtx.app.util.Constant;
import com.zhtx.app.util.VolleySRUtil;
import com.zhtx.myclass.MenuItem;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentMyWork extends Fragment {
	
	private ListView menulv;
	private List<MenuItem> menuList;
	private MenuListAdapter mAdapter;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.menu_list_notoolbar, container, false);
        menulv=(ListView)layout.findViewById(R.id.menulv);
        menuList=new ArrayList<MenuItem>();
        
        mAdapter=new MenuListAdapter(getActivity(),menuList);
        menulv.setAdapter(mAdapter);   
       
        return layout;
    }

    @Override
    public void onResume(){
    	super.onResume();
    	setDate();
    }
    
	private void setDate() {
			menuList.clear();
			MenuItem mi;
			if (NavigationActivity.workList.get(1)){
				mi=new MenuItem(R.drawable.ic_baoxiao,"����",String.valueOf(NavigationActivity.task[4]+NavigationActivity.task[12]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(2)){
				mi=new MenuItem(R.drawable.ic_fahuo,"����",String.valueOf(NavigationActivity.task[5]+NavigationActivity.task[11]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(3)){
				mi=new MenuItem(R.drawable.ic_jinhuo,"����",String.valueOf(NavigationActivity.task[6]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(12)){
				mi=new MenuItem(R.drawable.ic_kucun,"������",String.valueOf(NavigationActivity.task[13]
						+NavigationActivity.task[14]+NavigationActivity.task[15]+NavigationActivity.task[20]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(16)){
				mi=new MenuItem(R.drawable.ic_gdzcgl,"�̶��ʲ�����",String.valueOf(NavigationActivity.task[17]+NavigationActivity.task[18]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(4)){
				mi=new MenuItem(R.drawable.ic_salary,"�㹤��",String.valueOf(NavigationActivity.task[7]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(5)){
				mi=new MenuItem(R.drawable.ic_transmoney,"�ո���",String.valueOf(NavigationActivity.task[8]+NavigationActivity.task[9]+NavigationActivity.task[10]));
				menuList.add(mi);
			}
			if (NavigationActivity.workList.get(15)||NavigationActivity.workList.get(17)){
				mi=new MenuItem(R.drawable.ic_qita,"����",String.valueOf(NavigationActivity.task[16]+NavigationActivity.task[19]));
				menuList.add(mi);
			}
			if ((NavigationActivity.workList.get(10)&&(NavigationActivity.user.getId()==NavigationActivity.com.getSigndba_id()))
					||(NavigationActivity.workList.get(0))){
				mi=new MenuItem(R.drawable.ic_kaoqin,"����","0");
				menuList.add(mi);
			}
			//����Աר����ť
			if (NavigationActivity.user.getId()==NavigationActivity.com.getDba_id()){
			    mi=new MenuItem(R.drawable.ic_ssgl,"���¹���",String.valueOf(NavigationActivity.task[1]));
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
				
                if (m.getContent().equals("����")){
                	Intent i=new Intent(getActivity(),ReimburseActivity.class);
            		i.putExtra("db", NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            		startActivity(i);
				}
                if (m.getContent().equals("����")){
                	Intent i=new Intent(getActivity(),SellActivity.class);
            		i.putExtra("db", NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            		startActivity(i);
				}
                if (m.getContent().equals("����")){
                	Intent i=new Intent(getActivity(),StockActivity.class);
            		i.putExtra("db", NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            		startActivity(i);
				}
                if (m.getContent().equals("�㹤��")){
                	Intent i=new Intent(getActivity(),RequireListActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            		i.putExtra("act", "7");
            	    startActivity(i);
                }
                if (m.getContent().equals("�ո���")){
                	Intent i=new Intent(getActivity(),MoneyActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            	    startActivity(i);
                }
                if (m.getContent().equals("����")){
                	Intent i=new Intent(getActivity(),HandSignActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("name", String.valueOf(NavigationActivity.staff.getName()));
            	    startActivity(i);
                }
                if (m.getContent().equals("������")){
                	Intent i=new Intent(getActivity(),InventoryActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            	    startActivity(i);
                }
                if (m.getContent().equals("���¹���")){
                	Intent i=new Intent(getActivity(),StaffManageActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            	    startActivity(i);
                }
                if (m.getContent().equals("����")){
                	Intent i=new Intent(getActivity(),OtherActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            	    startActivity(i);
                }
                if (m.getContent().equals("�̶��ʲ�����")){
                	Intent i=new Intent(getActivity(),FixedAssetActivity.class);
            		i.putExtra("db",  NavigationActivity.com.getDb_name());
            		i.putExtra("userid", String.valueOf(NavigationActivity.user.getId()));
            	    startActivity(i);
                }
	        }  
	 }
}
