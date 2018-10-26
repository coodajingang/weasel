package com.dus.weasel.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dus.weasel.utils.FileUtil;
import com.dus.weasel.utils.StringUtils;

/**
 * 从文件中抽取的关键信息要素
 * @author ThinkPad
 *
 */
public class FileInfo {

	private static Logger logger = LoggerFactory.getLogger(FileInfo.class);
	
	// 绝对路径
	private String abpath;
	
	// 相对路径 
	private String relpath;
	
	// 文件名称  
	private String name;
	
	// 大小size 
	private String size; 
	
	// 创建时间  yyyymmddhh:MM:24
	private String createtime;
	
	// 上传者  
	private String author;
	
	// 链接地址 
	private String linkaddr;
	
	// 文件类型  1-目录  2-文件  3-链接 
	private String type;
	
	// 当前文件描述符
	private File file;

	private static final String dateFormatString = "yyyyMMddHHmm";
	
	// 文件图标的 css 类 
	private String iconClass;
	
	// 是否允许预览 
	private boolean allowpreview;
	
	// 预览文件名  
	private String viewName;
	
	/**
	 * 创建FileInfo , 当为link时, 需要传入type
	 * @param f
	 * @param type
	 * @return
	 */
	public static FileInfo createFromFiles(File f, String type) {
		FileInfo finfo = new FileInfo();
		
		finfo.setAbpath(f.getAbsolutePath());
		finfo.setName(f.getName());
		finfo.setAuthor("admin");
		
		// 大小 
		finfo.setSize((f.length()/1000.0) + "K");
		
		// 时间  
		long tm = f.lastModified();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(tm);
		finfo.setCreatetime(sdf.format(cal.getTime()));
		
		// 当未输入type时, 从file的名称中辨别 ;
		if (StringUtils.isBlank(type)) {
			if (f.getName().endsWith(FileUtil.LINK_SUFFIX)) {
				type = "3";
			}
		}
		
		// 若为link , 读取文件取链接地址
		if (StringUtils.equals(type, "3")) {
			finfo.setName(f.getName().substring(0, f.getName().length() - FileUtil.LINK_SUFFIX.length()));
			// 读取文件内容  
			
			BufferedReader reader = null;
			String line = null;
			
			try {
				reader = new BufferedReader(new FileReader(f));
				while( (line = reader.readLine()) != null) {
					finfo.setLinkaddr(line);
					break;
				}
				reader.close();
			} catch (IOException ioe) {
				logger.debug("读取link文件异常!" , ioe);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ioe) {
						logger.debug("读取link文件异常!" , ioe);
					}
				}
			}
		}
		
		// 若是普通文件 , 则获取其图标 iconClass 
		if (f.isFile() && !StringUtils.equals(type, "3")) {
			finfo.setIconClass(FileUtil.getIconClass(f));
			
			// 判断普通文件是否允许预览  
			finfo.setAllowpreview(FileUtil.allowpreview(f));
			
			// 判断转换后的文件名称 
			finfo.setViewName(FileUtil.previewFileName(finfo.getName()));

		}
		return finfo;
	}
	
	@Override
	public String toString() {
		return "FileInfo [abpath=" + abpath + ", relpath=" + relpath + ", name=" + name + ", size=" + size
				+ ", createtime=" + createtime + ", author=" + author + ", linkaddr=" + linkaddr + ", type=" + type
				+ ", file=" + file + ", iconClass=" + iconClass + ", allowpreview=" + allowpreview + ", viewName="
				+ viewName + "]";
	}

	public String getAbpath() {
		return abpath;
	}

	public void setAbpath(String abpath) {
		this.abpath = abpath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLinkaddr() {
		return linkaddr;
	}

	public void setLinkaddr(String linkaddr) {
		this.linkaddr = linkaddr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getRelpath() {
		return relpath;
	}

	public void setRelpath(String relpath) {
		this.relpath = relpath;
	}

	public String getIconClass() {
		return iconClass;
	}

	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}

	public boolean isAllowpreview() {
		return allowpreview;
	}

	public void setAllowpreview(boolean allowpreview) {
		this.allowpreview = allowpreview;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	
	
}
