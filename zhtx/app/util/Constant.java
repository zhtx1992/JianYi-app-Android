package com.zhtx.app.util;


import java.util.HashMap;

public class Constant {
	//������ip
    public static final String ip= "www.onless.cn";
    		//"120.24.51.184:80";
    //��ǰ�汾��
    public static final double version=1.129;
    //�������
    public static final String[] action={"","","","reimbursement","sell","stock","countsalary","payment"
    		,"collection","transmoney","cashsell","reimburse_credit","transinventory","material_inventory","product_inventory",
    		"transdebt","fixedasset_in","fixedasset_out","accountadjust","returngoods"};
    public static final String[] actionName={"���빫˾����","�������","��������","��������","����/Ԥ������������","��������","�㹤������"
    		,"��������","�տ�����","����ת������","�ֽ���������","�޹���������","���ת������","�����������","�����������","��ծת������",
    		"����̶��ʲ�/������̯����","���ù̶��ʲ�����","������������","�˻�����"};
    //��������
    public static final HashMap<String, String> requireAction=new HashMap<String, String>(){
    	{
    	  put("1","���빫˾����");
    	  put("2","�������");
    	  put("3","��������");
    	}
    };
    public static final String serverPath="http://www.onless.cn/ZhtxServer/";
    public static final String appIdForBugly="d3bdc62fab";
  
}