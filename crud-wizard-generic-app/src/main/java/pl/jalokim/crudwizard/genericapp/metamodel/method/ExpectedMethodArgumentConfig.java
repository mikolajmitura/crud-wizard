package pl.jalokim.crudwizard.genericapp.metamodel.method;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_STRING_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.STRING_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.TypePredicate.newTypePredicate;
import static pl.jalokim.utils.collection.Elements.elements;

import com.fasterxml.jackson.databind.JsonNode;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@Slf4j
public class ExpectedMethodArgumentConfig {

    private static final ClassMetaModelsPredicate METHOD_ARG_IS_SIMPLE_TYPE = (
        methodArgumentMetaModel,
        methodArgClassMetaModel,
        inputTypeOfMapperOrService) -> methodArgClassMetaModel.isSimpleType();

    private static final List<ExpectedMethodArgument> COMMON_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(EndpointMetaModel.class,
            HttpServletRequest.class,
            HttpServletResponse.class),
        argAnnotatedAndAsMetaModels(RequestBody.class,
            newTypePredicate(Object.class, (
                methodArgumentMetaModel,
                methodArgClassMetaModel,
                inputTypeOfMapperOrService) ->
                expectedTypeForRequestBody(methodArgClassMetaModel, inputTypeOfMapperOrService, 0)
            )).toBuilder()
            .argumentCanBeInputOfMapper(true)
            .build(),
        argAnnotatedAndAsMetaModels(RequestHeader.class,
            newTypePredicate(MAP_STRING_STRING_MODEL),
            newTypePredicate(OBJECT_MODEL, METHOD_ARG_IS_SIMPLE_TYPE,
                (
                    methodArgumentMetaModel,
                    methodArgClassMetaModel,
                    inputTypeOfMapperOrService) ->
                    StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(RequestHeader.class).name())
                        || StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(RequestHeader.class).value())
            )),
        argAnnotatedAndAsMetaModels(RequestParam.class,
            newTypePredicate(MAP_STRING_OBJECT_MODEL),
            newTypePredicate(OBJECT_MODEL, METHOD_ARG_IS_SIMPLE_TYPE,
                (
                    methodArgumentMetaModel,
                    methodArgClassMetaModel,
                    inputTypeOfMapperOrService) ->
                    StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(RequestParam.class).name())
                        || StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(RequestParam.class).value())
            )),
        argAnnotatedAndAsMetaModels(PathVariable.class,
            newTypePredicate(OBJECT_MODEL, METHOD_ARG_IS_SIMPLE_TYPE,
                (
                    methodArgumentMetaModel,
                    methodArgClassMetaModel,
                    inputTypeOfMapperOrService) ->
                    StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(PathVariable.class).name())
                        || StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(PathVariable.class).value())))
    );

    public static final List<ExpectedMethodArgument> SERVICE_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(GenericServiceArgument.class,
            ValidationSessionContext.class,
            JsonNode.class,
            TranslatedPayload.class)
    );

    public static final List<ExpectedMethodArgument> MAPPER_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(GenericMapperArgument.class),
        argAsPredicates(newTypePredicate(Object.class, (
            methodArgumentMetaModel,
            methodArgClassMetaModel,
            inputTypeOfMapper) -> canBeAssignAsMapperArgument(methodArgClassMetaModel, inputTypeOfMapper)
        )).toBuilder()
            .argumentCanBeInputOfMapper(true)
            .build()
    );

    public static List<ExpectedMethodArgument> getCommonExpectedArgsTypeAndOther(List<ExpectedMethodArgument> otherExpectedArguments) {
        return elements(COMMON_EXPECTED_ARGS_TYPE)
            .concat(otherExpectedArguments)
            .asList();
    }

    public static ExpectedMethodArgument argAsTypes(Class<?>... realClass) {
        return ExpectedMethodArgument.builder()
            .typePredicates(
                elements(realClass)
                    .map(TypePredicate::newTypePredicate)
                    .asList())
            .build();
    }

    private static boolean expectedTypeForRequestBody(ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel inputTypeOfMapperOrService, int genericTypeNestingLevel) {

        log.debug("methodArgClassMetaModel: {}, inputTypeOfMapperOrService: {}",
            methodArgClassMetaModel.getTypeDescription(),
            inputTypeOfMapperOrService.getTypeDescription());

        if (genericTypeNestingLevel == 0 && Elements.of(JsonNode.class,
            TranslatedPayload.class)
            .anyMatch(givenClass -> givenClass.equals(methodArgClassMetaModel.getRealClass()))) {
            log.debug("methodArgClassMetaModel is JsonNode or TranslatedPayload");
            return true;
        } else if (inputTypeOfMapperOrService.isGenericMetamodelEnum()) {
            boolean result = methodArgClassMetaModel.isSubTypeOf(STRING_MODEL);
            log.debug("when inputTypeOfMapperOrService is generic meta model, methodArgClassMetaModel is a String when, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isGenericModel()) {
            boolean result = methodArgClassMetaModel.isTheSameMetaModel(MAP_STRING_OBJECT_MODEL)
                || (!methodArgClassMetaModel.isSimpleType()
                && !methodArgClassMetaModel.isCollectionType()
                && !methodArgClassMetaModel.isMapType());
            log.debug("methodArgClassMetaModel is generic object, not simple, not map<String,Object>, not collection and not map, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isSimpleType()) {
            boolean result = methodArgClassMetaModel.isSimpleType();
            log.debug("methodArgClassMetaModel is simple, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isCollectionType()) {
            boolean result = methodArgClassMetaModel.isCollectionType()
                && expectedTypeForRequestBody(
                methodArgClassMetaModel.getGenericTypes().get(0),
                inputTypeOfMapperOrService.getGenericTypes().get(0),
                ++genericTypeNestingLevel
            );
            log.debug("methodArgClassMetaModel is collection, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isMapType()) {
            boolean result = methodArgClassMetaModel.isMapType()
                && expectedTypeForRequestBody(
                methodArgClassMetaModel.getGenericTypes().get(0),
                inputTypeOfMapperOrService.getGenericTypes().get(0),
                ++genericTypeNestingLevel
            ) && expectedTypeForRequestBody(
                methodArgClassMetaModel.getGenericTypes().get(1),
                inputTypeOfMapperOrService.getGenericTypes().get(1),
                genericTypeNestingLevel
            );
            log.debug("methodArgClassMetaModel is map, result: {}", result);
            return result;
        }

        boolean result = !methodArgClassMetaModel.isSimpleType()
            && !methodArgClassMetaModel.isCollectionType()
            && !methodArgClassMetaModel.isMapType();
        log.debug("methodArgClassMetaModel is not simple, not collection, not map, result: {}", result);
        return result;
    }

    private static boolean canBeAssignAsMapperArgument(ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel expectedInputTypeOfMapper) {

        log.debug("methodArgClassMetaModel: {}, expectedInputTypeOfMapper: {}",
            methodArgClassMetaModel.getTypeDescription(),
            expectedInputTypeOfMapper.getTypeDescription());

        if (expectedInputTypeOfMapper.isGenericMetamodelEnum()) {
            boolean result = methodArgClassMetaModel.isSubTypeOf(STRING_MODEL);
            log.debug("expectedInputTypeOfMapper is generic enum but methodArgClassMetaModel is type of String, result: {}", result);
            return result;
        } else if (expectedInputTypeOfMapper.isGenericModel()) {
            boolean result = methodArgClassMetaModel.isTheSameMetaModel(MAP_STRING_OBJECT_MODEL)
                || MAP_STRING_OBJECT_MODEL.isSubTypeOf(methodArgClassMetaModel);
            log.debug("expectedInputTypeOfMapper is generic metamodel but methodArgClassMetaModel is type of Map<String, Object> "
                + "or Map is sub type of methodArgClassMetaModel, result: {}", result);
            return result;
        } else if (expectedInputTypeOfMapper.hasGenericTypes()) {

            boolean result = methodArgClassMetaModel.hasGenericTypes()
                && expectedInputTypeOfMapper.getGenericTypes().size() == methodArgClassMetaModel.getGenericTypes().size();

            log.debug("methodArgClassMetaModel has generic types and the same size like expectedInputTypeOfMapper, result: {}", result);

            AtomicBoolean matchAll = new AtomicBoolean(true);
            if (result) {
                var methodGenericTypes = methodArgClassMetaModel.getGenericTypes();
                var expectedGenericTypes = expectedInputTypeOfMapper.getGenericTypes();
                elements(methodGenericTypes).forEachWithIndex((index, methodArgumentType) -> {
                        matchAll.set(matchAll.get() && canBeAssignAsMapperArgument(methodArgumentType, (expectedGenericTypes.get(index))));
                    }
                );
            }
            log.debug("generic types of expectedInputTypeOfMapper and methodArgClassMetaModel are the same, result: {}", matchAll);

            return result && matchAll.get();
        }

        boolean result = expectedInputTypeOfMapper.isSubTypeOf(methodArgClassMetaModel);
        log.debug("expectedInputTypeOfMapper is sub type of methodArgClassMetaModel, result: {}", result);
        return result;
    }

    @Builder(toBuilder = true)
    @Value
    public static class ExpectedMethodArgument {

        @Builder.Default
        List<TypePredicate> typePredicates = new ArrayList<>();
        Class<? extends Annotation> isAnnotatedWith;

        /**
         * Can be input of mapper only when one of typePredicates is passed and
         * when is annotated with isAnnotatedWith
         */
        @Builder.Default
        boolean argumentCanBeInputOfMapper = false;
    }

    @Builder
    @Value
    @Getter
    public static class TypePredicate {

        @NotNull
        ClassMetaModel isSubTypeOf;
        @Builder.Default
        List<ClassMetaModelsPredicate> predicatesOfModel = List.of();

        static TypePredicate newTypePredicate(Class<?> isSubTypeOf, ClassMetaModelsPredicate... predicateOfType) {
            return newTypePredicate(ClassMetaModel.builder()
                .realClass(isSubTypeOf)
                .build(), predicateOfType);
        }

        static TypePredicate newTypePredicate(ClassMetaModel isSubTypeOf, ClassMetaModelsPredicate... predicateOfType) {
            return TypePredicate.builder()
                .isSubTypeOf(isSubTypeOf)
                .predicatesOfModel(elements(predicateOfType).asList())
                .build();
        }
    }

    private static ExpectedMethodArgument argAsPredicates(TypePredicate... typePredicates) {
        return argAnnotatedAndAsMetaModels(null, typePredicates);
    }

    private static ExpectedMethodArgument argAnnotatedAndAsMetaModels(Class<? extends Annotation> isAnnotatedWith, TypePredicate... typePredicates) {
        return ExpectedMethodArgument.builder()
            .isAnnotatedWith(isAnnotatedWith)
            .typePredicates(elements(typePredicates).asList())
            .build();
    }

    public interface ClassMetaModelsPredicate {

        boolean test(MethodArgumentMetaModel methodArgumentMetaModel,
            ClassMetaModel methodArgClassMetaModel,
            ClassMetaModel inputTypeOfMapperOrService);
    }
}
