package skala.springai.day1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// day2 패키지도 함께 훑도록 범위를 skala.springai 로 넓힌다
@SpringBootApplication(scanBasePackages = "skala.springai")
public class Day1Application {

	public static void main(String[] args) {
		SpringApplication.run(Day1Application.class, args);
	}

}
