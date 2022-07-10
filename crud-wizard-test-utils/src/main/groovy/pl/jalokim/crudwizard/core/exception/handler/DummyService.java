package pl.jalokim.crudwizard.core.exception.handler;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import pl.jalokim.crudwizard.core.exception.BusinessLogicException;
import pl.jalokim.crudwizard.core.exception.CustomValidationException;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.exception.ErrorWithPlaceholders;
import pl.jalokim.crudwizard.core.exception.ResourceChangedException;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.sample.SamplePersonDtoWitOtherObject;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.crudwizard.core.validation.javax.groups.WithoutDefaultGroup;

@Validated
@Service
@AllArgsConstructor
public class DummyService {

    void dummyMethod(@Valid DummyDto dto) {

    }

    void dummyMethodCustomException() {
        throw new EntityNotFoundException("My entity was not found with id: id");
    }

    void dummyMethodOptimisticLockingException() {
        throw new ResourceChangedException();
    }

    void dummyMethodBindException() throws BindException {
        BindException bindException = new BindException(new DirectFieldBindingResult(new Object(), "objectName"), "objectA");
        ObjectError error = new ObjectError("objectName", "error");
        error.wrap(new SimpleConstraintViolation(PathImpl.createPathFromString("objectA"), "An error has occurred"));
        bindException.addError(error);
        throw bindException;
    }

    void dummyMethodThrowingBusinessLogicException() {
        throw new BusinessLogicException("Business logic exception");
    }

    void dummyMethodThrowingTechnicalException() {
        throw new TechnicalException("Technical exception");
    }

    void dummyMethodThrowingDataValidationException() {
        throw new CustomValidationException("some error",
            Set.of(
                ErrorWithPlaceholders.builder()
                    .property("property1")
                    .rawMessage("message 1")
                    .build(),
                ErrorWithPlaceholders.builder()
                    .property("property2")
                    .rawMessage("message 2")
                    .build(),
                ErrorWithPlaceholders.builder()
                    .property("property3")
                    .messagePlaceholder(MessagePlaceholder.builder()
                        .errorCode("error.code.from.placeholder")
                        .mainPlaceholder("example.property")
                        .build())
                    .build()
            ));
    }

    public void dummyMethodThrowingRawException() throws Exception {
        throw new Exception("raw exception message");
    }

    @Validated
    public void create(@Validated SimpleDummyDto dto) {

    }

    @Validated
    public void create2(@Validated({UpdateContext.class, WithoutDefaultGroup.class}) SimpleDummyDto dto) {

    }

    @Validated
    public void update(@NotNull Long someId, @NotNull @Validated(UpdateContext.class) SimpleDummyDto dto) {

    }

    public SamplePersonDtoWitOtherObject mapPersonPart1(Map<String, Object> object, String exampleString) {
        return null;
    }

    public Map<String, Object> fetchSomeMap(String someId) {
        return null;
    }

    public String getSomeRandomText() {
        return "randomText";
    }

    public LocalDate getSomeRandomLocalDate() {
        return LocalDate.of(2022, 5, 11);
    }

    public Long somePost(JsonNode jsonNode) {
        return 11L;
    }
}
