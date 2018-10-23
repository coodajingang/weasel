package com.dus.weasel.domain;

import java.io.File;
import java.util.List;

import com.dus.weasel.utils.FileUtil;
import com.dus.weasel.utils.StringUtils;

public class FileDomain {

	// 当前路径下的文件列表, 分为目录列表, 链接列表, 文件列表
	private List<FileInfo> dirList;
	private List<FileInfo> linkList;
	private List<FileInfo> fileList; 
	
	// 当前路径及当前路径对应的对象
	private String curPath;
	private String curRelPath; // 当前路径对应的相对路径 
	private File curFile;
	
	// 当相对路径的父目录 
	private String curRelParentPath;
	
	// 根路径 
	private String root;
	private File rootFile;
	
	// 自跟路径下, 至 当前路径下的路径面包屑
	private List<FilePathInfo> pathList;
	
	/**
	 * 
	 * @param path   相对路径 
	 * @param root   系统配置的绝对跟路径 
	 */
	public FileDomain(String path, File root) {
		this.curRelPath = path;
		this.rootFile =  root;
		this.root = this.rootFile.getAbsolutePath();
		
		this.curPath = FileUtil.contactPath(this.root, this.curRelPath);

		this.curFile = new File(this.curPath);
		init();
		
		// 计算当前相对路径的 父相对路径 
		if (this.pathList == null || this.pathList.size() < 2) {
			this.curRelParentPath = "";
		} else {
			this.curRelParentPath = this.pathList.get(this.pathList.size() - 2).getRpath();
		}
	}

	/**
	 * 目录是否存在
	 * @return
	 */
	public boolean isCurExist() {
		return this.curFile.exists();
	}
	
	

	private void init() {
		
		this.linkList = FileUtil.getLinkList(this.curFile);
		this.fileList = FileUtil.getFileList(this.curFile);
		this.dirList = FileUtil.getDirList(this.curFile);
		
		this.pathList = FileUtil.getPathList(this.rootFile, this.curFile); 
	}

	public List<FileInfo> getDirList() {
		return dirList;
	}

	public void setDirList(List<FileInfo> dirList) {
		this.dirList = dirList;
	}

	public List<FileInfo> getLinkList() {
		return linkList;
	}

	public void setLinkList(List<FileInfo> linkList) {
		this.linkList = linkList;
	}

	public List<FileInfo> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileInfo> fileList) {
		this.fileList = fileList;
	}

	public String getCurPath() {
		return curPath;
	}

	public void setCurPath(String curPath) {
		this.curPath = curPath;
	}

	public String getCurRelPath() {
		return curRelPath;
	}

	public void setCurRelPath(String curRelPath) {
		this.curRelPath = curRelPath;
	}

	public File getCurFile() {
		return curFile;
	}

	public void setCurFile(File curFile) {
		this.curFile = curFile;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public File getRootFile() {
		return rootFile;
	}

	public void setRootFile(File rootFile) {
		this.rootFile = rootFile;
	}

	public List<FilePathInfo> getPathList() {
		return pathList;
	}

	public void setPathList(List<FilePathInfo> pathList) {
		this.pathList = pathList;
	}

	@Override
	public String toString() {
		return "FileDomain [dirList=" + dirList + ", linkList=" + linkList + ", fileList=" + fileList + ", curPath="
				+ curPath + ", curRelPath=" + curRelPath + ", curFile=" + curFile + ", curRelParentPath="
				+ curRelParentPath + ", root=" + root + ", rootFile=" + rootFile + ", pathList=" + pathList + "]";
	}

	public String getCurRelParentPath() {
		return curRelParentPath;
	}

	public void setCurRelParentPath(String curRelParentPath) {
		this.curRelParentPath = curRelParentPath;
	}

	

	
}
