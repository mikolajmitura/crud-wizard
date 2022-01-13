package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

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
public class AdditionalPropertyEntity {

    private String name;
    private String valueRealClassName;
    private String rawJson;
}
