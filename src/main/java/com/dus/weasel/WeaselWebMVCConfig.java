package com.dus.weasel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dus.weasel.interceptor.LogInterceptor;

@Configuration
//@EnableMongoRepositories
public class WeaselWebMVCConfig  implements WebMvcConfigurer{

	@Bean
	public LogInterceptor getLogInterceptor() {
		return new LogInterceptor();
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(getLogInterceptor());
		WebMvcConfigurer.super.addInterceptors(registry);
	}
}
