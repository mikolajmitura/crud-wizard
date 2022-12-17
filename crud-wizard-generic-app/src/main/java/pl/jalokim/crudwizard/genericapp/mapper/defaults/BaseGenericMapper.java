package pl.jalokim.crudwizard.genericapp.mapper.defaults;

import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;

public interface BaseGenericMapper {

    Object mapToTarget(GenericMapperArgument mapperArgument);
}
