package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EndpointMetaModelDto extends AdditionalPropertyMetaModel {

    @NotNull(groups = UpdateContext.class)
    Long id;

    @NotNull
    ApiTagDto apiTag;

    @NotNull
    String baseUrl;

    @NotNull
    HttpMethod httpMethod;

    @NotNull
    String operationName;

    ClassMetaModel payloadMetamodel;
    List<ClassMetaModel> queryArguments;

    ServiceMetaModel serviceMetaModel;

    EndpointResponseMetaModel responseMetaModel;

    // TODO maybe here which headers distinct to this endpoint
    // TODO all models and DataStorage move to core module???
}
