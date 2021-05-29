package pl.jalokim.crudwizard

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
@SpringBootApplication
class TestsApplicationConfig {

    static void main(String[] args) {
        SpringApplication.run(TestsApplicationConfig.class, args)
    }
}
