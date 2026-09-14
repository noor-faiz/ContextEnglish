package com.contextenglish.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @ResponseStatus gives web (Thymeleaf) controllers a correct 404 response by
 * default when this is thrown uncaught. The API-side GlobalExceptionHandler
 * takes precedence for anything under /api/** and returns a JSON body instead.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
