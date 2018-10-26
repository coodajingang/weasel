package com.dus.weasel.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dus.weasel.config.FileIconCssConfig.ExtCss;
import com.dus.weasel.utils.StringUtils;

@Component
public class FileIconCssRegistry  {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FileIconCssConfig config;
	
	private static String defaultcss;
	private static Map<String, String> registry;
	private static String allowpreview;
	private static String allowconvert;
	
	@PostConstruct
	public void init() {
		logger.info("开始初始化文件图标css类 !");
		defaultcss = this.config.getDefaultcss();
		registry = new HashMap<String, String>();
		
		for (ExtCss ex : this.config.getIconclass()) {
			String tmpex = ex.getExt();
			String[] extss = tmpex.split(",");
			for (String s : extss) {
				registry.put(s, ex.getCss());
			}
		}
		
		allowconvert = config.getAllowconvert();
		allowpreview = config.getAllowpreview();
	}
	
	/**
	 * 根据后缀获取相应的css 
	 * @return
	 */
	public static String getFileIconCss(String ext) {
		if (StringUtils.isBlank(ext)) {
			return defaultcss;
		}
		
		String css  = registry.get(ext);
		if (StringUtils.isBlank(css)) {
			css = registry.get(ext.toUpperCase());
		}
		
		if (StringUtils.isBlank(css)) {
			return defaultcss;
		}
		return css;
	}

	public FileIconCssConfig getConfig() {
		return config;
	}

	public void setConfig(FileIconCssConfig config) {
		this.config = config;
	}

	public String getDefaultcss() {
		return defaultcss;
	}

	public void setDefaultcss(String defaultcss) {
		this.defaultcss = defaultcss;
	}

	public Map<String, String> getRegistry() {
		return registry;
	}

	public void setRegistry(Map<String, String> registry) {
		this.registry = registry;
	}

	/**
	 * 检查是否允许预览  
	 * @param ext
	 * @return
	 */
	public static boolean getAllowpreview(String ext) {
		return allowpreview.contains(ext);
	}
	
	/**
	 * 检查是否允许转换
	 * @param ext
	 * @return
	 */
	public static boolean getAllowconvert(String ext) {
		return allowconvert.contains(ext);
	}
}
