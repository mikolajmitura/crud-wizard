package pl.jalokim.crudwizard.examples.simpleapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example of usage default jdbc data storage without custom entities and jpa configurations
 */
@SpringBootApplication
public class SimpleApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplicationMain.class, args);
    }

}
