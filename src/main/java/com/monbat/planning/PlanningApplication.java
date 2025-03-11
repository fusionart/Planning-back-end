package com.monbat.planning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan({"com.sap.cloud.sdk", "com.sap.cloud.sdk.tutorial"})
//@ServletComponentScan({"com.sap.cloud.sdk", "com.sap.cloud.sdk.tutorial"})
public class PlanningApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanningApplication.class, args);
	}

}
