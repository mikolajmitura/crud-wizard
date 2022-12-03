package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression.NULL_ASSIGN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class EnumEntriesMapping {

    Long id;
    /**
     * as key is source enum value as value target enum value
     */
    @Builder.Default
    Map<String, String> targetEnumBySourceEnum = new HashMap<>();

    @Builder.Default
    List<String> ignoredSourceEnum = new ArrayList<>();

    @Builder.Default
    String whenNotMappedEnum = NULL_ASSIGN;
}
