package com.zhtx.myclass;

import java.sql.Time;
import java.sql.Timestamp;

public class Company {
    private String id;
    private String name;
    private String db_name;
    private int dba_id;
    private Timestamp create_time;
    private Time sign_time;
    private int signdba_id;
    
	public Time getSign_time() {
		return sign_time;
	}
	public void setSign_time(Time sign_time) {
		this.sign_time = sign_time;
	}
	public int getSigndba_id() {
		return signdba_id;
	}
	public void setSigndba_id(int signdba_id) {
		this.signdba_id = signdba_id;
	}
	public Timestamp getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDb_name() {
		return db_name;
	}
	public void setDb_name(String db_name) {
		this.db_name = db_name;
	}
	public int getDba_id() {
		return dba_id;
	}
	public void setDba_id(int dba_id) {
		this.dba_id = dba_id;
	}
    
}
