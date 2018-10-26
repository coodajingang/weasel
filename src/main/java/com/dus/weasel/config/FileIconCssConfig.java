package com.dus.weasel.config;

import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件转换相关配置 ;
 * 配置文件图标显示的css ; 
 * 配置允许预览的后缀 ;
 * 配置允许转换为pdf的后缀 ;
 * @author ThinkPad
 *
 */
@Configuration
@ConfigurationProperties(prefix="fileformat")
public class FileIconCssConfig {
	public static class ExtCss {
		private String ext;
		private String css;
		public String getExt() {
			return ext;
		}
		public void setExt(String ext) {
			this.ext = ext;
		}
		public String getCss() {
			return css;
		}
		public void setCss(String css) {
			this.css = css;
		}
		@Override
		public String toString() {
			return "ExtCss [ext=" + ext + ", css=" + css + "]";
		}
		
		
	}
	
	private ExtCss[] iconclass;
	private String defaultcss; 
	
	private String allowpreview;
	private String allowconvert;
	
	
	@Override
	public String toString() {
		return "FileIconCssConfig [iconclass=" + Arrays.toString(iconclass) + ", defaultcss=" + defaultcss
				+ ", allowpreview=" + allowpreview + ", allowconvert=" + allowconvert + "]";
	}

	public String getDefaultcss() {
		return defaultcss;
	}

	public void setDefaultcss(String defaultcss) {
		this.defaultcss = defaultcss;
	}

	public ExtCss[] getIconclass() {
		return iconclass;
	}

	public void setIconclass(ExtCss[] iconclass) {
		this.iconclass = iconclass;
	}

	public String getAllowpreview() {
		return allowpreview;
	}

	public void setAllowpreview(String allowpreview) {
		this.allowpreview = allowpreview;
	}

	public String getAllowconvert() {
		return allowconvert;
	}

	public void setAllowconvert(String allowconvert) {
		this.allowconvert = allowconvert;
	}
	
	
}
