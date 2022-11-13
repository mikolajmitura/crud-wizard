package pl.jalokim.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class TestApplicationMain {

    private static ConfigurableApplicationContext springContext;

    public static void main(String... args) {
        springContext = SpringApplication.run(TestApplicationMain.class, args);
    }

    public static void closeApplication() {
        springContext.close();
    }

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}
