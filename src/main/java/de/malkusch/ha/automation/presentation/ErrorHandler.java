package de.malkusch.ha.automation.presentation;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import java.util.UUID;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
import de.malkusch.ha.shared.model.ApiException;
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

    @ExceptionHandler(CoolDownException.class)
    @ResponseStatus(TOO_MANY_REQUESTS)
    @ResponseBody
    public String apiError(CoolDownException error) {
        return String.format("Too many requests. Try again after %s", formatTime(error.retryAfter));
    }
}
