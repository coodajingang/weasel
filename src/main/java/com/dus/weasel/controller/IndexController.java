package com.dus.weasel.controller;

import java.io.File;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dus.weasel.domain.FileDomain;
import com.dus.weasel.utils.FileUtil;
import com.dus.weasel.utils.StringUtils;

@Controller
@RequestMapping("/")
public class IndexController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${sharefileroot}")
	private String fileRoot;
	
	/**
	 * curpath :  显示当前路径文件列表 , 空时显示为根路径 ;
	 * subPath :  子目录    
	 * @return
	 */
	@RequestMapping(path= {"/", "/index", "/index.html"})
	public ModelAndView getIndex(String currentPath, String subPath) {

		// 当subPath 不空时, 显示当前目录的子目录  ;  
		// 当subPath 为空时, 显示当前目录 ; 
		
		ModelAndView view = new ModelAndView("index");
		
		logger.debug("接收请请index, 显示目录列表..根目录为:" + this.fileRoot);
		logger.debug("请求参数:" + currentPath + " :" + subPath);
		
		String contactpath = FileUtil.contactPath(currentPath, subPath);
		// 获取共享文档根目录 ; 
		// 根据跟目录读取文档 分为三个类型 ;
		
		File root = new File(this.fileRoot);
		
		
		FileDomain file = new FileDomain(contactpath, root);
		
		logger.debug(file.toString());
		
		view.addObject("dirlist", file.getDirList());
		view.addObject("filelist", file.getFileList());
		view.addObject("linklist", file.getLinkList());
		
		view.addObject("pathlist", file.getPathList());
		
		view.addObject("currentPath", file.getCurRelPath());
		view.addObject("parentPath", file.getCurRelParentPath());
		return view;
	}
	
	
	/**
	 * delpath :  待删除的路径 ,是相对路径  ;
	 * delname :  待删除的 文件\链接\目录名称  
	 * @return
	 */
	@RequestMapping(path= {"/del"})
	public ModelAndView delFiles(String delname, String delpath) {
		logger.debug("接收请求删除目录:" + delpath + " " + delname);
		ModelAndView view = new ModelAndView("index");
		
		String msg = "";
		boolean isfail = false;
		
		File root = new File(this.fileRoot);
		
		File delfile = new File(FileUtil.contactPath(FileUtil.contactPath(this.fileRoot, delpath), delname));
		
		logger.debug(delfile.getAbsolutePath());

		if (delfile.exists()) {
			try {
				FileUtil.deleteFile(delfile);
			} catch (Exception e) {
				logger.debug("删除操作异常", e);
				isfail = true;
				msg = "删除操作异常," + e.getMessage();
			}

		} else {
			isfail = true;
			msg = "待删除文件不存在!";
		}

		FileDomain file = new FileDomain(delpath, root);
		logger.debug(file.toString());
		view.addObject("dirlist", file.getDirList());
		view.addObject("filelist", file.getFileList());
		view.addObject("linklist", file.getLinkList());
		view.addObject("pathlist", file.getPathList());	
		view.addObject("currentPath", file.getCurRelPath());
		view.addObject("parentPath", file.getCurRelParentPath());
		
		if (isfail) {
			view.addObject("show_fail", true);
			view.addObject("show_msg", msg);
		} else {
			view.addObject("show_success", true);
			view.addObject("show_msg", "删除操作成功!");
		}
		
		return view;
	}
	
	/**
	 * 新增链接文件 
	 * /createlink?linkname=&linkcurpath=%5Chome%5Cweblogic%5Clog&linkaddr=
	 * @return
	 */
	@RequestMapping(path= {"/createlink"})
	public ModelAndView createlink(String linkname, String linkcurpath, String linkaddr) {
		logger.debug("接收请求创建链接:" + linkname + " " + linkcurpath + " " + linkaddr);
		ModelAndView view = new ModelAndView("index");
		String msg = "";
		boolean isfail = false;
		
		if (StringUtils.isBlank(linkname) || StringUtils.isBlank(linkaddr)) {
			isfail = true;
			msg = "输入的链接名称/链接地址为空";
		}
		
		if (!isfail) {
			// 开始创建文件  
			String path = FileUtil.contactPath(this.fileRoot, linkcurpath);
			String filepath = FileUtil.contactPath(path, linkname);
			FileWriter fw = null;
			
			if (!filepath.endsWith(FileUtil.LINK_SUFFIX)) {
				filepath += FileUtil.LINK_SUFFIX;
			}
			
			logger.info("创建连接文件:" + filepath);
			
			try {
				File f = new File(filepath);
				fw = new FileWriter(f);
				fw.write(linkaddr);
				fw.close();
			} catch (Exception e) {
				logger.error("写链接文件出现异常:", e);
				isfail = true;
				msg = "写链接文件出现异常" + e.getMessage();
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (Exception e) {
						
					}
				}
			}
		}
		
		File root = new File(this.fileRoot);
		
		// 当前路径列表显示  
		FileDomain file = new FileDomain(linkcurpath, root);
		logger.debug(file.toString());
		view.addObject("dirlist", file.getDirList());
		view.addObject("filelist", file.getFileList());
		view.addObject("linklist", file.getLinkList());
		view.addObject("pathlist", file.getPathList());	
		view.addObject("currentPath", file.getCurRelPath());
		view.addObject("parentPath", file.getCurRelParentPath());
		
		if (isfail) {
			view.addObject("show_fail", true);
			view.addObject("show_msg", msg);
		} else {
			view.addObject("show_success", true);
			view.addObject("show_msg", "创建链接成功!");
		}
		return view;
	}
	
	/**
	 * 新增目录 
	 * 参数: 创建的目录  , 当前路径 (相对) 
	 * @return
	 */
	@RequestMapping(path= {"/createdir"})
	public ModelAndView createdir(String crepaths, String crecurpath) {
		logger.debug("接收请求创建目录:" + crepaths + " " + crecurpath);
		ModelAndView view = new ModelAndView("index");
		String msg = "";
		boolean isfail = false;
		
		if (StringUtils.isBlank(crepaths) || StringUtils.equals(crepaths, File.separator)) {
			isfail = true;
			msg = "输入的创建目录项为空";
		}
		
		if (!isfail) {
			// 处理目录字符串 			
			// 开始创建
			String path = FileUtil.contactPath(this.fileRoot, crecurpath);
			String filepath = FileUtil.contactPath(path, crepaths);
			
			logger.info("创建de目录:" + filepath);
			
			try {
				File f = new File(filepath);
				
				if (f.exists() && f.isDirectory()) {
					logger.debug("待创建的目录已经存在!");
					isfail = true;
					msg = "待创建的目录已经存在!";
				} else {
					f.mkdirs();
				}
			} catch (Exception e) {
				logger.error("创建目录出现异常:", e);
				isfail = true;
				msg = "创建目录出现异常" + e.getMessage();
			}
		}
		
		File root = new File(this.fileRoot);
		
		// 当前路径列表显示  
		FileDomain file = new FileDomain(crecurpath, root);
		logger.debug(file.toString());
		view.addObject("dirlist", file.getDirList());
		view.addObject("filelist", file.getFileList());
		view.addObject("linklist", file.getLinkList());
		view.addObject("pathlist", file.getPathList());	
		view.addObject("currentPath", file.getCurRelPath());
		view.addObject("parentPath", file.getCurRelParentPath());
		
		if (isfail) {
			view.addObject("show_fail", true);
			view.addObject("show_msg", msg);
		} else {
			view.addObject("show_success", true);
			view.addObject("show_msg", "创建目录成功!");
		}
		return view;
	}
}
