package com.dus.weasel;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.dus.weasel.config.FileIconCssConfig;

@SpringBootApplication
@ComponentScan(basePackages= {"com.dus.weasel", "org.jodconverter", "org.dus"})
public class WeaselApplication {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start WeaselApplication");
		SpringApplication.run(WeaselApplication.class, args);
	}
	
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            FileIconCssConfig icon = (FileIconCssConfig) ctx.getBean("fileIconCssConfig");
            System.out.println(icon.toString());
        };
    }

}
