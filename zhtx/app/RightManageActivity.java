package com.zhtx.app;

import java.util.ArrayList;
import java.util.List;

import com.zhtx.app.adapter.MenuListAdapter;
import com.zhtx.myclass.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RightManageActivity extends AppCompatActivity {
	
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
        getSupportActionBar().setTitle("Ȩ�޹���");
        
        menulv=(ListView)findViewById(R.id.menulv);
        menuList=new ArrayList<MenuItem>();
        mAdapter=new MenuListAdapter(this,menuList);
        menulv.setAdapter(mAdapter);   
        i=getIntent();
        setDate();
    }
    private void setDate() {
    	menuList.clear();
    	MenuItem mi;
    	if (NavigationActivity.workList.get(1)){
    		mi=new MenuItem(R.drawable.ic_baoxiao,"��ͨ��������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(11)){
    		mi=new MenuItem(R.drawable.ic_baoxiao2,"�޹���������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(2)){
    		if (NavigationActivity.workList.get(6)){
    		    mi=new MenuItem(R.drawable.ic_fahuo,"����/Ԥ�տ���������","0");
    		    menuList.add(mi);
    		}
    		if (NavigationActivity.workList.get(7)){
    		    mi=new MenuItem(R.drawable.ic_fahuo,"�ֽ���������","0");
    		    menuList.add(mi);
    		}
    	}
    	if (NavigationActivity.workList.get(3)){
    		mi=new MenuItem(R.drawable.ic_jinhuo,"�ɹ��������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(4)){
    		mi=new MenuItem(R.drawable.ic_salary,"�㹤������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(5)){
    		mi=new MenuItem(R.drawable.ic_payment,"��������","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_collection,"�տ�����","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_transmoney,"����ת������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(12)){
    		mi=new MenuItem(R.drawable.ic_kczy,"���ת������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(13)){
    		mi=new MenuItem(R.drawable.ic_scll,"�����������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(14)){
    		mi=new MenuItem(R.drawable.ic_scrk,"�����������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(18)){
    		mi=new MenuItem(R.drawable.ic_tuihuo,"�˻�����","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(15)){
    		mi=new MenuItem(R.drawable.ic_fzzy,"��ծת������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(17)){
    		mi=new MenuItem(R.drawable.ic_wldz,"������������","0");
    		menuList.add(mi);
    	}
    	if (NavigationActivity.workList.get(16)){
    		mi=new MenuItem(R.drawable.ic_gdzcgr,"����̶��ʲ�/����̯λ����","0");
    		menuList.add(mi);
    		mi=new MenuItem(R.drawable.ic_gdzccz,"���ù̶��ʲ�����","0");
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
			
            if (m.getContent().equals("��ͨ��������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_reimbursement");
        		startActivity(intent);
			}
            if (m.getContent().equals("�޹���������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_reimburse_credit");
        		startActivity(intent);
			}
            if (m.getContent().equals("����/Ԥ�տ���������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_sell");
        		startActivity(intent);
			}
            if (m.getContent().equals("�ֽ���������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_cashsell");
        		startActivity(intent);
			}
            if (m.getContent().equals("�ɹ��������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_stock");
        		startActivity(intent);
			}
            if (m.getContent().equals("�㹤������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_countsalary");
        		startActivity(intent);    		
			}
            if (m.getContent().equals("��������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_payment");
        		startActivity(intent);
            }
            if (m.getContent().equals("�տ�����")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_collection");
        		startActivity(intent);
            }
            if (m.getContent().equals("����ת������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_transmoney");
        		startActivity(intent);
            }
            if (m.getContent().equals("���ת������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_transinventory");
        		startActivity(intent);
            }
            if (m.getContent().equals("�����������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_material_inventory");
        		startActivity(intent);
            }
            if (m.getContent().equals("�����������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_product_inventory");
        		startActivity(intent);
            }
            if (m.getContent().equals("�˻�����")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_returngoods");
        		startActivity(intent);
            }
            if (m.getContent().equals("��ծת������")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "right_transdebt");
        		startActivity(intent);
            }
            if (m.getContent().equals("����̶��ʲ�/����̯λ����")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "fixedasset_in");
        		startActivity(intent);
            }
            if (m.getContent().equals("���ù̶��ʲ�")){
            	Intent intent=new Intent(RightManageActivity.this,RightManageListActivity.class);
            	intent.putExtra("db", i.getStringExtra("db"));
            	intent.putExtra("table", "fixedasset_out");
        		startActivity(intent);
            }
        }  
    }	

}
