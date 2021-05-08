package pl.jalokim.crudwizard.examples.customized;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example of usage jdbc data storage with custom entities and jpa configuration
 */
@SpringBootApplication
public class CustomizedApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(CustomizedApplicationMain.class, args);
    }

}
