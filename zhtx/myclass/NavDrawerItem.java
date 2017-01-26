package com.zhtx.myclass;

public class NavDrawerItem {
	    private boolean showNotify;
	    private String title;
	    private int image;
	 
	    public int getImage() {
			return image;
		}

		public void setImage(int image) {
			this.image = image;
		}

		public NavDrawerItem() {
	 
	    }
	 
	    public NavDrawerItem(boolean showNotify, String title) {
	        this.showNotify = showNotify;
	        this.title = title;
	    }
	 
	    public boolean isShowNotify() {
	        return showNotify;
	    }
	 
	    public void setShowNotify(boolean showNotify) {
	        this.showNotify = showNotify;
	    }
	 
	    public String getTitle() {
	        return title;
	    }
	 
	    public void setTitle(String title) {
	        this.title = title;
	    }
	
}
