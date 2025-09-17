package io.github.dmhomes.exceptions;

/**
 * Custom exception class for DM-Homes plugin operations
 */
public class DMHomesException extends Exception {

    /**
     * Creates a new DMHomesException with the specified message
     * @param message the exception message
     */
    public DMHomesException(final String message) {
        super(message);
    }

    /**
     * Creates a new DMHomesException with the specified message and cause
     * @param message the exception message
     * @param cause the underlying cause
     */
    public DMHomesException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DMHomesException with the specified cause
     * @param cause the underlying cause
     */
    public DMHomesException(final Throwable cause) {
        super(cause);
    }
}