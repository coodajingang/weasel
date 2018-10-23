package com.dus.weasel.domain;

public class FilePathInfo {

	// 显示的路径名称
	private String pname;
	
	// 相对跟路径名称 
	private String rpath;
	
	// 绝对路径名称  
	private String abpath;

	
	
	
	@Override
	public String toString() {
		return "FilePathInfo [pname=" + pname + ", rpath=" + rpath + ", abpath=" + abpath + "]";
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public String getRpath() {
		return rpath;
	}

	public void setRpath(String rpath) {
		this.rpath = rpath;
	}

	public String getAbpath() {
		return abpath;
	}

	public void setAbpath(String abpath) {
		this.abpath = abpath;
	}
	
	
	
}
