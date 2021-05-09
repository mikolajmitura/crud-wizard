package pl.jalokim.crudwizard.core.annotations;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = "spring"
)
public interface MapperAsSpringBeanConfig {

}
