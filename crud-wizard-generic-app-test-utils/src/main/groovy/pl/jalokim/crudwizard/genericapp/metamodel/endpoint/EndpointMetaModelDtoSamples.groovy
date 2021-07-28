package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto

@Component
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

    static EndpointMetaModelDto createValidPutEndpointMetaModelDto() {
        EndpointMetaModelDto.builder()
            .baseUrl("base-path/{basePath}/next-url/{nextId}")
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.PUT)
            .operationName(randomText())
            .pathParams(ClassMetaModelDto.builder()
                .name(randomText())
                .fields([
                    createValidFieldMetaModelDto("basePath", String),
                    createValidFieldMetaModelDto("nextId", Long)
                ])
                .build())
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

    static EndpointMetaModelDto emptyEndpointMetaModelDto() {
        EndpointMetaModelDto.builder().build()
    }
}
