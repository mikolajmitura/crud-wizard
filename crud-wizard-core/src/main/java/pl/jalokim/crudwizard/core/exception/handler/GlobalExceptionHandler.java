package pl.jalokim.crudwizard.core.exception.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static pl.jalokim.crudwizard.core.exception.handler.ErrorWithMessagePlaceholderMapper.convertToErrorsDto;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.hasPlaceholderFormat;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.jalokim.crudwizard.core.exception.ApplicationException;
import pl.jalokim.crudwizard.core.exception.BusinessLogicException;
import pl.jalokim.crudwizard.core.exception.CustomValidationException;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.exception.ResourceChangedException;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.rest.response.converter.ConstraintViolationToErrorConverter;
import pl.jalokim.crudwizard.core.rest.response.error.ErrorResponseDto;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    public static final String EXCEPTION_HANDLED_MESSAGE = "Exception handled.";
    public static final String VALIDATION_ERROR_MESSAGE = "Invalid request";

    @ExceptionHandler(BusinessLogicException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDto handleBusinessLogicException(BusinessLogicException ex, HttpServletRequest httpRequest) {
        log.warn("Error from endpoint: {} with message: {}", prepareEndpointUrl(httpRequest), ex.getMessage());
        return buildErrorResponseDto(ex);
    }

    @ExceptionHandler(TechnicalException.class)
    @ResponseStatus(NOT_IMPLEMENTED)
    @ResponseBody
    public ErrorResponseDto handleTechnicalException(TechnicalException ex) {
        log.warn(EXCEPTION_HANDLED_MESSAGE, ex);
        return buildErrorResponseDto(ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDto handleApplicationValidationException(ConstraintViolationException ex) {
        log.trace(EXCEPTION_HANDLED_MESSAGE, ex);
        return buildErrorResponseForConstraintViolation(ex);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDto handleBindException(BindException ex) {
        log.trace(EXCEPTION_HANDLED_MESSAGE, ex);
        return buildErrorResponseForBindingResult(ex.getBindingResult(), ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDto handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.trace(EXCEPTION_HANDLED_MESSAGE, ex);
        return buildErrorResponseForBindingResult(ex.getBindingResult(), ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public ErrorResponseDto handleModelNotFoundException(EntityNotFoundException ex) {
        log.trace(EXCEPTION_HANDLED_MESSAGE, ex);
        return buildErrorResponseDto(ex);
    }

    @ExceptionHandler(ResourceChangedException.class)
    @ResponseStatus(CONFLICT)
    @ResponseBody
    public ErrorResponseDto handleOptimisticLockException(ResourceChangedException ex) {
        log.trace(EXCEPTION_HANDLED_MESSAGE, ex);
        return buildErrorResponseDto(ex.getLocalizedMessage());
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomValidationException(CustomValidationException dataValidationException) {
        return  ResponseEntity
            .status(dataValidationException.getStatusCode())
            .body(ErrorResponseDto.builder()
                .message(dataValidationException.getMessage())
                .errors(convertToErrorsDto(dataValidationException.getErrors()))
                .build());

    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponseDto handleUnmappedException(Exception ex, HttpServletRequest httpRequest) {
        log.error("Error from endpoint: {} with message: {}", prepareEndpointUrl(httpRequest), ex.getMessage(), ex);
        return buildErrorResponseDto(ex.getMessage());
    }

    private ErrorResponseDto buildErrorResponseForConstraintViolation(ConstraintViolationException ex) {
        return ErrorResponseDto.builder()
            .message(VALIDATION_ERROR_MESSAGE)
            .errors(ex.getConstraintViolations().stream()
                .map(ConstraintViolationToErrorConverter::toErrorDto)
                .collect(Collectors.toSet()))
            .build();
    }

    private ErrorResponseDto buildErrorResponseForBindingResult(BindingResult bindingResult, Exception originalCause) {
        try {
            return ErrorResponseDto.builder()
                .message(VALIDATION_ERROR_MESSAGE)
                .errors(bindingResult.getAllErrors().stream()
                    .map(error -> error.unwrap(ConstraintViolation.class))
                    .map(ConstraintViolationToErrorConverter::toErrorDto)
                    .collect(Collectors.toSet()))
                .build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ErrorResponseDto.builder()
                .message(String.format("%s due to: %s", VALIDATION_ERROR_MESSAGE, originalCause.getMessage()))
                .build();
        }
    }

    public static ErrorResponseDto buildErrorResponseDto(String localizedMessage) {
        return ErrorResponseDto.builder()
            .message(localizedMessage)
            .build();
    }

    public static ErrorResponseDto buildErrorResponseDto(ApplicationException ex) {
        return ErrorResponseDto.builder()
            .message(ex.getMessage())
            .errorCode(extractErrorCode(ex))
            .build();
    }

    public static String prepareEndpointUrl(HttpServletRequest httpRequest) {
        return httpRequest.getRequestURL().toString();
    }

    @VisibleForTesting
    static String extractErrorCode(ApplicationException applicationException) {
        if (applicationException.hasMessagePlaceholder()) {
            MessagePlaceholder messagePlaceHolder = applicationException.getMessagePlaceHolder();
            return Optional.ofNullable(messagePlaceHolder.getErrorCode())
                .orElse(messagePlaceHolder.getMainPlaceholder());
        }
        if (hasPlaceholderFormat(applicationException.getOriginalTextOrPlaceholder())) {
            return MessagePlaceholder.extractPropertyKey(applicationException.getOriginalTextOrPlaceholder());
        }
        return null;
    }
}
