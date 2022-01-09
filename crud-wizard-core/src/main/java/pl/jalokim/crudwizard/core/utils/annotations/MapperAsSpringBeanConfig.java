package pl.jalokim.crudwizard.core.utils.annotations;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    componentModel = "spring"
)
public interface MapperAsSpringBeanConfig {

}
