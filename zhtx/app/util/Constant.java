package com.zhtx.app.util;


import java.util.HashMap;

public class Constant {
	//服务器ip
    public static final String ip= "www.onless.cn";
    		//"120.24.51.184:80";
    //当前版本号
    public static final double version=1.129;
    //动作编号
    public static final String[] action={"","","","reimbursement","sell","stock","countsalary","payment"
    		,"collection","transmoney","cashsell","reimburse_credit","transinventory","material_inventory","product_inventory",
    		"transdebt","fixedasset_in","fixedasset_out","accountadjust","returngoods"};
    public static final String[] actionName={"加入公司申请","请假申请","出差申请","报销流程","赊销/预付款销售流程","进货流程","算工资流程"
    		,"付款流程","收款流程","款项转移流程","现金销售流程","赊购报销流程","库存转移流程","库存领料流程","生产入库流程","负债转移流程",
    		"购入固定资产/报销待摊费用","处置固定资产流程","往来调帐流程","退货流程"};
    //审批动作
    public static final HashMap<String, String> requireAction=new HashMap<String, String>(){
    	{
    	  put("1","加入公司申请");
    	  put("2","请假申请");
    	  put("3","出差审批");
    	}
    };
    public static final String serverPath="http://www.onless.cn/ZhtxServer/";
    public static final String appIdForBugly="d3bdc62fab";
  
}