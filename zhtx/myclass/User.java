package com.zhtx.myclass;

//封装了app用户信息的类
public class User {
	private int id;
	private String username;
	private String password;
    private String company_id;
    private String company_name;
    private String db;
    private String deviceId;
    
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCompany_id() {
		return company_id;
	}
	public void setCompany_id(String compant_id) {
		this.company_id = compant_id;
	}
    
}
