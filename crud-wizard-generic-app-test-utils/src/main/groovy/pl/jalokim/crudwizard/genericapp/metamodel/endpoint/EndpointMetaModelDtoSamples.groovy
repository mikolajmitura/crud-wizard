package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDtoSamples.createAdditionalValidatorsForExtendedPerson
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto

@Component
class EndpointMetaModelDtoSamples {

    static EndpointMetaModelDto createValidPostEndpointMetaModelDto() {
        EndpointMetaModelDto.builder()
            .baseUrl("users")
            .operationName("createUser")
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.POST)
            .payloadMetamodel(createValidClassMetaModelDtoWithName())
            .responseMetaModel(createValidEndpointResponseMetaModelDto())
            .build()
    }

    static EndpointMetaModelDto createValidPostExtendedUserWithValidators() {
        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(extendedPersonClassMetaModel())
            .payloadMetamodelAdditionalValidators(createAdditionalValidatorsForExtendedPerson())
            .build()
    }

    static EndpointMetaModelDto createValidPostWithSimplePerson() {
        createValidPostEndpointMetaModelDto()
            .toBuilder()
            .payloadMetamodel(simplePersonClassMetaModel())
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
