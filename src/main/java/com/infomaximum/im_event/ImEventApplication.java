package com.infomaximum.im_event;

import com.infomaximum.im_event.Service.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ImEventApplication {

	public static ConfigurableApplicationContext run;

	private static final ImEventApplication app = new ImEventApplication();

	public ImEventApplication(){}

	public static ImEventApplication getInstance() {
		return app;
	}

	public static void main(String[] args) {
		 run = SpringApplication.run(ImEventApplication.class, args);
	}

	public <T> T getBean(Class<T> tClass){
		return ImEventApplication.run.getBean(tClass);
	}


}
