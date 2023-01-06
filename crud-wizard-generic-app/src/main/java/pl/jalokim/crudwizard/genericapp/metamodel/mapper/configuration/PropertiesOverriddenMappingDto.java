package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@Value
@Builder
@FieldShouldWhenOther(field = PropertiesOverriddenMappingDto.SOURCE_ASSIGN_EXPRESSION, should = NOT_NULL,
    whenField = PropertiesOverriddenMappingDto.IGNORE_FIELD, is = EQUAL_TO_ANY, otherFieldValues = "false")
@FieldShouldWhenOther(field = PropertiesOverriddenMappingDto.SOURCE_ASSIGN_EXPRESSION, should = NULL,
    whenField = PropertiesOverriddenMappingDto.IGNORE_FIELD, is = EQUAL_TO_ANY, otherFieldValues = "true")
public class PropertiesOverriddenMappingDto {

    static final String SOURCE_ASSIGN_EXPRESSION = "sourceAssignExpression";
    static final String IGNORE_FIELD = "ignoreField";

    Long id;

    @NotNull
    @Size(min = 1, max = 250)
    String targetAssignPath;

    @Size(max = 100)
    String sourceAssignExpression;

    boolean ignoreField;

    boolean ignoredAllMappingProblem;

    public static PropertiesOverriddenMappingDto mappingEntry(String target, String source) {
        return PropertiesOverriddenMappingDto.builder()
            .targetAssignPath(target)
            .sourceAssignExpression(source)
            .build();
    }
}
