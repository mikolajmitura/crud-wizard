package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto

class EndpointMetaModelDtoSamples {

    static EndpointMetaModelDto createValidPostEndpointMetaModelDto() {
        EndpointMetaModelDto.builder()
            .baseUrl(randomText())
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.POST)
            .operationName(randomText())
            .payloadMetamodel(createValidClassMetaModelDtoWithName())
            .responseMetaModel(createValidEndpointResponseMetaModelDto())
            .build()
    }

    static EndpointResponseMetaModelDto createValidEndpointResponseMetaModelDto() {
        EndpointResponseMetaModelDto.builder()
            .classMetaModel(createClassMetaModelDtoFromClass(Long))
            .successHttpCode(201)
            .build()
    }
}
