package pl.jalokim.crudwizard.core.exception.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.core.datetime.TimeZoneRequestHolder;
import pl.jalokim.crudwizard.core.exception.BusinessLogicException;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.translations.AppMessageSource;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.groups.CreateContext;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.crudwizard.core.validation.javax.groups.WithoutDefaultGroup;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class DummyRestController {

    private final DummyService dummyService;

    @PostMapping
    void dummyMethod(@RequestBody DummyDto request) {
        dummyService.dummyMethod(request);
    }

    @GetMapping("/modelnotfound")
    void dummyMethodCustomException() {
        dummyService.dummyMethodCustomException();
    }

    @GetMapping("/bindexception")
    void dummyMethodBindException() throws BindException {
        dummyService.dummyMethodBindException();
    }

    @GetMapping("/optimisticLocking")
    void dummyMethodOptimisticLocking() {
        dummyService.dummyMethodOptimisticLockingException();
    }

    @GetMapping("/businessLogicException")
    void dummyMethodThrowingBusinessLogicException() {
        dummyService.dummyMethodThrowingBusinessLogicException();
    }

    @GetMapping("/technicalException")
    void dummyMethodThrowingTechnicalException() {
        dummyService.dummyMethodThrowingTechnicalException();
    }

    @GetMapping("/data-validation-exception")
    void dummyMethodThrowingDataValidationException() {
        dummyService.dummyMethodThrowingDataValidationException();
    }

    @GetMapping("/raw-exception")
    void dummyMethodThrowingRawException() throws Exception {
        dummyService.dummyMethodThrowingRawException();
    }

    @GetMapping("/throw-exception-without-text-placeholder")
    void throwExceptionWithoutTextPlaceHolder() {
        throw new BusinessLogicException("not translated message");
    }

    @GetMapping("/throw-exception-with-text-placeholder")
    void throwExceptionWithTextPlaceHolder() {
        throw new EntityNotFoundException(MessagePlaceholder.wrapAsPlaceholder("some.global.exception.message"));
    }

    @GetMapping("/throw-exception-with-message-placeholder-object")
    void throwExceptionWithMessagePlaceholderObject() {
        throw new BusinessLogicException(MessagePlaceholder.builder()
            .mainPlaceholder(AppMessageSource.buildPropertyKey(getClass(), "some.code"))
            .argumentsAsPlaceholders("some.property1")
            .rawArguments(12L)
            .errorCode("fixed.error.code")
            .build());
    }

    @PostMapping("/create")
    void create(@RequestBody @Validated(CreateContext.class) SomeBean someBean) {

    }

    @PostMapping("/update")
    void update(@RequestBody @Validated(UpdateContext.class) SomeBean someBean) {

    }

    @PostMapping("/update-without-default-group")
    void updateWithoutDefaultGroup(@RequestBody @Validated({UpdateContext.class, WithoutDefaultGroup.class}) SomeBean someBean) {

    }

    @GetMapping("/required-time-zone-header")
    List<String> requiredTimeZoneHeader() {
        return List.of(TimeZoneRequestHolder.geRequestedZoneId().toString());
    }

}
