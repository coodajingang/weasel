package com.dus.weasel.test.file;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.junit.Test;

import com.dus.weasel.domain.FileDomain;
import com.dus.weasel.domain.FileInfo;
import com.dus.weasel.domain.FilePathInfo;
import com.dus.weasel.utils.FileUtil;

public class TestFileDomain {

	@Test
	public void test4Files() {

		System.out.println(FileUtil.contactPath("E:\\D\\", "\\"));
		System.out.println(FileUtil.contactPath("E:\\D\\", ""));
		System.out.println(FileUtil.contactPath("E:\\D", "\\"));
		
		System.out.println(FileUtil.contactPath("E:\\D\\", "\\sub\\sub1"));
		System.out.println(FileUtil.contactPath("E:\\D\\", "sub\\bus1"));
		
		System.out.println(FileUtil.contactPath("E:\\D", "\\sub\\sub1\\"));
		System.out.println(FileUtil.contactPath("E:\\D", "\\sub\\sub1"));
		
		System.out.println(FileUtil.contactPath("\\", "\\"));
		System.out.println(FileUtil.contactPath("\\", ""));
		System.out.println(FileUtil.contactPath("", "\\"));
		System.out.println(FileUtil.contactPath("", ""));
	}
	
	
	@Test
	public void test4Files22() {
		File ff  = new File("E:\\D");
		
		File[]  fs = ff.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return pathname.isFile();
			}
		});
		
		for (File f : fs) {
			System.out.println(f.getName());
			if (f.getName().endsWith(".link")) {
				System.out.println("============= true " + f.getName() );
			}
		}
		
		List<FileInfo> linkList = FileUtil.getLinkList(ff);
		for (FileInfo fi : linkList) {
			System.out.println(fi.toString());
		}
		
		System.out.println("===================");
		List<FileInfo> fileList = FileUtil.getFileList(ff);
		for (FileInfo fi : fileList) {
			System.out.println(fi.toString());
		}
	}
	
	@Test
	public void test4GetFilepathlist() {
		File root = new File("E:\\D");
		File c = new File("E:\\D\\p1\\p2\\p3\\");
		File c2 = new File("E:\\D");

		
		List<FilePathInfo> pathList = FileUtil.getPathList(root, c);
		
		for (FilePathInfo p : pathList) {
			System.out.println(p.toString());
		}
	}
	
	@Test
	public void test4Filedomain() {
		File root = new File("E:\\D\\");
		
		FileDomain df = new FileDomain("p1\\", root);
		System.out.println(df.toString());
	}
}
