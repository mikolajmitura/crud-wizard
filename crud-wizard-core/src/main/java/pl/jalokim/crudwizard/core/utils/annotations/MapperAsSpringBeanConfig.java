package pl.jalokim.crudwizard.core.utils.annotations;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    // TODO enable in future to ensure that all will be mapped as expected
    //unmappedTargetPolicy = ReportingPolicy.ERROR,
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface MapperAsSpringBeanConfig {

}
