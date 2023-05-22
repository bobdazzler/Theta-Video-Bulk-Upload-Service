package com.theta.ThetaVideoApiBulkUpload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ThetaVideoApiBulkUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThetaVideoApiBulkUploadApplication.class, args);
	}

}
