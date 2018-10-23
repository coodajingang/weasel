package com.dus.weasel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class CreateController {
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${sharefileroot}")
	private String fileRoot;
	
	
}
