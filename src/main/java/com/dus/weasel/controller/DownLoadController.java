package com.dus.weasel.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dus.weasel.domain.FileUpResult;
import com.dus.weasel.preview.ConvertRunnable;
import com.dus.weasel.utils.FileUtil;
import com.dus.weasel.utils.StringUtils;

@Controller
@RequestMapping("/")
public class DownLoadController {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${sharefileroot}")
	private String fileRoot;
	
	@Value("${download_tmproot}")
	private String download_root;
	
	@Resource(name="convertThreadPool")
	private ExecutorService convertservice;
	
	@Value("${convert_pdfroot}")
	private String convert_pdfroot;
	
	@Value("${preview.reconverttm}")
	private long reconverttm;
	
	@RequestMapping(value = "/upload")
	public String upload(@RequestParam("file") MultipartFile file) {
		try {
			if (file.isEmpty()) {
				return "文件为空";
			}
			// 获取文件名
			String fileName = file.getOriginalFilename();
			log.info("上传的文件名为：" + fileName);
			// 获取文件的后缀名
			String suffixName = fileName.substring(fileName.lastIndexOf("."));
			log.info("文件的后缀名为：" + suffixName);
			// 设置文件存储路径
			String filePath = "/Users/dalaoyang/Downloads/";
			String path = filePath + fileName;
			File dest = new File(path);
			// 检测是否存在目录
			if (!dest.getParentFile().exists()) {
				dest.getParentFile().mkdirs();// 新建文件夹
			}
			file.transferTo(dest);// 文件写入
			return "上传成功";
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "上传失败";
	}

	/**
	 * 批量上传 ; 
	 * 前端使用的是fileinput组件, 使用ajax 异步单笔上传;
	 * @param request
	 * @return
	 */
	@PostMapping("/batchupload")
	@ResponseBody
	public FileUpResult handleFileUpload(HttpServletRequest request) {
		log.debug("In batchupload...");
		BufferedOutputStream stream = null;
		int rcount = 0;
		FileUpResult data = new FileUpResult();
		
		MultipartFile mulfile = ((MultipartHttpServletRequest) request).getFile("upfile");
		
		String curpath = request.getParameter("curpath");
		String file_id = request.getParameter("file_id");
		
		log.debug("接收到的上传参数:" + mulfile.getName() + " 当前目录:" + curpath + " " + file_id);
		
		data.setFile_id(file_id);
		data.setFilename(mulfile.getName());
		
		if (!mulfile.isEmpty()) {
			// 拼接文件路径 
			String filepath = FileUtil.contactPath(this.fileRoot, curpath);
			filepath = FileUtil.contactPath(filepath, mulfile.getOriginalFilename());
			File file = new File(filepath);
			
			// 判断是否允许预览  , 允许预览 且 无需转换时, 同步写预览文件  
			// 不允许预览 , 则不写  
			// 预览需要转换时  ,  则不写 
			// 放异步中进行处理  
			
			try {
				byte[] buf = new byte[1024];
				stream = new BufferedOutputStream(new FileOutputStream(file));
				InputStream in =  mulfile.getInputStream();
				
				while ((rcount = in.read(buf, 0, 1024)) != -1) {
					stream.write(buf, 0, rcount);
				}
				
				stream.close();
				data.setMsg("写文件成功");
			} catch (Exception e) {
				log.error("写文件异常" , e);
				data.setMsg("写文件异常" + e.getMessage());
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (Exception e) {
						
					}
				}
			}
			
			// 1. 判断是否允许预览  
			// 2. 判断是否需要转换 
			// 放异步进行处理 
			if (FileUtil.allowpreview(file)) {
				this.convertservice.execute(new ConvertRunnable(file, curpath, mulfile.getOriginalFilename(), this.convert_pdfroot, this.reconverttm));
			}
			
		} else {
			data.setMsg("上传失败, 文件为空");
		}

		return data;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @{/download(currentPath=${currentPath},subPath=${d.getName()},download=1)}
	 * @{/download(currentPath=${currentPath}, fileName=${d.getName()})}
	 * @return
	 */
	@GetMapping("/download")
	public ResponseEntity<FileSystemResource> downloadFile(HttpServletRequest request) {

		// 获取上送参数 
		String currentPath  = request.getParameter("currentPath");
		String subPath  = request.getParameter("subPath");
		String fileName  = request.getParameter("fileName");
		String download  = request.getParameter("download");
		
		String dpath = "";
		String dname = "";
		boolean isfail = false;
		String msg = "";
		
		log.debug("下载文件请求参数: " + currentPath + " " + subPath + " " + fileName + " " + download);
		
		// 下载目录时, 检查  download 为1  , 压缩目录 root + currentPath + subPath 
		if (StringUtils.equals(download, "1")) {
			String tmppath  = FileUtil.contactPath(currentPath, subPath);
			String dirpath = FileUtil.contactPath(this.fileRoot, tmppath);
			String zipname = ".zip";
			
			if (StringUtils.isBlank(tmppath) || StringUtils.equals(tmppath, File.separator)) {
				// 当tmppath 为空时, 取名为 root 
				zipname = "root" + zipname;
			} else {
				String[] strs = tmppath.split(Matcher.quoteReplacement(File.separator));
				zipname = strs[strs.length - 1] + zipname;
			}
			
			// zip 文件保存路径 
			// 使用随机字符串生成路径
			String zippath = FileUtil.contactPath(this.download_root, UUID.randomUUID().toString());
			
			log.debug("开始压缩文件: " + dirpath + " " + zippath + " " + zipname);
			
			try {
				FileUtil.folder2zip(dirpath, zippath, zipname);
			} catch (Exception exp) {
				log.error("压缩文件出现异常 :" ,exp);
				isfail = true;
				msg = "压缩文件异常:" + exp.getMessage();
			}
			
			dpath = FileUtil.contactPath(zippath, zipname);
			dname = zipname;
			
		} else {
			// 下载文件时, 检查  fileName不空 , 路径为 root + currentPath + fileName 
			
			if (StringUtils.isBlank(fileName)) {
				log.debug("下载文件时, 上送文件名称为空!");
				isfail = true;
				msg ="下载文件时, 上送文件名称为空!";
			} else {
				dpath  = FileUtil.contactPath(this.fileRoot, FileUtil.contactPath(currentPath,  fileName));
				dname = fileName;
			}
		}
		
		log.info("下载文件路径及名称:" + dpath + " " + dname);
		
		if (isfail) {
			log.info("下载失败" + msg);
			return null;
		} 
		
		return this.export(new File(dpath));
		/**
		else {
			response.setContentType("application/force-download");// 设置强制下载不打开
			response.addHeader("Content-Disposition", "attachment;fileName=" + dname);// 设置文件名
			
			File file = new File(dpath);
			
			byte[] buffer = new byte[1024];
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			try {
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				OutputStream os = response.getOutputStream();
				int i = bis.read(buffer);
				while (i != -1) {
					os.write(buffer, 0, i);
					i = bis.read(buffer);
				}
				os.close();
				return  ;
			} catch (Exception e) {
				log.error("读取下载文件异常!" , e);
				
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						log.error("" , e);
						e.printStackTrace();
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						log.error("" , e);
						e.printStackTrace();
					}
				}
			}
		}
		**/
	}
	
	/**
	 * 下载文件时, 对于中文文件名称, 会出现乱码  
	 * @param file
	 * @return
	 */
    public ResponseEntity<FileSystemResource> export(File file) {
        if (file == null) {
            return null;
        }
        String filename = "";
		try {
			filename = new String(file.getName().getBytes("UTF-8"),"iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));
    }

}
