package com.dus.weasel.test.file;

import java.io.File;

import org.junit.Test;

import com.dus.weasel.utils.FileUtil;

public class TestFileUtil {

	@Test
	public void test4zip() {
		FileUtil.folder2zip("E:\\logs", "E:\\logss\\download", "w1.zip");
	}
	
	@Test
	public void test4pathsplit() {
		System.out.println(File.pathSeparator + " " + File.separator);
		String tmpath = FileUtil.contactPath("E:\\D", "") ;
		
		System.out.println(tmpath);
		
		//String[] ss  = tmpath.split(File.separator);  // 异常 
		//String[] ss  = tmpath.split("\\");  // 异常 
		String[] ss  = tmpath.split("\\\\");  // 正常  
		//String[] ss  = tmpath.split(Matcher.quoteReplacement(File.separator));  // 正常  
		
		System.out.println(ss.length);
		System.out.println(ss[ss.length -1]);
		
		String tmp = "doc";
		String[] ts = tmp.split(",");
		System.out.println(ts[0]);
	}
	
	@Test
	public void test4getextention() {
		System.out.println(FileUtil.getFileExtension(""));
		System.out.println(FileUtil.getFileExtension("."));
		System.out.println(FileUtil.getFileExtension(".."));
		System.out.println(FileUtil.getFileExtension(".doc"));
		System.out.println(FileUtil.getFileExtension(".doc.xls"));
		System.out.println(FileUtil.getFileExtension(".doc.xls."));
	}
	
	@Test
	public void test4strjoin() {
		String path = "D:\\viewport\\dir1\\dir2|\\dir3\\axx.html.pdf";
		
		System.out.println(FileUtil.toUrlPath(path));
		System.out.println(FileUtil.toUrlPath(""));
		System.out.println(FileUtil.toUrlPath("viewroot"));
		System.out.println(FileUtil.toUrlPath("\\viewroot"));
		System.out.println(FileUtil.toUrlPath("\\viewroot\\"));
		System.out.println(FileUtil.toUrlPath("viewroot\\"));
	}
}
