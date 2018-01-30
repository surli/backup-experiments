package ru.siksmfp.basic.structure.exceptions;

/**
 * Created by Artyom Karnov on 15.11.16.
 * artyom-karnov@yandex.ru
 **/
public class IncorrectIndexException extends RuntimeException {
    /**
     * Exception with message for situation when something goes wrong in structure
     *
     * @param message message for exception
     */
    public IncorrectIndexException(String message) {
        super(message);
    }

    /**
     * exception with message and throwable for situation when something goes wrong in structure
     *
     * @param message   message for exception
     * @param throwable object for exception
     */
    public IncorrectIndexException(String message, Throwable throwable) {
        super(message, throwable);
    }
}