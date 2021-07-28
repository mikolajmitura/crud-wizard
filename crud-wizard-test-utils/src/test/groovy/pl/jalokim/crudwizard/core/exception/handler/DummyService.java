package pl.jalokim.crudwizard.core.exception.handler;

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
import pl.jalokim.crudwizard.core.exception.ErrorWithMessagePlaceholder;
import pl.jalokim.crudwizard.core.exception.ResourceChangedException;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;

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
                ErrorWithMessagePlaceholder.builder()
                    .property("property1")
                    .rawMessage("message 1")
                    .build(),
                ErrorWithMessagePlaceholder.builder()
                    .property("property2")
                    .rawMessage("message 2")
                    .build(),
                ErrorWithMessagePlaceholder.builder()
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

    public void create(@Validated SimpleDummyDto dto) {

    }

    public void update(@NotNull Long someId, @NotNull @Validated(UpdateContext.class) SimpleDummyDto dto) {

    }
}
