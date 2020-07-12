package de.malkusch.ha.automation.presentation;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.UUID;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
final class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public String notFound(NotFoundException error) {
        return error.getMessage();
    }

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(BAD_GATEWAY)
    @ResponseBody
    public String apiError(ApiException error) {
        var reference = UUID.randomUUID();
        log.error("Upstream API error [{}]", reference, error);
        return String.format("Upstream API error [%s]", reference);
    }
}
