package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider

class QueryProviderDtoSamples {

    static QueryProviderDto createQueryProviderDto(Class<?> className){
        QueryProviderDto.builder()
            .className(className.canonicalName)
            .build()
    }
}
