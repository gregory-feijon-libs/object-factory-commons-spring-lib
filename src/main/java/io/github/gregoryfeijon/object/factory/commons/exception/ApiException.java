package io.github.gregoryfeijon.object.factory.commons.exception;

import java.io.Serial;
import java.util.List;

/**
 * A custom runtime exception for API-related errors.
 * <p>
 * This exception provides various constructors to handle different error scenarios,
 * including the ability to aggregate multiple error messages into a single exception.
 *
 * @author gregory.feijon
 */
public class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 697767481629667239L;

    /**
     * Constructs an exception with a single error message.
     *
     * @param message The error message
     */
    public ApiException(String message) {
        super(message);
    }

    /**
     * Constructs an exception from a list of error messages.
     * <p>
     * The messages are concatenated into a single string with appropriate formatting.
     *
     * @param messages A list of error messages
     */
    public ApiException(List<String> messages) {
        super(buildErrorMessage(messages));
    }

    /**
     * Constructs an exception by wrapping another throwable.
     *
     * @param ex The throwable to wrap
     */
    public ApiException(Throwable ex) {
        super(ex);
    }

    /**
     * Constructs an exception with a message and a cause.
     *
     * @param message The error message
     * @param ex The throwable that caused this exception
     */
    public ApiException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Builds a formatted error message from a list of messages.
     * <p>
     * Messages containing ":" are followed by two newlines, while others
     * are followed by a single newline.
     *
     * @param errors The list of error messages
     * @return A concatenated string of all error messages
     */
    private static String buildErrorMessage(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        errors.forEach(error -> {
            if (error.contains(":")) {
                sb.append(error).append("\n\n");
            } else {
                sb.append(error).append("\n");
            }
        });
        return sb.toString();
    }
}