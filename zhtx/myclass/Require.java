package com.zhtx.myclass;

import java.sql.Timestamp;

public class Require {
	private int id,sender,reciver,previd,action,status;
	private String data,proid;
	private Timestamp time;
	public int getId() {
		return id;
	}
	public String getProid() {
		return proid;
	}
	public void setProid(String proid) {
		this.proid = proid;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSender() {
		return sender;
	}
	public void setSender(int sender) {
		this.sender = sender;
	}
	public int getReciver() {
		return reciver;
	}
	public void setReciver(int reciver) {
		this.reciver = reciver;
	}
	public int getPrevid() {
		return previd;
	}
	public void setPrevid(int previd) {
		this.previd = previd;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}
	

}
