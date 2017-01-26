package com.zhtx.myclass;

public class Product {
    private String name,type,client;
    private double price;
    private boolean account;
    private int id;
	public boolean isAccount() {
		return account;
	}
	public void setAccount(boolean account) {
		this.account = account;
	}
	public int getId() {
		return id;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
}
