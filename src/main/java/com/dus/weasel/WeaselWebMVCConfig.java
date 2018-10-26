package com.dus.weasel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dus.weasel.interceptor.LogInterceptor;

@Configuration
//@EnableMongoRepositories
public class WeaselWebMVCConfig  implements WebMvcConfigurer{

	@Value("${convert_pdfroot}")
	private String convert_pdfroot;
	
	@Bean
	public LogInterceptor getLogInterceptor() {
		return new LogInterceptor();
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(getLogInterceptor());
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

		// registry.addResourceHandler("/upload/**").addResourceLocations("file:D:/wekuku_root/upload/"
		// ); //+ uploadProperties.getPath());
		registry.addResourceHandler("/viewroot/**")
				.addResourceLocations("file:" + convert_pdfroot + "/");

    }
    
    /**
     * 配置拦截器
     */
	/**
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(new RequestResponseLogInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(new UserSecurityInterceptor()).addPathPatterns("/user/**", "/doc/upload", "/doc/upload/", "/doc/save", "/doc/save/", "/doc/delfile", "/doc/delfile/").excludePathPatterns("/user/u/**", "/user/doc_list", "/user/doc_list/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (uploadProperties.getOsType() == 1) {
            registry.addResourceHandler("/upload/**").addResourceLocations("file:///" + uploadProperties.getPath());
        } else {
            // registry.addResourceHandler("/upload/**").addResourceLocations("file:D:/wekuku_root/upload/" ); //+ uploadProperties.getPath());
        	registry.addResourceHandler("/upload/**").addResourceLocations("file:" + uploadProperties.getPath() + uploadProperties.getPublicPath() + "/");
        }
    }
    **/
}
