package pl.jalokim.crudwizard.genericapp.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "crud.wizard.defaults")
public class DefaultBeansConfiguration {

    private String nameOfDataSource;
    private String classOfDataSource;

}
