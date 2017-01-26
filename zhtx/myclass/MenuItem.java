package com.zhtx.myclass;

public class MenuItem {

	private int image;
	private String content, num;
	
	public MenuItem(int image, String content, String num) {
		super();
		this.image = image;
		this.content = content;
		this.num = num;
	}
	public int getImage() {
		return image;
	}
	public void setImage(int image) {
		this.image = image;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}

	
}
