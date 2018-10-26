package com.dus.weasel.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dus.weasel.config.FileIconCssRegistry;
import com.dus.weasel.domain.FileInfo;
import com.dus.weasel.domain.FilePathInfo;

public class FileUtil {

	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static final String LINK_SUFFIX = ".link";
	
	public static void getFilesList(String path) {

	}

	/**
	 * 获取rootFile下的目录文件列表  
	 * @param rootFile
	 * @return
	 */
	public static List<FileInfo> getDirList(File rootFile) {
		if (rootFile == null || !rootFile.exists() || rootFile.isFile()) {
			return null;
		}
		
		File[] fs =  rootFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return pathname.isDirectory();
			}
		});
		
		if (fs == null || fs.length == 0) {
			return null;
		}
		
		List<FileInfo> fl = new ArrayList<FileInfo>();
		for (File f : fs) {
			fl.add(FileInfo.createFromFiles(f, ""));
		}
		return fl;
	}
	
	/**
	 * 获取rootFile下的文件列表  
	 * @param rootFile
	 * @return
	 */
	public static List<FileInfo> getFileList(File rootFile) {
		if (rootFile == null || !rootFile.exists() || rootFile.isFile()) {
			return null;
		}
		
		File[] fs =  rootFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				
				if (pathname.isFile() && !isFileLink(pathname)) {
					return true;
				}
				return false;
			}
		});
		
		if (fs == null || fs.length == 0) {
			return null;
		}
		
		List<FileInfo> fl = new ArrayList<FileInfo>();
		for (File f : fs) {
			fl.add(FileInfo.createFromFiles(f, ""));
		}
		return fl;
	}
	
	/**
	 * 获取rootFile下的链接文件列表  
	 * @param rootFile
	 * @return
	 */
	public static List<FileInfo> getLinkList(File rootFile) {
		if (rootFile == null || !rootFile.exists() || rootFile.isFile()) {
			return null;
		}
		
		File[] fs =  rootFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {

				if (pathname.isFile() && isFileLink(pathname)) {

					return true;
				}
				return false;
			}
		});
		
		if (fs == null || fs.length == 0) {
			return null;
		}
		
		List<FileInfo> fl = new ArrayList<FileInfo>();
		for (File f : fs) {
			fl.add(FileInfo.createFromFiles(f, "3"));
		}
		return fl;
	}
	
	private static boolean isFileLink(File f) {
		String name = f.getName();
		if (name.endsWith(LINK_SUFFIX)) {
			return true;
		}
		return false;
	}

	/**
	 * 拼接路径   root / curRelPath 
	 * @param root
	 * @param curRelPath
	 * @return
	 */
	public static String contactPath(String root, String curRelPath) {
		String path = root;
		
		if (StringUtils.isBlank(curRelPath)) {
			return root;
		}
		if (root.endsWith(File.separator)) {
			path = path.substring(0, path.length() -1);
		}
		
		if (curRelPath.startsWith(File.separator)) {
			return path + curRelPath;
		}
		return path + File.separator + curRelPath ;
	}

	public static String contactPath(String... paths) {
		if (paths.length == 0) {
			return "";
		}
		if (paths.length == 1) {
			return paths[0];
		}
		
		String tmpres = paths[0];
		
		for (int i = 1; i < paths.length ; ++i) {
			tmpres = contactPath(tmpres, paths[i]);
		}
		
		return tmpres;
	}
	
	/**
	 * 获取从当前目录 到跟目录的所有的路径  
	 * @param rootFile
	 * @param curFile
	 * @return
	 */
	public static List<FilePathInfo> getPathList(File rootFile, File curFile) {
		List<FilePathInfo> list = new ArrayList<FilePathInfo>();
		
		File tmpFile = curFile;
		int begin = rootFile.getAbsolutePath().length();
		
		while (!StringUtils.equals(tmpFile.getAbsolutePath(), rootFile.getAbsolutePath())) {
			// 将当前目录加入到列表中  
			FilePathInfo path = new FilePathInfo();
			path.setAbpath(tmpFile.getAbsolutePath());
			path.setPname(tmpFile.getName());
			path.setRpath(tmpFile.getAbsolutePath().substring(begin));
			
			list.add(path);
			// 取上级目录 ;
			tmpFile = tmpFile.getParentFile();
		}
		
		// 将根目录加入列表中  
		// 不用加入了 , 直接写死 ;
		// 列表翻转 
		Collections.reverse(list);
		return list;
	}

	/**
	 * 循环删除文件 
	 * @param delfile
	 */
	public static void deleteFile(File delfile) {
		if (delfile.isFile()) {
			delfile.delete();
			return ;
		}
		
		File[] fs = delfile.listFiles();
		
		for (File f : fs) {
			deleteFile(f);
		}
		
		delfile.delete();
	}
	
	/**
	 * 压缩当前目录path下的所有文件 , 生成文件名称为 zipname , 放在路径zippath下 ; 
	 * 有异常则抛出 ;
	 * @param path
	 * @param zippath
	 * @param zipname
	 * @throws IOException 
	 */
	public static void folder2zip(String path, String zippath, String zipname) {
		File src = new File(path);
		ZipOutputStream zos = null;
		
		
		if (src == null  || !src.exists() || !src.isDirectory()) {
			// 源目录不存在 或不是目录 , 则异常  
			logger.error("压缩源目录不存在或非目录!" + path);
			throw new RuntimeException("压缩源目录不存在或非目录!" + path);
		}
		
		File destdir = new File(zippath);
		
		if (!destdir.exists()) {
			// 创建目录  
			logger.info("压缩目标路径不存在, 创建之." + zippath);
			destdir.mkdirs();
		}
		
		File zipfile = new File(contactPath(zippath, zipname));
		
		
		File[] srclist = src.listFiles();
		
		if (srclist == null || srclist.length == 0) {
			// 源目录内容为空,无需压缩 
			logger.error("源目录内容为空,无需压缩下载!" + path);
			throw new RuntimeException("源目录内容为空,无需压缩下载!" + path);
		}
		
		try {
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipfile)));
			
			// 递归压缩目录下所有的文件  ;
			compress(zos, src, src.getName());
			
			zos.close();
			
		} catch (FileNotFoundException e) {
			logger.error("压缩文件不存在", e);
			throw new RuntimeException("压缩目标文件不存在!" + e.getMessage());
		} catch (IOException e) {
			logger.error("压缩文件IO异常", e);
			throw new RuntimeException("压缩文件IO异常!" + e.getMessage());
		} 
		finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		
	}
	
	/**
	 * 递归压缩文件 
	 * @param zos
	 * @param src
	 * @throws IOException 
	 */
	private static void compress(ZipOutputStream zos, File src, String name) throws IOException {
		if (src == null || !src.exists()) {
			return ;
		} 
		if (src.isFile()) {
			byte[] bufs = new byte[10240];
			
			ZipEntry zentry = new ZipEntry(name);
			zos.putNextEntry(zentry);
			
			logger.debug("压缩:" + src.getAbsolutePath());
			FileInputStream in = new FileInputStream(src);
			
			BufferedInputStream bin = new BufferedInputStream(in, 10240);
			
			int readcount = 0 ; 
			
			while( (readcount = bin.read(bufs, 0 , 10240)) != -1) {
				zos.write(bufs, 0 , readcount);
			}
			
			zos.closeEntry();
			bin.close();
		} else {
			// 文件夹  
			File[] fs = src.listFiles();
			
			if (fs == null || fs.length == 0 ) {
				zos.putNextEntry(new ZipEntry(name + File.separator ));
				zos.closeEntry();
				return ;
			}
			
			for (File f : fs) {
				compress(zos, f, name + File.separator + f.getName());
			}
		}
	}

	/** 
	 * 根据图标获取相应的css class 
	 * @param f
	 * @return
	 */
	public static String getIconClass(File f) {
		String ext = getFileExtension(f.getName());
		return FileIconCssRegistry.getFileIconCss(ext);
	}
	
	/**
	 * 返回文件后缀 , 没有则返回空 
	 * "" : null 
	 * ".": 空
	 * ".doc" : doc 
	 * @param name
	 * @return
	 */
	public static String getFileExtension(String name) {
		int i = name.lastIndexOf(".");
		
		if (i == -1 ) {
			return "";
		}
		return name.substring(i + 1);
	}

	/**
	 * 根据文件后缀 判断文件是否允许预览  
	 * @param f
	 * @return
	 */
	public static boolean allowpreview(File f) {
		String ext = getFileExtension(f.getName());
		
		return FileIconCssRegistry.getAllowpreview(ext);
	}

	/**
	 * 根据文件后缀 判断文件是否允许转化为pdf  
	 * @param f
	 * @return
	 */
	public static boolean allowconvert(File f) {
		String ext = getFileExtension(f.getName());
		
		return FileIconCssRegistry.getAllowconvert(ext);
	}

	/**
	 * 根据原来的文件名称 ,确定预览的文件名称 ; 
	 * 若允许转换, 则返回转换的文件名 ;
	 * 否则 返回原文件名;
	 * @param fileName
	 * @return
	 */
	public static String previewFileName(String fileName) {
		String ext = getFileExtension(fileName);
		
		if (FileIconCssRegistry.getAllowconvert(ext)) {
			return fileName + ".pdf";
		}
		
		return fileName;
	}

	/**
	 * 转换文件系统路径 到 web url路径 
	 * @param viewpath
	 * @return
	 */
	public static String toUrlPath(String viewpath) {
		String[] tmps = viewpath.split(Matcher.quoteReplacement(File.separator));
		
		if (tmps.length == 1) {
			return viewpath;
		}
		
		return StringUtils.join(tmps, "/");
	}
}
