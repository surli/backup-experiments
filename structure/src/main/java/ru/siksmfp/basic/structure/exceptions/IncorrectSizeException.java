package ru.siksmfp.basic.structure.exceptions;

/**
 * @author Artem Karnov @date 02.12.16.
 * artem.karnov@t-systems.com
 **/
public class IncorrectSizeException extends RuntimeException {
    /**
     * Exception with message for situations when size of structure is wrong
     *
     * @param message message for exception
     */
    public IncorrectSizeException(String message) {
        super(message);
    }

    /**
     * Exception with message and throwable for situations when size of structure is wrong
     *
     * @param message   message for exception
     * @param throwable object for exception
     */
    public IncorrectSizeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}