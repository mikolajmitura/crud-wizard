package pl.jalokim.crudwizard.genericapp.metamodel.method;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class BeanAndMethodDto {

    @Size(min = 3, max = 250)
    @NotNull
    String className;

    @Size(min = 3, max = 100)
    String beanName;

    @Size(min = 3, max = 100)
    @NotNull
    String methodName;
}
