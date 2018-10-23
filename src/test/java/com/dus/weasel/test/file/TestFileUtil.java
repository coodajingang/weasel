package com.dus.weasel.test.file;

import java.io.File;
import java.util.regex.Matcher;

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
		
		String[] ss  = tmpath.split(File.separator);  // 异常 
		//String[] ss  = tmpath.split("\\");  // 异常 
		//String[] ss  = tmpath.split("\\\\");  // 正常  
		//String[] ss  = tmpath.split(Matcher.quoteReplacement(File.separator));  // 正常  
		
		System.out.println(ss.length);
		System.out.println(ss[ss.length -1]);
	}
}
