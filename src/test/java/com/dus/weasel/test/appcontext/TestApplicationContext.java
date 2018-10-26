package com.dus.weasel.test.appcontext;

import org.jodconverter.DocumentConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.dus.weasel.ApplicationContextProvider;
import com.dus.weasel.config.FileIconCssRegistry;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplicationContext {

	@Test
	public void test4genbean() {
		
		DocumentConverter convert = (DocumentConverter) ApplicationContextProvider.getBean(DocumentConverter.class);
		//FileIconCssRegistry convert = ApplicationContextProvider.getBean(FileIconCssRegistry.class);
		System.out.println(convert.toString());
	}
}
