package pl.jalokim.crudwizard.genericapp.metamodel.method;

import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class BeanAndMethodEntity {

    private String className;

    private String beanName;

    private String methodName;
}
