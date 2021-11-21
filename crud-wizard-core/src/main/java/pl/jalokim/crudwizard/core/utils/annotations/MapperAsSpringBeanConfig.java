package pl.jalokim.crudwizard.core.utils.annotations;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface MapperAsSpringBeanConfig {

}
