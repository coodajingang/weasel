package com.dus.weasel.preview;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.dus.weasel.ApplicationContextProvider;
import com.dus.weasel.utils.FileUtil;

public class ConvertRunnable implements Runnable{

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	// 原文件  预览文件  预览转换等待文件 
	private File oriFile;

	private String curPath;
	private String oriFileName;
	
	// 转换的文件跟目录 
	private String root;
	
	// 超时重新进行转换的域值 
	private long reconvertm;
	
	public ConvertRunnable(File file, String curpath, String originalFilename, String root, long reconvertm) {
		this.oriFile = file;
		this.curPath = curpath;
		this.oriFileName = originalFilename;
		this.root = root;
		this.reconvertm = reconvertm;
	}

	/**
	 * 1. 检查是否需要预览 , 不需要预览 ,则返回  
	 * 2. 检查是否需要转换  , 不需要转换 ,则 直接生成待预览文件 ; 
	 * 3. 若需要转换  , 则 进行转换 ; 
	 */
	@Override
	public void run() {
		String prefilename = FileUtil.previewFileName(this.oriFileName);
		
		// 获取配置参数 
		String prepath = FileUtil.contactPath(this.root, this.curPath, prefilename);
		String prewaitpath = prepath + ".wait";
		
		String dirpath = FileUtil.contactPath(this.root, this.curPath);
		
		// 检查目录是否存在, 不存在则 创建 
		File dirfile = new File(dirpath);
		if (!dirfile.exists() || dirfile.isFile()) {
			dirfile.mkdirs();
		}
		
		log.info("文件预览处理:" + prepath);
		
		// 1. 判断是否需要预览 
		if (!FileUtil.allowpreview(this.oriFile)) {
			log.info("该文件无需进行预览 " + this.oriFile.getAbsolutePath());
			return ;
		}
		
		// 2. 是否需要转换文件 
		if (FileUtil.allowconvert(this.oriFile)) {
			// 待预览文件转换  
			File prefile = new File(prepath) ;
			File prewaitfile = new File(prewaitpath);
			
			// 1. 若文件已经存在 ,则 直接返回 
			if (prefile.exists()) {
				log.info("转换文件已存在!");
				return ;
			}
			
			boolean reconvert = false;
			
			if (prewaitfile.exists()) {
				// 检查时间戳  
				long creattm = prewaitfile.lastModified();
				
				if ((System.currentTimeMillis() - creattm) > this.reconvertm) {
					log.info("超时未转换完成 , 重新转换 !");
					// 3-1. 重写wait文件 
					// 3-2. 启动新转换任务 
					reconvert = true;
				} else {
					log.info("转换进行中  , 等待.转换 !");
					return;
				}
			}
			
			if (!prewaitfile.exists() || reconvert) {
				// 重生成convert 文件 
				// 3-1. 写wait文件  
				FileWriter writer = null;
				log.debug("重写wait文件...");
				try {
					writer = new FileWriter(prewaitfile);
					writer.write("开始转换=" + System.currentTimeMillis() + "\n");
					writer.flush();
					writer.close();
				} catch (IOException e) {
					log.error("写转换wait文件异常:" + prewaitpath , e);
					throw new RuntimeException("写转换wait文件异常 !" + prewaitpath , e);
				} finally  {
					if (writer != null) {
						try {
							writer.close();
						} catch (IOException exp) {
							
						}
					}
				}
				
				// 进行转换  
				log.debug("进行转换 ...");
				DocumentConverter convert = ApplicationContextProvider.getBean(DocumentConverter.class);
				
				try {
					convert
						.convert(this.oriFile)
						.as(DefaultDocumentFormatRegistry.getFormatByExtension(FileUtil.getFileExtension(this.oriFile.getName())))
						.to(prefile)
						.as(DefaultDocumentFormatRegistry.getFormatByExtension(FileUtil.getFileExtension(prefile.getName())))
						.execute();
				} catch (OfficeException e) {
					log.error("进行预览文件转换时出现异常!" + prefile.getAbsolutePath(), e);
					return ;
				}
				
				// 转换完成后删除 wait文件  
				log.debug("转换完成删除wait文件!");
				if (prewaitfile.exists()) {
					prewaitfile.delete();
				}
			}
			
		} else {
			// 无需转换  
			File prefile = new File(prepath) ;
			if (prefile.exists()) {
				log.info("预览文件已存在!");
				return ;
			}
			
			// 若不存在 , 则 生成之  , 直接从原文件拷贝一份 ;  
			try {
				Files.copy(this.oriFile.toPath(), prefile.toPath());
			} catch (IOException e) {
				log.error("从源文件拷贝到目标文件出现异常:" ,e);
				return ;
			}
		}

	}

}
