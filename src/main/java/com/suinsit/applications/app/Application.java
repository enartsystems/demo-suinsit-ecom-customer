package com.suinsit.applications.app;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
//@Controller
@SpringBootApplication(scanBasePackages = {"com.suinsit.framework.integration.mail.mailjet","com.suinsit.webapp","com.suinsit.web.security"})
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		System.setProperty("user.timezone", "Europe/Madrid");
		SpringApplication.run(Application.class);
	}
	//@GetMapping("/")
    public String login() {
        return "login.zhtml";
    }
}
