package com.dus.weasel.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dus.weasel.domain.FileDomain;
import com.dus.weasel.preview.ConvertRunnable;
import com.dus.weasel.utils.FileUtil;
import com.dus.weasel.utils.StringUtils;

@Controller
@RequestMapping("/")
public class IndexController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${sharefileroot}")
	private String fileRoot;
	@Value("${convert_pdfroot}")
	private String convert_pdfroot;
	
	@Value("${preview.reconverttm}")
	private long reconverttm;
	
	@Resource(name="convertThreadPool")
	private ExecutorService convertservice;
	
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
			
			// 同步删除预览文件  
			if (delfile.isFile() && FileUtil.allowpreview(delfile)) {
				String prename = FileUtil.previewFileName(delname);
				String prepath = FileUtil.contactPath(this.convert_pdfroot, delpath, prename);
				logger.debug("同步删除预览文件!");
				File f = new File(prepath);
				
				if (f.exists()) {
					f.delete();
				}
			}
			
			if (delfile.isDirectory()) {
				logger.debug("同步删除预览目录!");
				File predir = new File(FileUtil.contactPath(this.convert_pdfroot, delpath, delname));
				try {
					FileUtil.deleteFile(predir);
				} catch (Exception e) {
					logger.debug("删除操作异常", e);
					isfail = true;
					msg = "删除操作异常," + e.getMessage();
				}
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
	
	/**
	 * 请求预览pdf文件 
	 * th:href="@{/preview(currentPath=${currentPath}, fileName=${d.getName()})}" 
	 */
	@GetMapping("/preview")
	public ModelAndView preview(String currentPath, String fileName) {
		logger.info("预览文件请求:  " + currentPath + " " + fileName);
		
		ModelAndView view = new ModelAndView("preview");
		
		String prefilename = FileUtil.previewFileName(fileName);
		
		String viewpath = FileUtil.contactPath("viewroot" ,currentPath, prefilename);
		String oripath = FileUtil.contactPath(this.fileRoot, currentPath, fileName);
		String prepath = FileUtil.contactPath(this.convert_pdfroot, currentPath, prefilename);
		String prewaitpath = prepath + ".wait";
		
		File orifile = new File(oripath);

		// 转换文件系统路径 到  url路径 
		viewpath = FileUtil.toUrlPath(viewpath);
		
		logger.debug("转换文件的URL路径为:" + viewpath);
		
		// showmsg  : 1-错误  2-存在 3-正在转换 
		// 1. 检查路径中文件是否存在  (应该都存在)  
		if (!orifile.exists()) {
			logger.info("待预览文件不存在: 请检查:" + currentPath + " " + fileName);
			view.addObject("showmsg", 1);
			view.addObject("msg", "文件不存在,请检查:" + currentPath + " " + fileName);
			return view;
		}
		
		// 2. 检查文件是否允许预览 
		if (!FileUtil.allowpreview(orifile)) {
			logger.info("该文件不允许预览: 请检查:" + currentPath + " " + fileName);
			view.addObject("showmsg", 1);
			view.addObject("msg", "该文件不允许预览,请检查:" + currentPath + " " + fileName);
			return view;
		}
		
		// 2. 检查pdf是否已经生成 , 没有生成 则新增任务进行转换  
		File prefile = new File(prepath);
		if (prefile.exists()) {
			logger.debug("预览文件已经生成 , 直接返回 ");
			view.addObject("previewurl", viewpath);
			view.addObject("showmsg", 2);
			view.addObject("msg", "预览文件已经生成,马上刷新!");
			view.addObject("refreshtm", 200);
			return view;
		}
		
		// 3. 若没有生成  ,则检查.wait是否存在  , 存在则检查时间戳  
		File prewaitfile = new File(prewaitpath);
		
		boolean reconvert = false;
		if (prewaitfile.exists()) {
			// 检查时间戳  
			long creattm = prewaitfile.lastModified();
			
			if ((System.currentTimeMillis() - creattm) > this.reconverttm) {
				logger.info("超时未转换完成 , 重新转换 !");
				// 3-1. 重写wait文件 
				// 3-2. 启动新转换任务 
				reconvert = true;
			}
		}
		
		if (!prewaitfile.exists() || reconvert) {
			// 3-2. 启动新转换任务 
			logger.info("开始提交转换任务 ");
			this.convertservice.execute(new ConvertRunnable(orifile, currentPath, fileName, this.convert_pdfroot, this.reconverttm));
		}
		
		// 返回正在转换 , 等待转换完成  ;
		logger.info("正在转换, 请等待 ....");
		view.addObject("previewurl", viewpath);
		view.addObject("showmsg", 3);
		view.addObject("refreshtm", 2000);
		view.addObject("msg", "正在生成预览文件...请稍后(生成完成后将自动刷新跳转)!");
		
		return view;
	}
}
