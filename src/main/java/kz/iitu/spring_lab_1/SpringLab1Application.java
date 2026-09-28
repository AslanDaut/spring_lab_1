package kz.iitu.spring_lab_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringLab1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringLab1Application.class, args);
	}

}
