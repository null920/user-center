package com.ycr.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ycr.usercenter.mapper")
@EnableScheduling
public class UserCenterBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserCenterBackendApplication.class, args);
	}

}
