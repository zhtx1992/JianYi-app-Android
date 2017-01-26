package com.zhtx.myclass;
 
public class RequireCard   
{  
  
   private String act,sender,time;
   private int status;
     
public int getStatus() {
	return status;
}

public void setStatus(int status) {
	this.status = status;
}

public RequireCard(String act,String sender,String time,int status)  
   {  
       this.act=act;  
       this.sender=sender;  
       this.time=time;
       this.status=status;
   }

public String getAct() {
	return act;
}

public void setAct(String act) {
	this.act = act;
}

public String getSender() {
	return sender;
}

public void setSender(String sender) {
	this.sender = sender;
}

public String getTime() {
	return time;
}

public void setTime(String time) {
	this.time = time;
}  
     
   
  
}  