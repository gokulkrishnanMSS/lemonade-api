package com.lemon.lemonade.Exceptions;

public class PasswordNotValidException extends  RuntimeException {
    public PasswordNotValidException(String message) {
        super(message);
    }
}
